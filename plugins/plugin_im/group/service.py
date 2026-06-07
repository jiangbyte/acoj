"""Group chat service — mirrors hei-gin plugins/plugin-im/group/service.go."""

from __future__ import annotations

import json
from datetime import datetime, timedelta
from typing import Optional

from sqlalchemy import or_, and_, func

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
)
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message as WSMessage

import logging
logger = logging.getLogger(__name__)


def _fmt_dt(dt) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def _resolve_user_name(user_id: str, user_type: str, db) -> str:
    if user_type == UserTypeBusiness:
        from plugins.plugin_sys.user.models import SysUser
        u = db.query(SysUser).filter(SysUser.id == user_id).first()
        return u.nickname or u.username or user_id if u else user_id
    else:
        from plugins.plugin_client.user.models import ClientUser
        u = db.query(ClientUser).filter(ClientUser.id == user_id).first()
        return u.nickname or u.username or user_id if u else user_id


# ── Helpers ────────────────────────────────────────────────────────────

def _check_owner_or_admin(db, group_id: str, operator_id: str, operator_type: str) -> Optional[GroupMember]:
    member = db.query(GroupMember).filter(
        GroupMember.group_id == group_id,
        GroupMember.user_id == operator_id,
        GroupMember.user_type == operator_type,
        GroupMember.status == MemberActive,
    ).first()
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
        db.add(group)
        db.flush()

        owner_member = GroupMember(
            id=generate_id(), group_id=group.id,
            user_id=owner_id, user_type=owner_type,
            role=RoleOwner, joined_at=now, status=MemberActive,
        )
        db.add(owner_member)

        if p.member_ids:
            _validate_member_type(group_type, p.member_type)
            existing = db.query(GroupMember).filter(
                GroupMember.group_id == group.id,
                GroupMember.user_id.in_(p.member_ids),
                GroupMember.user_type == p.member_type,
                GroupMember.status == MemberActive,
            ).count()
            if existing > 0:
                raise BusinessException("部分成员已在群中", 400)

            current_count = db.query(GroupMember).filter(
                GroupMember.group_id == group.id,
                GroupMember.status == MemberActive,
            ).count()
            if current_count + len(p.member_ids) > group.max_members:
                raise BusinessException(f"群成员数量不能超过{group.max_members}人", 400)

            for uid in p.member_ids:
                if uid == owner_id:
                    continue
                db.add(GroupMember(
                    id=generate_id(), group_id=group.id,
                    user_id=uid, user_type=p.member_type,
                    role=RoleMember, joined_at=now, status=MemberActive,
                ))
                extra_sys = MsgExtraSystem(action="join", user_id=uid, user_type=p.member_type)
                db.add(GroupMessage(
                    id=generate_id(), group_id=group.id,
                    sender_id=owner_id, sender_type=owner_type,
                    content="欢迎加入群聊",
                    extra=json.dumps(extra_sys.__dict__, ensure_ascii=False),
                    msg_type=MsgTypeSystem, created_at=now,
                ))

        db.commit()
        return GroupVO(id=group.id, name=group.name, avatar=group.avatar or "",
                       owner_id=group.owner_id, owner_type=group.owner_type,
                       group_type=group.group_type, member_count=1)
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
        if not p.group_id:
            raise BusinessException("参数错误", 400)
        member = _check_owner_or_admin(db, p.group_id, operator_id, operator_type)

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
            db.query(Group).filter(Group.id == p.group_id).update(updates)
            db.commit()
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
        if not group_id or not operator_id:
            raise BusinessException("参数错误", 400)
        group = db.query(Group).filter(Group.id == group_id).first()
        if not group:
            raise BusinessException("群不存在", 400)
        if group.owner_id != operator_id:
            raise BusinessException("仅群主可解散群", 403)

        now = datetime.now()
        group.status = GroupDissolved
        group.updated_at = now
        db.query(GroupMember).filter(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
        ).update({"status": MemberLeft})
        db.commit()
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
        group = db.query(Group).filter(Group.id == group_id).first()
        if not group:
            return None
        count = db.query(GroupMember).filter(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
        ).count()
        return GroupVO(
            id=group.id, name=group.name, avatar=group.avatar or "",
            owner_id=group.owner_id, owner_type=group.owner_type,
            group_type=group.group_type, notice=group.notice or "",
            member_count=count,
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
        members = db.query(GroupMember).filter(
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
            GroupMember.status == MemberActive,
        ).all()
        if not members:
            return []

        group_ids = [m.group_id for m in members]
        groups = db.query(Group).filter(
            Group.id.in_(group_ids),
            Group.status == GroupNormal,
        ).all()

        # Batch count active members per group -- avoid N+1
        counts = db.query(
            GroupMember.group_id, func.count(GroupMember.id)
        ).filter(
            GroupMember.group_id.in_(group_ids),
            GroupMember.status == MemberActive,
        ).group_by(GroupMember.group_id).all()
        count_map = {c[0]: c[1] for c in counts}

        result = []
        for g in groups:
            result.append(GroupVO(
                id=g.id, name=g.name, avatar=g.avatar or "",
                owner_id=g.owner_id, owner_type=g.owner_type,
                group_type=g.group_type, notice=g.notice or "",
                member_count=count_map.get(g.id, 0),
            ))
        return result
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
        _check_owner_or_admin(db, p.group_id, operator_id, operator_type)
        group = db.query(Group).filter(Group.id == p.group_id).first()
        if not group:
            raise BusinessException("群不存在", 400)
        _validate_member_type(group.group_type, p.user_type)

        existing = db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id.in_(p.user_ids),
            GroupMember.user_type == p.user_type,
            GroupMember.status == MemberActive,
        ).count()
        if existing > 0:
            raise BusinessException("部分成员已在群中", 400)

        current_count = db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.status == MemberActive,
        ).count()
        if current_count + len(p.user_ids) > group.max_members:
            raise BusinessException(f"群成员数量不能超过{group.max_members}人", 400)

        now = datetime.now()
        for uid in p.user_ids:
            db.add(GroupMember(
                id=generate_id(), group_id=p.group_id,
                user_id=uid, user_type=p.user_type,
                role=RoleMember, joined_at=now, status=MemberActive,
            ))

        db.commit()
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
        group = db.query(Group).filter(Group.id == group_id).first()
        if not group:
            raise BusinessException("群不存在", 400)

        if group.is_public:
            now = datetime.now()
            db.add(GroupMember(
                id=generate_id(), group_id=group_id,
                user_id=user_id, user_type=user_type,
                role=RoleMember, joined_at=now, status=MemberActive,
            ))
            db.commit()
        else:
            existing_req = db.query(GroupJoinRequest).filter(
                GroupJoinRequest.group_id == group_id,
                GroupJoinRequest.user_id == user_id,
                GroupJoinRequest.user_type == user_type,
                GroupJoinRequest.status == "pending",
            ).count()
            if existing_req > 0:
                raise BusinessException("已发送过加群请求", 400)
            db.add(GroupJoinRequest(
                id=generate_id(), group_id=group_id,
                user_id=user_id, user_type=user_type,
                status="pending", created_at=datetime.now(),
            ))
            db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def pending_join_requests(group_id: str) -> list:
    db = SessionLocal()
    try:
        reqs = db.query(GroupJoinRequest).filter(
            GroupJoinRequest.group_id == group_id,
            GroupJoinRequest.status == "pending",
        ).all()
        return [{"id": r.id, "group_id": r.group_id, "user_id": r.user_id,
                  "user_type": r.user_type, "remark": r.remark or "",
                  "created_at": _fmt_dt(r.created_at)} for r in reqs]
    finally:
        db.close()


