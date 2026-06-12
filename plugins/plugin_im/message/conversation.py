"""Conversation handling — single-chat + group unified list.

Mirrors hei-gin plugins/plugin-im/message/conversation.go.
"""

from __future__ import annotations

from datetime import datetime

from core.db import SessionLocal
from core.exception import BusinessException
from core.enums import LoginTypeEnum
from plugins.plugin_im.model.message import (
    generate_conversation_id,
)
from plugins.plugin_im.message.params import (
    ConversationVO, ConversationMessageVO, GetOrCreateConversationParam,
)
from plugins.plugin_im.group import my_group_conversations
from plugins.plugin_im.message.repository import MessageRepository
from plugins.plugin_im.message.user_repository import IMUserRepository

import logging
logger = logging.getLogger(__name__)


def _fmt_dt(dt) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


# ═════════════════════════════════════════════════════════════════════
# Conversations (unified: single-chat + group)
# ═════════════════════════════════════════════════════════════════════

def conversations(current_user_id: str, user_type: str, cursor: str = "", size: int = 20) -> tuple[list[ConversationVO], bool]:
    if size < 1:
        size = 20
    if size > 100:
        size = 100

    # Build single-chat conversations from im_message
    single_result = _build_from_messages(current_user_id, user_type)

    # Add group conversations
    group_convs = my_group_conversations(current_user_id, user_type)
    for gv in group_convs:
        key = "group:" + gv.group_id
        single_result[key] = ConversationVO(
            conversation_id=key,
            conversation_type="group",
            group_id=gv.group_id,
            group_name=gv.group_name,
            group_avatar=gv.group_avatar,
            member_count=gv.member_count,
            last_content=gv.last_content,
            last_time=gv.last_time,
            unread_count=gv.unread_count,
        )

    # Sort by last_time descending
    result = sorted(single_result.values(), key=lambda x: x.last_time, reverse=True)

    # Apply cursor pagination
    has_more = False
    if cursor:
        result = [vo for vo in result if vo.last_time > cursor]
    if len(result) > size:
        result = result[:size]
        has_more = True

    return result, has_more


def _build_from_messages(current_user_id: str, user_type: str) -> dict[str, ConversationVO]:
    db = SessionLocal()
    try:
        repository = MessageRepository(db)
        user_repository = IMUserRepository(db)
        rows = repository.list_latest_message_rows(current_user_id)
        if not rows:
            return {}

        conv_ids = [r.conversation_id for r in rows]
        unread_map = repository.unread_counts_by_conversation(conv_ids, current_user_id)
        business_ids = []
        consumer_ids = []
        result_map = {}

        for r in rows:
            if r.sender_id == current_user_id and r.sender_type == user_type:
                other_id, other_type = r.receiver_id, r.receiver_type
            else:
                other_id, other_type = r.sender_id, r.sender_type

            if other_type == "BUSINESS":
                business_ids.append(other_id)
            else:
                consumer_ids.append(other_id)

            result_map[r.conversation_id] = ConversationVO(
                conversation_id=r.conversation_id,
                conversation_type="single",
                other_user_id=other_id,
                other_user_type=other_type,
                last_content=r.content or "",
                last_time=_fmt_dt(r.created_at),
                unread_count=unread_map.get(r.conversation_id, 0),
            )

        nickname_map = {}
        avatar_map = {}
        if business_ids:
            users = user_repository.list_sys_users(business_ids)
            for u in users:
                nickname_map["BUSINESS:" + u.id] = u.nickname or u.username or ""
                avatar_map["BUSINESS:" + u.id] = u.avatar or ""
        if consumer_ids:
            users = user_repository.list_client_users(consumer_ids)
            for u in users:
                nickname_map["CONSUMER:" + u.id] = u.nickname or u.username or ""
                avatar_map["CONSUMER:" + u.id] = u.avatar or ""
        for vo in result_map.values():
            key = vo.other_user_type + ":" + vo.other_user_id
            vo.other_nickname = nickname_map.get(key, "")
            vo.other_avatar = avatar_map.get(key, "")

        return result_map
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Conversation Messages
# ═════════════════════════════════════════════════════════════════════

def conversation_messages(
    current_user_id: str, conversation_id: str, cursor: str = "", size: int = 20
) -> tuple[list[ConversationMessageVO], bool]:
    if size < 1:
        size = 20
    if size > 100:
        size = 100

    db = SessionLocal()
    try:
        repository = MessageRepository(db)
        cursor_dt = None
        if cursor:
            try:
                cursor_dt = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                pass
        records = repository.list_conversation_messages(current_user_id, conversation_id, cursor_dt, size)
        has_more = len(records) > size
        if has_more:
            records = records[:size]

        result = []
        for m in records:
            file_url = ""
            if m.msg_type in ("IMAGE", "FILE"):
                from plugins.plugin_im.message.im_file import resolve_file_url
                file_url = resolve_file_url(m.content or "", m.extra or "")
            result.append(ConversationMessageVO(
                id=m.id,
                sender_id=m.sender_id or "",
                sender_type=m.sender_type or "",
                content=m.content or "",
                msg_type=m.msg_type,
                extra=m.extra or "",
                status=m.status,
                file_url=file_url,
                created_at=_fmt_dt(m.created_at),
            ))
        return result, has_more
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Get or Create Conversation
# ═════════════════════════════════════════════════════════════════════

def get_or_create_conversation(user_id: str, user_type: str, param: GetOrCreateConversationParam) -> tuple[str, str]:
    if not param.user_id or not param.user_type:
        raise BusinessException("参数错误", 400)

    cid = generate_conversation_id(
        user_id, LoginTypeEnum(user_type),
        param.user_id, LoginTypeEnum(param.user_type),
    )

    display_name = param.user_id
    db = SessionLocal()
    try:
        user_repository = IMUserRepository(db)
        if param.user_type == "BUSINESS":
            u = user_repository.find_sys_user(param.user_id)
            if u:
                display_name = u.nickname or u.username or param.user_id
        else:
            u = user_repository.find_client_user(param.user_id)
            if u:
                display_name = u.nickname or u.username or param.user_id
    finally:
        db.close()

    return cid, display_name
