from typing import Annotated

from fastapi import APIRouter, Depends, Header
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope, UserType
from app.core.response.schema import success
from app.core.security.session import SessionPayload, session_store
from app.deps.auth import get_current_session, require_scope
from app.deps.db import get_db_session
from app.modules.auth.schema import (
    LoginApiResponse,
    LoginPayload,
    LoginRequest,
    LoginResponse,
    LogoutApiResponse,
    LogoutResponse,
)
from app.modules.auth.service import AuthService

admin_router = APIRouter()
portal_router = APIRouter()


@admin_router.post("/login", response_model=LoginApiResponse)
async def admin_login(
    payload: LoginRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> LoginApiResponse:
    """管理端登录入口，仅允许管理端用户体系访问。"""
    session = await AuthService(db).login(
        LoginPayload(
            account=payload.account,
            password=payload.password,
            login_scope=LoginScope.ADMIN,
        )
    )
    return success(
        LoginResponse(
            token=session.token,
            account_id=session.account_id,
            account_type=UserType(str(session.account_type)),
            login_scope=LoginScope(str(session.login_scope)),
        )
    )


@portal_router.post("/login", response_model=LoginApiResponse)
async def portal_login(
    payload: LoginRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> LoginApiResponse:
    """门户端登录入口，仅允许门户用户体系访问。"""
    session = await AuthService(db).login(
        LoginPayload(
            account=payload.account,
            password=payload.password,
            login_scope=LoginScope.PORTAL,
        )
    )
    return success(
        LoginResponse(
            token=session.token,
            account_id=session.account_id,
            account_type=UserType(str(session.account_type)),
            login_scope=LoginScope(str(session.login_scope)),
        )
    )


@portal_router.post(
    "/logout",
    response_model=LogoutApiResponse,
    dependencies=[Depends(require_scope(LoginScope.PORTAL))],
)
@admin_router.post(
    "/logout",
    response_model=LogoutApiResponse,
    dependencies=[Depends(require_scope(LoginScope.ADMIN))],
)
async def logout(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    authorization: Annotated[str | None, Header(alias="Authorization")] = None,
) -> LogoutApiResponse:
    """统一注销接口，优先读取请求头中的原始 token。"""
    token = authorization or session.token
    await session_store.delete(token)
    return success(LogoutResponse(success=True))
