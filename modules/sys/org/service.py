from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from .models import SysOrg
from .params import OrgVO, OrgPageParam, OrgExportParam, OrgImportParam, GrantOrgRoleParam
from .dao import OrgDao
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.exception import BusinessException
from core.enums import ExportTypeEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class OrgService:
    def __init__(self, db: Session):
        self.dao = OrgDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: OrgPageParam) -> dict:
        result = self.dao.find_page(param)
        return page_data(
            records=[OrgVO.model_validate(r).model_dump() for r in result["records"]],
            total=result["total"],
            page=param.current,
            size=param.size
        )

    async def create(self, vo: OrgVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        entity = SysOrg(**strip_system_fields(vo.model_dump()))
        entity.created_by = created_by
        self.dao.insert(entity)

    async def modify(self, vo: OrgVO, request: Optional[Request] = None) -> None:
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

    def detail(self, param: IdParam) -> Optional[OrgVO]:
        entity = self.dao.find_by_id(param.id)
        return OrgVO.model_validate(entity) if entity else None

    def export(self, param: OrgExportParam):
        records: List[SysOrg] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            result = self.dao.find_page(param.current or 1, param.size or 10)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [OrgVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "组织数据", "组织数据")

    def download_template(self):
        return export_excel(make_template(SysOrg), "组织导入模板", "组织数据")

    async def import_data(self, param: OrgImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")

        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = SysOrg(**strip_system_fields(vo.model_dump()))
            entity.created_by = created_by
            entities.append(entity)

        self.dao.insert_batch(entities)
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    async def grant_roles(self, param: GrantOrgRoleParam, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        self.dao.grant_roles(param.org_id, param.role_ids, created_by, param.scope, param.custom_scope_group_ids)

    def get_org_role_ids(self, org_id: str) -> List[str]:
        return self.dao.get_role_ids_by_org_id(org_id)
