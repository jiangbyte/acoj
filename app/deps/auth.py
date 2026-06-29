from typing import Annotated

from fastapi import Depends, Header
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, AccountType
from app.core.exceptions.business import AuthenticationError, AuthorizationError
from app.core.security.permission import PermissionChecker
from app.core.security.permission_registry import ACCOUNT_TYPE_META_ATTR, PERMISSION_META_ATTR
from app.core.security.session import SessionPayload, session_store
from app.core.security.account_type import assert_account_type_allowed
from app.deps.context import account_id_ctx, account_type_ctx
from app.deps.db import get_db_session
from app.modules.iam.account.repository import AccountRepository


async def get_current_session(
    authorization: Annotated[str | None, Header(alias="Authorization")] = None,
) -> SessionPayload:
    """从请求头读取原始 token，并加载对应登录会话。"""
    if not authorization:
        raise AuthenticationError("Missing authorization token")
    token = authorization.strip()
    session = await session_store.get(token)
    if not session:
        raise AuthenticationError("Invalid or expired token")
    account_id_ctx.set(session.account_id)
    account_type_ctx.set(session.account_type)
    return session


async def get_current_account(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
):
    account_id_ctx.set(session.account_id)
    account_type_ctx.set(session.account_type)
    account = await AccountRepository(db).get_account_by_id(session.account_id)
    if (
        not account
        or account.cancelled_at is not None
        or account.account_status != AccountStatusEnum.ENABLED.value
    ):
        raise AuthenticationError("Account is inactive or missing")
    return account


def require_account_type(*account_types: AccountType):
    """基于账户类型枚举生成依赖校验函数。"""

    async def dependency(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        account=Depends(get_current_account),
    ) -> SessionPayload:
        assert_account_type_allowed(session.account_type, set(account_types))
        return session

    setattr(
        dependency,
        ACCOUNT_TYPE_META_ATTR,
        {"account_types": [account_type.value for account_type in account_types]},
    )
    return dependency


def require_permission(permission_code: str):
    async def dependency(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        account=Depends(get_current_account),
    ) -> SessionPayload:
        if not PermissionChecker.has_permission(session.permission_keys, permission_code):
            raise AuthorizationError(f"Permission denied: {permission_code}")
        return session

    setattr(dependency, PERMISSION_META_ATTR, {"permission_key": permission_code})
    return dependency
