from typing import Annotated

from fastapi import Depends, Header
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountStatusEnum, LoginScope
from app.core.exceptions.business import AuthenticationError, AuthorizationError
from app.core.security.permission import PermissionChecker
from app.core.security.session import SessionPayload, session_store
from app.core.security.user_scope import assert_scope_allowed
from app.deps.context import account_id_ctx, account_type_ctx, login_scope_ctx
from app.deps.db import get_db_session
from app.core.security.permission_registry import PERMISSION_META_ATTR, SCOPE_META_ATTR
from app.modules.iam.repository import AccountRepository


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
    return session


async def get_current_account(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
):
    account_id_ctx.set(session.account_id)
    account_type_ctx.set(session.account_type)
    login_scope_ctx.set(session.login_scope)
    account = await AccountRepository(db).get_account_by_id(session.account_id)
    if (
        not account
        or account.cancelled_at is not None
        or account.account_status != AccountStatusEnum.ENABLED.value
    ):
        raise AuthenticationError("Account is inactive or missing")
    return account


def require_scope(*scopes: LoginScope):
    """基于登录域枚举生成依赖校验函数。"""
    async def dependency(
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> SessionPayload:
        assert_scope_allowed(session.login_scope, set(scopes))
        return session

    setattr(dependency, SCOPE_META_ATTR, {"login_scopes": [scope.value for scope in scopes]})
    return dependency


def require_permission(permission_code: str):
    async def dependency(
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> SessionPayload:
        if not PermissionChecker.has_permission(session.permission_keys, permission_code):
            raise AuthorizationError(f"Permission denied: {permission_code}")
        return session

    setattr(dependency, PERMISSION_META_ATTR, {"permission_key": permission_code})
    return dependency
