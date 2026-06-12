"""Banner service — class-based service with DI-friendly provider."""

from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data
from sdk.utils import generate_id
import logging

from .params import BannerPageParam, BannerVO, BannerVOToSysBanner, SysBannerToBannerVO
from .repository import BannerRepository

logger = logging.getLogger(__name__)


class BannerService:
    def __init__(self, repository: BannerRepository):
        self.repository = repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "BannerService":
        return cls(BannerRepository(db))

    def page(self, param: BannerPageParam) -> dict:
        result = self.repository.find_page(param)
        records = [SysBannerToBannerVO(r) for r in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[BannerVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return SysBannerToBannerVO(entity)

    def create(self, vo: BannerVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        entity = BannerVOToSysBanner(vo)
        entity.id = generate_id()
        entity.created_at = now
        entity.updated_at = now
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: BannerVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        up = {
            "title": vo.title,
            "image": vo.image,
            "link_type": vo.link_type,
            "category": vo.category,
            "type": vo.type,
            "position": vo.position,
            "sort_code": vo.sort_code,
            "view_count": vo.view_count,
            "click_count": vo.click_count,
            "updated_at": datetime.now(),
        }
        if vo.url is not None:
            up["url"] = vo.url
        if vo.summary is not None:
            up["summary"] = vo.summary
        if vo.description is not None:
            up["description"] = vo.description
        if actor and actor.user_id:
            up["updated_by"] = actor.user_id
        self.repository.update_by_id(vo.id, up)

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.delete_by_ids(ids)

    def options(self) -> list[BannerVO]:
        rows = self.repository.list_all_ordered()
        return [SysBannerToBannerVO(r) for r in rows]


def get_banner_service(db: Session = Depends(get_db)) -> BannerService:
    return BannerService.from_db(db)
