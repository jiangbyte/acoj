"""Message service — single-chat message CRUD.

Mirrors hei-gin plugins/plugin-im/message/service.go.
"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import or_, and_

from sdk.infra.db import SessionLocal
from sdk.web.exception import BusinessException
from sdk.web.result import page_data
from sdk.utils import generate_id
from sdk.enums import LoginTypeEnum as LTE
from plugins.plugin_im.model.message import (
    Message, Conversation, MsgTypeText, MsgTypeSystem,
    generate_conversation_id,
)
from plugins.plugin_im.message.params import (
    MessageVO, MessagePageParam, MessageSendParam,
    RecallParam, ForwardParam, SearchParam, ConversationMessageVO, MessageToMessageVO,
)
from plugins.plugin_im.message.repository import MessageRepository
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message as WSMessage

import logging
import asyncio

logger = logging.getLogger(__name__)
def _fmt_dt(dt) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


# ═════════════════════════════════════════════════════════════════════
# Send
# ═════════════════════════════════════════════════════════════════════

async def send_message(param: MessageSendParam, sender_id: str, sender_type: str) -> list[str]:
    db = SessionLocal()
    try:
        repository = MessageRepository(db)
        # Rate limit check
        if im_ws.GlobalCrossHub and not await im_ws.GlobalCrossHub.allow_message(sender_id, sender_type):
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

        repository.create_messages(records)

        conversations = [
            Conversation(
                id=rec.conversation_id,
                from_id=rec.sender_id,
                from_type=rec.sender_type,
                to_id=rec.receiver_id,
                to_type=rec.receiver_type,
                last_msg=rec.content,
                last_time=now,
                created_at=now,
                updated_at=now,
            )
            for rec in records
        ]
        repository.upsert_conversations(conversations)
        repository.commit()

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
                if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(rid, ws_msg, rec.id))
            else:
                if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(rid, ws_msg, rec.id))

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
        repository = MessageRepository(db)
        records, total = repository.page_messages(user_id, param)
        return page_data([MessageToMessageVO(r) for r in records], total, param.current, param.size)
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Unread Count
# ═════════════════════════════════════════════════════════════════════

def unread_count(user_id: str) -> int:
    db = SessionLocal()
    try:
        return MessageRepository(db).count_unread(user_id)
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Detail
# ═════════════════════════════════════════════════════════════════════

def detail_message(message_id: str) -> Optional[MessageVO]:
    db = SessionLocal()
    try:
        entity = MessageRepository(db).find_by_id(message_id)
        return MessageToMessageVO(entity) if entity else None
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Mark Read
# ═════════════════════════════════════════════════════════════════════

def mark_read(message_id: str) -> None:
    db = SessionLocal()
    try:
        MessageRepository(db).mark_read(message_id)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def mark_conversation_read(receiver_id: str, conversation_id: str) -> None:
    db = SessionLocal()
    try:
        MessageRepository(db).mark_conversation_read(receiver_id, conversation_id)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def mark_all_read(receiver_id: str) -> None:
    db = SessionLocal()
    try:
        MessageRepository(db).mark_all_read(receiver_id)
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
        MessageRepository(db).soft_delete_messages(user_id, ids)
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
        msg = MessageRepository(db).find_by_id(param.message_id)
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
        original = MessageRepository(db).find_by_id(param.message_id)
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
        cursor_dt = None
        if param.cursor:
            try:
                cursor_dt = datetime.strptime(param.cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                pass
        records = MessageRepository(db).search_messages(user_id, param.keyword, cursor_dt, param.size)
        has_more = len(records) > param.size
        if has_more:
            records = records[:param.size]
        return [MessageToMessageVO(r) for r in records], has_more
    finally:
        db.close()
