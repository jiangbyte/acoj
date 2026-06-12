"""Group chat service — thin orchestration over GroupRepository."""

from __future__ import annotations

import asyncio
import json
from datetime import datetime, timedelta

from core.db import SessionLocal
from core.exception import BusinessException
from core.utils import generate_id
from plugins.plugin_im.model.group import (
    Group, GroupMember, GroupJoinRequest, GroupMessage, GroupMessageRead,
)
from plugins.plugin_im.model.message import MsgTypeSystem, MsgExtraSystem
from plugins.plugin_im.group.constants import *
from plugins.plugin_im.group.params import (
    CreateParam, UpdateParam, InviteParam, KickParam, SetRoleParam,
    SendMessageParam, GroupVO, MemberVO, MessageVO,
    HandleJoinRequestParam, TransferOwnerParam, SetNicknameParam, ConversationVO,
    GroupMessageToMessageVO,
)
from plugins.plugin_im.group.repository import GroupRepository
from plugins.plugin_im.message.user_repository import IMUserRepository
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message as WSMessage

import logging
logger = logging.getLogger(__name__)


def _fmt_dt(dt) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def _check_owner_or_admin(repository: GroupRepository, group_id: str, operator_id: str, operator_type: str) -> GroupMember:
    member = repository.find_active_member(group_id, operator_id, operator_type)
    if not member or member.role not in (RoleOwner, RoleAdmin):
        raise BusinessException("无权限", 403)
    return member


def _validate_member_type(group_type: str, member_type: str) -> None:
    if group_type == GroupTypeConsumerOnly and member_type != UserTypeConsumer:
        raise BusinessException("C端群只能邀请C端成员", 400)


# ═════════════════════════════════════════════════════════════════════
# Create
# ═════════════════════════════════════════════════════════════════════

def create(owner_id: str, owner_type: str, p: CreateParam) -> GroupVO:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        if not p.name:
            raise BusinessException("群名称不能为空", 400)
        if len(p.name) > 100:
            raise BusinessException("群名称不能超过100个字符", 400)
        if not owner_id:
            raise BusinessException("用户未登录", 401)

        group_type = GroupTypeMixed
        if owner_type == UserTypeConsumer:
            group_type = GroupTypeConsumerOnly

        now = datetime.now()
        group = Group(
            id=generate_id(),
            name=p.name,
            avatar=p.avatar,
            owner_id=owner_id,
            owner_type=owner_type,
            group_type=group_type,
            max_members=200,
            status=GroupNormal,
            created_at=now,
            updated_at=now,
        )
        repository.add(group)
        repository.flush()

        owner_member = GroupMember(
            id=generate_id(), group_id=group.id,
            user_id=owner_id, user_type=owner_type,
            role=RoleOwner, joined_at=now, status=MemberActive,
        )
        repository.add(owner_member)

        if p.member_ids:
            _validate_member_type(group_type, p.member_type)
            existing = repository.count_existing_active_members(group.id, p.member_ids, p.member_type)
            if existing > 0:
                raise BusinessException("部分成员已在群中", 400)
            current_count = repository.count_active_members(group.id)
            if current_count + len(p.member_ids) > group.max_members:
                raise BusinessException(f"群成员数量不能超过{group.max_members}人", 400)
            members_to_add: list[GroupMember] = []
            messages_to_add: list[GroupMessage] = []
            for uid in p.member_ids:
                if uid == owner_id:
                    continue
                members_to_add.append(GroupMember(
                    id=generate_id(), group_id=group.id,
                    user_id=uid, user_type=p.member_type,
                    role=RoleMember, joined_at=now, status=MemberActive,
                ))
                extra_sys = MsgExtraSystem(action="join", user_id=uid, user_type=p.member_type)
                messages_to_add.append(GroupMessage(
                    id=generate_id(), group_id=group.id,
                    sender_id=owner_id, sender_type=owner_type,
                    content="欢迎加入群聊",
                    extra=json.dumps(extra_sys.__dict__, ensure_ascii=False),
                    msg_type=MsgTypeSystem, created_at=now,
                ))
            repository.add_members(members_to_add)
            repository.add_messages(messages_to_add)
        repository.commit()
        return GroupVO(id=group.id, name=group.name, avatar=group.avatar or "",
                       owner_id=group.owner_id, owner_type=group.owner_type,
                       group_type=group.group_type, member_count=repository.count_active_members(group.id))
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Update
# ═════════════════════════════════════════════════════════════════════

