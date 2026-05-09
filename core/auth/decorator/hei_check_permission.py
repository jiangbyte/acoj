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


def HeiCheckPermission(permission: Union[str, List[str]], mode: str = CheckModeEnum.AND, login_type: str = LoginTypeEnum.LOGIN):
    def decorator(func):
        # Attach permission metadata for auto-discovery scanner
        func._hei_permission = permission
        func._hei_permission_mode = mode
        func._hei_login_type = login_type

        @wraps(func)
        async def wrapper(*args, **kwargs):
            request = _get_request(*args, **kwargs)
            auth_tool = HeiClientAuthTool if login_type == LoginTypeEnum.CLIENT else HeiAuthTool
            if not await auth_tool.isLogin(request):
                raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未授权/未登录")

            permissions = [permission] if isinstance(permission, str) else permission

            if mode == CheckModeEnum.AND:
                if not await HeiPermissionTool.hasPermissionAnd(*permissions, request=request, login_type=login_type):
                    raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少权限: {permissions}")
            else:
                if not await HeiPermissionTool.hasPermissionOr(*permissions, request=request, login_type=login_type):
                    raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少权限: {permissions}")

            return await func(*args, **kwargs)

        # Copy metadata to wrapper
        wrapper._hei_permission = getattr(func, "_hei_permission", None)
        wrapper._hei_permission_mode = getattr(func, "_hei_permission_mode", None)
        wrapper._hei_login_type = getattr(func, "_hei_login_type", None)
        return wrapper
    return decorator


def hei_check_permission(permission: Union[str, List[str]], mode: str = CheckModeEnum.AND, login_type: str = LoginTypeEnum.LOGIN):
    return HeiCheckPermission(permission, mode, login_type)