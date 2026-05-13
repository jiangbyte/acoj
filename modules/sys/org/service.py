from typing import Optional, List
from fastapi import Request
from .params import OrgVO, OrgPageParam, GrantOrgRoleParam, OrgTreeParam
from .dao import OrgDao
from .models import SysOrg
from core.pojo import IdsParam
from core.result import page_data, PageDataField
from core.enums import SoftDeleteEnum
from core.exception import BusinessException
from core.utils import apply_update
from core.db.base_service import BaseCrudService


class OrgService(BaseCrudService):
    model_class = SysOrg
    vo_class = OrgVO
    dao_class = OrgDao
    page_param_class = OrgPageParam
    export_name = "组织数据"

    def page(self, param: OrgPageParam) -> dict:
        result = self.dao.find_page_by_filters(param)
        return page_data(
            records=[OrgVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]],
            total=result[PageDataField.TOTAL],
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

    async def modify(self, vo: OrgVO, request: Optional[Request] = None) -> None:
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
        """递归收集所有子组织ID。"""
        all_ids = set(ids)
        stack = list(ids)
        while stack:
            parent_id = stack.pop()
            children = self.dao.db.query(SysOrg).filter(
                SysOrg.parent_id == parent_id,
                SysOrg.is_deleted == SoftDeleteEnum.NO
            ).all()
            for child in children:
                if child.id not in all_ids:
                    all_ids.add(child.id)
                    stack.append(child.id)
        return list(all_ids)

    def remove(self, param: IdsParam) -> None:
        from ..user.models import SysUser
        from ..group.models import SysGroup
        from ..position.models import SysPosition
        from .models import RelOrgRole

        all_ids = self._collect_descendant_ids(param.ids)
        db = self.dao.db

        if db.query(SysUser).filter(
            SysUser.org_id.in_(all_ids), SysUser.is_deleted == SoftDeleteEnum.NO
        ).count() > 0:
            raise BusinessException("组织存在关联用户，无法删除")

        if db.query(SysGroup).filter(
            SysGroup.org_id.in_(all_ids), SysGroup.is_deleted == SoftDeleteEnum.NO
        ).count() > 0:
            raise BusinessException("组织下存在用户组，无法删除")

        db.query(RelOrgRole).filter(RelOrgRole.org_id.in_(all_ids)).delete(synchronize_session=False)

        db.query(SysPosition).filter(
            SysPosition.org_id.in_(all_ids), SysPosition.is_deleted == SoftDeleteEnum.NO
        ).update({"org_id": None}, synchronize_session=False)

        self.dao.delete_by_ids(all_ids)

    async def grant_roles(self, param: GrantOrgRoleParam, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        self.dao.grant_roles(param.org_id, param.role_ids, created_by, param.scope, param.custom_scope_group_ids)

    def get_org_role_ids(self, org_id: str) -> List[str]:
        return self.dao.get_role_ids_by_org_id(org_id)