def update_group(operator_id: str, operator_type: str, p: UpdateParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        if not p.group_id:
            raise BusinessException("参数错误", 400)
        member = _check_owner_or_admin(repository, p.group_id, operator_id, operator_type)

        updates = {}
        if p.name is not None:
            if len(p.name) > 100:
                raise BusinessException("群名称不能超过100个字符", 400)
            updates["name"] = p.name
        if p.avatar is not None:
            updates["avatar"] = p.avatar
        if p.notice is not None and member.role == RoleOwner:
            updates["notice"] = p.notice
        if updates:
            repository.update_group(p.group_id, updates)
            repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Dissolve
# ═════════════════════════════════════════════════════════════════════

def dissolve(operator_id: str, group_id: str) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        if not group_id or not operator_id:
            raise BusinessException("参数错误", 400)
        group = repository.find_group(group_id)
        if not group:
            raise BusinessException("群不存在", 400)
        if group.owner_id != operator_id:
            raise BusinessException("仅群主可解散群", 403)
        repository.dissolve_group(group_id, datetime.now(), MemberLeft, GroupDissolved)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Detail
# ═════════════════════════════════════════════════════════════════════

def detail(group_id: str) -> Optional[GroupVO]:
    if not group_id:
        return None
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        group = repository.find_group(group_id)
        if not group:
            return None
        return GroupVO(
            id=group.id, name=group.name, avatar=group.avatar or "",
            owner_id=group.owner_id, owner_type=group.owner_type,
            group_type=group.group_type, notice=group.notice or "",
            member_count=repository.count_active_members(group_id),
        )
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# My Groups
# ═════════════════════════════════════════════════════════════════════

def my_groups(user_id: str, user_type: str) -> list[GroupVO]:
    if not user_id:
        return []
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        group_ids = repository.list_member_group_ids(user_id, user_type)
        groups = repository.list_groups_by_ids(group_ids)
        count_map = repository.active_member_counts(group_ids)
        return [
            GroupVO(
                id=g.id, name=g.name, avatar=g.avatar or "",
                owner_id=g.owner_id, owner_type=g.owner_type,
                group_type=g.group_type, notice=g.notice or "",
                member_count=count_map.get(g.id, 0),
            )
            for g in groups
        ]
    finally:
        db.close()

# ═════════════════════════════════════════════════════════════════════
# Invite
# ═════════════════════════════════════════════════════════════════════

def invite(operator_id: str, operator_type: str, p: InviteParam) -> None:
    if not p.user_ids:
        return
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        _check_owner_or_admin(repository, p.group_id, operator_id, operator_type)
        group = repository.find_group(p.group_id)
        if not group:
            raise BusinessException("群不存在", 400)
        _validate_member_type(group.group_type, p.user_type)
        existing = repository.count_existing_active_members(p.group_id, p.user_ids, p.user_type)
        if existing > 0:
            raise BusinessException("部分成员已在群中", 400)
        current_count = repository.count_active_members(p.group_id)
        if current_count + len(p.user_ids) > group.max_members:
            raise BusinessException(f"群成员数量不能超过{group.max_members}人", 400)
        now = datetime.now()
        repository.add_members([
            GroupMember(
                id=generate_id(), group_id=p.group_id,
                user_id=uid, user_type=p.user_type,
                role=RoleMember, joined_at=now, status=MemberActive,
            )
            for uid in p.user_ids
        ])
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Join / Handle Join Request
# ═════════════════════════════════════════════════════════════════════

def join_group(user_id: str, user_type: str, group_id: str) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        group = repository.find_group(group_id)
        if not group:
            raise BusinessException("群不存在", 400)

        if group.is_public:
            now = datetime.now()
            repository.add_member(GroupMember(
                id=generate_id(), group_id=group_id,
                user_id=user_id, user_type=user_type,
                role=RoleMember, joined_at=now, status=MemberActive,
            ))
            repository.commit()
        else:
            existing_req = repository.count_pending_join_request(group_id, user_id, user_type)
            if existing_req > 0:
                raise BusinessException("已发送过加群请求", 400)
            repository.create_join_request(GroupJoinRequest(
                id=generate_id(), group_id=group_id,
                user_id=user_id, user_type=user_type,
                status="pending", created_at=datetime.now(),
            ))
            repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def pending_join_requests(group_id: str) -> list:
    db = SessionLocal()
    try:
        reqs = GroupRepository(db).list_pending_join_requests(group_id)
        return [{"id": r.id, "group_id": r.group_id, "user_id": r.user_id,
                  "user_type": r.user_type, "remark": getattr(r, "remark", "") or "",
                  "created_at": _fmt_dt(r.created_at)} for r in reqs]
    finally:
        db.close()


def handle_join_request(operator_id: str, operator_type: str, p: HandleJoinRequestParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        req = repository.find_join_request(p.request_id)
        if not req or req.status != "pending":
            raise BusinessException("请求不存在或已处理", 400)
        _check_owner_or_admin(repository, req.group_id, operator_id, operator_type)

        req.status = p.action
        req.updated_at = datetime.now()

        if p.action == "approved":
            repository.add_member(GroupMember(
                id=generate_id(), group_id=req.group_id,
                user_id=req.user_id, user_type=req.user_type,
                role=RoleMember, joined_at=datetime.now(), status=MemberActive,
            ))
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Leave
# ═════════════════════════════════════════════════════════════════════

def leave_group(user_id: str, user_type: str, group_id: str) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        repository.update_member_status(group_id, user_id, user_type, MemberLeft)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Kick
# ═════════════════════════════════════════════════════════════════════

def kick(operator_id: str, operator_type: str, p: KickParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        _check_owner_or_admin(repository, p.group_id, operator_id, operator_type)
        repository.update_member_status(p.group_id, p.user_id, p.user_type, MemberKicked)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Set Role
# ═════════════════════════════════════════════════════════════════════

def set_role(operator_id: str, p: SetRoleParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        group = repository.find_group(p.group_id)
        if not group or group.owner_id != operator_id:
            raise BusinessException("仅群主可设置角色", 403)
        now = datetime.now()
        if p.role == RoleOwner:
            repository.update_member_role(p.group_id, operator_id, group.owner_type, RoleAdmin)
            repository.update_member_role(p.group_id, p.user_id, p.user_type, RoleOwner)
            repository.update_group(p.group_id, {"owner_id": p.user_id, "owner_type": p.user_type, "updated_at": now})
        else:
            repository.update_member_role(p.group_id, p.user_id, p.user_type, p.role)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Transfer Owner
# ═════════════════════════════════════════════════════════════════════

def transfer_owner(operator_id: str, p: TransferOwnerParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        group = repository.find_group(p.group_id)
        if not group or group.owner_id != operator_id:
            raise BusinessException("仅群主可转让群", 403)
        new_owner = repository.find_active_member(p.group_id, p.new_owner_id, p.new_owner_type)
        if not new_owner:
            raise BusinessException("新群主不在群中", 400)
        now = datetime.now()
        repository.update_group(p.group_id, {"owner_id": p.new_owner_id, "owner_type": p.new_owner_type, "updated_at": now})
        repository.update_member_role(p.group_id, operator_id, group.owner_type, RoleAdmin)
        repository.update_member_role(p.group_id, p.new_owner_id, p.new_owner_type, RoleOwner)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Set Nickname
# ═════════════════════════════════════════════════════════════════════

def set_member_nickname(operator_id: str, operator_type: str, p: SetNicknameParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        _check_owner_or_admin(repository, p.group_id, operator_id, operator_type)
        repository.update_member_nickname(p.group_id, p.user_id, p.user_type, p.nickname)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Members
# ═════════════════════════════════════════════════════════════════════

def members(group_id: str) -> list[MemberVO]:
    if not group_id:
        return []
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        user_repository = IMUserRepository(db)
        records = repository.list_members(group_id)
        business_ids = [m.user_id for m in records if m.user_type == UserTypeBusiness]
        consumer_ids = [m.user_id for m in records if m.user_type == UserTypeConsumer]
        name_map: dict[str, str] = {}
        if business_ids:
            users = user_repository.list_sys_users(business_ids)
            for u in users:
                name_map[f"BUSINESS:{u.id}"] = u.nickname or u.username or u.id
        if consumer_ids:
            users = user_repository.list_client_users(consumer_ids)
            for u in users:
                name_map[f"CONSUMER:{u.id}"] = u.nickname or u.username or u.id
        return [
            MemberVO(
                user_id=m.user_id, user_type=m.user_type,
                role=m.role, nickname=m.nickname or name_map.get(f"{m.user_type}:{m.user_id}", m.user_id),
                joined_at=_fmt_dt(m.joined_at), is_muted=m.muted_until is not None and m.muted_until > datetime.now(),
            )
            for m in records
        ]
    finally:
        db.close()

# ═════════════════════════════════════════════════════════════════════
# Messages
# ═════════════════════════════════════════════════════════════════════

def messages(group_id: str, cursor: str = "", size: int = 20) -> tuple[list[MessageVO], bool]:
    if size < 1:
        size = 20
    if size > 100:
        size = 100
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        cursor_dt = None
        if cursor:
            try:
                cursor_dt = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                pass
        records = repository.page_group_messages(group_id, "", cursor_dt, size)
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        return [GroupMessageToMessageVO(m) for m in records], has_more
    finally:
        db.close()


def search_messages(group_id: str, keyword: str, cursor: str = "", size: int = 20) -> tuple[list[MessageVO], bool]:
    if size < 1:
        size = 20
    if size > 100:
        size = 100
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        cursor_dt = None
        if cursor:
            try:
                cursor_dt = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                pass
        records = repository.page_group_messages(group_id, keyword, cursor_dt, size)
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        return [GroupMessageToMessageVO(m) for m in records], has_more
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Send Message
# ═════════════════════════════════════════════════════════════════════

def send_message(sender_id: str, sender_type: str, p: SendMessageParam) -> MessageVO:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        msg_type = p.msg_type or "TEXT"
        now = datetime.now()
        msg = GroupMessage(
            id=generate_id(),
            group_id=p.group_id,
            sender_id=sender_id,
            sender_type=sender_type,
            content=p.content,
            extra=p.extra,
            msg_type=msg_type,
            reply_to=p.reply_to or None,
            created_at=now,
        )
        repository.add(msg)
        repository.commit()
        vo = GroupMessageToMessageVO(msg)
        payload = {"message_id": msg.id, "group_id": p.group_id,
                    "sender_id": sender_id, "sender_type": sender_type,
                    "content": p.content, "msg_type": msg_type,
                    "extra": p.extra, "created_at": _fmt_dt(now)}
        ws_msg = WSMessage(type="group_message", payload=payload)
        members_list = repository.list_other_active_members(p.group_id, sender_id, sender_type)
        for m in members_list:
            if m.user_type == UserTypeConsumer:
                if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(m.user_id, ws_msg))
            else:
                if im_ws.GlobalCrossHub: asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(m.user_id, ws_msg))

        return vo
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Recall Message
# ═════════════════════════════════════════════════════════════════════

def recall_message(group_id: str, message_id: str, user_id: str, user_type: str) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        msg = repository.find_group_message(group_id, message_id)
        if not msg:
            raise BusinessException("消息不存在", 400)
        if msg.sender_id != user_id or msg.sender_type != user_type:
            raise BusinessException("只能撤回自己的消息", 403)
        if msg.created_at and (datetime.now() - msg.created_at) > timedelta(minutes=5):
            raise BusinessException("超过5分钟，无法撤回", 400)
        msg.content = "消息已被撤回"
        msg.msg_type = MsgTypeSystem
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Mark Read (conversation-level)
# ═════════════════════════════════════════════════════════════════════

def mark_read(group_id: str, user_id: str, user_type: str, message_id: str = "") -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        now = datetime.now()
        existing = repository.find_group_read(group_id, user_id, user_type)
        if existing:
            existing.read_at = now
        else:
            repository.add(GroupMessageRead(
                id=generate_id(), group_id=group_id,
                user_id=user_id, user_type=user_type, read_at=now,
            ))
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def mark_conversation_read(group_id: str, user_id: str, user_type: str) -> None:
    mark_read(group_id, user_id, user_type)


# ═════════════════════════════════════════════════════════════════════
# Mute / Unmute
# ═════════════════════════════════════════════════════════════════════

def mute_member(operator_id: str, operator_type: str, p: KickParam, duration: timedelta = timedelta(minutes=60)) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        _check_owner_or_admin(repository, p.group_id, operator_id, operator_type)
        repository.update_member_muted_until(p.group_id, p.user_id, p.user_type, datetime.now() + duration)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def unmute_member(operator_id: str, operator_type: str, p: KickParam) -> None:
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        _check_owner_or_admin(repository, p.group_id, operator_id, operator_type)
        repository.update_member_muted_until(p.group_id, p.user_id, p.user_type, None)
        repository.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Search Groups
# ═════════════════════════════════════════════════════════════════════

def search_groups(keyword: str, limit: int = 20) -> list[dict]:
    if not keyword:
        return []
    if limit > 50:
        limit = 50
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        groups = repository.search_groups(keyword, limit)
        group_ids = [g.id for g in groups]
        count_map = repository.active_member_counts(group_ids)
        return [{"id": g.id, "name": g.name, "avatar": g.avatar or "",
                  "member_count": count_map.get(g.id, 0)} for g in groups]
    finally:
        db.close()

# ═════════════════════════════════════════════════════════════════════
# My Group Conversations (for unified conversation list in message module)
# ═════════════════════════════════════════════════════════════════════

def my_group_conversations(user_id: str, user_type: str) -> list[ConversationVO]:
    if not user_id:
        return []
    db = SessionLocal()
    try:
        repository = GroupRepository(db)
        group_ids = repository.list_member_group_ids(user_id, user_type)
        groups = repository.list_groups_by_ids(group_ids)
        count_map = repository.active_member_counts(group_ids)
        last_map = repository.list_group_last_messages(group_ids)
        unread_map = repository.unread_group_counts(group_ids, user_id, user_type)
        result = []
        for g in groups:
            vo = ConversationVO(
                conversation_id=f"group:{g.id}",
                conversation_type="group",
                group_id=g.id, group_name=g.name,
                group_avatar=g.avatar or "",
                member_count=count_map.get(g.id, 0),
                unread_count=unread_map.get(g.id, 0),
            )
            if g.id in last_map:
                lm = last_map[g.id]
                vo.last_content = lm.content or ""
                vo.last_time = _fmt_dt(lm.created_at)
            result.append(vo)
        return result
    finally:
        db.close()
