from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from .models import SysModule, SysResource
from .params import ModuleVO, ResourceVO, ModulePageParam, ResourcePageParam
from .params import ModuleExportParam, ResourceExportParam, ModuleImportParam, ResourceImportParam
from .dao import ModuleDao, ResourceDao
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class ModuleService:
    def __init__(self, db: Session):
        self.dao = ModuleDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: ModulePageParam) -> dict:
        result = self.dao.find_page(param)
        return page_data(
            records=[ModuleVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]],
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size
        )

    async def create(self, vo: ModuleVO, request: Optional[Request] = None) -> None:
        entity = SysModule(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo: ModuleVO, request: Optional[Request] = None) -> None:
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data)
        self.dao.update(entity, user_id=await self._get_current_user_id(request))

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[ModuleVO]:
        entity = self.dao.find_by_id(param.id)
        return ModuleVO.model_validate(entity) if entity else None

    def export(self, param: ModuleExportParam):
        records: List[SysModule] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            page_param = ModulePageParam(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
            records = result[PageDataField.RECORDS]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [ModuleVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "模块数据", "模块数据")

    def download_template(self):
        return export_excel(make_template(SysModule), "模块导入模板", "模块数据")

    async def import_data(self, param: ModuleImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")
        entities = [SysModule(**strip_system_fields(vo.model_dump())) for vo in param.data]
        self.dao.insert_batch(entities, user_id=await self._get_current_user_id(request))
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}


class ResourceService:
    def __init__(self, db: Session):
        self.dao = ResourceDao(db)

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
            return user_id
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: ResourcePageParam) -> dict:
        result = self.dao.find_page(param)
        return page_data(
            records=[ResourceVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]],
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size
        )

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

    async def create(self, vo: ResourceVO, request: Optional[Request] = None) -> None:
        entity = SysResource(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

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

    def detail(self, param: IdParam) -> Optional[ResourceVO]:
        entity = self.dao.find_by_id(param.id)
        return ResourceVO.model_validate(entity) if entity else None

    def export(self, param: ResourceExportParam):
        records: List[SysResource] = []

        if param.export_type == ExportTypeEnum.CURRENT.value:
            page_param = ResourcePageParam(current=param.current or 1, size=param.size or 10)
            result = self.dao.find_page(page_param)
            records = result[PageDataField.RECORDS]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_ids or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [ResourceVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "资源数据", "资源数据")

    def download_template(self):
        return export_excel(make_template(SysResource), "资源导入模板", "资源数据")

    async def import_data(self, param: ResourceImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")
        entities = [SysResource(**strip_system_fields(vo.model_dump())) for vo in param.data]
        self.dao.insert_batch(entities, user_id=await self._get_current_user_id(request))
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}
