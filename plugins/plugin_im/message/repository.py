from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import and_, func, or_, select, text, update as sa_update
from sqlalchemy.dialects.mysql import insert as mysql_upsert
from sqlalchemy.orm import Session

from .params import MessagePageParam
from ..model.message import Conversation, Message


class MessageRepository:
    def __init__(self, db: Session):
        self.db = db

    def create_messages(self, records: list[Message]) -> None:
        for record in records:
            self.db.add(record)
        self.db.flush()

    def upsert_conversations(self, conversations: list[Conversation]) -> None:
        for rec in conversations:
            stmt = mysql_upsert(Conversation).values(
                id=rec.id,
                from_id=rec.from_id,
                from_type=rec.from_type,
                to_id=rec.to_id,
                to_type=rec.to_type,
                last_msg=rec.last_msg,
                last_time=rec.last_time,
                created_at=rec.created_at,
                updated_at=rec.updated_at,
            ).on_duplicate_key_update(
                last_msg=rec.last_msg,
                last_time=rec.last_time,
                updated_at=rec.updated_at,
            )
            self.db.execute(stmt)

    def commit(self) -> None:
        self.db.commit()

    def rollback(self) -> None:
        self.db.rollback()

    def page_messages(self, user_id: str, user_type: str, param: MessagePageParam) -> tuple[list[Message], int]:
        stmt = select(Message).where(
            or_(
                and_(Message.sender_id == user_id, Message.sender_type == user_type),
                and_(Message.receiver_id == user_id, Message.receiver_type == user_type),
            ),
            or_(Message.deleted_by != user_id, Message.deleted_by.is_(None)),
        )
        if param.status:
            stmt = stmt.where(Message.status == param.status)
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = int(self.db.execute(count_stmt).scalar() or 0)
        stmt = stmt.order_by(Message.created_at.desc()).offset((param.current - 1) * param.size).limit(param.size)
        return list(self.db.execute(stmt).scalars().all()), total

    def count_unread(self, user_id: str, user_type: str) -> int:
        stmt = select(func.count()).select_from(Message).where(
            Message.receiver_id == user_id,
            Message.receiver_type == user_type,
            Message.status == "unread",
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def find_by_id(self, message_id: str) -> Optional[Message]:
        stmt = select(Message).where(Message.id == message_id)
        return self.db.execute(stmt).scalar_one_or_none()

    def find_owned_by_id(self, message_id: str, user_id: str, user_type: str) -> Optional[Message]:
        stmt = select(Message).where(
            Message.id == message_id,
            or_(
                and_(Message.sender_id == user_id, Message.sender_type == user_type),
                and_(Message.receiver_id == user_id, Message.receiver_type == user_type),
            ),
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def mark_read(self, message_id: str, user_id: str, user_type: str) -> None:
        self.db.execute(sa_update(Message).where(Message.id == message_id).values(
            status="read", read_at=datetime.now(),
        ).where(
            Message.receiver_id == user_id,
            Message.receiver_type == user_type,
        ))
        self.db.commit()

    def mark_conversation_read(self, receiver_id: str, receiver_type: str, conversation_id: str) -> None:
        self.db.execute(sa_update(Message).where(
            Message.conversation_id == conversation_id,
            Message.receiver_id == receiver_id,
            Message.receiver_type == receiver_type,
            Message.status == "unread",
        ).values(status="read", read_at=datetime.now()))
        self.db.commit()

    def mark_all_read(self, receiver_id: str, receiver_type: str) -> None:
        self.db.execute(sa_update(Message).where(
            Message.receiver_id == receiver_id,
            Message.receiver_type == receiver_type,
            Message.status == "unread",
        ).values(status="read", read_at=datetime.now()))
        self.db.commit()

    def soft_delete_messages(self, user_id: str, ids: list[str]) -> None:
        self.db.execute(sa_update(Message).where(
            Message.id.in_(ids),
            or_(Message.sender_id == user_id, Message.receiver_id == user_id),
        ).values(deleted_by=user_id))
        self.db.commit()

    def search_messages(self, user_id: str, user_type: str, keyword: str, cursor_dt: Optional[datetime], size: int) -> list[Message]:
        stmt = select(Message).where(
            or_(
                and_(Message.sender_id == user_id, Message.sender_type == user_type),
                and_(Message.receiver_id == user_id, Message.receiver_type == user_type),
            ),
            Message.content.like(f"%{keyword}%"),
        )
        if cursor_dt is not None:
            stmt = stmt.where(Message.created_at < cursor_dt)
        stmt = stmt.order_by(Message.created_at.desc()).limit(size + 1)
        return list(self.db.execute(stmt).scalars().all())

    def list_conversation_messages(self, current_user_id: str, user_type: str, conversation_id: str, cursor_dt: Optional[datetime], size: int) -> list[Message]:
        stmt = select(Message).where(
            Message.conversation_id == conversation_id,
            or_(
                and_(Message.sender_id == current_user_id, Message.sender_type == user_type),
                and_(Message.receiver_id == current_user_id, Message.receiver_type == user_type),
            ),
            or_(Message.deleted_by != current_user_id, Message.deleted_by.is_(None)),
        )
        if cursor_dt is not None:
            stmt = stmt.where(Message.created_at < cursor_dt)
        order = text("created_at ASC") if cursor_dt is not None else Message.created_at.desc()
        stmt = stmt.order_by(order).limit(size + 1)
        return list(self.db.execute(stmt).scalars().all())

    def list_latest_message_rows(self, current_user_id: str, user_type: str) -> list:
        subq = select(
            Message.conversation_id,
            func.max(Message.created_at).label("max_ct"),
        ).where(
            or_(
                and_(Message.sender_id == current_user_id, Message.sender_type == user_type),
                and_(Message.receiver_id == current_user_id, Message.receiver_type == user_type),
            ),
            or_(Message.deleted_by != current_user_id, Message.deleted_by.is_(None)),
        ).group_by(Message.conversation_id).subquery()

        stmt = select(
            Message.conversation_id,
            Message.sender_id,
            Message.sender_type,
            Message.receiver_id,
            Message.receiver_type,
            Message.content,
            Message.created_at,
            Message.status,
        ).join(
            subq,
            and_(
                subq.c.conversation_id == Message.conversation_id,
                subq.c.max_ct == Message.created_at,
            )
        ).order_by(Message.created_at.desc())
        return list(self.db.execute(stmt).all())

    def unread_counts_by_conversation(self, conv_ids: list[str], current_user_id: str, user_type: str) -> dict[str, int]:
        if not conv_ids:
            return {}
        stmt = select(
            Message.conversation_id,
            func.count(Message.id).label("count"),
        ).where(
            Message.conversation_id.in_(conv_ids),
            Message.receiver_id == current_user_id,
            Message.receiver_type == user_type,
            Message.status == "unread",
        ).group_by(Message.conversation_id)
        return {row.conversation_id: row.count for row in self.db.execute(stmt).all()}
