from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request

from . import SysPosition
from .params import PositionVO, PositionPageParam, PositionExportParam, PositionImportParam
from .dao import PositionDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.enums import ExportTypeEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class PositionService:
    def __init__(self, db: Session):
        self.dao = PositionDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: PositionPageParam) -> dict:
        if not param.group_id:
            return page_data(records=[], total=0, page=param.current, size=param.size)
        result = self.dao.find_page_by_filters(param)
        return page_data(
            records=[PositionVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]],
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size
        )

    async def create(self, vo: PositionVO, request: Optional[Request] = None) -> None:
        entity = SysPosition(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo: PositionVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[PositionVO]:
        entity = self.dao.find_by_id(param.id)
        return PositionVO.model_validate(entity) if entity else None

    def export(self, param: PositionExportParam):
        records: List[SysPosition] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            page_param = PositionPageParam(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
            records = result[PageDataField.RECORDS]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [PositionVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "职位数据", "职位数据")

    def download_template(self):
        return export_excel(make_template(SysPosition), "职位导入模板", "职位数据")

    async def import_data(self, param: PositionImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")
        entities = [SysPosition(**strip_system_fields(vo.model_dump())) for vo in param.data]
        self.dao.insert_batch(entities, user_id=await self._get_current_user_id(request))
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

