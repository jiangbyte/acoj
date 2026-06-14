"""
ClientPlugin — lifecycle hooks for the consumer-facing plugin.

Core services (auth, captcha) are handled by core_plugins.
"""

import logging

from sdk.auth import CONSUMER_REALM_ID, register_access_provider
from sdk.auth.provider import EmptyPermissionProvider
from sdk.kernel.plugin import HeiPlugin, PluginInfo
from plugins.plugin_client.migrate import register_all_models
from plugins.plugin_client.auth.captcha.api.v1.api import router as auth_captcha_router
from plugins.plugin_client.auth.sm2.api.v1.api import router as auth_sm2_router
from plugins.plugin_client.auth.username.api.v1.api import router as auth_username_router
from plugins.plugin_client.session.api.v1.api import router as session_router
from plugins.plugin_client.user.api.v1.api import router as user_router

logger = logging.getLogger(__name__)


class ClientPlugin(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="plugin_client",
            version="1.0.0",
            description="Consumer-facing APIs: auth, session, user profile",
            dependencies=["auth"],
            settings_prefix="plugins.plugin_client",
        )

    @classmethod
    def routers(cls) -> tuple[object, ...]:
        return (
            session_router,
            user_router,
            auth_captcha_router,
            auth_sm2_router,
            auth_username_router,
        )

    def on_init(self):
        """注册 C 端模型和空权限提供器。"""
        register_all_models()
        register_access_provider(CONSUMER_REALM_ID, EmptyPermissionProvider())
        logger.info("[ClientPlugin] Models registered")

    async def on_stop(self):
        logger.info("[ClientPlugin] Stopped")
