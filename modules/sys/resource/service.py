from typing import Optional, List
from fastapi import Request
from .models import SysModule, SysResource
from .params import ModuleVO, ResourceVO, ModulePageParam, ResourcePageParam
from .dao import ModuleDao, ResourceDao
from core.pojo import IdsParam
from core.enums import SoftDeleteEnum
from core.exception import BusinessException
from core.utils import apply_update, generate_id
from core.db.base_service import BaseCrudService


class ModuleService(BaseCrudService):
    model_class = SysModule
    vo_class = ModuleVO
    dao_class = ModuleDao
    page_param_class = ModulePageParam
    export_name = "模块数据"


class ResourceService(BaseCrudService):
    model_class = SysResource
    vo_class = ResourceVO
    dao_class = ResourceDao
    page_param_class = ResourcePageParam
    export_name = "资源数据"

    def tree(self) -> list:
        records = self.dao.find_all()
        records.sort(key=lambda r: r.sort_code or 0)
        nodes = [ResourceVO.model_validate(r).model_dump() for r in records]
        children_map: dict[str, list] = {}
        for n in nodes:
            pid = n.get("parent_id") or ""
            children_map.setdefault(pid, []).append(n)

        def build(pid: str) -> list:
            result = []
            for n in children_map.get(pid, []):
                n["children"] = build(n["id"])
                result.append(n)
            return result

        return build("")

    async def modify(self, vo: ResourceVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        if vo.parent_id is not None and vo.parent_id != entity.parent_id:
            self._check_circular_parent(vo.id, vo.parent_id)

        old_extra = entity.extra
        update_data = {k: getattr(vo, k) for k in vo.model_fields_set if k != 'id'}
        apply_update(entity, update_data)
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

        # If extra.permission_code changed, sync role-permission assignments
        if old_extra != entity.extra:
            import json
            from ..role.models import RelRoleResource, RelRolePermission

            old_code = None
            new_code = None
            try:
                if old_extra:
                    old_code = json.loads(old_extra).get("permission_code")
                if entity.extra:
                    new_code = json.loads(entity.extra).get("permission_code")
            except (json.JSONDecodeError, TypeError):
                pass

            if old_code != new_code:
                role_ids = [
                    r.role_id for r in self.dao.db.query(RelRoleResource)
                    .filter(RelRoleResource.resource_id == entity.id)
                    .all()
                ]
                for role_id in role_ids:
                    if old_code:
                        self.dao.db.query(RelRolePermission).filter(
                            RelRolePermission.role_id == role_id,
                            RelRolePermission.permission_code == old_code
                        ).delete(synchronize_session=False)
                    if new_code:
                        exists = self.dao.db.query(RelRolePermission).filter(
                            RelRolePermission.role_id == role_id,
                            RelRolePermission.permission_code == new_code
                        ).count()
                        if not exists:
                            rel = RelRolePermission(
                                id=generate_id(), role_id=role_id,
                                permission_code=new_code, scope="ALL",
                            )
                            self.dao.db.add(rel)
                self.dao.db.commit()

    def remove(self, param: IdsParam) -> None:
        from ..role.models import RelRoleResource

        all_ids = self._collect_descendant_ids(param.ids)
        db = self.dao.db

        db.query(RelRoleResource).filter(
            RelRoleResource.resource_id.in_(all_ids)
        ).delete(synchronize_session=False)

        self.dao.delete_by_ids(all_ids)

    def _collect_descendant_ids(self, ids: List[str]) -> List[str]:
        """递归收集所有子资源ID。"""
        from .models import SysResource
        all_ids = set(ids)
        stack = list(ids)
        while stack:
            parent_id = stack.pop()
            children = self.dao.db.query(SysResource).filter(
                SysResource.parent_id == parent_id,
                SysResource.is_deleted == SoftDeleteEnum.NO
            ).all()
            for child in children:
                if child.id not in all_ids:
                    all_ids.add(child.id)
                    stack.append(child.id)
        return list(all_ids)

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
