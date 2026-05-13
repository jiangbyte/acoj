from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.enums import ExportTypeEnum
from core.utils import strip_system_fields, apply_update, export_excel, make_template
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class BaseCrudService:
    """Standardized CRUD service base class.

    Subclasses must set class attributes:
        model_class   - SQLAlchemy model class
        vo_class      - Pydantic VO class (must have model_validate, model_dump)
        dao_class     - DAO class extending BaseDAO
        export_name   - export filename/title (e.g., \"Banner数据\")

    For export() to work, param must have:
        export_type, current, size, selected_ids

    Subclasses override page(), create(), modify(), remove() etc.
    when custom logic is needed.
    """

    model_class = None
    vo_class = None
    dao_class = None
    page_param_class = None  # PageBound subclass for export()
    export_name = "导出数据"

    def __init__(self, db: Session):
        self.dao = self.dao_class(db)

    @property
    def _auth_tool(self):
        return HeiAuthTool

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await self._auth_tool.getLoginIdDefaultNull(request)
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param) -> dict:
        result = self.dao.find_page(param)
        return page_data(
            records=[self.vo_class.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]],
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size,
        )

    def detail(self, param: IdParam):
        entity = self.dao.find_by_id(param.id)
        return self.vo_class.model_validate(entity) if entity else None

    async def create(self, vo, request: Optional[Request] = None) -> None:
        entity = self.model_class(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        apply_update(entity, vo.model_dump(exclude_unset=True))
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def export(self, param):
        records: List = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            page_param = self.page_param_class(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
            records = result[PageDataField.RECORDS]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [self.vo_class.model_validate(r).model_dump() for r in records]
        return export_excel(data, self.export_name, self.export_name)

    def download_template(self):
        return export_excel(make_template(self.model_class), f"{self.export_name}导入模板", self.export_name)

    async def import_data(self, param, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")
        entities = [self.model_class(**strip_system_fields(vo.model_dump())) for vo in param.data]
        self.dao.insert_batch(entities, user_id=await self._get_current_user_id(request))
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}
