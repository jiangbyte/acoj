"""
WebSocket client — single connection handler.

Mirrors hei-gin's ``plugins/plugin-im/ws/client.go``.
"""

from __future__ import annotations

import asyncio
import logging

from fastapi import WebSocket
from .message import Message, OnlineCountPayload, MsgOnlineCount
from .config import ws_cfg

logger = logging.getLogger(__name__)


class Client:
    """Represents a single WebSocket connection."""

    def __init__(
        self,
        websocket: WebSocket,
        user_id: str,
        user_type: str,
        ip: str = "",
    ) -> None:
        self.websocket = websocket
        self.user_id = user_id
        self.user_type = user_type
        self.ip = ip
        self._closed = False

    async def send_json(self, msg: Message) -> None:
        """Send a JSON message to this client."""
        if self._closed:
            return
        try:
            await asyncio.wait_for(self.websocket.send_json(msg.to_dict()), timeout=ws_cfg.write_timeout)
        except TimeoutError:
            self._closed = True
            logger.warning("WebSocket send timeout for client %s/%s", self.user_type, self.user_id)
        except Exception:
            self._closed = True
            logger.debug("Failed to send to client %s/%s", self.user_type, self.user_id)

    async def send_pong(self, online_count: int) -> None:
        """Reply to a heartbeat with online count."""
        await self.send_json(Message(
            type=MsgOnlineCount,
            payload=OnlineCountPayload(count=online_count).model_dump(),
        ))

    async def close(self) -> None:
        """Close the connection."""
        self._closed = True
        try:
            await self.websocket.close()
        except Exception:
            pass
