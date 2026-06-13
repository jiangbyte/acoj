from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import and_, delete as sa_delete, func, or_, select, update as sa_update
from sqlalchemy.orm import Session

from plugins.plugin_im.model.friend import FriendBlock, FriendRequest, Friendship


class FriendRepository:
    def __init__(self, db: Session):
        self.db = db

    def count_friendship(self, user_id: str, user_type: str, friend_id: str, friend_type: str) -> int:
        stmt = select(func.count()).select_from(Friendship).where(
            or_(
                and_(Friendship.user_id == user_id, Friendship.user_type == user_type,
                     Friendship.friend_id == friend_id, Friendship.friend_type == friend_type),
                and_(Friendship.user_id == friend_id, Friendship.user_type == friend_type,
                     Friendship.friend_id == user_id, Friendship.friend_type == user_type),
            )
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def count_pending_request(self, sender_id: str, sender_type: str, receiver_id: str, receiver_type: str) -> int:
        stmt = select(func.count()).select_from(FriendRequest).where(
            FriendRequest.sender_id == sender_id,
            FriendRequest.sender_type == sender_type,
            FriendRequest.receiver_id == receiver_id,
            FriendRequest.receiver_type == receiver_type,
            FriendRequest.status == "pending",
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def create_request(self, entity: FriendRequest) -> FriendRequest:
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def find_pending_request_for_receiver(self, request_id: str, user_id: str, user_type: str) -> Optional[FriendRequest]:
        stmt = select(FriendRequest).where(
            FriendRequest.id == request_id,
            FriendRequest.receiver_id == user_id,
            FriendRequest.receiver_type == user_type,
            FriendRequest.status == "pending",
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def accept_request(self, request: FriendRequest, pair1: Friendship, pair2: Friendship) -> None:
        request.status = "accepted"
        request.updated_at = datetime.now()
        self.db.add(pair1)
        self.db.add(pair2)
        self.db.commit()

    def reject_request(self, request_id: str, user_id: str, user_type: str) -> int:
        stmt = sa_update(FriendRequest).where(
            FriendRequest.id == request_id,
            FriendRequest.receiver_id == user_id,
            FriendRequest.receiver_type == user_type,
            FriendRequest.status == "pending",
        ).values(status="rejected", updated_at=datetime.now())
        result = self.db.execute(stmt)
        self.db.commit()
        return int(result.rowcount or 0)

    def list_friendships(self, user_id: str, user_type: str) -> list[Friendship]:
        stmt = select(Friendship).where(
            Friendship.user_id == user_id,
            Friendship.user_type == user_type,
        )
        return list(self.db.execute(stmt).scalars().all())

    def list_pending_incoming(self, user_id: str, user_type: str) -> list[FriendRequest]:
        stmt = select(FriendRequest).where(
            FriendRequest.receiver_id == user_id,
            FriendRequest.receiver_type == user_type,
            FriendRequest.status == "pending",
        )
        return list(self.db.execute(stmt).scalars().all())

    def list_pending_outgoing(self, user_id: str, user_type: str) -> list[FriendRequest]:
        stmt = select(FriendRequest).where(
            FriendRequest.sender_id == user_id,
            FriendRequest.sender_type == user_type,
            FriendRequest.status == "pending",
        )
        return list(self.db.execute(stmt).scalars().all())

    def remove_friendship_pair(self, user_id: str, user_type: str, friend_id: str, friend_type: str) -> None:
        stmt = sa_delete(Friendship).where(
            or_(
                and_(Friendship.user_id == user_id, Friendship.user_type == user_type,
                     Friendship.friend_id == friend_id, Friendship.friend_type == friend_type),
                and_(Friendship.user_id == friend_id, Friendship.user_type == friend_type,
                     Friendship.friend_id == user_id, Friendship.friend_type == user_type),
            )
        )
        self.db.execute(stmt)
        self.db.commit()

    def count_block(self, user_id: str, user_type: str, blocked_id: str, blocked_type: str) -> int:
        stmt = select(func.count()).select_from(FriendBlock).where(
            FriendBlock.user_id == user_id,
            FriendBlock.user_type == user_type,
            FriendBlock.blocked_id == blocked_id,
            FriendBlock.blocked_type == blocked_type,
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def create_block(self, entity: FriendBlock) -> None:
        self.db.add(entity)
        self.db.commit()

    def remove_block(self, user_id: str, user_type: str, blocked_id: str, blocked_type: str) -> int:
        stmt = sa_delete(FriendBlock).where(
            FriendBlock.user_id == user_id,
            FriendBlock.user_type == user_type,
            FriendBlock.blocked_id == blocked_id,
            FriendBlock.blocked_type == blocked_type,
        )
        result = self.db.execute(stmt)
        self.db.commit()
        return int(result.rowcount or 0)

    def list_blocks(self, user_id: str, user_type: str) -> list[FriendBlock]:
        stmt = select(FriendBlock).where(
            FriendBlock.user_id == user_id,
            FriendBlock.user_type == user_type,
        )
        return list(self.db.execute(stmt).scalars().all())

    def update_friend_remark(self, user_id: str, user_type: str, friend_id: str, friend_type: str, remark: str) -> int:
        stmt = sa_update(Friendship).where(
            Friendship.user_id == user_id,
            Friendship.user_type == user_type,
            Friendship.friend_id == friend_id,
            Friendship.friend_type == friend_type,
        ).values(remark=remark)
        result = self.db.execute(stmt)
        self.db.commit()
        return int(result.rowcount or 0)
