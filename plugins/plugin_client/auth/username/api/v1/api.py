"""Client auth username API — mirrors hei-gin plugin-client/auth/username/api/v1/api.go."""

from fastapi import APIRouter, Request
from sdk.auth import CONSUMER_REALM_ID
from sdk.web.result import Result, success
from micosauth.decorators import require_login
from sdk.log import SysLog
from sdk.web.middleware import RateLimiter
from ...logic import do_login, do_register, do_logout
from ...params import (
    UsernameLoginParam, UsernameLoginResult,
    UsernameRegisterParam, UsernameRegisterResult,
    UsernameLogoutResult,
)

router = APIRouter()


@router.post(
    "/api/v1/public/c/login",
    summary="C端用户名密码登录",
    response_model=Result[UsernameLoginResult],
)
async def login(request: Request, param: UsernameLoginParam):
    return success(await do_login(param, request))


@router.post(
    "/api/v1/public/c/register",
    summary="C端用户名注册",
    response_model=Result[UsernameRegisterResult],
)
@SysLog("注册")
@RateLimiter("norepeat:client:register", window=5, max_requests=1)
async def register(request: Request, param: UsernameRegisterParam):
    return success(await do_register(param, request=request))


@router.post(
    "/api/v1/c/logout",
    summary="C端用户登出",
    response_model=Result[UsernameLogoutResult],
)
@require_login(realm=CONSUMER_REALM_ID)
async def logout(request: Request):
    return success(await do_logout(request))
