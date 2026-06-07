"""
CrossHub — Redis-backed cross-instance WebSocket message delivery.

Mirrors hei-gin's ``plugins/plugin-im/ws/cross_hub.go``.
"""

from __future__ import annotations

import asyncio
import json
import logging
import time
from typing import Optional

from .hub import Hub
from .client import Client
from .message import (
    Message, MsgNewMessage, MsgUnreadCount, MsgPresence, PresencePayload,
)
from .config import ws_cfg
from core.db import get_redis
from core.plugin import event_bus

logger = logging.getLogger(__name__)


class CrossHub:
    """Wraps a local Hub with Redis-backed cross-instance IM delivery."""

    def __init__(self, local: Hub) -> None:
        self.local = local
        self._rdb = get_redis()
        self._instance_id = "0"
        self._running = True

        # Try to get instance ID from settings
        try:
            from config.settings import settings
            self._instance_id = str(getattr(settings, "snowflake", {}).get("instance", 0) or 0)
        except Exception:
            self._instance_id = "0"

        # Register hub lifecycle hooks
        local.on_client_registered = self._on_client_registered
        local.on_client_unregistered = self._on_client_unregistered

        if self._rdb:
            logger.info("CrossHub cross-instance mode enabled, instance=%s", self._instance_id)

    # ── Lifecycle hooks ──────────────────────────────────────────────

    def _on_client_registered(self, client: Client) -> None:
        asyncio.ensure_future(self._track_connection(client))
        asyncio.ensure_future(self._broadcast_presence(client.user_id, client.user_type, True))
        asyncio.ensure_future(self._send_unread_on_connect(client))

    def _on_client_unregistered(self, client: Client) -> None:
        asyncio.ensure_future(self._untrack_connection(client))
        asyncio.ensure_future(self._check_presence(client))

    async def _send_unread_on_connect(self, client: Client) -> None:
        msg = Message(type=MsgUnreadCount)
        if client.user_type == "BUSINESS":
            await self.local.send_to_user(client.user_id, msg)
        else:
            await self.local.send_to_consumer(client.user_id, msg)

    async def _check_presence(self, client: Client) -> None:
        if not await self._is_user_online_anywhere(client.user_id, client.user_type):
            await self._broadcast_presence(client.user_id, client.user_type, False)

    # ── Redis helpers ────────────────────────────────────────────────

    def _user_set_key(self, user_type: str, user_id: str) -> str:
        return f"ws:user:{user_type}:{user_id}"

    def _instance_key(self) -> str:
        return f"ws:instance:{self._instance_id}"

    def _msg_list_key(self) -> str:
        return f"ws:messages:{self._instance_id}"

    def _dedup_key(self, message_id: str) -> str:
        return f"ws:dedup:{message_id}"

    def _rate_limit_key(self, user_id: str, user_type: str) -> str:
        return f"ws:ratelimit:{user_type}:{user_id}"

    # ── Connection tracking ──────────────────────────────────────────

    async def _track_connection(self, client: Client) -> None:
        if not self._rdb:
            return
        key = self._user_set_key(client.user_type, client.user_id)
        await self._rdb.sadd(key, self._instance_id)
        await self._rdb.expire(key, ws_cfg.instance_ttl * 2)

        inst_key = self._instance_key()
        await self._rdb.setex(inst_key, ws_cfg.instance_ttl, str(time.time()))

    async def _untrack_connection(self, client: Client) -> None:
        if not self._rdb:
            return
        key = self._user_set_key(client.user_type, client.user_id)
        await self._rdb.srem(key, self._instance_id)

    def _get_target_instances(self, user_id: str, user_type: str) -> list[str]:
        """Get all instance IDs where a user is connected (synchronous Redis call)."""
        if not self._rdb:
            return []
        try:
            members = self._rdb.smembers(self._user_set_key(user_type, user_id))
            return [m.decode() if isinstance(m, bytes) else m for m in members]
        except Exception:
            return []

    async def _is_user_online_anywhere(self, user_id: str, user_type: str) -> bool:
        if not self._rdb:
            return self.local.is_user_connected(user_id, user_type)
        try:
            members = await self._rdb.smembers(self._user_set_key(user_type, user_id))
            return len(members) > 0
        except Exception:
            return False

    # ── Presence ─────────────────────────────────────────────────────

    async def _broadcast_presence(self, user_id: str, user_type: str, online: bool) -> None:
        msg = Message(
            type=MsgPresence,
            payload=PresencePayload(
                user_id=user_id, user_type=user_type, online=online,
            ).__dict__,
        )
        if user_type == "BUSINESS":
            await self.local.broadcast_business(msg)
        else:
            await self.local.broadcast_consumers(msg)

    # ── Rate limiting ────────────────────────────────────────────────

    async def allow_message(self, sender_id: str, sender_type: str) -> bool:
        """Check if the sender is allowed to send a message (rate limit)."""
        if not self._rdb:
            return True
        key = self._rate_limit_key(sender_id, sender_type)
        try:
            current = await self._rdb.incr(key)
            if current == 1:
                await self._rdb.expire(key, ws_cfg.rate_limit_window)
            return current <= ws_cfg.rate_limit_max
        except Exception:
            return True

    # ── Cross-instance delivery ──────────────────────────────────────

    async def _publish_to_remote(
        self, user_id: str, user_type: str, msg: Message, message_id: str = ""
    ) -> None:
        if not self._rdb:
            return
        instances = self._get_target_instances(user_id, user_type)
        if not instances:
            return

        raw_msg = json.dumps(msg.to_dict(), ensure_ascii=False)
        x_msg = json.dumps({
            "to_user_id": user_id,
            "to_user_type": user_type,
            "message": raw_msg,
            "message_id": message_id,
            "timestamp": int(time.time() * 1000),
        }, ensure_ascii=False)

        for inst_id in instances:
            if inst_id == self._instance_id:
                continue
            list_key = f"ws:messages:{inst_id}"
            try:
                await self._rdb.lpush(list_key, x_msg)
                await self._rdb.expire(list_key, 300)  # 5 min TTL
            except Exception:
                logger.exception("Failed to push to instance %s", inst_id)

    # ── Public API ───────────────────────────────────────────────────

    async def send_to_user(self, user_id: str, msg: Message, message_id: str = "") -> None:
        await self.local.send_to_user(user_id, msg)
        await self._publish_to_remote(user_id, "BUSINESS", msg, message_id)

    async def send_to_users(self, user_ids: list[str], msg: Message) -> None:
        await self.local.send_to_users(user_ids, msg)
        for uid in user_ids:
            await self._publish_to_remote(uid, "BUSINESS", msg)

    async def send_to_consumer(self, user_id: str, msg: Message, message_id: str = "") -> None:
        await self.local.send_to_consumer(user_id, msg)
        await self._publish_to_remote(user_id, "CONSUMER", msg, message_id)

    async def send_to_consumers(self, user_ids: list[str], msg: Message) -> None:
        await self.local.send_to_consumers(user_ids, msg)
        for uid in user_ids:
            await self._publish_to_remote(uid, "CONSUMER", msg)

    async def broadcast_all(self, msg: Message) -> None:
        await self.local.broadcast_all(msg)

    async def broadcast_business(self, msg: Message) -> None:
        await self.local.broadcast_business(msg)

    async def broadcast_consumers(self, msg: Message) -> None:
        await self.local.broadcast_consumers(msg)

    def online_count(self) -> int:
        return self.local.online_count()

    # ── Poll loop ────────────────────────────────────────────────────

    async def start_poll_loop(self) -> None:
        """Poll for cross-instance messages intended for this instance."""
        if not self._rdb:
            return
        while self._running:
            try:
                result = await self._rdb.brpop(self._msg_list_key(), timeout=ws_cfg.poll_timeout)
                if result is None:
                    continue
                _, raw = result
                data = json.loads(raw)
                msg_data = json.loads(data["message"])
                msg = Message(**msg_data)

                to_type = data.get("to_user_type", "BUSINESS")
                to_id = data.get("to_user_id")

                if to_type == "BUSINESS":
                    await self.local.send_to_user(to_id, msg)
                else:
                    await self.local.send_to_consumer(to_id, msg)
            except Exception:
                if self._running:
                    logger.exception("CrossHub poll error")

    # ── Heartbeat ────────────────────────────────────────────────────

    async def start_heartbeat(self) -> None:
        """Periodically refresh the instance key in Redis."""
        if not self._rdb:
            return
        interval = max(ws_cfg.heartbeat_interval, 15)
        while self._running:
            await asyncio.sleep(interval)
            try:
                inst_key = self._instance_key()
                await self._rdb.setex(inst_key, ws_cfg.instance_ttl, str(time.time()))
            except Exception:
                logger.exception("CrossHub heartbeat error")

    # ── Close ────────────────────────────────────────────────────────

    async def close(self) -> None:
        self._running = False
        if self._rdb:
            try:
                await self._rdb.delete(self._instance_key())
                await self._rdb.delete(self._msg_list_key())
            except Exception:
                pass
