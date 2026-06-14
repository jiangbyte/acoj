"""
SysPlugin — lifecycle hooks for the system management plugin.

Mirrors hei-gin's ``plugins/plugin-sys/plugin.go``.
"""

import logging

from sdk.kernel.plugin import HeiPlugin, PluginInfo
from sdk.auth.auth.base_auth_tool import BaseAuthTool
from sdk.auth.provider import DatabasePermissionProvider
from sdk.infra.db import SessionLocal
from sdk.infra.db.redis import get_client as get_redis_client
from sdk.log import configure_async_log_persister, start_log_persister, stop_log_persister, set_op_user_resolver
from plugins.plugin_sys.persistence import DbLogPersister
from plugins.plugin_sys.migrate import register_all_models, register_all_seeds
from plugins.plugin_sys.user.service import LoginUserService
from plugins.plugin_sys.file.service import cleanup_stale_chunk_uploads

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
        """Register permission interface + log persister."""
        register_all_models()
        register_all_seeds()
        BaseAuthTool.set_permission_provider(
            DatabasePermissionProvider(session_factory=SessionLocal, redis_client_getter=get_redis_client)
        )
        set_op_user_resolver(LoginUserService(SessionLocal).get_username)

        # Set up log persistence (mirrors hei-gin's LogPersistence assignment)
        configure_async_log_persister(DbLogPersister(SessionLocal))

        logger.info("[SysPlugin] Permission interface and log persister registered")

    async def on_start(self):
        await start_log_persister()
        cleaned_chunks = cleanup_stale_chunk_uploads()
        if cleaned_chunks:
            logger.info("[SysPlugin] Cleaned stale chunk upload directories: %d", cleaned_chunks)
        logger.info("[SysPlugin] Async log persister started")

    async def on_stop(self):
        await stop_log_persister()
        logger.info("[SysPlugin] Stopped")
