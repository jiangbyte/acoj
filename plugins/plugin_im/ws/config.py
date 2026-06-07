"""
WebSocket configuration, loaded from settings.

Mirrors hei-gin's ``plugins/plugin-im/ws/config.go``.
"""

from __future__ import annotations

import logging
from dataclasses import dataclass, field
from config.settings import settings

logger = logging.getLogger(__name__)


@dataclass
class WSConfig:
    """WebSocket configuration."""
    read_buffer_size: int = 1024
    write_buffer_size: int = 1024
    heartbeat_interval: int = 30          # seconds
    instance_ttl: int = 60                # seconds
    stale_clean_interval: int = 5         # minutes
    rate_limit_window: int = 10           # seconds
    rate_limit_max: int = 30              # messages per window
    dedup_ttl: int = 30                   # seconds
    poll_timeout: int = 2                 # seconds
    pong_timeout: int = 10                # seconds
    write_timeout: int = 10               # seconds
    online_broadcast_interval: int = 60   # seconds
    max_clients_per_ip: int = 10
    max_clients_per_user: int = 3


def load_config() -> WSConfig:
    """Load WS config from settings."""
    cfg = WSConfig()
    raw = getattr(settings, "ws", None) or {}
    for key, default_type in [
        ("read_buffer_size", int), ("write_buffer_size", int),
        ("heartbeat_interval", int), ("instance_ttl", int),
        ("stale_clean_interval", int), ("rate_limit_window", int),
        ("rate_limit_max", int), ("dedup_ttl", int),
        ("poll_timeout", int), ("pong_timeout", int),
        ("write_timeout", int), ("online_broadcast_interval", int),
        ("max_clients_per_ip", int), ("max_clients_per_user", int),
    ]:
        val = raw.get(key)
        if val is not None:
            try:
                setattr(cfg, key, default_type(val))
            except (ValueError, TypeError):
                logger.warning("Invalid ws config value for %s: %s", key, val)
    return cfg


# Module-level singleton
ws_cfg: WSConfig = load_config()
