from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, LoginScope, UserType
from app.core.config.settings import settings
from app.core.exceptions.business import AuthenticationError
from app.core.security.password import verify_password
from app.core.security.session import SessionPayload, session_store
from app.core.security.token import generate_token
from app.modules.auth.schema import LoginPayload
from app.modules.iam.account.model import SysAccount
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.grant.repository import GrantRepository


class AuthService:
    """认证服务，负责登录态签发、登录域校验与会话数据组装。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.account_repo = AccountRepository(db)
        self.grant_repo = GrantRepository(db)

    async def login(self, payload: LoginPayload) -> SessionPayload:
        """执行登录流程，使用对象承载参数以避免接口层平铺传参。"""
        account = await self.account_repo.get_account_by_account(payload.account)
        self._validate_account(account, payload.password, payload.login_scope)
        assert account is not None
        permission_grants = await self.grant_repo.get_account_effective_permissions(account.id)
        role_ids = await self.account_repo.get_account_role_ids(account.id)
        dept_ids = await self.account_repo.get_account_dept_ids(account.id)
        group_ids = await self.account_repo.get_account_group_ids(account.id)
        session_payload = SessionPayload(
            token=generate_token(),
            account_id=account.id,
            account_type=account.account_type,
            login_scope=payload.login_scope.value,
            role_ids=role_ids,
            dept_ids=dept_ids,
            group_ids=group_ids,
            permission_keys=sorted({grant["permission_key"] for grant in permission_grants}),
            permission_grants=permission_grants,
        )
        await session_store.set(session_payload, ttl_seconds=settings.auth.token_ttl_seconds)
        return session_payload

    async def logout(self, token: str) -> None:
        """注销指定 token 对应的会话。"""
        await session_store.delete(token)

    def _validate_account(
        self,
        account: SysAccount | None,
        password: str,
        login_scope: LoginScope,
    ) -> None:
        """校验账号密码、账号状态以及当前登录域是否允许访问。"""
        if not account or not verify_password(password, account.password_hash):
            raise AuthenticationError("Invalid account or password")
        if (
            account.account_status == AccountStatusEnum.CANCELLED.value
            or account.cancelled_at is not None
        ):
            raise AuthenticationError("Account is cancelled")
        if account.account_status != AccountStatusEnum.ENABLED.value:
            raise AuthenticationError("Account is inactive")
        if login_scope == LoginScope.ADMIN and account.account_type != UserType.ADMIN.value:
            raise AuthenticationError("Account is not allowed to access admin scope")
        if login_scope == LoginScope.PORTAL and account.account_type != UserType.PORTAL.value:
            raise AuthenticationError("Account is not allowed to access portal scope")
