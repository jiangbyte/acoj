"""
SysPlugin — lifecycle hooks for the system management plugin.

Mirrors hei-gin's ``plugins/plugin-sys/plugin.go``.
"""

import logging

from sdk.kernel.plugin import HeiPlugin, PluginInfo
from sdk.auth import HeiPermissionInterface, HeiPermissionInterfaceManager
from sdk.infra.db import SessionLocal
from sdk.infra.db.redis import get_client as get_redis_client
from sdk.log import set_log_persister, set_op_user_resolver
from plugins.plugin_sys.persistence import DbLogPersister
from plugins.plugin_sys.migrate import register_all_models, register_all_seeds
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
        register_all_models()
        register_all_seeds()
        HeiPermissionInterfaceManager.registerInterface(
            HeiPermissionInterface(session_factory=SessionLocal, redis_client_getter=get_redis_client)
        )

        login_user_api = BLoginUserApiProvider(SessionLocal)
        init_sys_auth(login_user_api)
        set_op_user_resolver(login_user_api.get_username_by_id)

        # Set up log persistence (mirrors hei-gin's LogPersistence assignment)
        set_log_persister(DbLogPersister(SessionLocal))

        logger.info("[SysPlugin] Permission interface, auth providers, and log persister registered")

    async def on_stop(self):
        logger.info("[SysPlugin] Stopped")
