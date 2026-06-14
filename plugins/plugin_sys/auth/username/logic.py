import asyncio
import bcrypt
from fastapi import Request
from sdk.infra.db import SessionLocal
from plugins.plugin_sys.shared import USER_STATUS_ACTIVE, USER_STATUS_INACTIVE, USER_STATUS_LOCKED
from plugins.plugin_sys.user.service import LoginUserService
from sdk.auth import Business
from sdk.infra.concurrency import run_blocking
from sdk.web.exception import BusinessException
from sdk.utils import decrypt
from sdk.utils.user_agent_utils import get_browser
from sdk.captcha import b_captcha
from sdk.log import record_auth_log
from .params import (
    UsernameLoginParam,
    UsernameLoginResult,
    UsernameRegisterParam,
    UsernameRegisterResult,
    UsernameLogoutResult,
)
import logging

logger = logging.getLogger(__name__)

login_user_service = LoginUserService(SessionLocal)


async def do_login(param: UsernameLoginParam, request: Request) -> UsernameLoginResult:
    try:
        await b_captcha.check_captcha(param.captcha_id, param.captcha_code)
    except Exception as e:
        raise BusinessException(str(e))

    user_info = await run_blocking(login_user_service.get_user_by_username, param.username)

    try:
        if not user_info:
            logger.warning(f"User not found: {param.username}")
            raise BusinessException("用户名或密码错误")

        if user_info.status == USER_STATUS_LOCKED:
            logger.warning(f"User account is locked: {param.username}")
            raise BusinessException("账号已被锁定")
        if user_info.status == USER_STATUS_INACTIVE:
            logger.warning(f"User account is inactive: {param.username}")
            raise BusinessException("账号已停用")
        if user_info.status != USER_STATUS_ACTIVE:
            logger.warning(f"User account status abnormal: {param.username}, status={user_info.status}")
            raise BusinessException("账号状态异常")

        try:
            raw_password = decrypt(param.password)
            if not await asyncio.to_thread(
                bcrypt.checkpw,
                raw_password.encode("utf-8"),
                user_info.password.encode("utf-8"),
            ):
                logger.warning("Password verification failed")
                raise BusinessException("用户名或密码错误")
        except BusinessException:
            raise
        except Exception as e:
            logger.warning(f"Password decryption failed: {e}")
            raise BusinessException("用户名或密码错误")

        extra = {
            "username": user_info.username,
            "nickname": user_info.nickname,
            "status": user_info.status
        }
        # Auto-detect device type from User-Agent, device_id from frontend
        user_agent = request.headers.get("User-Agent", "")
        extra["device_type"] = get_browser(user_agent)
        extra["device_id"] = param.device_id

        token = await Business.login(request, user_info.id, extra)

        try:
            await run_blocking(login_user_service.record_login, user_info.id, request)
        except Exception as e:
            logger.warning(f"Failed to record login info: {e}")

        # 记录登录日志
        record_auth_log(request, "登录", "LOGIN", op_user=user_info.username)

        return UsernameLoginResult(token=token)
    except BusinessException as e:
        record_auth_log(
            request, "登录", "LOGIN",
            exe_status="FAIL", exe_message=e.message,
            op_user=user_info.username if user_info else param.username,
        )
        request.state._exception_logged = True
        raise


async def do_register(param: UsernameRegisterParam, request: Request = None) -> UsernameRegisterResult:
    try:
        await b_captcha.check_captcha(param.captcha_id, param.captcha_code)
    except Exception as e:
        raise BusinessException(str(e))

    raw_password = decrypt(param.password)
    if not raw_password:
        raise BusinessException("密码解密失败")
    hashed_password = (
        await asyncio.to_thread(lambda: bcrypt.hashpw(raw_password.encode('utf-8'), bcrypt.gensalt()))
    ).decode('utf-8')

    await run_blocking(login_user_service.create_user, param.username, hashed_password)

    if request:
        record_auth_log(request, "注册", "REGISTER", op_user=param.username)

    return UsernameRegisterResult(message="注册成功")


async def do_logout(request: Request) -> UsernameLogoutResult:
    # 获取当前用户用于日志记录
    try:
        user_id = await Business.get_login_id(request)
        if user_id:
            op_user = await run_blocking(login_user_service.get_username, user_id)
            record_auth_log(request, "登出", "LOGOUT", op_user=op_user)
    except Exception as e:
        logger.warning(f"Failed to record logout log: {e}")

    await Business.logout(request=request)
    return UsernameLogoutResult(message="登出成功")
