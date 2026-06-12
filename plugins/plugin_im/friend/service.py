"""
Friend management service — mirrors hei-gin plugins/plugin-im/friend/service.go 1:1.
All functions: send/accept/reject request, friend list, pending requests,
remove friend, search users, block/unblock/block-list, update remark.
"""

from __future__ import annotations

import asyncio
from datetime import datetime
from typing import Optional

from sdk.infra.db import SessionLocal
from sdk.web.exception import BusinessException
from sdk.utils import generate_id
from plugins.plugin_im.model.friend import FriendRequest, Friendship, FriendBlock
from plugins.plugin_im.friend.params import (
    SendRequestParam, HandleRequestParam, FriendVO, FriendRequestVO, BlockVO, SearchResult,
    FriendRequestToFriendRequestVO,
)
from plugins.plugin_im.friend.repository import FriendRepository
from plugins.plugin_im.message.user_repository import IMUserRepository
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message


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
        repository = FriendRepository(db)
        if not sender_id or not p.receiver_id or not p.receiver_type:
            raise BusinessException("参数错误", 400)
        if sender_id == p.receiver_id and sender_type == p.receiver_type:
            raise BusinessException("不能添加自己为好友", 400)

        count = repository.count_friendship(sender_id, sender_type, p.receiver_id, p.receiver_type)
        if count > 0:
            raise BusinessException("已经是好友了", 400)

        existing = repository.count_pending_request(sender_id, sender_type, p.receiver_id, p.receiver_type)
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
        repository.create_request(req)

        payload = {"request_id": req.id, "sender_id": sender_id,
                    "sender_type": sender_type, "remark": p.remark, "action": "friend_request"}
        msg = Message(type="friend_request", payload=payload)
        if im_ws.GlobalCrossHub is not None:
            if p.receiver_type == "CONSUMER":
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(p.receiver_id, msg))
            else:
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(p.receiver_id, msg))
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
        repository = FriendRepository(db)
        if not user_id or not p.request_id:
            raise BusinessException("参数错误", 400)

        req = repository.find_pending_request_for_receiver(p.request_id, user_id, user_type)
        if not req:
            raise BusinessException("好友请求不存在或已处理", 400)

        now = datetime.now()
        pair1 = Friendship(
            id=generate_id(), user_id=req.receiver_id, user_type=req.receiver_type,
            friend_id=req.sender_id, friend_type=req.sender_type, created_at=now,
        )
        pair2 = Friendship(
            id=generate_id(), user_id=req.sender_id, user_type=req.sender_type,
            friend_id=req.receiver_id, friend_type=req.receiver_type, created_at=now,
        )
        repository.accept_request(req, pair1, pair2)

        payload = {
            "request_id": req.id, "receiver_id": user_id,
            "receiver_type": user_type, "action": "friend_request_accepted",
        }
        msg = Message(type="friend_request", payload=payload)
        if im_ws.GlobalCrossHub is not None:
            if req.sender_type == "CONSUMER":
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(req.sender_id, msg))
            else:
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(req.sender_id, msg))
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
        repository = FriendRepository(db)
        if not user_id or not p.request_id:
            raise BusinessException("参数错误", 400)

        result = repository.reject_request(p.request_id, user_id, user_type)
        if result == 0:
            raise BusinessException("好友请求不存在或已处理", 400)
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
        repository = FriendRepository(db)
        user_repository = IMUserRepository(db)
        rows = repository.list_friendships(user_id, user_type)

        # Batch resolve user names/avatars -- avoid N+1
        business_ids = [r.friend_id for r in rows if r.friend_type == "BUSINESS"]
        consumer_ids = [r.friend_id for r in rows if r.friend_type == "CONSUMER"]
        nickname_map: dict[str, str] = {}
        avatar_map: dict[str, str] = {}

        if business_ids:
            users = user_repository.list_sys_users(business_ids)
            for u in users:
                key = f"BUSINESS:{u.id}"
                nickname_map[key] = u.nickname or ""
                avatar_map[key] = u.avatar or ""

        if consumer_ids:
            users = user_repository.list_client_users(consumer_ids)
            for u in users:
                key = f"CONSUMER:{u.id}"
                nickname_map[key] = u.nickname or ""
                avatar_map[key] = u.avatar or ""

        results = []
        for r in rows:
            key = f"{r.friend_type}:{r.friend_id}"
            results.append(FriendVO(
                user_id=r.friend_id,
                user_type=r.friend_type,
                nickname=nickname_map.get(key, ""),
                avatar=avatar_map.get(key, ""),
                remark=r.remark or "",
                added_at=_fmt_dt(r.created_at),
            ))
        return results
    finally:
        db.close()

# ═════════════════════════════════════════════════════════════════════
# Pending Requests
# ═════════════════════════════════════════════════════════════════════

def pending_requests(user_id: str, user_type: str) -> tuple[list[FriendRequestVO], list[FriendRequestVO]]:
    db = SessionLocal()
    try:
        repository = FriendRepository(db)
        incoming_rows = repository.list_pending_incoming(user_id, user_type)
        outgoing_rows = repository.list_pending_outgoing(user_id, user_type)

        incoming = [FriendRequestToFriendRequestVO(r) for r in incoming_rows]
        outgoing = [FriendRequestToFriendRequestVO(r) for r in outgoing_rows]
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
        FriendRepository(db).remove_friendship_pair(user_id, user_type, friend_id, friend_type)
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
        user_repository = IMUserRepository(db)
        results = []
        sys_users = user_repository.search_sys_users(keyword, limit)
        for u in sys_users:
            results.append(SearchResult(
                user_id=u.id, user_type="BUSINESS",
                nickname=u.nickname or "", avatar=u.avatar or "",
            ))

        remaining = limit - len(results)
        if remaining > 0:
            cli_users = user_repository.search_client_users(keyword, remaining)
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
        repository = FriendRepository(db)
        if not user_id or not blocked_id or not blocked_type:
            raise BusinessException("参数错误", 400)
        if user_id == blocked_id and user_type == blocked_type:
            raise BusinessException("不能拉黑自己", 400)

        existing = repository.count_block(user_id, user_type, blocked_id, blocked_type)
        if existing > 0:
            raise BusinessException("已经拉黑了该用户", 400)

        now = datetime.now()
        repository.create_block(FriendBlock(
            id=generate_id(), user_id=user_id, user_type=user_type,
            blocked_id=blocked_id, blocked_type=blocked_type, created_at=now,
        ))
        repository.remove_friendship_pair(user_id, user_type, blocked_id, blocked_type)
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
        result = FriendRepository(db).remove_block(user_id, user_type, blocked_id, blocked_type)
        if result == 0:
            raise BusinessException("未拉黑该用户", 400)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def block_list(user_id: str, user_type: str) -> list[BlockVO]:
    db = SessionLocal()
    try:
        rows = FriendRepository(db).list_blocks(user_id, user_type)
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
        FriendRepository(db).update_friend_remark(user_id, user_type, friend_id, friend_type, remark)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()
