"""
plugin_sys provider — interface implementations.
Mirrors hei-gin's ``plugins/plugin-sys/provider/``.
"""

from .user_provider import UserProvider
from .permission_provider import PermissionProvider

__all__ = ["UserProvider", "PermissionProvider"]
