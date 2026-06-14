from fastapi import APIRouter, Request
from sdk.auth import BusinessID
from sdk.web.result import Result, success
from sdk.auth.decorator import CheckLogin
from sdk.log import SysLog
from sdk.auth.decorator import NoRepeat
from ...logic import do_login, do_register, do_logout
from ...params import (
    UsernameLoginParam,
    UsernameLoginResult,
    UsernameLogoutResult,
    UsernameRegisterParam,
    UsernameRegisterResult,
)

router = APIRouter()


@router.post(
    "/api/v1/public/b/login",
    summary="B端用户名密码登录",
    response_model=Result[UsernameLoginResult]
)
async def login(request: Request, param: UsernameLoginParam):
    return success(await do_login(param, request))


@router.post(
    "/api/v1/public/b/register",
    summary="B端用户名注册",
    response_model=Result[UsernameRegisterResult]
)
@SysLog("注册")
@NoRepeat(interval=5000)
async def register(request: Request, param: UsernameRegisterParam):
    return success(await do_register(param, request=request))


@router.post(
    "/api/v1/b/logout",
    summary="B端用户登出",
    response_model=Result[UsernameLogoutResult]
)
@CheckLogin(realm_id=BusinessID)
async def logout(request: Request):
    return success(await do_logout(request))