def handle_join_request(operator_id: str, operator_type: str, p: HandleJoinRequestParam) -> None:
    db = SessionLocal()
    try:
        _check_owner_or_admin(db, "", operator_id, operator_type)
        req = db.query(GroupJoinRequest).filter(GroupJoinRequest.id == p.request_id).first()
        if not req or req.status != "pending":
            raise BusinessException("请求不存在或已处理", 400)

        req.status = p.action
        req.updated_at = datetime.now()

        if p.action == "approved":
            now = datetime.now()
            db.add(GroupMember(
                id=generate_id(), group_id=req.group_id,
                user_id=req.user_id, user_type=req.user_type,
                role=RoleMember, joined_at=now, status=MemberActive,
            ))
        db.commit()
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
        db.query(GroupMember).filter(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
        ).update({"status": MemberLeft})
        db.commit()
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
        _check_owner_or_admin(db, p.group_id, operator_id, operator_type)
        db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == p.user_id,
            GroupMember.user_type == p.user_type,
        ).update({"status": MemberKicked})
        db.commit()
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
        # Verify operator is owner
        group = db.query(Group).filter(Group.id == p.group_id).first()
        if not group or group.owner_id != operator_id:
            raise BusinessException("仅群主可设置角色", 403)

        now = datetime.now()
        if p.role == RoleOwner:
            # Transfer owner: demote old owner to admin, promote new
            db.query(GroupMember).filter(
                GroupMember.group_id == p.group_id,
                GroupMember.user_id == operator_id,
            ).update({"role": RoleAdmin})
            db.query(GroupMember).filter(
                GroupMember.group_id == p.group_id,
                GroupMember.user_id == p.user_id,
                GroupMember.user_type == p.user_type,
            ).update({"role": RoleOwner})
            db.query(Group).filter(Group.id == p.group_id).update(
                {"owner_id": p.user_id, "owner_type": p.user_type, "updated_at": now}
            )
        else:
            db.query(GroupMember).filter(
                GroupMember.group_id == p.group_id,
                GroupMember.user_id == p.user_id,
                GroupMember.user_type == p.user_type,
            ).update({"role": p.role})
        db.commit()
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
        group = db.query(Group).filter(Group.id == p.group_id).first()
        if not group or group.owner_id != operator_id:
            raise BusinessException("仅群主可转让群", 403)

        new_owner = db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == p.new_owner_id,
            GroupMember.user_type == p.new_owner_type,
            GroupMember.status == MemberActive,
        ).first()
        if not new_owner:
            raise BusinessException("新群主不在群中", 400)

        now = datetime.now()
        db.query(Group).filter(Group.id == p.group_id).update(
            {"owner_id": p.new_owner_id, "owner_type": p.new_owner_type, "updated_at": now}
        )
        db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == operator_id,
        ).update({"role": RoleAdmin})
        db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == p.new_owner_id,
            GroupMember.user_type == p.new_owner_type,
        ).update({"role": RoleOwner})
        db.commit()
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
        _check_owner_or_admin(db, p.group_id, operator_id, operator_type)
        db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == p.user_id,
            GroupMember.user_type == p.user_type,
        ).update({"nickname": p.nickname})
        db.commit()
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
        records = db.query(GroupMember).filter(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
        ).all()

        # Batch resolve user names -- avoid N+1
        business_ids = [m.user_id for m in records if m.user_type == UserTypeBusiness]
        consumer_ids = [m.user_id for m in records if m.user_type == UserTypeConsumer]
        name_map: dict[str, str] = {}

        if business_ids:
            from plugins.plugin_sys.user.models import SysUser
            users = db.query(SysUser).filter(SysUser.id.in_(business_ids)).all()
            for u in users:
                name_map[f"BUSINESS:{u.id}"] = u.nickname or u.username or u.id

        if consumer_ids:
            from plugins.plugin_client.user.models import ClientUser
            users = db.query(ClientUser).filter(ClientUser.id.in_(consumer_ids)).all()
            for u in users:
                name_map[f"CONSUMER:{u.id}"] = u.nickname or u.username or u.id

        result = []
        for m in records:
            nick = m.nickname or name_map.get(f"{m.user_type}:{m.user_id}", m.user_id)
            result.append(MemberVO(
                user_id=m.user_id, user_type=m.user_type,
                role=m.role, nickname=nick,
                joined_at=_fmt_dt(m.joined_at), is_muted=m.muted_until is not None and m.muted_until > datetime.now(),
            ))
        return result
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
        q = db.query(GroupMessage).filter(GroupMessage.group_id == group_id)
        if cursor:
            try:
                t = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
                q = q.filter(GroupMessage.created_at < t)
            except ValueError:
                pass
        records = q.order_by(GroupMessage.created_at.desc()).limit(size + 1).all()
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        vos = [_msg_to_vo(m) for m in records]
        return vos, has_more
    finally:
        db.close()


