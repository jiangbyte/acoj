from typing import Optional, List
from sqlalchemy.orm import Session
from fastapi import Request
from .models import SysPermission
from .params import PermissionVO, PermissionPageParam, PermissionExportParam, PermissionImportParam
from .dao import PermissionDao
from core.pojo import IdParam, IdsParam
from core.result import page_data
from core.exception import BusinessException
from core.enums import ExportTypeEnum, SoftDeleteEnum
from core.utils import export_excel, strip_system_fields, apply_update, make_template, generate_id
from core.auth import HeiAuthTool
import logging

logger = logging.getLogger(__name__)


class PermissionService:
    def __init__(self, db: Session):
        self.dao = PermissionDao(db)
        self.db = db

    async def _get_current_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception as e:
            logger.warning(f"Failed to get current user: {e}")
            return None

    def page(self, param: PermissionPageParam) -> dict:
        from sqlalchemy import select, func
        base_filters = [SysPermission.is_deleted == SoftDeleteEnum.NO]

        if param.keyword:
            keyword = f"%{param.keyword}%"
            base_filters.append(
                SysPermission.code.ilike(keyword) | SysPermission.name.ilike(keyword)
            )
        if param.module:
            base_filters.append(SysPermission.module == param.module)

        count_query = select(func.count()).select_from(SysPermission).where(*base_filters)
        total = self.db.execute(count_query).scalar() or 0

        offset = (param.current - 1) * param.size
        query = select(SysPermission).where(*base_filters).order_by(SysPermission.sort_code).offset(offset).limit(param.size)
        records = [PermissionVO.model_validate(r).model_dump() for r in self.db.execute(query).scalars().all()]
        return page_data(
            records=records,
            total=total,
            page=param.current,
            size=param.size
        )

    async def create(self, vo: PermissionVO, request: Optional[Request] = None) -> None:
        created_by = await self._get_current_user_id(request)
        entity = SysPermission(**strip_system_fields(vo.model_dump()))
        entity.created_by = created_by
        self.dao.insert(entity)

    async def modify(self, vo: PermissionVO, request: Optional[Request] = None) -> None:
        updated_by = await self._get_current_user_id(request)
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        update_data = vo.model_dump(exclude_unset=True)
        apply_update(entity, update_data, extra_protected={'code'})
        entity.updated_by = updated_by
        self.dao.update(entity)

    def remove(self, param: IdsParam) -> None:
        self.dao.delete_by_ids(param.ids)

    def detail(self, param: IdParam) -> Optional[PermissionVO]:
        entity = self.dao.find_by_id(param.id)
        return PermissionVO.model_validate(entity) if entity else None

    def export(self, param: PermissionExportParam):
        records: List[SysPermission] = []
        if param.export_type == ExportTypeEnum.CURRENT.value:
            result = self.dao.find_page(param.current or 1, param.size or 10)
            records = result["records"]
        elif param.export_type == ExportTypeEnum.SELECTED.value:
            records = self.dao.find_by_ids(param.selected_id or [])
        elif param.export_type == ExportTypeEnum.ALL.value:
            records = self.dao.find_all()
        else:
            raise BusinessException("导出类型错误")

        data = [PermissionVO.model_validate(r).model_dump() for r in records]
        return export_excel(data, "权限数据", "权限数据")

    def download_template(self):
        return export_excel(make_template(SysPermission), "权限导入模板", "权限数据")

    async def import_data(self, param: PermissionImportParam, request: Optional[Request] = None) -> dict:
        if not param.data:
            raise BusinessException("导入数据不能为空")
        created_by = await self._get_current_user_id(request)
        entities = []
        for vo in param.data:
            entity = SysPermission(**strip_system_fields(vo.model_dump()))
            entity.created_by = created_by
            entities.append(entity)
        self.dao.insert_batch(entities)
        return {"total": len(entities), "message": f"成功导入{len(entities)}条数据"}

    # ---- Permission group/module discovery (from Redis) ----
    async def list_modules(self) -> List[str]:
        """Get distinct permission module prefixes from Redis cache."""
        from core.auth.permission_scan import get_modules_from_redis
        return await get_modules_from_redis()

    async def list_permissions_by_module(self, module: str) -> List[dict]:
        """Get all permissions under a module from Redis cache."""
        from core.auth.permission_scan import get_permissions_by_module_from_redis
        return await get_permissions_by_module_from_redis(module)
