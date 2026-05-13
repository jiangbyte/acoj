from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request

from . import SysGroup
from .params import GroupVO, GroupTreeVO, GroupPageParam, GroupTreeParam, GroupExportParam, GroupImportParam
from .dao import GroupDao
from ..org.params import OrgVO
from ..org.dao import OrgDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.exception import BusinessException
from core.utils import export_excel, strip_system_fields, apply_update, make_template
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class GroupService:
    def __init__(self, db: Session):
        self.dao = GroupDao(db)
        self._org_dao = OrgDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: GroupPageParam) -> dict:
        if not param.parent_id and not param.org_id:
            return page_data(records=[], total=0, page=param.current, size=param.size)
        result = self.dao.find_page_by_filters(param)
        return page_data(
            records=[GroupVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]],
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size
        )

    def tree(self, param: GroupTreeParam) -> List[dict]:
        if not param.org_id:
            return []
        records = self.dao.find_all_ordered()
        filtered = []
        for r in records:
            if r.org_id != param.org_id:
                continue
            if param.keyword and param.keyword.lower() not in (r.name or "").lower():
                continue
            filtered.append(r)

        node_map = {}
        roots = []
        for r in filtered:
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
        orgs = self._org_dao.find_all_ordered()
        groups = self.dao.find_all_ordered()

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

        for gid, node in group_nodes.items():
            pid = node.get("parent_id")
            if pid and pid in group_nodes:
                group_nodes[pid]["children"].append(node)

        orphan_groups: dict[str, list[dict]] = {}
        for gid, node in group_nodes.items():
            pid = node.get("parent_id")
            if not pid or pid not in group_nodes:
                oid = node.get("org_id") or ""
                orphan_groups.setdefault(oid, []).append(node)

        for oid, node in org_nodes.items():
            if oid in orphan_groups:
                node["children"] = orphan_groups[oid] + node["children"]

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
        entity = SysGroup(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo: GroupVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        if vo.parent_id is not None and vo.parent_id != entity.parent_id:
            self._check_circular_parent(vo.id, vo.parent_id)
        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def _check_circular_parent(self, entity_id: str, new_parent_id: Optional[str]) -> None:
        if not new_parent_id:
            return
        current = new_parent_id
        while current:
            if current == entity_id:
                raise BusinessException("父级不能选择自身或子节点")
            parent = self.dao.find_by_id(current)
            if not parent or not parent.parent_id:
                break
            current = parent.parent_id

    def _collect_descendant_ids(self, ids: List[str]) -> List[str]:
        """递归收集所有子用户组ID。"""
        all_ids = set(ids)
        stack = list(ids)
        while stack:
            parent_id = stack.pop()
            children = self.dao.db.query(SysGroup).filter(
                SysGroup.parent_id == parent_id,
                SysGroup.is_deleted == SoftDeleteEnum.NO
            ).all()
            for child in children:
                if child.id not in all_ids:
                    all_ids.add(child.id)
                    stack.append(child.id)
        return list(all_ids)

    def remove(self, param: IdsParam) -> None:
        from ..user.models import RelUserGroup
        from ..position.models import SysPosition

        all_ids = self._collect_descendant_ids(param.ids)
        db = self.dao.db

        if db.query(RelUserGroup).filter(
            RelUserGroup.group_id.in_(all_ids)
        ).count() > 0:
            raise BusinessException("用户组存在关联用户，无法删除")

        db.query(RelUserGroup).filter(RelUserGroup.group_id.in_(all_ids)).delete(synchronize_session=False)

        db.query(SysPosition).filter(
            SysPosition.group_id.in_(all_ids), SysPosition.is_deleted == SoftDeleteEnum.NO
        ).update({"group_id": None}, synchronize_session=False)

        self.dao.delete_by_ids(all_ids)

    def detail(self, param: IdParam) -> Optional[GroupVO]:
        entity = self.dao.find_by_id(param.id)
        return GroupVO.model_validate(entity) if entity else None

    def export(self, param: GroupExportParam):
        records: List[SysGroup] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            page_param = GroupPageParam(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
            records = result[PageDataField.RECORDS]
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
        entities = [SysGroup(**strip_system_fields(vo.model_dump())) for vo in param.data]
        self.dao.insert_batch(entities, user_id=await self._get_current_user_id(request))
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}
