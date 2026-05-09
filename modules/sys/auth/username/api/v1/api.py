from fastapi import APIRouter, Request
from core.result import Result, success
from core.auth.decorator import HeiCheckLogin
from ...logic import do_login, do_register, do_logout
from ...params import UsernameLoginParam, UsernameLoginResult, UsernameRegisterParam, UsernameRegisterResult, UsernameLogoutResult

router = APIRouter()


@router.post(
    "/api/v1/public/b/login",
    summary="B端用户名密码登录",
    response_model=Result[UsernameLoginResult]
)
async def login(request: Request, param: UsernameLoginParam):
    result = await do_login(param, request)
    return success(result.model_dump())


@router.post(
    "/api/v1/public/b/register",
    summary="B端用户名注册",
    response_model=Result[UsernameRegisterResult]
)
async def register(param: UsernameRegisterParam):
    result = await do_register(param)
    return success(result.model_dump())


@router.post(
    "/api/v1/b/logout",
    summary="B端用户登出",
    response_model=Result[UsernameLogoutResult]
)
@HeiCheckLogin
async def logout(request: Request):
    result = await do_logout(request)
    return success(result.model_dump())
