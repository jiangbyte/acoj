"""
ClientPlugin — lifecycle hooks for the consumer-facing plugin.

Core services (auth, captcha) are handled by core_plugins.
"""

import logging

from sdk.kernel.plugin import HeiPlugin, PluginInfo
from plugins.plugin_client.migrate import register_all_models

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
        """Register C-side models."""
        register_all_models()
        logger.info("[ClientPlugin] Models registered")

    async def on_stop(self):
        logger.info("[ClientPlugin] Stopped")
