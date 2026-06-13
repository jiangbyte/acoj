"""Friend service."""

from __future__ import annotations

import asyncio
from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message

from plugins.plugin_im.model.friend import FriendBlock, FriendRequest, Friendship
from .params import (
    BlockVO,
    FriendRequestToFriendRequestVO,
    FriendRequestVO,
    FriendVO,
    HandleRequestParam,
    SearchResult,
    SendRequestParam,
)
from .repository import FriendRepository
from ..message.user_repository import IMUserRepository


def _fmt_dt(dt: Optional[datetime]) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


class FriendService:
    def __init__(self, repository: FriendRepository):
        self.repository = repository
        self.db = repository.db
        self.user_repository = IMUserRepository(self.db)

    @classmethod
    def from_db(cls, db: Session) -> "FriendService":
        return cls(FriendRepository(db))

    def send_request(self, sender_id: str, sender_type: str, param: SendRequestParam) -> None:
        if not sender_id or not param.receiver_id or not param.receiver_type:
            raise BusinessException("参数错误", 400)
        if sender_id == param.receiver_id and sender_type == param.receiver_type:
            raise BusinessException("不能添加自己为好友", 400)

        count = self.repository.count_friendship(sender_id, sender_type, param.receiver_id, param.receiver_type)
        if count > 0:
            raise BusinessException("已经是好友了", 400)

        existing = self.repository.count_pending_request(
            sender_id, sender_type, param.receiver_id, param.receiver_type
        )
        if existing > 0:
            raise BusinessException("已发送过好友请求，请等待回复", 400)

        now = datetime.now()
        request = FriendRequest(
            id=generate_id(),
            sender_id=sender_id,
            sender_type=sender_type,
            receiver_id=param.receiver_id,
            receiver_type=param.receiver_type,
            remark=param.remark,
            status="pending",
            created_at=now,
            updated_at=now,
        )
        self.repository.create_request(request)

        payload = {
            "request_id": request.id,
            "sender_id": sender_id,
            "sender_type": sender_type,
            "remark": param.remark,
            "action": "friend_request",
        }
        msg = Message(type="friend_request", payload=payload)
        if im_ws.GlobalCrossHub is not None:
            if param.receiver_type == "CONSUMER":
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(param.receiver_id, msg))
            else:
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(param.receiver_id, msg))

    def accept_request(self, user_id: str, user_type: str, param: HandleRequestParam) -> None:
        if not user_id or not param.request_id:
            raise BusinessException("参数错误", 400)

        request = self.repository.find_pending_request_for_receiver(param.request_id, user_id, user_type)
        if not request:
            raise BusinessException("好友请求不存在或已处理", 400)

        now = datetime.now()
        pair1 = Friendship(
            id=generate_id(),
            user_id=request.receiver_id,
            user_type=request.receiver_type,
            friend_id=request.sender_id,
            friend_type=request.sender_type,
            created_at=now,
        )
        pair2 = Friendship(
            id=generate_id(),
            user_id=request.sender_id,
            user_type=request.sender_type,
            friend_id=request.receiver_id,
            friend_type=request.receiver_type,
            created_at=now,
        )
        self.repository.accept_request(request, pair1, pair2)

        payload = {
            "request_id": request.id,
            "receiver_id": user_id,
            "receiver_type": user_type,
            "action": "friend_request_accepted",
        }
        msg = Message(type="friend_request", payload=payload)
        if im_ws.GlobalCrossHub is not None:
            if request.sender_type == "CONSUMER":
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(request.sender_id, msg))
            else:
                asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(request.sender_id, msg))

    def reject_request(self, user_id: str, user_type: str, param: HandleRequestParam) -> None:
        if not user_id or not param.request_id:
            raise BusinessException("参数错误", 400)

        result = self.repository.reject_request(param.request_id, user_id, user_type)
        if result == 0:
            raise BusinessException("好友请求不存在或已处理", 400)

    def friend_list(self, user_id: str, user_type: str) -> list[FriendVO]:
        rows = self.repository.list_friendships(user_id, user_type)

        business_ids = [row.friend_id for row in rows if row.friend_type == "BUSINESS"]
        consumer_ids = [row.friend_id for row in rows if row.friend_type == "CONSUMER"]
        nickname_map: dict[str, str] = {}
        avatar_map: dict[str, str] = {}

        if business_ids:
            users = self.user_repository.list_sys_users(business_ids)
            for user in users:
                key = f"BUSINESS:{user.id}"
                nickname_map[key] = user.nickname or ""
                avatar_map[key] = user.avatar or ""

        if consumer_ids:
            users = self.user_repository.list_client_users(consumer_ids)
            for user in users:
                key = f"CONSUMER:{user.id}"
                nickname_map[key] = user.nickname or ""
                avatar_map[key] = user.avatar or ""

        return [
            FriendVO(
                user_id=row.friend_id,
                user_type=row.friend_type,
                nickname=nickname_map.get(f"{row.friend_type}:{row.friend_id}", ""),
                avatar=avatar_map.get(f"{row.friend_type}:{row.friend_id}", ""),
                remark=row.remark or "",
                added_at=_fmt_dt(row.created_at),
            )
            for row in rows
        ]

    def pending_requests(self, user_id: str, user_type: str) -> tuple[list[FriendRequestVO], list[FriendRequestVO]]:
        incoming_rows = self.repository.list_pending_incoming(user_id, user_type)
        outgoing_rows = self.repository.list_pending_outgoing(user_id, user_type)
        incoming = [FriendRequestToFriendRequestVO(row) for row in incoming_rows]
        outgoing = [FriendRequestToFriendRequestVO(row) for row in outgoing_rows]
        return incoming, outgoing

    def remove_friend(self, user_id: str, user_type: str, friend_id: str, friend_type: str) -> None:
        if not user_id or not friend_id:
            raise BusinessException("参数错误", 400)
        self.repository.remove_friendship_pair(user_id, user_type, friend_id, friend_type)

    def search_users(self, keyword: str, limit: int = 20) -> list[SearchResult]:
        if not keyword or limit < 1:
            return []
        if limit > 50:
            limit = 50

        results = [
            SearchResult(
                user_id=user.id,
                user_type="BUSINESS",
                nickname=user.nickname or "",
                avatar=user.avatar or "",
            )
            for user in self.user_repository.search_sys_users(keyword, limit)
        ]

        remaining = limit - len(results)
        if remaining > 0:
            results.extend(
                SearchResult(
                    user_id=user.id,
                    user_type="CONSUMER",
                    nickname=user.nickname or "",
                    avatar=user.avatar or "",
                )
                for user in self.user_repository.search_client_users(keyword, remaining)
            )
        return results

    def block_user(self, user_id: str, user_type: str, blocked_id: str, blocked_type: str) -> None:
        if not user_id or not blocked_id or not blocked_type:
            raise BusinessException("参数错误", 400)
        if user_id == blocked_id and user_type == blocked_type:
            raise BusinessException("不能拉黑自己", 400)

        existing = self.repository.count_block(user_id, user_type, blocked_id, blocked_type)
        if existing > 0:
            raise BusinessException("已经拉黑了该用户", 400)

        self.repository.create_block(
            FriendBlock(
                id=generate_id(),
                user_id=user_id,
                user_type=user_type,
                blocked_id=blocked_id,
                blocked_type=blocked_type,
                created_at=datetime.now(),
            )
        )
        self.repository.remove_friendship_pair(user_id, user_type, blocked_id, blocked_type)

    def unblock_user(self, user_id: str, user_type: str, blocked_id: str, blocked_type: str) -> None:
        if not user_id or not blocked_id:
            raise BusinessException("参数错误", 400)
        result = self.repository.remove_block(user_id, user_type, blocked_id, blocked_type)
        if result == 0:
            raise BusinessException("未拉黑该用户", 400)

    def block_list(self, user_id: str, user_type: str) -> list[BlockVO]:
        rows = self.repository.list_blocks(user_id, user_type)
        return [
            BlockVO(
                blocked_id=row.blocked_id,
                blocked_type=row.blocked_type,
                created_at=_fmt_dt(row.created_at),
            )
            for row in rows
        ]

    def update_friend_remark(
        self, user_id: str, user_type: str, friend_id: str, friend_type: str, remark: str
    ) -> None:
        if not user_id or not friend_id:
            raise BusinessException("参数错误", 400)
        self.repository.update_friend_remark(user_id, user_type, friend_id, friend_type, remark)


def get_friend_service(db: Session = Depends(get_db)) -> FriendService:
    return FriendService.from_db(db)
