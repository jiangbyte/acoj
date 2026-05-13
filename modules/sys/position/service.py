from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from . import SysPosition
from .params import PositionVO, PositionPageParam, PositionExportParam, PositionImportParam
from .dao import PositionDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template
from core.auth import HeiAuthTool
from core.db.base_service import BaseCrudService
import logging

logger = logging.getLogger(__name__)


class PositionService(BaseCrudService):
    model_class = SysPosition
    vo_class = PositionVO
    dao_class = PositionDao
    page_param_class = PositionPageParam
    export_name = "职位数据"

    def page(self, param: PositionPageParam) -> dict:
        if not param.group_id:
            return page_data(records=[], total=0, page=param.current, size=param.size)
        return super().page(param)

    def remove(self, param: IdsParam) -> None:
        from sqlalchemy import func, select
        from ..user.models import SysUser

        ids = param.ids
        db = self.dao.db

        if db.execute(
            select(func.count()).select_from(SysUser).where(
                SysUser.position_id.in_(ids), SysUser.is_deleted == SoftDeleteEnum.NO
            )
        ).scalar() > 0:
            raise BusinessException("职位存在关联用户，无法删除")

        self.dao.delete_by_ids(ids)
