"""
Friend management service — mirrors hei-gin plugins/plugin-im/friend/service.go 1:1.
All functions: send/accept/reject request, friend list, pending requests,
remove friend, search users, block/unblock/block-list, update remark.
"""

from __future__ import annotations

import asyncio
from datetime import datetime
from typing import Optional

from sqlalchemy import or_, and_

from core.db import SessionLocal
from core.exception import BusinessException
from core.utils import generate_id
from plugins.plugin_im.model.friend import FriendRequest, Friendship, FriendBlock
from plugins.plugin_im.friend.params import (
    SendRequestParam, HandleRequestParam, FriendVO, FriendRequestVO, BlockVO, SearchResult,
)
from plugins.plugin_im.ws import GlobalCrossHub, Message


def _fmt_dt(dt: Optional[datetime]) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


# ═════════════════════════════════════════════════════════════════════
# Send Friend Request
# ═════════════════════════════════════════════════════════════════════

def send_request(sender_id: str, sender_type: str, p: SendRequestParam) -> None:
    db = SessionLocal()
    try:
        if not sender_id or not p.receiver_id or not p.receiver_type:
            raise BusinessException("参数错误", 400)
        if sender_id == p.receiver_id and sender_type == p.receiver_type:
            raise BusinessException("不能添加自己为好友", 400)

        count = db.query(Friendship).filter(
            or_(
                and_(Friendship.user_id == sender_id, Friendship.user_type == sender_type,
                     Friendship.friend_id == p.receiver_id, Friendship.friend_type == p.receiver_type),
                and_(Friendship.user_id == p.receiver_id, Friendship.user_type == p.receiver_type,
                     Friendship.friend_id == sender_id, Friendship.friend_type == sender_type),
            )
        ).count()
        if count > 0:
            raise BusinessException("已经是好友了", 400)

        existing = db.query(FriendRequest).filter(
            FriendRequest.sender_id == sender_id,
            FriendRequest.sender_type == sender_type,
            FriendRequest.receiver_id == p.receiver_id,
            FriendRequest.receiver_type == p.receiver_type,
            FriendRequest.status == "pending",
        ).count()
        if existing > 0:
            raise BusinessException("已发送过好友请求，请等待回复", 400)

        now = datetime.now()
        req = FriendRequest(
            id=generate_id(),
            sender_id=sender_id,
            sender_type=sender_type,
            receiver_id=p.receiver_id,
            receiver_type=p.receiver_type,
            remark=p.remark,
            status="pending",
            created_at=now,
            updated_at=now,
        )
        db.add(req)
        db.commit()

        payload = {"request_id": req.id, "sender_id": sender_id,
                    "sender_type": sender_type, "remark": p.remark, "action": "friend_request"}
        msg = Message(type="friend_request", payload=payload)
        if GlobalCrossHub is not None:
            if p.receiver_type == "CONSUMER":
                asyncio.ensure_future(GlobalCrossHub.send_to_consumer(p.receiver_id, msg))
            else:
                asyncio.ensure_future(GlobalCrossHub.send_to_user(p.receiver_id, msg))
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Accept Friend Request
# ═════════════════════════════════════════════════════════════════════

def accept_request(user_id: str, user_type: str, p: HandleRequestParam) -> None:
    db = SessionLocal()
    try:
        if not user_id or not p.request_id:
            raise BusinessException("参数错误", 400)

        req = db.query(FriendRequest).filter(
            FriendRequest.id == p.request_id,
            FriendRequest.receiver_id == user_id,
            FriendRequest.receiver_type == user_type,
            FriendRequest.status == "pending",
        ).first()
        if not req:
            raise BusinessException("好友请求不存在或已处理", 400)

        now = datetime.now()
        req.status = "accepted"
        req.updated_at = now

        pair1 = Friendship(
            id=generate_id(), user_id=req.receiver_id, user_type=req.receiver_type,
            friend_id=req.sender_id, friend_type=req.sender_type, created_at=now,
        )
        pair2 = Friendship(
            id=generate_id(), user_id=req.sender_id, user_type=req.sender_type,
            friend_id=req.receiver_id, friend_type=req.receiver_type, created_at=now,
        )
        db.add(pair1)
        db.add(pair2)
        db.commit()

        payload = {
            "request_id": req.id, "receiver_id": user_id,
            "receiver_type": user_type, "action": "friend_request_accepted",
        }
        msg = Message(type="friend_request", payload=payload)
        if GlobalCrossHub is not None:
            if req.sender_type == "CONSUMER":
                asyncio.ensure_future(GlobalCrossHub.send_to_consumer(req.sender_id, msg))
            else:
                asyncio.ensure_future(GlobalCrossHub.send_to_user(req.sender_id, msg))
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Reject Friend Request
# ═════════════════════════════════════════════════════════════════════

