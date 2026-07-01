from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.constants import SUPER_ADMIN_ROLE_CODE
from app.core.config.enums import AccountStatusEnum, AccountType
from app.core.config.settings import settings
from app.core.exceptions.business import AuthenticationError
from app.core.security.password import hash_password, verify_password
from app.core.security.session import SessionPayload, session_store
from app.core.security.token import generate_token
from app.modules.auth.schema import CancelAccountRequest, LoginPayload, RegisterRequest, RegisterResponse
from app.modules.iam.account.model import SysAccount
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.account.schema import AccountCancelPayload, AccountCreateRequest
from app.modules.iam.enums import AccountIdentityType
from app.modules.iam.grant.repository import GrantRepository
from app.modules.user.admin.schema import AdminProfileUpsertPayload
from app.modules.user.admin.service import AdminUserProfileService
from app.modules.user.portal.schema import PortalProfileUpsertPayload
from app.modules.user.portal.service import PortalUserProfileService
from app.platform.db.transaction import transactional


class AuthService:
    """认证服务，负责登录态签发、账户类型校验与会话数据组装。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.account_repo = AccountRepository(db)
        self.grant_repo = GrantRepository(db)

    async def login(self, payload: LoginPayload) -> SessionPayload:
        """执行登录流程，使用对象承载参数以避免接口层平铺传参。"""
        account = await self.account_repo.get_account_by_identifier(
            payload.account,
            [
                AccountIdentityType.ACCOUNT,
                AccountIdentityType.EMAIL,
                AccountIdentityType.PHONE,
            ],
        )
        self._validate_account(account, payload.password, payload.account_type)
        assert account is not None
        session_payload = await self.build_session_payload(account, generate_token())
        await session_store.set(session_payload, ttl_seconds=settings.auth.token_ttl_seconds)
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
            await AdminUserProfileService(self.db).upsert_profile(
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
        return RegisterResponse(
            account_id=account.id,
            account=payload.account,
            account_type=AccountType.ADMIN,
        )

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
            await PortalUserProfileService(self.db).upsert_profile(
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
        return RegisterResponse(
            account_id=account.id,
            account=payload.account,
            account_type=AccountType.PORTAL,
        )

    async def build_session_payload(self, account: SysAccount, token: str) -> SessionPayload:
        authorization = await self.grant_repo.get_account_authorization(account.id)
        return self._build_session_payload_from_authorization(account, token, authorization)

    def _build_session_payload_from_authorization(
        self,
        account: SysAccount,
        token: str,
        authorization: dict,
    ) -> SessionPayload:
        permission_keys = set(authorization["permission_keys"])
        button_codes = set(authorization["button_codes"])
        if SUPER_ADMIN_ROLE_CODE in authorization["role_codes"]:
            permission_keys.add("*:*:*")
            button_codes.add("*:*:*")
        return SessionPayload(
            token=token,
            account_id=account.id,
            account_type=account.account_type,
            role_ids=authorization["role_ids"],
            dept_ids=authorization["dept_ids"],
            group_ids=authorization["group_ids"],
            resource_ids=authorization["resource_ids"],
            button_codes=sorted(button_codes),
            permission_keys=sorted(permission_keys),
            permission_grants=authorization["permission_grants"],
        )

    async def refresh_account_sessions(self, account_id: str) -> None:
        await self.refresh_accounts_sessions([account_id])

    async def delete_account_sessions(self, account_type: str, account_id: str) -> None:
        await session_store.delete_account_sessions(account_type, account_id)

    async def refresh_accounts_sessions(self, account_ids: list[str]) -> None:
        accounts = await self.account_repo.list_accounts_by_ids(account_ids)
        if not accounts:
            return
        account_map = {account.id: account for account in accounts}
        authorizations = await self.grant_repo.get_accounts_authorization(list(account_map.keys()))

        for account in accounts:
            authorization = authorizations[account.id]

            async def payload_factory(
                token: str,
                current_account: SysAccount = account,
                current_authorization: dict = authorization,
            ) -> SessionPayload:
                return self._build_session_payload_from_authorization(
                    current_account,
                    token,
                    current_authorization,
                )

            await session_store.refresh_account_sessions(
                account.account_type,
                account.id,
                payload_factory,
            )

    async def logout(self, token: str) -> None:
        """注销指定 token 对应的会话。"""
        await session_store.delete(token)

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
        await self.delete_account_sessions(account.account_type, account.id)

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
