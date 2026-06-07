"""
WebSocket configuration, loaded from settings.

Mirrors hei-gin's plugins/plugin-im/ws/config.go.
"""

from __future__ import annotations

import logging
from dataclasses import dataclass
from config.settings import settings

logger = logging.getLogger(__name__)


@dataclass
class WSConfig:
    """WebSocket configuration."""
    read_buffer_size: int = 1024
    write_buffer_size: int = 1024
    heartbeat_interval: int = 30
    instance_ttl: int = 60
    stale_clean_interval: int = 5
    rate_limit_window: int = 10
    rate_limit_max: int = 30
    dedup_ttl: int = 30
    poll_timeout: int = 2
    pong_timeout: int = 10
    write_timeout: int = 10
    online_broadcast_interval: int = 60
    max_clients_per_ip: int = 10
    max_clients_per_user: int = 3


def load_config() -> WSConfig:
    """Load WS config from settings."""
    s = getattr(settings, "ws", None)
    if s is None:
        return WSConfig()
    return WSConfig(
        read_buffer_size=getattr(s, "read_buffer_size", 1024),
        write_buffer_size=getattr(s, "write_buffer_size", 1024),
        heartbeat_interval=getattr(s, "heartbeat_interval", 30),
        instance_ttl=getattr(s, "instance_ttl", 60),
        stale_clean_interval=getattr(s, "stale_clean_interval", 5),
        rate_limit_window=getattr(s, "rate_limit_window", 10),
        rate_limit_max=getattr(s, "rate_limit_max", 30),
        dedup_ttl=getattr(s, "dedup_ttl", 30),
        poll_timeout=getattr(s, "poll_timeout", 2),
        pong_timeout=getattr(s, "pong_timeout", 10),
        write_timeout=getattr(s, "write_timeout", 10),
        online_broadcast_interval=getattr(s, "online_broadcast_interval", 60),
        max_clients_per_ip=getattr(s, "max_clients_per_ip", 10),
        max_clients_per_user=getattr(s, "max_clients_per_user", 3),
    )


ws_cfg: WSConfig = load_config()