def reject_request(user_id: str, user_type: str, p: HandleRequestParam) -> None:
    db = SessionLocal()
    try:
        if not user_id or not p.request_id:
            raise BusinessException("参数错误", 400)

        result = db.query(FriendRequest).filter(
            FriendRequest.id == p.request_id,
            FriendRequest.receiver_id == user_id,
            FriendRequest.receiver_type == user_type,
            FriendRequest.status == "pending",
        ).update({"status": "rejected", "updated_at": datetime.now()})
        if result == 0:
            raise BusinessException("好友请求不存在或已处理", 400)
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Friend List
# ═════════════════════════════════════════════════════════════════════

def friend_list(user_id: str, user_type: str) -> list[FriendVO]:
    db = SessionLocal()
    try:
        rows = db.query(Friendship).filter(
            Friendship.user_id == user_id,
            Friendship.user_type == user_type,
        ).all()
        results = []
        for r in rows:
            vo = FriendVO(
                user_id=r.friend_id,
                user_type=r.friend_type,
                remark=r.remark or "",
                added_at=_fmt_dt(r.created_at),
            )
            # Fetch nickname/avatar
            if r.friend_type == "BUSINESS":
                from plugins.plugin_sys.user.models import SysUser
                u = db.query(SysUser).filter(SysUser.id == r.friend_id).first()
                if u:
                    vo.nickname = u.nickname or ""
                    vo.avatar = u.avatar or ""
            else:
                from plugins.plugin_client.user.models import ClientUser
                u = db.query(ClientUser).filter(ClientUser.id == r.friend_id).first()
                if u:
                    vo.nickname = u.nickname or ""
                    vo.avatar = u.avatar or ""
            results.append(vo)
        return results
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Pending Requests
# ═════════════════════════════════════════════════════════════════════

def pending_requests(user_id: str, user_type: str) -> tuple[list[FriendRequestVO], list[FriendRequestVO]]:
    db = SessionLocal()
    try:
        incoming_rows = db.query(FriendRequest).filter(
            FriendRequest.receiver_id == user_id,
            FriendRequest.receiver_type == user_type,
            FriendRequest.status == "pending",
        ).all()
        outgoing_rows = db.query(FriendRequest).filter(
            FriendRequest.sender_id == user_id,
            FriendRequest.sender_type == user_type,
            FriendRequest.status == "pending",
        ).all()

        def to_vo(req: FriendRequest) -> FriendRequestVO:
            return FriendRequestVO(
                id=req.id, sender_id=req.sender_id, sender_type=req.sender_type,
                receiver_id=req.receiver_id, receiver_type=req.receiver_type,
                remark=req.remark or "", status=req.status,
                created_at=_fmt_dt(req.created_at),
            )

        incoming = [to_vo(r) for r in incoming_rows]
        outgoing = [to_vo(r) for r in outgoing_rows]
        return incoming, outgoing
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Remove Friend (delete both directions)
# ═════════════════════════════════════════════════════════════════════

def remove_friend(user_id: str, user_type: str, friend_id: str, friend_type: str) -> None:
    db = SessionLocal()
    try:
        if not user_id or not friend_id:
            raise BusinessException("参数错误", 400)
        db.query(Friendship).filter(
            or_(
                and_(Friendship.user_id == user_id, Friendship.user_type == user_type,
                     Friendship.friend_id == friend_id, Friendship.friend_type == friend_type),
                and_(Friendship.user_id == friend_id, Friendship.user_type == friend_type,
                     Friendship.friend_id == user_id, Friendship.friend_type == user_type),
            )
        ).delete()
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Search Users (sys + client)
# ═════════════════════════════════════════════════════════════════════

