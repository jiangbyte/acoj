"""Migration registration for plugin_sys.role."""

from core.db import register_model
from .models import SysRole, RelRolePermission, RelRoleResource

register_model(SysRole)
register_model(RelRolePermission)
register_model(RelRoleResource)
