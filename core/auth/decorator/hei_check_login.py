from functools import wraps
from fastapi import HTTPException, status, Request
from ..auth import HeiAuthTool, HeiClientAuthTool
from core.enums import LoginTypeEnum


def _get_request(*args, **kwargs):
    """从参数中获取 Request 对象"""
    request = kwargs.get('request')
    if request:
        return request
    for arg in args:
        if isinstance(arg, Request):
            return arg
    return None


def HeiCheckLogin(func=None, *, login_type: str = LoginTypeEnum.LOGIN):
    def decorator(f):
        @wraps(f)
        async def wrapper(*args, **kwargs):
            request = _get_request(*args, **kwargs)
            auth_tool = HeiClientAuthTool if login_type == LoginTypeEnum.CLIENT else HeiAuthTool
            if not await auth_tool.isLogin(request):
                raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未授权/未登录")
            return await f(*args, **kwargs)
        return wrapper
    
    if func is not None:
        return decorator(func)
    return decorator


def hei_check_login(func=None, *, login_type: str = LoginTypeEnum.LOGIN):
    return HeiCheckLogin(func, login_type=login_type)