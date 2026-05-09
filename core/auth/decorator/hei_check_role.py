from functools import wraps
from typing import List, Union
from fastapi import HTTPException, status, Request
from ..auth import HeiAuthTool, HeiClientAuthTool
from ..permission import HeiPermissionTool
from core.enums import LoginTypeEnum, CheckModeEnum


def _get_request(*args, **kwargs):
    """从参数中获取 Request 对象"""
    request = kwargs.get('request')
    if request:
        return request
    for arg in args:
        if isinstance(arg, Request):
            return arg
    return None


def HeiCheckRole(role: Union[str, List[str]], mode: str = CheckModeEnum.AND, login_type: str = LoginTypeEnum.LOGIN):
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            request = _get_request(*args, **kwargs)
            auth_tool = HeiClientAuthTool if login_type == LoginTypeEnum.CLIENT else HeiAuthTool
            if not await auth_tool.isLogin(request):
                raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未授权/未登录")
            
            roles = [role] if isinstance(role, str) else role
            
            if mode == CheckModeEnum.AND:
                if not await HeiPermissionTool.hasRoleAnd(*roles, request=request, login_type=login_type):
                    raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少角色: {roles}")
            else:
                if not await HeiPermissionTool.hasRoleOr(*roles, request=request, login_type=login_type):
                    raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少角色: {roles}")
            
            return await func(*args, **kwargs)
        return wrapper
    return decorator


def hei_check_role(role: Union[str, List[str]], mode: str = CheckModeEnum.AND, login_type: str = LoginTypeEnum.LOGIN):
    return HeiCheckRole(role, mode, login_type)