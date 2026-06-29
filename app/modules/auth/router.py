from typing import Annotated

from fastapi import APIRouter, Depends, Header
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import success
from app.core.security.session import SessionPayload, session_store
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.auth.schema import (
    CancelAccountApiResponse,
    CancelAccountRequest,
    CancelAccountResponse,
    LoginApiResponse,
    LoginPayload,
    LoginRequest,
    LoginResponse,
    LogoutApiResponse,
    LogoutResponse,
    RegisterApiResponse,
    RegisterRequest,
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
            account_type=AccountType.ADMIN,
        )
    )
    return success(
        LoginResponse(
            token=session.token,
            account_id=session.account_id,
            account_type=AccountType(str(session.account_type)),
        )
    )


@admin_router.post("/register", response_model=RegisterApiResponse)
async def admin_register(
    payload: RegisterRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> RegisterApiResponse:
    """管理端注册入口，创建账户主体、主登录标识和管理端资料。"""
    return success(await AuthService(db).register_admin(payload))


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
            account_type=AccountType.PORTAL,
        )
    )
    return success(
        LoginResponse(
            token=session.token,
            account_id=session.account_id,
            account_type=AccountType(str(session.account_type)),
        )
    )


@portal_router.post(
    "/logout",
    response_model=LogoutApiResponse,
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
@admin_router.post(
    "/logout",
    response_model=LogoutApiResponse,
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def logout(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    authorization: Annotated[str | None, Header(alias="Authorization")] = None,
) -> LogoutApiResponse:
    """统一退出登录接口，优先读取请求头中的原始 token。"""
    token = authorization or session.token
    await session_store.delete(token)
    return success(LogoutResponse(success=True))


@portal_router.post(
    "/cancel",
    response_model=CancelAccountApiResponse,
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
@admin_router.post(
    "/cancel",
    response_model=CancelAccountApiResponse,
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def cancel_account(
    payload: CancelAccountRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> CancelAccountApiResponse:
    """统一账号注销接口，只注销当前登录账号。"""
    await AuthService(db).cancel_current_account(payload, session)
    return success(CancelAccountResponse(success=True))
