"""plugin_sys compatibility exports."""

import logging

from sdk.kernel.registry import HeiBase, get_registered_models

logger = logging.getLogger(__name__)

logger.info(
    "[plugin_sys.models] Loaded %d registered models",
    len(get_registered_models()),
)

from plugins.plugin_sys.user.models import SysUser, RelUserRole, RelUserPermission
from plugins.plugin_sys.role.models import SysRole, RelRolePermission, RelRoleResource
from plugins.plugin_sys.resource.models import SysModule, SysResource
from plugins.plugin_sys.dict.models import SysDict
from plugins.plugin_sys.config.models import SysConfig
from plugins.plugin_sys.banner.models import SysBanner
from plugins.plugin_sys.log.models import SysLog
from plugins.plugin_sys.notice.models import SysNotice
from plugins.plugin_sys.org.models import SysOrg
from plugins.plugin_sys.position.models import SysPosition
from plugins.plugin_sys.group.models import SysGroup
from plugins.plugin_sys.file.models import SysFile
from plugins.plugin_sys.home.models import SysQuickAction

__all__ = [
    "HeiBase",
    "SysUser", "RelUserRole", "RelUserPermission",
    "SysRole", "RelRolePermission", "RelRoleResource",
    "SysModule", "SysResource",
    "SysDict",
    "SysConfig",
    "SysBanner",
    "SysLog",
    "SysNotice",
    "SysOrg",
    "SysPosition",
    "SysGroup",
    "SysFile",
    "SysQuickAction",
]
