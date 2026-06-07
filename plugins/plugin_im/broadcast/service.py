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
from plugins.plugin_im.broadcast.params import BroadcastVO, SendBroadcastParam
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
        db.add(Broadcast(
            id=generate_id(),
            title=p.title,
            content=p.content,
            scope=scope,
            sender_id=sender_id,
            created_at=now,
            updated_at=now,
        ))
        db.commit()
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
        q = db.query(Broadcast)
        if cursor:
            try:
                t = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
                q = q.filter(Broadcast.created_at < t)
            except ValueError:
                pass

        records = q.order_by(Broadcast.created_at.desc()).limit(size + 1).all()
        has_more = len(records) > size
        if has_more:
            records = records[:size]

        result = [
            BroadcastVO(
                id=b.id, title=b.title, content=b.content or "",
                scope=b.scope, sender_id=b.sender_id,
                created_at=_fmt_dt(b.created_at),
            )
            for b in records
        ]
        return result, has_more
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Unread List
# ═════════════════════════════════════════════════════════════════════

def unread_list(user_id: str, user_type: str) -> tuple[list[BroadcastVO], bool]:
    db = SessionLocal()
    try:
        records = db.query(Broadcast).order_by(Broadcast.created_at.desc()).limit(50).all()
        read_records = db.query(BroadcastRead).filter(
            BroadcastRead.user_id == user_id,
            BroadcastRead.user_type == user_type,
        ).all()
        read_map = {r.broadcast_id: r.read_at for r in read_records}

        result = []
        for b in records:
            is_read = b.id in read_map
            read_at = read_map.get(b.id)
            vo = BroadcastVO(
                id=b.id, title=b.title, content=b.content or "",
                scope=b.scope, read=is_read,
                created_at=_fmt_dt(b.created_at),
            )
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
        now = datetime.now()
        existing = db.query(BroadcastRead).filter(
            BroadcastRead.broadcast_id == broadcast_id,
            BroadcastRead.user_id == user_id,
            BroadcastRead.user_type == user_type,
        ).first()
        if not existing:
            db.add(BroadcastRead(
                id=generate_id(),
                broadcast_id=broadcast_id,
                user_id=user_id,
                user_type=user_type,
                read_at=now,
            ))
            db.commit()
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
        b = db.query(Broadcast).filter(Broadcast.id == broadcast_id).first()
        if not b:
            return None
        return BroadcastVO(
            id=b.id, title=b.title, content=b.content or "",
            scope=b.scope, sender_id=b.sender_id,
            created_at=_fmt_dt(b.created_at),
        )
    finally:
        db.close()
