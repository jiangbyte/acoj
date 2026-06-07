"""
SysPlugin — lifecycle hooks for the system management plugin.

Mirrors hei-gin's ``plugins/plugin-sys/plugin.go``.
"""

import logging

from core.plugin import HeiPlugin, PluginInfo
from core.auth import HeiPermissionInterface, HeiPermissionInterfaceManager
from core.db import SessionLocal
from core.log import set_log_persister
from plugins.plugin_sys.persistence import DbLogPersister
from plugins.plugin_sys.user import LoginUserApiProvider as BLoginUserApiProvider
from plugins.plugin_sys.auth import init_auth as init_sys_auth

logger = logging.getLogger(__name__)


class SysPlugin(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="plugin_sys",
            version="1.0.0",
            description="System management: users, roles, permissions, resources, ...",
        )

    def on_init(self):
        """Register permission interface + B-side auth providers + log persister."""
        HeiPermissionInterfaceManager.registerInterface(HeiPermissionInterface())

        login_user_api = BLoginUserApiProvider(SessionLocal)
        init_sys_auth(login_user_api)

        # Set up log persistence (mirrors hei-gin's LogPersistence assignment)
        set_log_persister(DbLogPersister())

        logger.info("[SysPlugin] Permission interface, auth providers, and log persister registered")

    async def on_stop(self):
        logger.info("[SysPlugin] Stopped")