def search_users(keyword: str, limit: int = 20) -> list[SearchResult]:
    if not keyword or limit < 1:
        return []
    if limit > 50:
        limit = 50

    db = SessionLocal()
    try:
        like = f"%{keyword}%"
        results = []

        from plugins.plugin_sys.user.models import SysUser
        sys_users = db.query(SysUser).filter(
            or_(SysUser.username.like(like), SysUser.nickname.like(like))
        ).limit(limit).all()
        for u in sys_users:
            results.append(SearchResult(
                user_id=u.id, user_type="BUSINESS",
                nickname=u.nickname or "", avatar=u.avatar or "",
            ))

        remaining = limit - len(results)
        if remaining > 0:
            from plugins.plugin_client.user.models import ClientUser
            cli_users = db.query(ClientUser).filter(
                or_(ClientUser.username.like(like), ClientUser.nickname.like(like))
            ).limit(remaining).all()
            for u in cli_users:
                results.append(SearchResult(
                    user_id=u.id, user_type="CONSUMER",
                    nickname=u.nickname or "", avatar=u.avatar or "",
                ))

        return results
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Block / Unblock / BlockList
# ═════════════════════════════════════════════════════════════════════

def block_user(user_id: str, user_type: str, blocked_id: str, blocked_type: str) -> None:
    db = SessionLocal()
    try:
        if not user_id or not blocked_id or not blocked_type:
            raise BusinessException("参数错误", 400)
        if user_id == blocked_id and user_type == blocked_type:
            raise BusinessException("不能拉黑自己", 400)

        existing = db.query(FriendBlock).filter(
            FriendBlock.user_id == user_id,
            FriendBlock.user_type == user_type,
            FriendBlock.blocked_id == blocked_id,
            FriendBlock.blocked_type == blocked_type,
        ).count()
        if existing > 0:
            raise BusinessException("已经拉黑了该用户", 400)

        now = datetime.now()
        db.add(FriendBlock(
            id=generate_id(), user_id=user_id, user_type=user_type,
            blocked_id=blocked_id, blocked_type=blocked_type, created_at=now,
        ))

        # Also remove friendship
        db.query(Friendship).filter(
            or_(
                and_(Friendship.user_id == user_id, Friendship.user_type == user_type,
                     Friendship.friend_id == blocked_id, Friendship.friend_type == blocked_type),
                and_(Friendship.user_id == blocked_id, Friendship.user_type == blocked_type,
                     Friendship.friend_id == user_id, Friendship.friend_type == user_type),
            )
        ).delete()
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def unblock_user(user_id: str, user_type: str, blocked_id: str, blocked_type: str) -> None:
    db = SessionLocal()
    try:
        if not user_id or not blocked_id:
            raise BusinessException("参数错误", 400)
        result = db.query(FriendBlock).filter(
            FriendBlock.user_id == user_id,
            FriendBlock.user_type == user_type,
            FriendBlock.blocked_id == blocked_id,
            FriendBlock.blocked_type == blocked_type,
        ).delete()
        if result == 0:
            raise BusinessException("未拉黑该用户", 400)
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def block_list(user_id: str, user_type: str) -> list[BlockVO]:
    db = SessionLocal()
    try:
        rows = db.query(FriendBlock).filter(
            FriendBlock.user_id == user_id,
            FriendBlock.user_type == user_type,
        ).all()
        return [BlockVO(blocked_id=r.blocked_id, blocked_type=r.blocked_type,
                         created_at=_fmt_dt(r.created_at)) for r in rows]
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Update Friend Remark
# ═════════════════════════════════════════════════════════════════════

def update_friend_remark(user_id: str, user_type: str, friend_id: str, friend_type: str, remark: str) -> None:
    db = SessionLocal()
    try:
        if not user_id or not friend_id:
            raise BusinessException("参数错误", 400)
        db.query(Friendship).filter(
            Friendship.user_id == user_id,
            Friendship.user_type == user_type,
            Friendship.friend_id == friend_id,
            Friendship.friend_type == friend_type,
        ).update({"remark": remark})
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()
