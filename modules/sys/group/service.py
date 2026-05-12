from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from fastapi import Request
from .models import SysGroup
from .params import GroupVO, GroupTreeVO, GroupPageParam, GroupTreeParam, GroupExportParam, GroupImportParam
from .dao import GroupDao
from ..org.models import SysOrg
from ..org.params import OrgVO
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.exception import BusinessException
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class GroupService:
    def __init__(self, db: Session):
        self.dao = GroupDao(db)
        self.db = db

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: GroupPageParam) -> dict:
        base_filters = [SysGroup.is_deleted == SoftDeleteEnum.NO]
        if param.parent_id:
            base_filters.append(
                (SysGroup.parent_id == param.parent_id) | (SysGroup.id == param.parent_id)
            )
        if param.org_id:
            base_filters.append(SysGroup.org_id == param.org_id)
        if param.keyword:
            keyword = f"%{param.keyword}%"
            base_filters.append(SysGroup.name.ilike(keyword))

        count_query = select(func.count()).select_from(SysGroup).where(*base_filters)
        total = self.db.execute(count_query).scalar() or 0

        offset = (param.current - 1) * param.size
        query = select(SysGroup).where(*base_filters).order_by(SysGroup.sort_code).offset(offset).limit(param.size)
        records = [GroupVO.model_validate(r).model_dump() for r in self.db.execute(query).scalars().all()]
        return page_data(
            records=records,
            total=total,
            page=param.current,
            size=param.size
        )

    def tree(self, param: GroupTreeParam) -> List[dict]:
        if not param.org_id:
            return []
        base_filters = [SysGroup.is_deleted == SoftDeleteEnum.NO]
        if param.org_id:
            base_filters.append(SysGroup.org_id == param.org_id)
        if param.keyword:
            keyword = f"%{param.keyword}%"
            base_filters.append(SysGroup.name.ilike(keyword))

        query = select(SysGroup).where(*base_filters).order_by(SysGroup.sort_code)
        records = list(self.db.execute(query).scalars().all())

        node_map = {}
        roots = []
        for r in records:
            r_dict = GroupVO.model_validate(r).model_dump()
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
                GroupService._sort_tree(children)

    def union_tree(self) -> List[dict]:
        """Build a combined tree of orgs and groups.
        Each node has _type: 'org' or 'group' to distinguish.
        """
        org_query = select(SysOrg).where(
            SysOrg.is_deleted == SoftDeleteEnum.NO
        ).order_by(SysOrg.sort_code)
        orgs = list(self.db.execute(org_query).scalars().all())

        group_query = select(SysGroup).where(
            SysGroup.is_deleted == SoftDeleteEnum.NO
        ).order_by(SysGroup.sort_code)
        groups = list(self.db.execute(group_query).scalars().all())

        org_nodes: dict[str, dict] = {}
        for o in orgs:
            node = OrgVO.model_validate(o).model_dump()
            node["_type"] = "org"
            node["children"] = []
            org_nodes[o.id] = node

        group_nodes: dict[str, dict] = {}
        for g in groups:
            node = GroupVO.model_validate(g).model_dump()
            node["_type"] = "group"
            node["children"] = []
            group_nodes[g.id] = node

        # Nest groups under parent groups
        for gid, node in group_nodes.items():
            pid = node.get("parent_id")
            if pid and pid in group_nodes:
                group_nodes[pid]["children"].append(node)

        # Collect root-level groups (no group parent) by org_id
        orphan_groups: dict[str, list[dict]] = {}
        for gid, node in group_nodes.items():
            pid = node.get("parent_id")
            if not pid or pid not in group_nodes:
                oid = node.get("org_id") or ""
                orphan_groups.setdefault(oid, []).append(node)

        # Attach orphan groups to their org
        for oid, node in org_nodes.items():
            if oid in orphan_groups:
                node["children"] = orphan_groups[oid] + node["children"]

        # Build org hierarchy and collect roots
        roots: list[dict] = []
        for oid, node in org_nodes.items():
            pid = node.get("parent_id")
            if pid and pid in org_nodes:
                org_nodes[pid]["children"].append(node)
            else:
                roots.append(node)

        self._sort_tree(roots)
        return roots

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
            page_param = GroupPageParam(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
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

    # 用户组-角色关联已废弃（使用 ral_role_permission.scope 的 GROUP / CUSTOM_GROUP）
