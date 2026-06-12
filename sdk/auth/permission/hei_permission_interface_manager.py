from typing import Optional
from .hei_permission_interface import HeiPermissionInterface


class HeiPermissionInterfaceManager:
    """
    权限接口管理器
    用于注册和获取权限加载接口实现
    """
    _interface: Optional[HeiPermissionInterface] = None

    @classmethod
    def registerInterface(cls, interface: HeiPermissionInterface):
        """
        注册权限接口实现
        
        Args:
            interface: HeiPermissionInterface 的实现实例
        """
        cls._interface = interface

    @classmethod
    def getInterface(cls) -> Optional[HeiPermissionInterface]:
        """
        获取权限接口实现
        
        Returns:
            HeiPermissionInterface 实例，如果未注册则返回 None
        """
        return cls._interface

    @classmethod
    def hasInterface(cls) -> bool:
        """
        检查是否已注册权限接口
        
        Returns:
            是否已注册
        """
        return cls._interface is not None
