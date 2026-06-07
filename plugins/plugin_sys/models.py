"""
plugin_sys — Consolidated ORM models using HeiBase.

Gradual migration strategy:
- Imports existing model classes from ``plugins/plugin_sys/*/models.py`` and
  re-registers them so the migration tool and plugin system can discover them.
- New models should be defined **directly here** using ``HeiBase``.
"""

import logging

from core.plugin.registry import HeiBase, register_model, get_registered_models

logger = logging.getLogger(__name__)

# ═════════════════════════════════════════════════════════════════════
# Re-register existing models from plugins/plugin_sys/
# ═════════════════════════════════════════════════════════════════════

# ── User ──
from plugins.plugin_sys.user.models import (
    SysUser as _SysUser,
    RelUserRole as _RelUserRole,
    RelUserPermission as _RelUserPermission,
)
register_model(_SysUser)
register_model(_RelUserRole)
register_model(_RelUserPermission)

# ── Role ──
from plugins.plugin_sys.role.models import (
    SysRole as _SysRole,
    RelRolePermission as _RelRolePermission,
    RelRoleResource as _RelRoleResource,
)
register_model(_SysRole)
register_model(_RelRolePermission)
register_model(_RelRoleResource)

# ── Resource ──
from plugins.plugin_sys.resource.models import (
    SysModule as _SysModule,
    SysResource as _SysResource,
)
register_model(_SysModule)
register_model(_SysResource)

# ── Dict ──
from plugins.plugin_sys.dict.models import (
    SysDict as _SysDict,
    SysDictItem as _SysDictItem,
)
register_model(_SysDict)
register_model(_SysDictItem)

# ── Config ──
from plugins.plugin_sys.config.models import SysConfig as _SysConfig
register_model(_SysConfig)

# ── Banner ──
from plugins.plugin_sys.banner.models import SysBanner as _SysBanner
register_model(_SysBanner)

# ── Log ──
from plugins.plugin_sys.log.models import SysLog as _SysLog
register_model(_SysLog)

# ── Notice ──
from plugins.plugin_sys.notice.models import SysNotice as _SysNotice
register_model(_SysNotice)

# ── Org ──
from plugins.plugin_sys.org.models import SysOrg as _SysOrg
register_model(_SysOrg)

# ── Position ──
from plugins.plugin_sys.position.models import SysPosition as _SysPosition
register_model(_SysPosition)

# ── Group ──
from plugins.plugin_sys.group.models import SysGroup as _SysGroup
register_model(_SysGroup)

# ── File ──
from plugins.plugin_sys.file.models import SysFile as _SysFile
register_model(_SysFile)

# ── Home ──
from plugins.plugin_sys.home.models import SysQuickAction as _SysQuickAction
register_model(_SysQuickAction)

logger.info(
    "[plugin_sys.models] Registered %d models via HeiBase",
    len(get_registered_models()),
)

# ═════════════════════════════════════════════════════════════════════
# Re-export for backward compatibility
# ═════════════════════════════════════════════════════════════════════

from plugins.plugin_sys.user.models import SysUser, RelUserRole, RelUserPermission
from plugins.plugin_sys.role.models import SysRole, RelRolePermission, RelRoleResource
from plugins.plugin_sys.resource.models import SysModule, SysResource
from plugins.plugin_sys.dict.models import SysDict, SysDictItem
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
    "SysDict", "SysDictItem",
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
