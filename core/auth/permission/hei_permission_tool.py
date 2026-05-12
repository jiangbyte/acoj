from typing import List, Union, Optional
from fastapi import Request, HTTPException, status

from ..auth import HeiAuthTool, HeiClientAuthTool
from .hei_permission_matcher import HeiPermissionMatcher
from .hei_permission_interface_manager import HeiPermissionInterfaceManager
from core.enums import LoginTypeEnum


class HeiPermissionTool:
    """
    权限工具类
    通过 HeiPermissionInterface 获取用户权限和角色信息
    支持通配符权限匹配
    """

    @classmethod
    def _getAuthTool(cls, login_type: str):
        """
        根据登录类型获取对应的认证工具
        
        Args:
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            对应的认证工具类
        """
        if login_type == LoginTypeEnum.CLIENT:
            return HeiClientAuthTool
        return HeiAuthTool

    @classmethod
    async def getPermissionList(cls, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> List[str]:
        """
        获取当前登录用户的权限列表
        
        Args:
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            权限码列表
        """
        interface = HeiPermissionInterfaceManager.getInterface()
        if not interface:
            return []
        
        auth_tool = cls._getAuthTool(login_type)
        login_id = await auth_tool.getLoginIdDefaultNull(request)
        if not login_id:
            return []
        
        return await interface.getPermissionList(login_id, login_type)

    @classmethod
    async def getRoleList(cls, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> List[str]:
        """
        获取当前登录用户的角色列表
        
        Args:
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            角色标识列表
        """
        interface = HeiPermissionInterfaceManager.getInterface()
        if not interface:
            return []
        
        auth_tool = cls._getAuthTool(login_type)
        login_id = await auth_tool.getLoginIdDefaultNull(request)
        if not login_id:
            return []
        
        return await interface.getRoleList(login_id, login_type)

    @classmethod
    async def getPermissionListByLoginId(cls, login_id: Union[str, int], login_type: str = LoginTypeEnum.LOGIN.value) -> List[str]:
        """
        根据登录ID获取权限列表
        
        Args:
            login_id: 登录用户ID
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            权限码列表
        """
        interface = HeiPermissionInterfaceManager.getInterface()
        if not interface:
            return []
        
        return await interface.getPermissionList(login_id, login_type)

    @classmethod
    async def getRoleListByLoginId(cls, login_id: Union[str, int], login_type: str = LoginTypeEnum.LOGIN.value) -> List[str]:
        """
        根据登录ID获取角色列表
        
        Args:
            login_id: 登录用户ID
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            角色标识列表
        """
        interface = HeiPermissionInterfaceManager.getInterface()
        if not interface:
            return []
        
        return await interface.getRoleList(login_id, login_type)

    @classmethod
    async def hasPermission(cls, permission: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> bool:
        """
        判断当前用户是否拥有指定权限
        支持通配符匹配
        
        Args:
            permission: 权限码
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            是否拥有该权限
        """
        permissions = await cls.getPermissionList(request, login_type)
        return HeiPermissionMatcher.has_permission(permission, permissions)

    @classmethod
    async def hasPermissionAnd(cls, *permission_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> bool:
        """
        判断当前用户是否拥有所有指定权限
        支持通配符匹配
        
        Args:
            permission_array: 权限码列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            是否拥有所有权限
        """
        permissions = await cls.getPermissionList(request, login_type)
        return HeiPermissionMatcher.has_permission_and(list(permission_array), permissions)

    @classmethod
    async def hasPermissionOr(cls, *permission_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> bool:
        """
        判断当前用户是否拥有任意一个指定权限
        支持通配符匹配
        
        Args:
            permission_array: 权限码列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            是否拥有任意一个权限
        """
        permissions = await cls.getPermissionList(request, login_type)
        return HeiPermissionMatcher.has_permission_or(list(permission_array), permissions)

    @classmethod
    async def checkPermission(cls, permission: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value):
        """
        校验当前用户是否拥有指定权限，没有则抛出异常
        
        Args:
            permission: 权限码
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Raises:
            HTTPException: 权限不足时抛出 403 异常
        """
        if not await cls.hasPermission(permission, request, login_type):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少权限: {permission}")

    @classmethod
    async def checkPermissionAnd(cls, *permission_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value):
        """
        校验当前用户是否拥有所有指定权限，没有则抛出异常
        
        Args:
            permission_array: 权限码列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Raises:
            HTTPException: 权限不足时抛出 403 异常
        """
        for permission in permission_array:
            await cls.checkPermission(permission, request, login_type)

    @classmethod
    async def checkPermissionOr(cls, *permission_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value):
        """
        校验当前用户是否拥有任意一个指定权限，没有则抛出异常
        
        Args:
            permission_array: 权限码列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Raises:
            HTTPException: 权限不足时抛出 403 异常
        """
        if not await cls.hasPermissionOr(*permission_array, request=request, login_type=login_type):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少权限: {list(permission_array)}")

    @classmethod
    async def hasRole(cls, role: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> bool:
        """
        判断当前用户是否拥有指定角色
        
        Args:
            role: 角色标识
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            是否拥有该角色
        """
        roles = await cls.getRoleList(request, login_type)
        return role in roles

    @classmethod
    async def hasRoleAnd(cls, *role_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> bool:
        """
        判断当前用户是否拥有所有指定角色
        
        Args:
            role_array: 角色标识列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            是否拥有所有角色
        """
        roles = await cls.getRoleList(request, login_type)
        return all(role in roles for role in role_array)

    @classmethod
    async def hasRoleOr(cls, *role_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value) -> bool:
        """
        判断当前用户是否拥有任意一个指定角色
        
        Args:
            role_array: 角色标识列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Returns:
            是否拥有任意一个角色
        """
        roles = await cls.getRoleList(request, login_type)
        return any(role in roles for role in role_array)

    @classmethod
    async def checkRole(cls, role: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value):
        """
        校验当前用户是否拥有指定角色，没有则抛出异常
        
        Args:
            role: 角色标识
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Raises:
            HTTPException: 权限不足时抛出 403 异常
        """
        if not await cls.hasRole(role, request, login_type):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少角色: {role}")

    @classmethod
    async def checkRoleAnd(cls, *role_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value):
        """
        校验当前用户是否拥有所有指定角色，没有则抛出异常
        
        Args:
            role_array: 角色标识列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Raises:
            HTTPException: 权限不足时抛出 403 异常
        """
        for role in role_array:
            await cls.checkRole(role, request, login_type)

    @classmethod
    async def checkRoleOr(cls, *role_array: str, request: Request = None, login_type: str = LoginTypeEnum.LOGIN.value):
        """
        校验当前用户是否拥有任意一个指定角色，没有则抛出异常
        
        Args:
            role_array: 角色标识列表
            request: FastAPI Request 对象
            login_type: 登录类型，如 LoginTypeEnum.LOGIN.value, LoginTypeEnum.CLIENT.value
        
        Raises:
            HTTPException: 权限不足时抛出 403 异常
        """
        if not await cls.hasRoleOr(*role_array, request=request, login_type=login_type):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少角色: {list(role_array)}")
