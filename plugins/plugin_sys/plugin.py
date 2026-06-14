"""
SysPlugin — lifecycle hooks for the system management plugin.

Mirrors hei-gin's ``plugins/plugin-sys/plugin.go``.
"""

import logging

from sdk.auth import Business
from sdk.auth.provider import DatabasePermissionProvider
from sdk.infra.db import AsyncSessionLocal, SessionLocal, get_redis
from sdk.kernel.plugin import HeiPlugin, PluginInfo
from sdk.log import configure_async_log_persister, start_log_persister, stop_log_persister, set_op_user_resolver
from sdk.shared.contracts import SUPER_ADMIN_CODE
from plugins.plugin_sys.persistence import DbLogPersister
from plugins.plugin_sys.migrate import register_all_models, register_all_seeds
from plugins.plugin_sys.role.models import RelRolePermission, SysRole
from plugins.plugin_sys.user.models import RelUserPermission, RelUserRole
from plugins.plugin_sys.user.service import LoginUserService
from plugins.plugin_sys.file.service import cleanup_stale_chunk_uploads
from plugins.plugin_sys.analyze.api.v1.api import router as analyze_router
from plugins.plugin_sys.auth.captcha.api.v1.api import router as auth_captcha_router
from plugins.plugin_sys.auth.sm2.api.v1.api import router as auth_sm2_router
from plugins.plugin_sys.auth.username.api.v1.api import router as auth_username_router
from plugins.plugin_sys.banner.api.v1.api import router as banner_router
from plugins.plugin_sys.config.api.v1.api import router as config_router
from plugins.plugin_sys.dict.api.v1.api import router as dict_router
from plugins.plugin_sys.file.api.v1.api import client_router as file_client_router
from plugins.plugin_sys.file.api.v1.api import router as file_router
from plugins.plugin_sys.group.api.v1.api import router as group_router
from plugins.plugin_sys.home.api.v1.api import router as home_router
from plugins.plugin_sys.log.api.v1.api import router as log_router
from plugins.plugin_sys.notice.api.v1.api import router as notice_router
from plugins.plugin_sys.org.api.v1.api import router as org_router
from plugins.plugin_sys.permission.api.v1.api import router as permission_router
from plugins.plugin_sys.position.api.v1.api import router as position_router
from plugins.plugin_sys.resource.api.v1.api import router as resource_router
from plugins.plugin_sys.role.api.v1.api import router as role_router
from plugins.plugin_sys.session.api.v1.api import router as session_router
from plugins.plugin_sys.user.api.v1.api import router as user_router

logger = logging.getLogger(__name__)


class SysPlugin(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="plugin_sys",
            version="1.0.0",
            description="System management: users, roles, permissions, resources, ...",
            dependencies=["auth"],
            settings_prefix="plugins.plugin_sys",
        )

    @classmethod
    def routers(cls) -> tuple[object, ...]:
        return (
            user_router,
            role_router,
            org_router,
            group_router,
            position_router,
            dict_router,
            config_router,
            banner_router,
            home_router,
            log_router,
            notice_router,
            file_router,
            file_client_router,
            resource_router,
            session_router,
            permission_router,
            analyze_router,
            auth_username_router,
            auth_captcha_router,
            auth_sm2_router,
        )

    def on_init(self):
        """注册权限提供器和日志持久化器。"""
        register_all_models()
        register_all_seeds()
        Business.set_permission_provider(
            DatabasePermissionProvider(
                session_factory=AsyncSessionLocal,
                role_model=SysRole,
                role_permission_model=RelRolePermission,
                user_role_model=RelUserRole,
                user_permission_model=RelUserPermission,
                super_admin_code=SUPER_ADMIN_CODE,
                redis_client_getter=get_redis,
            )
        )
        set_op_user_resolver(LoginUserService(AsyncSessionLocal).get_username)
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
