"""Notice service — class-based service with DI-friendly provider."""

from typing import Optional
from datetime import datetime
from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data
from sdk.utils import generate_id
from .models import SysNotice
from .params import NoticeVO, NoticePageParam, NoticeLatestParam, SysNoticeToNoticeVO
from .repository import NoticeRepository
import logging

logger = logging.getLogger(__name__)


def _parse_time(s: Optional[str]) -> Optional[datetime]:
    if not s:
        return None
    try:
        return datetime.strptime(s, "%Y-%m-%d %H:%M:%S")
    except ValueError:
        return None
class NoticeService:
    def __init__(self, repository: NoticeRepository):
        self.repository = repository

    @classmethod
    def from_db(cls, db: Session) -> "NoticeService":
        return cls(NoticeRepository(db))

    def page(self, param: NoticePageParam) -> dict:
        result = self.repository.find_page(param)
        records = [SysNoticeToNoticeVO(r) for r in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[NoticeVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return SysNoticeToNoticeVO(entity)

    def create(self, vo: NoticeVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        entity = SysNotice(
            id=generate_id(),
            title=vo.title,
            category=vo.category,
            type=vo.type,
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
        )
        if vo.summary is not None:
            entity.summary = vo.summary
        if vo.content is not None:
            entity.content = vo.content
        if vo.cover is not None:
            entity.cover = vo.cover
        if vo.level:
            entity.level = vo.level
        if vo.status:
            entity.status = vo.status
        if vo.is_top:
            entity.is_top = vo.is_top
        if vo.author is not None:
            entity.author = vo.author
        if vo.publish_at is not None:
            entity.publish_at = _parse_time(vo.publish_at)
        if vo.expire_at is not None:
            entity.expire_at = _parse_time(vo.expire_at)
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: NoticeVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        up = {
            "title": vo.title,
            "category": vo.category,
            "type": vo.type,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
        }
        if vo.summary is not None:
            up["summary"] = vo.summary
        if vo.content is not None:
            up["content"] = vo.content
        if vo.cover is not None:
            up["cover"] = vo.cover
        if vo.level:
            up["level"] = vo.level
        if vo.status:
            up["status"] = vo.status
        if vo.is_top:
            up["is_top"] = vo.is_top
        if vo.author is not None:
            up["author"] = vo.author
        if vo.publish_at is not None:
            up["publish_at"] = _parse_time(vo.publish_at)
        if vo.expire_at is not None:
            up["expire_at"] = _parse_time(vo.expire_at)
        if actor and actor.user_id:
            up["updated_by"] = actor.user_id
        self.repository.update_by_id(vo.id, up)

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.delete_by_ids(ids)

    def options(self) -> list:
        return [SysNoticeToNoticeVO(r) for r in self.repository.list_all_ordered()]

    def latest(self, param: NoticeLatestParam) -> list:
        return [SysNoticeToNoticeVO(r) for r in self.repository.find_latest(param.size)]

    def public_page(self, param: NoticePageParam) -> dict:
        result = self.repository.find_public_page(param)
        records = [SysNoticeToNoticeVO(r) for r in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def public_detail(self, id: str) -> Optional[NoticeVO]:
        if not id:
            return None
        entity = self.repository.find_public_by_id(id)
        if not entity:
            return None
        return SysNoticeToNoticeVO(entity)


def get_notice_service(db: Session = Depends(get_db)) -> NoticeService:
    return NoticeService.from_db(db)
