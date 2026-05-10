from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from fastapi import Request
from .models import SysPosition
from .params import PositionVO, PositionPageParam, PositionExportParam, PositionImportParam
from .dao import PositionDao
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.enums import SoftDeleteEnum
from core.exception import BusinessException
from core.enums import ExportTypeEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class PositionService:
    def __init__(self, db: Session):
        self.dao = PositionDao(db)
        self.db = db

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: PositionPageParam) -> dict:
        filters = [SysPosition.is_deleted == SoftDeleteEnum.NO]
        if param.keyword:
            keyword = f"%{param.keyword}%"
            filters.append(SysPosition.name.ilike(keyword))
        if param.group_id:
            filters.append(SysPosition.group_id == param.group_id)
        if param.org_id:
            filters.append(SysPosition.org_id == param.org_id)

        count_query = select(func.count()).select_from(SysPosition).where(*filters)
        total = self.db.execute(count_query).scalar() or 0

        offset = (param.current - 1) * param.size
        query = select(SysPosition).where(*filters).order_by(SysPosition.sort_code).offset(offset).limit(param.size)
        records = [PositionVO.model_validate(r).model_dump() for r in self.db.execute(query).scalars().all()]

        return page_data(
            records=records,
            total=total,
            page=param.current,
            size=param.size
        )

    async def create(self, vo: PositionVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        entity = SysPosition(**strip_system_fields(vo.model_dump()))
        entity.created_by = created_by
        self.dao.insert(entity)

    async def modify(self, vo: PositionVO, request: Optional[Request] = None) -> None:
        updated_by = await self._get_current_user_id(request)
        entity = self.dao.find_by_id(vo.id)

        if not entity:
            raise BusinessException("数据不存在")

        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)

        entity.updated_by = updated_by
        self.dao.update(entity)

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
            records = result["records"]
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

        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = SysPosition(**strip_system_fields(vo.model_dump()))
            entity.created_by = created_by
            entities.append(entity)

        self.dao.insert_batch(entities)
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

