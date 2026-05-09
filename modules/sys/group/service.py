from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from .models import SysGroup
from .params import GroupVO, GroupPageParam, GroupExportParam, GroupImportParam, GrantGroupRoleParam
from .dao import GroupDao
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.exception import BusinessException
from core.enums import ExportTypeEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class GroupService:
    def __init__(self, db: Session):
        self.dao = GroupDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: GroupPageParam) -> dict:
        result = self.dao.find_page(param.current, param.size)
        return page_data(
            records=[GroupVO.model_validate(r).model_dump() for r in result["records"]],
            total=result["total"],
            page=param.current,
            size=param.size
        )

    async def create(self, vo: GroupVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        entity = SysGroup(**strip_system_fields(vo.model_dump()))
        entity.created_by = created_by
        self.dao.insert(entity)

    async def modify(self, vo: GroupVO, request: Optional[Request] = None) -> None:
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

    def detail(self, param: IdParam) -> Optional[GroupVO]:
        entity = self.dao.find_by_id(param.id)
        return GroupVO.model_validate(entity) if entity else None

    def export(self, param: GroupExportParam):
        records: List[SysGroup] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            result = self.dao.find_page(param.current or 1, param.size or 10)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_id or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [GroupVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "用户组数据", "用户组数据")

    def download_template(self):
        return export_excel(make_template(SysGroup), "用户组导入模板", "用户组数据")

    async def import_data(self, param: GroupImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")

        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = SysGroup(**strip_system_fields(vo.model_dump()))
            entity.created_by = created_by
            entities.append(entity)

        self.dao.insert_batch(entities)
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    async def grant_roles(self, param: GrantGroupRoleParam, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        self.dao.grant_roles(param.group_id, param.role_ids, created_by, param.scope, param.custom_scope_group_ids)

    def get_group_role_ids(self, group_id: str) -> List[str]:
        return self.dao.get_role_ids_by_group_id(group_id)
