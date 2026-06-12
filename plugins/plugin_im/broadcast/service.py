"""Broadcast management service.

Mirrors hei-gin plugins/plugin-im/broadcast/service.go.
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from core.db import SessionLocal
from core.exception import BusinessException
from core.utils import generate_id
from plugins.plugin_im.model.broadcast import Broadcast, BroadcastRead
from plugins.plugin_im.broadcast.params import BroadcastVO, SendBroadcastParam, BroadcastToBroadcastVO
from plugins.plugin_im.broadcast.repository import BroadcastRepository
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message as WSMessage

import asyncio
import logging
logger = logging.getLogger(__name__)


def _fmt_dt(dt) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


# ═════════════════════════════════════════════════════════════════════
# Send
# ═════════════════════════════════════════════════════════════════════

def send(sender_id: str, p: SendBroadcastParam) -> None:
    scope = p.scope or "ALL"
    now = datetime.now()

    db = SessionLocal()
    try:
        BroadcastRepository(db).insert(Broadcast(
            id=generate_id(),
            title=p.title,
            content=p.content,
            scope=scope,
            sender_id=sender_id,
            created_at=now,
            updated_at=now,
        ))
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()

    # WS broadcast
    payload = {"title": p.title, "content": p.content, "scope": scope, "action": "broadcast"}
    ws_msg = WSMessage(type="broadcast", payload=payload)

    if scope == "ALL":
        if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.broadcast_all(ws_msg))
    elif scope == "BUSINESS":
        if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.broadcast_business(ws_msg))
    elif scope == "CONSUMER":
        if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.broadcast_consumers(ws_msg))


# ═════════════════════════════════════════════════════════════════════
# List (admin)
# ═════════════════════════════════════════════════════════════════════

def list_broadcasts(cursor: str = "", size: int = 20) -> tuple[list[BroadcastVO], bool]:
    if size < 1:
        size = 20
    if size > 100:
        size = 100

    db = SessionLocal()
    try:
        repository = BroadcastRepository(db)
        cursor_dt = None
        if cursor:
            try:
                cursor_dt = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                pass

        records = repository.page(cursor_dt, size)
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        result = [BroadcastToBroadcastVO(b) for b in records]
        return result, has_more
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Unread List
# ═════════════════════════════════════════════════════════════════════

def unread_list(user_id: str, user_type: str) -> tuple[list[BroadcastVO], bool]:
    db = SessionLocal()
    try:
        repository = BroadcastRepository(db)
        records = repository.latest(50)
        read_records = repository.list_reads(user_id, user_type)
        read_map = {r.broadcast_id: r.read_at for r in read_records}

        result = []
        for b in records:
            is_read = b.id in read_map
            read_at = read_map.get(b.id)
            vo = BroadcastToBroadcastVO(b)
            vo.read = is_read
            if is_read and read_at:
                vo.read_at = _fmt_dt(read_at)
            result.append(vo)
        return result, False
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Mark Read
# ═════════════════════════════════════════════════════════════════════

def mark_read(user_id: str, user_type: str, broadcast_id: str) -> None:
    db = SessionLocal()
    try:
        repository = BroadcastRepository(db)
        now = datetime.now()
        existing = repository.find_read(broadcast_id, user_id, user_type)
        if not existing:
            repository.mark_read(BroadcastRead(
                id=generate_id(),
                broadcast_id=broadcast_id,
                user_id=user_id,
                user_type=user_type,
                read_at=now,
            ))
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Detail
# ═════════════════════════════════════════════════════════════════════

def detail(broadcast_id: str) -> Optional[BroadcastVO]:
    db = SessionLocal()
    try:
        b = BroadcastRepository(db).find_by_id(broadcast_id)
        if not b:
            return None
        return BroadcastToBroadcastVO(b)
    finally:
        db.close()
