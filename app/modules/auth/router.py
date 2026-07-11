from typing import Annotated

from fastapi import APIRouter, Depends, Header, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import success
from app.core.security.transport import (
    CaptchaApiResponse,
    PasswordKeyApiResponse,
    create_captcha,
    create_password_key,
    decrypt_passwords,
    verify_captcha,
)
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.auth.schema import (
    CancelAccountApiResponse,
    CancelAccountRequest,
    CancelAccountResponse,
    ForgotPasswordRequest,
    LoginApiResponse,
    LoginPayload,
    LoginRequest,
    LoginResponse,
    LogoutApiResponse,
    LogoutResponse,
    RegisterApiResponse,
    RegisterRequest,
    ResetPasswordRequest,
)
from app.modules.auth.service import AuthService

admin_router = APIRouter()
portal_router = APIRouter()


@admin_router.get("/captcha", response_model=CaptchaApiResponse)
@portal_router.get("/captcha", response_model=CaptchaApiResponse)
async def captcha(
    image_format: str = Query(default="svg", alias="format", pattern="^(svg|png)$"),
) -> CaptchaApiResponse:
    return success(await create_captcha(image_format))


@admin_router.get("/password-key", response_model=PasswordKeyApiResponse)
@portal_router.get("/password-key", response_model=PasswordKeyApiResponse)
async def password_key() -> PasswordKeyApiResponse:
    return success(await create_password_key())


@admin_router.post("/login", response_model=LoginApiResponse)
async def admin_login(
    payload: LoginRequest,
    request: Request,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> LoginApiResponse:
    """管理端登录入口，仅允许管理端用户体系访问。"""
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    password = (await decrypt_passwords(payload.password_key_id, payload.password))[0]
    session = await AuthService(db).login(
        LoginPayload(
            account=payload.account,
            password=password or "",
            account_type=AccountType.ADMIN,
            identity_type=payload.identity_type,
            client_ip=_client_ip(request),
            user_agent=request.headers.get("user-agent"),
            device_label=_device_label(request.headers.get("user-agent")),
        )
    )
    return success(
        LoginResponse(
            token=session.token,
            account_id=session.account_id,
            account_type=AccountType(str(session.account_type)),
        )
    )


@portal_router.post("/login", response_model=LoginApiResponse)
async def portal_login(
    payload: LoginRequest,
    request: Request,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> LoginApiResponse:
    """门户端登录入口，仅允许门户用户体系访问。"""
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    password = (await decrypt_passwords(payload.password_key_id, payload.password))[0]
    session = await AuthService(db).login(
        LoginPayload(
            account=payload.account,
            password=password or "",
            account_type=AccountType.PORTAL,
            identity_type=payload.identity_type,
            client_ip=_client_ip(request),
            user_agent=request.headers.get("user-agent"),
            device_label=_device_label(request.headers.get("user-agent")),
        )
    )
    return success(
        LoginResponse(
            token=session.token,
            account_id=session.account_id,
            account_type=AccountType(str(session.account_type)),
        )
    )


@portal_router.post("/register", response_model=RegisterApiResponse)
async def portal_register(
    payload: RegisterRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> RegisterApiResponse:
    """门户端注册入口，创建门户账户主体和门户资料。"""
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    password = (await decrypt_passwords(payload.password_key_id, payload.password))[0]
    return success(
        await AuthService(db).register_portal(
            payload.model_copy(update={"password": password or ""})
        )
    )


@admin_router.post("/forgot-password")
async def admin_forgot_password(
    payload: ForgotPasswordRequest,
    request: Request,
    db: Annotated[AsyncSession, Depends(get_db_session)],
):
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    await AuthService(db).forgot_password(
        payload,
        AccountType.ADMIN,
        client_ip=_client_ip(request),
        user_agent=request.headers.get("user-agent"),
    )
    return success()


@admin_router.post("/reset-password")
async def admin_reset_password(
    payload: ResetPasswordRequest,
    request: Request,
    db: Annotated[AsyncSession, Depends(get_db_session)],
):
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    password = (await decrypt_passwords(payload.password_key_id, payload.password))[0]
    await AuthService(db).reset_password(
        payload.model_copy(update={"password": password or ""}),
        AccountType.ADMIN,
        client_ip=_client_ip(request),
        user_agent=request.headers.get("user-agent"),
    )
    return success()


@portal_router.post("/forgot-password")
async def portal_forgot_password(
    payload: ForgotPasswordRequest,
    request: Request,
    db: Annotated[AsyncSession, Depends(get_db_session)],
):
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    await AuthService(db).forgot_password(
        payload,
        AccountType.PORTAL,
        client_ip=_client_ip(request),
        user_agent=request.headers.get("user-agent"),
    )
    return success()


@portal_router.post("/reset-password")
async def portal_reset_password(
    payload: ResetPasswordRequest,
    request: Request,
    db: Annotated[AsyncSession, Depends(get_db_session)],
):
    await verify_captcha(payload.captcha_id, payload.captcha_value)
    password = (await decrypt_passwords(payload.password_key_id, payload.password))[0]
    await AuthService(db).reset_password(
        payload.model_copy(update={"password": password or ""}),
        AccountType.PORTAL,
        client_ip=_client_ip(request),
        user_agent=request.headers.get("user-agent"),
    )
    return success()


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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    authorization: Annotated[str | None, Header(alias="Authorization")] = None,
) -> LogoutApiResponse:
    """统一退出登录接口，优先读取请求头中的原始 token。"""
    token = authorization or session.token
    await AuthService(db).logout(token)
    return success(LogoutResponse(success=True))


def _client_ip(request: Request) -> str | None:
    forwarded_for = request.headers.get("x-forwarded-for")
    if forwarded_for:
        return forwarded_for.split(",", 1)[0].strip()
    return request.client.host if request.client else None


def _device_label(user_agent: str | None) -> str | None:
    if not user_agent:
        return None
    value = user_agent.lower()
    if "mobile" in value or "android" in value or "iphone" in value:
        return "Mobile"
    if "ipad" in value or "tablet" in value:
        return "Tablet"
    return "Desktop"


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
