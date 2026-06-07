"""
WebSocket message type definitions.

Mirrors hei-gin's ``plugins/plugin-im/ws/message.go``.
"""

from __future__ import annotations

from typing import Any, Optional
from dataclasses import dataclass, field


# ── Message types ─────────────────────────────────────────────────────

MsgHeartbeat = "heartbeat"
MsgOnlineCount = "online_count"
MsgNewMessage = "new_message"
MsgUnreadCount = "unread_count"
MsgPresence = "presence"
MsgDeliveryAck = "delivery_ack"
MsgTyping = "typing"
MsgConversation = "conversation"


@dataclass
class Message:
    """Envelope sent over WebSocket."""
    type: str
    payload: Any = None

    def to_dict(self) -> dict:
        return {"type": self.type, "payload": self.payload}


# ── Payloads ──────────────────────────────────────────────────────────

@dataclass
class HeartbeatPayload:
    timestamp: int = 0  # milliseconds


@dataclass
class OnlineCountPayload:
    count: int = 0


@dataclass
class NewMessagePayload:
    message_id: str = ""
    conversation_id: str = ""
    title: str = ""
    content: str = ""
    sender_id: str = ""
    sender_type: str = ""
    msg_type: str = ""
    extra: str = ""
    created_at: str = ""


@dataclass
class UnreadCountPayload:
    count: int = 0


@dataclass
class PresencePayload:
    user_id: str = ""
    user_type: str = ""
    online: bool = False


@dataclass
class DeliveryAckPayload:
    message_id: str = ""
    status: str = ""  # "delivered" | "read"


@dataclass
class TypingPayload:
    conversation_id: str = ""
    user_id: str = ""
    user_type: str = ""
