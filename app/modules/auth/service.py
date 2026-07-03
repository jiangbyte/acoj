from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, AccountType
from app.core.config.settings import settings
from app.core.exceptions.business import AuthenticationError
from app.core.security.password import hash_password, verify_password
from app.core.security.session import SessionPayload, session_store
from app.core.security.token import generate_token
from app.modules.auth.protection import login_protection_service
from app.modules.auth.schema import (
    CancelAccountRequest,
    LoginPayload,
    RegisterRequest,
    RegisterResponse,
)
from app.modules.auth.session_service import AccountSessionService
from app.modules.iam.account.model import SysAccount
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.account.schema import AccountCancelPayload, AccountCreateRequest
from app.modules.iam.enums import AccountIdentityType
from app.modules.sys.audit.service import OperationAuditService
from app.modules.user.admin.repository import AdminUserProfileRepository
from app.modules.user.admin.schema import AdminProfileUpsertPayload
from app.modules.user.portal.repository import PortalUserProfileRepository
from app.modules.user.portal.schema import PortalProfileUpsertPayload
from app.platform.db.transaction import transactional
from app.platform.observability.metrics import record_login_attempt


class AuthService:
    """认证服务，负责登录态签发、账户类型校验与会话数据组装。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.account_repo = AccountRepository(db)
        self.session_service = AccountSessionService(db)

    async def login(self, payload: LoginPayload) -> SessionPayload:
        """执行登录流程，使用对象承载参数以避免接口层平铺传参。"""
        try:
            await login_protection_service.ensure_allowed(
                account_type=payload.account_type,
                account=payload.account,
                client_ip=payload.client_ip,
            )
            account = await self.account_repo.get_account_by_identifier(
                payload.account,
                [
                    AccountIdentityType.ACCOUNT,
                    AccountIdentityType.EMAIL,
                    AccountIdentityType.PHONE,
                ],
            )
            self._validate_account(account, payload.password, payload.account_type)
        except AuthenticationError:
            await login_protection_service.record_failure(
                account_type=payload.account_type,
                account=payload.account,
                client_ip=payload.client_ip,
            )
            record_login_attempt(payload.account_type.value, "failure", "invalid_credentials")
            await OperationAuditService(self.db).record(
                module="auth",
                action="login",
                resource_type="account",
                resource_id=payload.account,
                summary=f"{payload.account_type.value} login failed",
                success=False,
                error_message="Invalid or locked login attempt",
                account_type=payload.account_type.value,
                ip=payload.client_ip,
                user_agent=payload.user_agent,
            )
            raise
        assert account is not None
        session_payload = await self.session_service.build_session_payload(
            account,
            generate_token(),
            client_ip=payload.client_ip,
            user_agent=payload.user_agent,
            device_label=payload.device_label,
        )
        await session_store.set(session_payload, ttl_seconds=settings.auth.token_ttl_seconds)
        await login_protection_service.record_success(
            account_type=payload.account_type,
            account=payload.account,
            client_ip=payload.client_ip,
        )
        record_login_attempt(payload.account_type.value, "success")
        await OperationAuditService(self.db).record(
            module="auth",
            action="login",
            resource_type="account",
            resource_id=account.id,
            summary=f"{payload.account_type.value} login succeeded",
            success=True,
            account_id=account.id,
            account_type=account.account_type,
            ip=payload.client_ip,
            user_agent=payload.user_agent,
        )
        return session_payload

    async def register_admin(self, payload: RegisterRequest) -> RegisterResponse:
        async with transactional(self.db):
            account_payload = AccountCreateRequest(
                account=payload.account,
                password=payload.password,
                account_type=AccountType.ADMIN,
                account_status=AccountStatusEnum.ENABLED,
                name=payload.name,
                nickname=payload.nickname,
                phone=payload.phone,
                email=payload.email,
                email_identity=payload.email,
                phone_identity=payload.phone,
                email_identity_verified=bool(payload.email),
                phone_identity_verified=bool(payload.phone),
            )
            account = await self.account_repo.create(
                account_payload,
                password_hash=hash_password(payload.password),
            )
            await AdminUserProfileRepository(self.db).upsert(
                AdminProfileUpsertPayload(
                    account_id=account.id,
                    name=payload.name,
                    nickname=payload.nickname,
                    phone=payload.phone,
                    email=payload.email,
                    avatar=None,
                    signature=None,
                    employee_no=None,
                    title=None,
                    remark=None,
                ),
            )
        response = RegisterResponse(
            account_id=account.id,
            account=payload.account,
            account_type=AccountType.ADMIN,
        )
        await OperationAuditService(self.db).record(
            module="auth",
            action="register",
            resource_type="account",
            resource_id=account.id,
            summary="Admin account registered",
            success=True,
            account_id=account.id,
            account_type=AccountType.ADMIN.value,
        )
        return response

    async def register_portal(self, payload: RegisterRequest) -> RegisterResponse:
        async with transactional(self.db):
            account_payload = AccountCreateRequest(
                account=payload.account,
                password=payload.password,
                account_type=AccountType.PORTAL,
                account_status=AccountStatusEnum.ENABLED,
                name=payload.name,
                nickname=payload.nickname,
                phone=payload.phone,
                email=payload.email,
                email_identity=payload.email,
                phone_identity=payload.phone,
                email_identity_verified=bool(payload.email),
                phone_identity_verified=bool(payload.phone),
            )
            account = await self.account_repo.create(
                account_payload,
                password_hash=hash_password(payload.password),
            )
            await PortalUserProfileRepository(self.db).upsert(
                PortalProfileUpsertPayload(
                    account_id=account.id,
                    name=payload.name,
                    nickname=payload.nickname,
                    phone=payload.phone,
                    email=payload.email,
                    avatar=None,
                    signature=None,
                    bio=None,
                    level=None,
                ),
            )
        response = RegisterResponse(
            account_id=account.id,
            account=payload.account,
            account_type=AccountType.PORTAL,
        )
        await OperationAuditService(self.db).record(
            module="auth",
            action="register",
            resource_type="account",
            resource_id=account.id,
            summary="Portal account registered",
            success=True,
            account_id=account.id,
            account_type=AccountType.PORTAL.value,
        )
        return response

    async def logout(self, token: str) -> None:
        """注销指定 token 对应的会话。"""
        await session_store.delete(token)
        await OperationAuditService(self.db).record(
            module="auth",
            action="logout",
            resource_type="account",
            resource_id=token,
            summary="Logout",
            success=True,
        )

    async def cancel_current_account(
        self,
        payload: CancelAccountRequest,
        session: SessionPayload,
    ) -> None:
        """注销当前登录账号，并清理该账号下全部会话。"""
        async with transactional(self.db):
            account = await self.account_repo.cancel(
                AccountCancelPayload(
                    id=session.account_id,
                    cancel_reason=payload.cancel_reason,
                ),
                cancelled_by=session.account_id,
            )
        await self.session_service.delete_account_sessions(account.account_type, account.id)
        await OperationAuditService(self.db).record(
            module="auth",
            action="cancel_account",
            resource_type="account",
            resource_id=account.id,
            summary="Cancel current account",
            success=True,
            account_id=account.id,
            account_type=account.account_type,
        )

    def _validate_account(
        self,
        account: SysAccount | None,
        password: str,
        account_type: AccountType,
    ) -> None:
        """校验账号密码、账号状态以及目标账户类型是否允许访问。"""
        if not account or not verify_password(password, account.password_hash):
            raise AuthenticationError("Invalid account or password")
        if (
            account.account_status == AccountStatusEnum.CANCELLED.value
            or account.cancelled_at is not None
        ):
            raise AuthenticationError("Account is cancelled")
        if account.account_status != AccountStatusEnum.ENABLED.value:
            raise AuthenticationError("Account is inactive")
        if account_type == AccountType.ADMIN and account.account_type != AccountType.ADMIN.value:
            raise AuthenticationError("Account is not allowed to access admin account type")
        if account_type == AccountType.PORTAL and account.account_type != AccountType.PORTAL.value:
            raise AuthenticationError("Account is not allowed to access portal account type")
