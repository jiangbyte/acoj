from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from .params import OrgVO, OrgPageParam, OrgExportParam, OrgImportParam, GrantOrgRoleParam, OrgTreeParam
from .dao import OrgDao
from .models import SysOrg
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.enums import ExportTypeEnum
from core.exception import BusinessException
from core.utils import export_excel, strip_system_fields, apply_update, make_template
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
        result = self.dao.find_page_by_filters(param)
        return page_data(
            records=[OrgVO.model_validate(r).model_dump() for r in result["records"]],
            total=result["total"],
            page=param.current,
            size=param.size
        )

    def tree(self, param: OrgTreeParam) -> List[dict]:
        records = self.dao.find_all_ordered()
        if param.category:
            records = [r for r in records if r.category == param.category]

        node_map = {}
        roots = []
        for r in records:
            r_dict = OrgVO.model_validate(r).model_dump()
            r_dict["children"] = []
            node_map[r.id] = r_dict

        for r_dict in node_map.values():
            pid = r_dict.get("parent_id")
            if pid and pid in node_map:
                node_map[pid]["children"].append(r_dict)
            else:
                roots.append(r_dict)

        self._sort_tree(roots)
        return roots

    @staticmethod
    def _sort_tree(nodes: List[dict]):
        nodes.sort(key=lambda x: x.get("sort_code", 0) or 0)
        for n in nodes:
            children = n.get("children")
            if children:
                OrgService._sort_tree(children)

    async def create(self, vo: OrgVO, request: Optional[Request] = None) -> None:
        entity = SysOrg(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo: OrgVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[OrgVO]:
        entity = self.dao.find_by_id(param.id)
        return OrgVO.model_validate(entity) if entity else None

    def export(self, param: OrgExportParam):
        records: List[SysOrg] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            page_param = OrgPageParam(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
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
        entities = [SysOrg(**strip_system_fields(vo.model_dump())) for vo in param.data]
        self.dao.insert_batch(entities, user_id=await self._get_current_user_id(request))
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    async def grant_roles(self, param: GrantOrgRoleParam, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        self.dao.grant_roles(param.org_id, param.role_ids, created_by, param.scope, param.custom_scope_group_ids)

    def get_org_role_ids(self, org_id: str) -> List[str]:
        return self.dao.get_role_ids_by_org_id(org_id)
