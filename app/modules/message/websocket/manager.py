"""WebSocket connection manager.

Handles per-instance WS connections and uses Redis Pub/Sub for cross-instance routing.
"""

from __future__ import annotations

import asyncio
import json
import logging
from datetime import datetime, timezone
from typing import Any

from fastapi import WebSocket

logger = logging.getLogger(__name__)


class ConnectionManager:
    """Manages WebSocket connections. Singleton per process instance.
    Uses Redis Pub/Sub for cross-instance message routing.
    """

    def __init__(self, redis_pool=None):
        # { account_type: { account_id: { terminal_id: WebSocket } } }
        self._connections: dict[str, dict[str, dict[str, WebSocket]]] = {}
        self._redis = redis_pool
        self._pubsub_task: asyncio.Task | None = None
        self._lock = asyncio.Lock()

    async def connect(self, account_type: str, account_id: str, terminal_id: str, ws: WebSocket) -> None:
        await ws.accept()
        async with self._lock:
            self._connections.setdefault(account_type, {}).setdefault(account_id, {})[terminal_id] = ws
        num = self._count_account_connections(account_type, account_id)
        logger.info("WS connect: %s/%s terminal=%s, total=%d connections for user", account_type, account_id, terminal_id, num)

    async def disconnect(self, account_type: str, account_id: str, terminal_id: str) -> None:
        async with self._lock:
            by_account = self._connections.get(account_type, {}).get(account_id, {})
            by_account.pop(terminal_id, None)
            if not by_account:
                self._connections.get(account_type, {}).pop(account_id, None)
        num = self._count_account_connections(account_type, account_id)
        logger.info("WS disconnect: %s/%s terminal=%s, remaining=%d", account_type, account_id, terminal_id, num)

    def _count_account_connections(self, account_type: str, account_id: str) -> int:
        return len(self._connections.get(account_type, {}).get(account_id, {}))

    def is_online(self, account_type: str, account_id: str) -> bool:
        return self._count_account_connections(account_type, account_id) > 0

    async def send_to_user(self, account_type: str, account_id: str, message: dict) -> None:
        """Send a JSON message to all WS connections of a user on this instance."""
        connections = self._connections.get(account_type, {}).get(account_id, {})
        payload = json.dumps(message, ensure_ascii=False, default=str)
        for terminal_id, ws in list(connections.items()):
            try:
                await ws.send_text(payload)
            except Exception:
                logger.warning("WS send failed to %s/%s terminal=%s, removing", account_type, account_id, terminal_id)
                await self.disconnect(account_type, account_id, terminal_id)

    def get_online_users(self) -> list[tuple[str, str]]:
        """Return list of (account_type, account_id) connected on this instance."""
        result = []
        for account_type, by_account in self._connections.items():
            for account_id in by_account:
                result.append((account_type, account_id))
        return result

    async def broadcast_instance(self, message: dict) -> None:
        """Broadcast to ALL connections on this instance."""
        payload = json.dumps(message, ensure_ascii=False, default=str)
        for account_type, by_account in list(self._connections.items()):
            for account_id, terminals in list(by_account.items()):
                for terminal_id, ws in list(terminals.items()):
                    try:
                        await ws.send_text(payload)
                    except Exception:
                        await self.disconnect(account_type, account_id, terminal_id)
