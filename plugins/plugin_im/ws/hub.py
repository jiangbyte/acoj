"""
WebSocket Hub — manages connected clients, online count, presence.

Mirrors hei-gin's ``plugins/plugin-im/ws/hub.go``.
"""

from __future__ import annotations

import asyncio
import json
import logging
from typing import Callable, Optional

from fastapi import WebSocket, WebSocketDisconnect

from .client import Client
from .message import (
    Message, MsgOnlineCount, MsgHeartbeat, OnlineCountPayload,
)
from .config import ws_cfg

logger = logging.getLogger(__name__)


class Hub:
    """Maintains the set of active clients, with per-IP and per-user limits."""

    def __init__(self) -> None:
        self._clients: dict[int, Client] = {}  # id(client) -> Client
        self._ip_count: dict[str, int] = {}
        self._lock = asyncio.Lock()

        # Lifecycle hooks (set by CrossHub)
        self.on_client_registered: Optional[Callable[[Client], None]] = None
        self.on_client_unregistered: Optional[Callable[[Client], None]] = None

    # ── Registration ─────────────────────────────────────────────────

    async def register(self, client: Client) -> bool:
        """Register a client with IP and per-user rate limiting."""
        async with self._lock:
            ip = client.ip
            if ip:
                if self._ip_count.get(ip, 0) >= ws_cfg.max_clients_per_ip:
                    logger.warning("IP %s exceeded max connections (%d)", ip, ws_cfg.max_clients_per_ip)
                    return False

            # Per-user limit
            user_count = sum(
                1 for c in self._clients.values()
                if c.user_id == client.user_id and c.user_type == client.user_type
            )
            if user_count >= ws_cfg.max_clients_per_user:
                logger.warning(
                    "User %s/%s exceeded max connections (%d)",
                    client.user_type, client.user_id, ws_cfg.max_clients_per_user,
                )
                return False

            cid = id(client)
            self._clients[cid] = client
            if ip:
                self._ip_count[ip] = self._ip_count.get(ip, 0) + 1

            count = len(self._clients)

        if self.on_client_registered:
            self.on_client_registered(client)

        logger.info(
            "Client connected: %s/%s from %s (online: %d)",
            client.user_type, client.user_id, ip, count,
        )
        return True

    async def unregister(self, client: Client) -> None:
        """Remove a client from the hub."""
        async with self._lock:
            cid = id(client)
            if cid not in self._clients:
                return
            del self._clients[cid]
            ip = client.ip
            if ip:
                self._ip_count[ip] = self._ip_count.get(ip, 0) - 1
                if self._ip_count[ip] <= 0:
                    del self._ip_count[ip]
            count = len(self._clients)

        if self.on_client_unregistered:
            self.on_client_unregistered(client)

        await client.close()
        logger.info(
            "Client disconnected: %s/%s (online: %d)",
            client.user_type, client.user_id, count,
        )

    # ── Queries ──────────────────────────────────────────────────────

    def online_count(self) -> int:
        return len(self._clients)

    def is_user_connected(self, user_id: str, user_type: str) -> bool:
        return any(
            c.user_id == user_id and c.user_type == user_type
            for c in self._clients.values()
        )

    # ── Send helpers ─────────────────────────────────────────────────

    async def send_to_user(self, user_id: str, msg: Message) -> None:
        for c in self._clients.values():
            if c.user_type == "BUSINESS" and c.user_id == user_id:
                await c.send_json(msg)

    async def send_to_users(self, user_ids: list[str], msg: Message) -> None:
        user_set = set(user_ids)
        for c in self._clients.values():
            if c.user_type == "BUSINESS" and c.user_id in user_set:
                await c.send_json(msg)

    async def send_to_consumer(self, user_id: str, msg: Message) -> None:
        for c in self._clients.values():
            if c.user_type == "CONSUMER" and c.user_id == user_id:
                await c.send_json(msg)

    async def send_to_consumers(self, user_ids: list[str], msg: Message) -> None:
        user_set = set(user_ids)
        for c in self._clients.values():
            if c.user_type == "CONSUMER" and c.user_id in user_set:
                await c.send_json(msg)

    async def broadcast_all(self, msg: Message) -> None:
        for c in self._clients.values():
            await c.send_json(msg)

    async def broadcast_business(self, msg: Message) -> None:
        for c in self._clients.values():
            if c.user_type == "BUSINESS":
                await c.send_json(msg)

    async def broadcast_consumers(self, msg: Message) -> None:
        for c in self._clients.values():
            if c.user_type == "CONSUMER":
                await c.send_json(msg)

    # ── Online count broadcast ───────────────────────────────────────

    async def start_online_broadcast(self) -> None:
        """Periodically broadcast the online count to all clients."""
        interval = max(ws_cfg.online_broadcast_interval, 10)
        while True:
            await asyncio.sleep(interval)
            try:
                count = self.online_count()
                await self.broadcast_all(Message(
                    type=MsgOnlineCount,
                    payload=OnlineCountPayload(count=count).__dict__,
                ))
            except Exception:
                logger.exception("Online broadcast failed")

    # ── WebSocket handler ────────────────────────────────────────────

    async def handle_websocket(
        self, websocket: WebSocket, user_id: str, user_type: str, ip: str = ""
    ) -> None:
        """Accept a WebSocket connection, register client, and run read/write loops."""
        await websocket.accept()

        client = Client(websocket, user_id, user_type, ip)
        if not await self.register(client):
            await websocket.close(code=1008)
            return

        try:
            # Read loop
            while True:
                raw = await websocket.receive_text()
                try:
                    data = json.loads(raw)
                    msg_type = data.get("type")
                    if msg_type == MsgHeartbeat:
                        await client.send_pong(self.online_count())
                except (json.JSONDecodeError, KeyError):
                    pass
        except WebSocketDisconnect:
            pass
        except Exception:
            logger.exception("WebSocket read error for %s/%s", user_type, user_id)
        finally:
            await self.unregister(client)


# ── Singletons ─────────────────────────────────────────────────────────

GlobalHub = Hub()
GlobalCrossHub = None
