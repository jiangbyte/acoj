"""Broadcast service."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.utils import generate_id
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message as WSMessage
from plugins.plugin_im.ws.tasks import schedule as schedule_ws_task

from plugins.plugin_im.model.broadcast import Broadcast, BroadcastRead
from .params import BroadcastToBroadcastVO, BroadcastVO, SendBroadcastParam
from .repository import BroadcastRepository


def _fmt_dt(dt: Optional[datetime]) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


class BroadcastService:
    def __init__(self, repository: BroadcastRepository):
        self.repository = repository

    @classmethod
    def from_db(cls, db: Session) -> "BroadcastService":
        return cls(BroadcastRepository(db))

    def send(self, sender_id: str, param: SendBroadcastParam) -> None:
        scope = param.scope or "ALL"
        now = datetime.now()
        self.repository.insert(
            Broadcast(
                id=generate_id(),
                title=param.title,
                content=param.content,
                scope=scope,
                sender_id=sender_id,
                created_at=now,
                updated_at=now,
            )
        )

        payload = {
            "title": param.title,
            "content": param.content,
            "scope": scope,
            "action": "broadcast",
        }
        ws_msg = WSMessage(type="broadcast", payload=payload)

        if scope == "ALL":
            if im_ws.GlobalCrossHub:
                schedule_ws_task(im_ws.GlobalCrossHub.broadcast_all(ws_msg))
        elif scope == "BUSINESS":
            if im_ws.GlobalCrossHub:
                schedule_ws_task(im_ws.GlobalCrossHub.broadcast_business(ws_msg))
        elif scope == "CONSUMER":
            if im_ws.GlobalCrossHub:
                schedule_ws_task(im_ws.GlobalCrossHub.broadcast_consumers(ws_msg))

    def list(self, cursor: str = "", size: int = 20) -> tuple[list[BroadcastVO], bool]:
        if size < 1:
            size = 20
        if size > 100:
            size = 100

        cursor_dt = None
        if cursor:
            try:
                cursor_dt = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                cursor_dt = None

        records = self.repository.page(cursor_dt, size)
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        return [BroadcastToBroadcastVO(record) for record in records], has_more

    def unread_list(self, user_id: str, user_type: str) -> tuple[list[BroadcastVO], bool]:
        records = self.repository.latest(50)
        read_records = self.repository.list_reads(user_id, user_type)
        read_map = {record.broadcast_id: record.read_at for record in read_records}

        results = []
        for record in records:
            vo = BroadcastToBroadcastVO(record)
            is_read = record.id in read_map
            vo.read = is_read
            read_at = read_map.get(record.id)
            if is_read and read_at:
                vo.read_at = _fmt_dt(read_at)
            results.append(vo)
        return results, False

    def mark_read(self, user_id: str, user_type: str, broadcast_id: str) -> None:
        existing = self.repository.find_read(broadcast_id, user_id, user_type)
        if existing:
            return
        self.repository.mark_read(
            BroadcastRead(
                id=generate_id(),
                broadcast_id=broadcast_id,
                user_id=user_id,
                user_type=user_type,
                read_at=datetime.now(),
            )
        )

    def detail(self, broadcast_id: str) -> Optional[BroadcastVO]:
        if not broadcast_id:
            return None
        entity = self.repository.find_by_id(broadcast_id)
        if not entity:
            return None
        return BroadcastToBroadcastVO(entity)


def get_broadcast_service(db: Session = Depends(get_db)) -> BroadcastService:
    return BroadcastService.from_db(db)
