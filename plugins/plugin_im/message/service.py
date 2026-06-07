"""Message service — single-chat message CRUD.

Mirrors hei-gin plugins/plugin-im/message/service.go.
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import or_, and_
from sqlalchemy.dialects.mysql import insert as mysql_upsert

from core.db import SessionLocal
from core.exception import BusinessException
from core.result import page_data
from core.utils import generate_id
from core.enums import LoginTypeEnum as LTE
from plugins.plugin_im.model.message import (
    Message, Conversation, MsgTypeText, MsgTypeSystem,
    generate_conversation_id,
)
from plugins.plugin_im.message.params import (
    MessageVO, MessagePageParam, MessageSendParam,
    RecallParam, ForwardParam, SearchParam, ConversationMessageVO,
)
from plugins.plugin_im.ws import GlobalCrossHub, Message as WSMessage

import logging
import asyncio

logger = logging.getLogger(__name__)


def _fmt_dt(dt) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def _to_vo(e: Message) -> MessageVO:
    return MessageVO(
        id=e.id, conversation_id=e.conversation_id,
        content=e.content or "", msg_type=e.msg_type,
        extra=e.extra or "",
        sender_id=e.sender_id or "", sender_type=e.sender_type or "",
        receiver_id=e.receiver_id or "", receiver_type=e.receiver_type or "",
        status=e.status,
        read_at=_fmt_dt(e.read_at) if e.read_at else None,
        created_at=_fmt_dt(e.created_at),
        updated_at=_fmt_dt(e.updated_at),
    )


def _to_vo_list(records: list[Message]) -> list[MessageVO]:
    return [_to_vo(r) for r in records]


# ═════════════════════════════════════════════════════════════════════
# Send
# ═════════════════════════════════════════════════════════════════════

async def send_message(param: MessageSendParam, sender_id: str, sender_type: str) -> list[str]:
    db = SessionLocal()
    try:
        # Rate limit check
        if GlobalCrossHub and not await GlobalCrossHub.allow_message(sender_id, sender_type):
            raise BusinessException("发送消息过于频繁，请稍后重试", 429)

        msg_type = param.msg_type or "TEXT"
        receiver_type = param.receiver_type or "BUSINESS"
        now = datetime.now()

        records = []
        for rid in param.receiver_ids:
            cid = generate_conversation_id(
                sender_id, LTE(sender_type),
                rid, LTE(receiver_type),
            )
            records.append(Message(
                id=generate_id(),
                conversation_id=cid,
                content=param.content,
                extra=param.extra,
                msg_type=msg_type,
                sender_id=sender_id,
                sender_type=sender_type,
                receiver_id=rid,
                receiver_type=receiver_type,
                status="unread",
                created_at=now,
                updated_at=now,
            ))

        for rec in records:
            db.add(rec)
        db.flush()

        # Upsert conversation cache
        for rec in records:
            stmt = mysql_upsert(Conversation).values(
                id=rec.conversation_id,
                from_id=rec.sender_id, from_type=rec.sender_type,
                to_id=rec.receiver_id, to_type=rec.receiver_type,
                last_msg=rec.content, last_time=now,
                created_at=now, updated_at=now,
            ).on_duplicate_key_update(
                last_msg=rec.content, last_time=now, updated_at=now,
            )
            db.execute(stmt)

        db.commit()

        # WS notifications
        for i, rid in enumerate(param.receiver_ids):
            rec = records[i]
            ws_payload = {
                "message_id": rec.id,
                "conversation_id": rec.conversation_id,
                "content": param.content,
                "msg_type": msg_type,
                "extra": param.extra or "",
                "sender_id": sender_id,
                "sender_type": sender_type,
                "title": "",
                "created_at": _fmt_dt(now),
            }
            ws_msg = WSMessage(type="new_message", payload=ws_payload)
            if receiver_type == "CONSUMER":
                asyncio.ensure_future(GlobalCrossHub.send_to_consumer(rid, ws_msg, rec.id))
            else:
                asyncio.ensure_future(GlobalCrossHub.send_to_user(rid, ws_msg, rec.id))

        return [r.conversation_id for r in records]
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Page
# ═════════════════════════════════════════════════════════════════════

def page_messages(user_id: str, param: MessagePageParam) -> dict:
    db = SessionLocal()
    try:
        q = db.query(Message).filter(
            or_(Message.sender_id == user_id, Message.receiver_id == user_id),
            or_(Message.deleted_by != user_id, Message.deleted_by.is_(None)),
        )
        if param.status:
            q = q.filter(Message.status == param.status)

        total = q.count()
        records = q.order_by(Message.created_at.desc()).offset(
            (param.current - 1) * param.size
        ).limit(param.size).all()

        return page_data(_to_vo_list(records), total, param.current, param.size)
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Unread Count
# ═════════════════════════════════════════════════════════════════════

def unread_count(user_id: str) -> int:
    db = SessionLocal()
    try:
        return db.query(Message).filter(
            Message.receiver_id == user_id,
            Message.status == "unread",
        ).count()
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Detail
# ═════════════════════════════════════════════════════════════════════

def detail_message(message_id: str) -> Optional[MessageVO]:
    db = SessionLocal()
    try:
        entity = db.query(Message).filter(Message.id == message_id).first()
        return _to_vo(entity) if entity else None
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Mark Read
# ═════════════════════════════════════════════════════════════════════

def mark_read(message_id: str) -> None:
    db = SessionLocal()
    try:
        now = datetime.now()
        db.query(Message).filter(Message.id == message_id).update(
            {"status": "read", "read_at": now}
        )
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def mark_conversation_read(receiver_id: str, conversation_id: str) -> None:
    db = SessionLocal()
    try:
        now = datetime.now()
        db.query(Message).filter(
            Message.conversation_id == conversation_id,
            Message.receiver_id == receiver_id,
            Message.status == "unread",
        ).update({"status": "read", "read_at": now})
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def mark_all_read(receiver_id: str) -> None:
    db = SessionLocal()
    try:
        now = datetime.now()
        db.query(Message).filter(
            Message.receiver_id == receiver_id,
            Message.status == "unread",
        ).update({"status": "read", "read_at": now})
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Remove (soft-delete)
# ═════════════════════════════════════════════════════════════════════

def remove_messages(user_id: str, ids: list[str]) -> None:
    if not ids:
        return
    db = SessionLocal()
    try:
        db.query(Message).filter(
            Message.id.in_(ids),
            or_(Message.sender_id == user_id, Message.receiver_id == user_id),
        ).update({"deleted_by": user_id})
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Recall (within 5 min)
# ═════════════════════════════════════════════════════════════════════

def recall_message(user_id: str, user_type: str, param: RecallParam) -> None:
    db = SessionLocal()
    try:
        msg = db.query(Message).filter(Message.id == param.message_id).first()
        if not msg:
            raise BusinessException("消息不存在", 400)
        if msg.sender_id != user_id or msg.sender_type != user_type:
            raise BusinessException("只能撤回自己的消息", 403)
        if msg.created_at and (datetime.now() - msg.created_at).total_seconds() > 300:
            raise BusinessException("超过5分钟，无法撤回", 400)

        now = datetime.now()
        msg.content = "消息已被撤回"
        msg.msg_type = MsgTypeSystem
        msg.updated_at = now
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Forward
# ═════════════════════════════════════════════════════════════════════

async def forward_message(user_id: str, user_type: str, param: ForwardParam) -> None:
    db = SessionLocal()
    try:
        original = db.query(Message).filter(Message.id == param.message_id).first()
        if not original:
            raise BusinessException("消息不存在", 400)
    finally:
        db.close()

    send_param = MessageSendParam(
        content=original.content,
        msg_type=original.msg_type,
        extra=original.extra,
        receiver_ids=param.target_ids,
        receiver_type=param.target_type,
    )
    await send_message(send_param, user_id, user_type)


# ═════════════════════════════════════════════════════════════════════
# Search
# ═════════════════════════════════════════════════════════════════════

def search_messages(user_id: str, param: SearchParam) -> tuple[list[MessageVO], bool]:
    if param.size < 1:
        param.size = 20
    if param.size > 100:
        param.size = 100
    db = SessionLocal()
    try:
        q = db.query(Message).filter(
            or_(Message.sender_id == user_id, Message.receiver_id == user_id),
            Message.content.like(f"%{param.keyword}%"),
        )
        if param.cursor:
            try:
                t = datetime.strptime(param.cursor, "%Y-%m-%d %H:%M:%S")
                q = q.filter(Message.created_at < t)
            except ValueError:
                pass
        records = q.order_by(Message.created_at.desc()).limit(param.size + 1).all()
        has_more = len(records) > param.size
        if has_more:
            records = records[:param.size]
        return _to_vo_list(records), has_more
    finally:
        db.close()
