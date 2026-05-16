from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from . import SysPosition
from .params import PositionVO, PositionPageParam
from .dao import PositionDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import strip_system_fields, apply_update
from core.auth import HeiAuthTool
from core.db.base_service import BaseCrudService
import logging

logger = logging.getLogger(__name__)


class PositionService(BaseCrudService):
    model_class = SysPosition
    vo_class = PositionVO
    dao_class = PositionDao
    page_param_class = PositionPageParam

    def page(self, param: PositionPageParam) -> dict:
        if not param.group_id:
            return page_data(records=[], total=0, page=param.current, size=param.size)
        result = self.dao.find_page_by_filters(param)
        records = [self.vo_class.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]]
        self._batch_enrich(records)
        return page_data(
            records=records,
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size,
        )

    def detail(self, param: IdParam) -> Optional[dict]:
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        vo = self.vo_class.model_validate(entity).model_dump()
        self._enrich_vo(vo)
        return vo

    def _enrich_vo(self, vo: dict) -> None:
        from core.db.base_service import _resolve_name_path
        from modules.sys.org.models import SysOrg
        from modules.sys.group.models import SysGroup
        vo["org_names"] = _resolve_name_path(vo.get("org_id"), self.dao.db, SysOrg)
        vo["group_names"] = _resolve_name_path(vo.get("group_id"), self.dao.db, SysGroup)

    def _batch_enrich(self, vo_list: List[dict]) -> None:
        from core.db.base_service import _resolve_name_path
        from modules.sys.org.models import SysOrg
        from modules.sys.group.models import SysGroup
        for vo in vo_list:
            vo["org_names"] = _resolve_name_path(vo.get("org_id"), self.dao.db, SysOrg)
            vo["group_names"] = _resolve_name_path(vo.get("group_id"), self.dao.db, SysGroup)

    def remove(self, param: IdsParam) -> None:
        from sqlalchemy import func, select
        from ..user.models import SysUser

        ids = param.ids
        db = self.dao.db

        if db.execute(
            select(func.count()).select_from(SysUser).where(
                SysUser.position_id.in_(ids)
            )
        ).scalar() > 0:
            raise BusinessException("职位存在关联用户，无法删除")

        self.dao.delete_by_ids(ids)
