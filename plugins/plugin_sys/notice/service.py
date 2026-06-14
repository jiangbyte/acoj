"""Notice service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import SysNotice
from .params import NoticeLatestParam, NoticePageParam, NoticeVO
from .repository import NoticeRepository


def _parse_time(s: Optional[str]) -> Optional[datetime]:
    if not s:
        return None
    try:
        return datetime.strptime(s, "%Y-%m-%d %H:%M:%S")
    except ValueError:
        return None


def _parse_notice_times(vo: NoticeVO) -> tuple[Optional[datetime], Optional[datetime]]:
    return _parse_time(vo.publish_at), _parse_time(vo.expire_at)


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


class NoticeService:
    def __init__(self, repository: NoticeRepository):
        self.repository = repository

    async def page(self, param: NoticePageParam) -> dict:
        return map_page_data(await self.repository.find_page(param), NoticeVO.model_validate, param.current, param.size)

    async def detail(self, id: str) -> Optional[NoticeVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return NoticeVO.model_validate(entity)

    async def create(self, vo: NoticeVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        publish_at, expire_at = _parse_notice_times(vo)
        entity = SysNotice(
            id=generate_id(),
            title=vo.title,
            category=vo.category,
            type=vo.type,
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
            summary=vo.summary,
            content=vo.content,
            cover=vo.cover,
            author=vo.author,
            publish_at=publish_at,
            expire_at=expire_at,
        )
        if vo.level:
            entity.level = vo.level
        if vo.status:
            entity.status = vo.status
        if vo.is_top:
            entity.is_top = vo.is_top
        if actor_user_id:
            entity.created_by = actor_user_id
            entity.updated_by = actor_user_id
        await self.repository.insert(entity)

    async def modify(self, vo: NoticeVO, actor: Optional[ActorContext] = None) -> None:
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        actor_user_id = _actor_user_id(actor)
        publish_at, expire_at = _parse_notice_times(vo)
        up = {
            "title": vo.title,
            "category": vo.category,
            "type": vo.type,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "summary": vo.summary,
            "content": vo.content,
            "cover": vo.cover,
            "author": vo.author,
            "publish_at": publish_at,
            "expire_at": expire_at,
        }
        if vo.level:
            up["level"] = vo.level
        if vo.status:
            up["status"] = vo.status
        if vo.is_top:
            up["is_top"] = vo.is_top
        if actor_user_id:
            up["updated_by"] = actor_user_id
        await self.repository.update_by_id(vo.id, up)

    async def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        await self.repository.delete_by_ids(ids)

    async def options(self) -> list:
        return [NoticeVO.model_validate(r) for r in await self.repository.list_all_ordered()]

    async def latest(self, param: NoticeLatestParam) -> list:
        return [NoticeVO.model_validate(r) for r in await self.repository.find_latest(param.size)]

    async def public_page(self, param: NoticePageParam) -> dict:
        return map_page_data(
            await self.repository.find_public_page(param),
            NoticeVO.model_validate,
            param.current,
            param.size,
        )

    async def public_detail(self, id: str) -> Optional[NoticeVO]:
        if not id:
            return None
        entity = await self.repository.find_public_by_id(id)
        if not entity:
            return None
        return NoticeVO.model_validate(entity)


def get_notice_service(db: AsyncSession = Depends(get_db)) -> NoticeService:
    return NoticeService(NoticeRepository(db))
