from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import select
from sqlalchemy.orm import Session

from plugins.plugin_im.model.broadcast import Broadcast, BroadcastRead


class BroadcastRepository:
    def __init__(self, db: Session):
        self.db = db

    def insert(self, entity: Broadcast) -> Broadcast:
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def page(self, cursor_dt: Optional[datetime], size: int) -> list[Broadcast]:
        stmt = select(Broadcast)
        if cursor_dt is not None:
            stmt = stmt.where(Broadcast.created_at < cursor_dt)
        stmt = stmt.order_by(Broadcast.created_at.desc()).limit(size + 1)
        return list(self.db.execute(stmt).scalars().all())

    def latest(self, size: int) -> list[Broadcast]:
        stmt = select(Broadcast).order_by(Broadcast.created_at.desc()).limit(size)
        return list(self.db.execute(stmt).scalars().all())

    def list_reads(self, user_id: str, user_type: str) -> list[BroadcastRead]:
        stmt = select(BroadcastRead).where(
            BroadcastRead.user_id == user_id,
            BroadcastRead.user_type == user_type,
        )
        return list(self.db.execute(stmt).scalars().all())

    def find_read(self, broadcast_id: str, user_id: str, user_type: str) -> Optional[BroadcastRead]:
        stmt = select(BroadcastRead).where(
            BroadcastRead.broadcast_id == broadcast_id,
            BroadcastRead.user_id == user_id,
            BroadcastRead.user_type == user_type,
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def mark_read(self, entity: BroadcastRead) -> None:
        self.db.add(entity)
        self.db.commit()

    def find_by_id(self, broadcast_id: str) -> Optional[Broadcast]:
        stmt = select(Broadcast).where(Broadcast.id == broadcast_id)
        return self.db.execute(stmt).scalar_one_or_none()