def search_messages(group_id: str, keyword: str, cursor: str = "", size: int = 20) -> tuple[list[MessageVO], bool]:
    if size < 1:
        size = 20
    if size > 100:
        size = 100
    db = SessionLocal()
    try:
        q = db.query(GroupMessage).filter(
            GroupMessage.group_id == group_id,
            GroupMessage.content.like(f"%{keyword}%"),
        )
        if cursor:
            try:
                t = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
                q = q.filter(GroupMessage.created_at < t)
            except ValueError:
                pass
        records = q.order_by(GroupMessage.created_at.desc()).limit(size + 1).all()
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        return [_msg_to_vo(m) for m in records], has_more
    finally:
        db.close()


def _msg_to_vo(m: GroupMessage) -> MessageVO:
    return MessageVO(
        id=m.id, sender_id=m.sender_id, sender_type=m.sender_type,
        content=m.content or "", extra=m.extra or "",
        msg_type=m.msg_type, reply_to=m.reply_to or "",
        created_at=_fmt_dt(m.created_at),
    )


# ═════════════════════════════════════════════════════════════════════
# Send Message
# ═════════════════════════════════════════════════════════════════════

def send_message(sender_id: str, sender_type: str, p: SendMessageParam) -> MessageVO:
    db = SessionLocal()
    try:
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
        db.add(msg)
        db.commit()

        vo = _msg_to_vo(msg)

        # WS push to all group members
        import asyncio
        payload = {"message_id": msg.id, "group_id": p.group_id,
                    "sender_id": sender_id, "sender_type": sender_type,
                    "content": p.content, "msg_type": msg_type,
                    "extra": p.extra, "created_at": _fmt_dt(now)}
        ws_msg = WSMessage(type="group_message", payload=payload)

        members_list = db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.status == MemberActive,
            or_(GroupMember.user_id != sender_id, GroupMember.user_type != sender_type),
        ).all()
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
        msg = db.query(GroupMessage).filter(
            GroupMessage.id == message_id,
            GroupMessage.group_id == group_id,
        ).first()
        if not msg:
            raise BusinessException("消息不存在", 400)
        if msg.sender_id != user_id or msg.sender_type != user_type:
            raise BusinessException("只能撤回自己的消息", 403)
        if msg.created_at and (datetime.now() - msg.created_at) > timedelta(minutes=5):
            raise BusinessException("超过5分钟，无法撤回", 400)

        msg.content = "消息已被撤回"
        msg.msg_type = MsgTypeSystem
        db.commit()
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
        now = datetime.now()
        existing = db.query(GroupMessageRead).filter(
            GroupMessageRead.group_id == group_id,
            GroupMessageRead.user_id == user_id,
            GroupMessageRead.user_type == user_type,
        ).first()
        if existing:
            existing.read_at = now
        else:
            db.add(GroupMessageRead(
                id=generate_id(), group_id=group_id,
                user_id=user_id, user_type=user_type, read_at=now,
            ))
        db.commit()
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
        _check_owner_or_admin(db, p.group_id, operator_id, operator_type)
        now = datetime.now()
        db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == p.user_id,
            GroupMember.user_type == p.user_type,
        ).update({"muted_until": now + duration})
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def unmute_member(operator_id: str, operator_type: str, p: KickParam) -> None:
    db = SessionLocal()
    try:
        _check_owner_or_admin(db, p.group_id, operator_id, operator_type)
        db.query(GroupMember).filter(
            GroupMember.group_id == p.group_id,
            GroupMember.user_id == p.user_id,
            GroupMember.user_type == p.user_type,
        ).update({"muted_until": None})
        db.commit()
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
        groups = db.query(Group).filter(
            Group.name.like(f"%{keyword}%"),
            Group.status == GroupNormal,
        ).limit(limit).all()

        # Batch count members -- avoid N+1
        group_ids = [g.id for g in groups]
        counts = db.query(
            GroupMember.group_id, func.count(GroupMember.id)
        ).filter(
            GroupMember.group_id.in_(group_ids),
            GroupMember.status == MemberActive,
        ).group_by(GroupMember.group_id).all()
        count_map = {c[0]: c[1] for c in counts}

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
        members = db.query(GroupMember.group_id).filter(
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
            GroupMember.status == MemberActive,
        ).all()
        if not members:
            return []
        group_ids = [m.group_id for m in members]

        groups = db.query(Group).filter(
            Group.id.in_(group_ids),
            Group.status == GroupNormal,
        ).all()

        # Member counts
        counts = db.query(
            GroupMember.group_id, func.count(GroupMember.id)
        ).filter(
            GroupMember.group_id.in_(group_ids),
            GroupMember.status == MemberActive,
        ).group_by(GroupMember.group_id).all()
        count_map = {c[0]: c[1] for c in counts}

        # Last message per group
        last_sub = db.query(
            GroupMessage.group_id,
            func.max(GroupMessage.created_at).label("max_ct")
        ).filter(
            GroupMessage.group_id.in_(group_ids)
        ).group_by(GroupMessage.group_id).subquery()

        last_msgs = db.query(
            GroupMessage.group_id, GroupMessage.content, GroupMessage.created_at
        ).join(
            last_sub,
            and_(
                last_sub.c.group_id == GroupMessage.group_id,
                last_sub.c.max_ct == GroupMessage.created_at,
            )
        ).all()
        last_map = {m.group_id: m for m in last_msgs}

        # Unread counts
        read_sub = db.query(
            GroupMessageRead.group_id,
            func.max(GroupMessageRead.read_at).label("max_read")
        ).filter(
            GroupMessageRead.user_id == user_id,
            GroupMessageRead.user_type == user_type,
        ).group_by(GroupMessageRead.group_id).subquery()

        unreads = db.query(
            GroupMessage.group_id,
            func.count(GroupMessage.id).label("count")
        ).outerjoin(
            read_sub, read_sub.c.group_id == GroupMessage.group_id
        ).filter(
            GroupMessage.group_id.in_(group_ids),
            or_(
                read_sub.c.max_read.is_(None),
                GroupMessage.created_at > read_sub.c.max_read,
            )
        ).group_by(GroupMessage.group_id).all()
        unread_map = {u.group_id: u.count for u in unreads}

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
