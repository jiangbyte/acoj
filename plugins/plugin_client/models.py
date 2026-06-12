"""plugin_client compatibility exports."""

import logging

from sdk.kernel.registry import HeiBase, get_registered_models

logger = logging.getLogger(__name__)

from plugins.plugin_client.user.models import ClientUser

logger.info(
    "[plugin_client.models] Loaded %d registered models",
    len(get_registered_models()),
)

__all__ = [
    "HeiBase",
    "ClientUser",
]
