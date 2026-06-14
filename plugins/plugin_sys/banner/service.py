"""Banner service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data
from sdk.utils import generate_id

from .models import SysBanner
from .params import BannerPageParam, BannerVO
from .repository import BannerRepository


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


class BannerService:
    def __init__(self, repository: BannerRepository):
        self.repository = repository
        self.db = repository.db

    async def page(self, param: BannerPageParam) -> dict:
        return map_page_data(await self.repository.find_page(param), BannerVO.model_validate, param.current, param.size)

    async def detail(self, id: str) -> Optional[BannerVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return BannerVO.model_validate(entity)

    async def create(self, vo: BannerVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysBanner(
            id=generate_id(),
            title=vo.title,
            image=vo.image,
            category=vo.category,
            type=vo.type,
            position=vo.position,
            url=vo.url,
            link_type=vo.link_type or "URL",
            summary=vo.summary,
            description=vo.description,
            sort_code=vo.sort_code or 0,
            view_count=vo.view_count or 0,
            click_count=vo.click_count or 0,
            created_at=now,
            updated_at=now,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        await self.repository.insert(entity)

    async def modify(self, vo: BannerVO, actor: Optional[ActorContext] = None) -> None:
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        actor_user_id = _actor_user_id(actor)
        up = {
            "title": vo.title,
            "image": vo.image,
            "link_type": vo.link_type,
            "category": vo.category,
            "type": vo.type,
            "position": vo.position,
            "url": vo.url,
            "summary": vo.summary,
            "description": vo.description,
            "sort_code": vo.sort_code,
            "view_count": vo.view_count,
            "click_count": vo.click_count,
            "updated_at": datetime.now(),
        }
        if actor_user_id:
            up["updated_by"] = actor_user_id
        await self.repository.update_by_id(vo.id, up)

    async def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        await self.repository.delete_by_ids(ids)

    async def options(self) -> list[BannerVO]:
        rows = await self.repository.list_all_ordered()
        return [BannerVO.model_validate(row) for row in rows]


def get_banner_service(db: AsyncSession = Depends(get_db)) -> BannerService:
    return BannerService(BannerRepository(db))
