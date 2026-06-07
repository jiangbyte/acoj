"""
plugin_client — Client-facing ORM models.

Imports and re-registers existing client models, and provides HeiBase
for future model definitions.
"""

import logging

from core.plugin.registry import HeiBase, register_model, get_registered_models

logger = logging.getLogger(__name__)

# ── Client User ──
from plugins.plugin_client.user.models import ClientUser as _ClientUser
register_model(_ClientUser)

logger.info(
    "[plugin_client.models] Registered %d models via HeiBase",
    len(get_registered_models()),
)

# ── Re-export for backward compatibility ────────────────────────────
from plugins.plugin_client.user.models import ClientUser

__all__ = [
    "HeiBase",
    "ClientUser",
]
