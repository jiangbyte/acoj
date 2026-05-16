from typing import Optional
from sqlalchemy.orm import Session
from fastapi import Request
from .params import NoticeVO, NoticePageParam
from .dao import NoticeDao
from .models import SysNotice
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import strip_system_fields, apply_update
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class NoticeService:
    def __init__(self, db: Session):
        self.dao = NoticeDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: NoticePageParam) -> dict:
        result = self.dao.find_page(param)
        records = [NoticeVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]]
        return page_data(
            records=records,
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size,
        )

    def detail(self, param: IdParam):
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        return NoticeVO.model_validate(entity).model_dump()

    async def create(self, vo: NoticeVO, request: Optional[Request] = None) -> None:
        entity = SysNotice(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo: NoticeVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        apply_update(entity, vo.model_dump(exclude_unset=True))
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)
