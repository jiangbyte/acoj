"""
ClientPlugin — lifecycle hooks for the consumer-facing plugin.

Core services (auth, captcha) are handled by core_plugins.
"""

import logging

from sdk.kernel.plugin import HeiPlugin, PluginInfo
from sdk.infra.db import SessionLocal
from plugins.plugin_client.migrate import register_all_models
from plugins.plugin_client.auth.username import init_auth as init_client_auth
from plugins.plugin_client.user import LoginUserApiProvider as CLoginUserApiProvider

logger = logging.getLogger(__name__)


class ClientPlugin(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="plugin_client",
            version="1.0.0",
            description="Consumer-facing APIs: auth, session, user profile",
        )

    def on_init(self):
        """Register C-side login user API provider."""
        register_all_models()
        login_user_api = CLoginUserApiProvider(SessionLocal)
        init_client_auth(login_user_api)
        logger.info("[ClientPlugin] Auth providers registered")

    async def on_stop(self):
        logger.info("[ClientPlugin] Stopped")
