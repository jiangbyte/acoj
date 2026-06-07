"""
Resource/Module service — explicit field-by-field matching Go pattern.
"""

from typing import Optional, List
from datetime import datetime
from fastapi import Request
from sqlalchemy.orm import Session
from sqlalchemy import select, delete as sa_delete, update as sa_update
from .models import SysModule, SysResource
from .params import ModuleVO, ResourceVO, ModulePageParam, ResourcePageParam
from .dao import ModuleDao, ResourceDao
from core.exception import BusinessException
from core.utils import generate_id
from core.auth import HeiAuthTool
from ..role.models import RelRoleResource, RelRolePermission
import json
import logging

logger = logging.getLogger(__name__)


def _module_to_vo(entity: SysModule) -> dict:
    vo = {"id": entity.id, "code": entity.code, "name": entity.name}
    if entity.description is not None:
        vo["description"] = entity.description
    if entity.sort_code is not None:
        vo["sort_code"] = entity.sort_code
    if entity.created_at is not None:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_at is not None:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    return vo


def _resource_to_vo(entity: SysResource) -> dict:
    vo = {
        "id": entity.id,
        "code": entity.code,
        "name": entity.name,
        "category": entity.category,
        "type": entity.type,
        "sort_code": entity.sort_code,
        "status": entity.status,
    }
    if entity.parent_id is not None:
        vo["parent_id"] = entity.parent_id
    if entity.route_path is not None:
        vo["route_path"] = entity.route_path
    if entity.component_path is not None:
        vo["component_path"] = entity.component_path
    if entity.redirect_path is not None:
        vo["redirect_path"] = entity.redirect_path
    if entity.icon is not None:
        vo["icon"] = entity.icon
    if entity.color is not None:
        vo["color"] = entity.color
    if entity.is_visible is not None:
        vo["is_visible"] = entity.is_visible
    if entity.is_cache is not None:
        vo["is_cache"] = entity.is_cache
    if entity.is_affix is not None:
        vo["is_affix"] = entity.is_affix
    if entity.is_breadcrumb is not None:
        vo["is_breadcrumb"] = entity.is_breadcrumb
    if entity.external_url is not None:
        vo["external_url"] = entity.external_url
    if entity.description is not None:
        vo["description"] = entity.description
    if entity.extra is not None:
        vo["extra"] = entity.extra
    if entity.created_at is not None:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_by is not None:
        vo["created_by"] = entity.created_by
    if entity.updated_at is not None:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_by is not None:
        vo["updated_by"] = entity.updated_by
    return vo


def _collect_descendant_ids(db: Session, ids: List[str]) -> List[str]:
    all_rows = db.execute(select(SysResource)).scalars().all()
    children_map = {}
    for r in all_rows:
        pid = r.parent_id or ""
        children_map.setdefault(pid, []).append(r.id)
    all_ids = set(ids)
    stack = list(ids)
    while stack:
        pid = stack.pop()
        for cid in children_map.get(pid, []):
            if cid not in all_ids:
                all_ids.add(cid)
                stack.append(cid)
    return list(all_ids)


# ═════════════════════════════════════════════════════════════════════
# Module functions
# ═════════════════════════════════════════════════════════════════════

def module_page(db: Session, param: ModulePageParam) -> dict:
    from core.result import page_data, PageDataField
    dao = ModuleDao(db)
    result = dao.find_page(param)
    records = [_module_to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def module_detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = ModuleDao(db).find_by_id(id)
    if not entity:
        return None
    return _module_to_vo(entity)


def module_create(db: Session, vo: ModuleVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysModule(
        id=generate_id(),
        code=vo.code,
        name=vo.name,
        created_at=now,
        updated_at=now,
    )
    if vo.description is not None:
        entity.description = vo.description
    if vo.sort_code is not None:
        entity.sort_code = vo.sort_code
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    ModuleDao(db).insert(entity)


def module_modify(db: Session, vo: ModuleVO, user_id: Optional[str] = None) -> None:
    dao = ModuleDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {"code": vo.code, "name": vo.name, "updated_at": now}
    if vo.description is not None:
        up["description"] = vo.description
    if vo.sort_code is not None:
        up["sort_code"] = vo.sort_code
    if user_id:
        up["updated_by"] = user_id
    dao.db.execute(sa_update(SysModule).where(SysModule.id == vo.id).values(**up))
    dao.db.commit()


def module_remove(db: Session, ids: list) -> None:
    if not ids:
        return
    ModuleDao(db).delete_by_ids(ids)


# ═════════════════════════════════════════════════════════════════════
# Resource functions
# ═════════════════════════════════════════════════════════════════════

def resource_page(db: Session, param: ResourcePageParam) -> dict:
    from core.result import page_data, PageDataField
    dao = ResourceDao(db)
    result = dao.find_page(param)
    records = [_resource_to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def resource_detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = ResourceDao(db).find_by_id(id)
    if not entity:
        return None
    return _resource_to_vo(entity)


def resource_create(db: Session, vo: ResourceVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysResource(
        id=generate_id(),
        code=vo.code,
        name=vo.name,
        category=vo.category,
        type=vo.type,
        sort_code=vo.sort_code or 0,
        status=vo.status or "ENABLED",
        created_at=now,
        updated_at=now,
    )
    if vo.parent_id is not None:
        entity.parent_id = vo.parent_id
    if vo.route_path is not None:
        entity.route_path = vo.route_path
    if vo.component_path is not None:
        entity.component_path = vo.component_path
    if vo.redirect_path is not None:
        entity.redirect_path = vo.redirect_path
    if vo.icon is not None:
        entity.icon = vo.icon
    if vo.color is not None:
        entity.color = vo.color
    if vo.is_visible is not None:
        entity.is_visible = vo.is_visible
    if vo.is_cache is not None:
        entity.is_cache = vo.is_cache
    if vo.is_affix is not None:
        entity.is_affix = vo.is_affix
    if vo.is_breadcrumb is not None:
        entity.is_breadcrumb = vo.is_breadcrumb
    if vo.external_url is not None:
        entity.external_url = vo.external_url
    if vo.description is not None:
        entity.description = vo.description
    if vo.extra is not None:
        entity.extra = vo.extra
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    ResourceDao(db).insert(entity)


def resource_modify(db: Session, vo: ResourceVO, user_id: Optional[str] = None) -> None:
    dao = ResourceDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")

    if vo.parent_id is not None and vo.parent_id != entity.parent_id:
        _check_circular_resource_parent(db, vo.id, vo.parent_id)

    old_extra = entity.extra
    now = datetime.now()
    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "type": vo.type,
        "sort_code": vo.sort_code,
        "status": vo.status,
        "updated_at": now,
    }
    if vo.parent_id is not None:
        up["parent_id"] = vo.parent_id if vo.parent_id not in ("", "0") else None
    if vo.route_path is not None:
        up["route_path"] = vo.route_path
    if vo.component_path is not None:
        up["component_path"] = vo.component_path
    if vo.redirect_path is not None:
        up["redirect_path"] = vo.redirect_path
    if vo.icon is not None:
        up["icon"] = vo.icon
    if vo.color is not None:
        up["color"] = vo.color
    if vo.is_visible is not None:
        up["is_visible"] = vo.is_visible
    if vo.is_cache is not None:
        up["is_cache"] = vo.is_cache
    if vo.is_affix is not None:
        up["is_affix"] = vo.is_affix
    if vo.is_breadcrumb is not None:
        up["is_breadcrumb"] = vo.is_breadcrumb
    if vo.external_url is not None:
        up["external_url"] = vo.external_url
    if vo.description is not None:
        up["description"] = vo.description
    if vo.extra is not None:
        up["extra"] = vo.extra
    if user_id:
        up["updated_by"] = user_id

    dao.db.execute(sa_update(SysResource).where(SysResource.id == vo.id).values(**up))
    dao.db.commit()

    # Sync permission code change
    if old_extra != vo.extra:
        old_code = None
        new_code = None
        try:
            if old_extra:
                old_code = json.loads(old_extra).get("permission_code")
            if vo.extra:
                new_code = json.loads(vo.extra).get("permission_code")
        except (json.JSONDecodeError, TypeError):
            pass

        if old_code != new_code:
            role_ids = list(db.execute(
                select(RelRoleResource.role_id).where(RelRoleResource.resource_id == entity.id)
            ).scalars().all())
            if role_ids:
                if old_code:
                    db.execute(sa_delete(RelRolePermission).where(
                        RelRolePermission.role_id.in_(role_ids),
                        RelRolePermission.permission_code == old_code,
                    ))
                if new_code:
                    existing_role_ids = set(db.execute(
                        select(RelRolePermission.role_id).where(
                            RelRolePermission.role_id.in_(role_ids),
                            RelRolePermission.permission_code == new_code,
                        )
                    ).scalars().all())
                    for rid in role_ids:
                        if rid not in existing_role_ids:
                            rel = RelRolePermission(
                                id=generate_id(), role_id=rid,
                                permission_code=new_code, scope="ALL",
                            )
                            db.add(rel)
                db.commit()


def resource_remove(db: Session, ids: list) -> None:
    if not ids:
        return
    all_ids = _collect_descendant_ids(db, ids)
    db.execute(sa_delete(RelRoleResource).where(RelRoleResource.resource_id.in_(all_ids)))
    ResourceDao(db).delete_by_ids(all_ids)


def resource_tree(db: Session) -> list:
    rows = db.execute(select(SysResource).order_by(SysResource.sort_code.asc())).scalars().all()
    nodes = [_resource_to_vo(r) for r in rows]
    children_map = {}
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


def _check_circular_resource_parent(db: Session, entity_id: str, new_parent_id: Optional[str]) -> None:
    if not new_parent_id:
        return
    all_rows = db.execute(select(SysResource)).scalars().all()
    parent_map = {r.id: r.parent_id for r in all_rows}
    current = new_parent_id
    while current:
        if current == entity_id:
            raise BusinessException("父级不能选择自身或子节点")
        current = parent_map.get(current)
        if not current or current == "0":
            break


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible classes
# ═════════════════════════════════════════════════════════════════════

class ModuleService:
    def __init__(self, db: Session):
        self.db = db
        self.dao = ModuleDao(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: ModulePageParam) -> dict:
        return module_page(self.db, param)

    def detail(self, id: str):
        return module_detail(self.db, id)

    async def create(self, vo: ModuleVO, request: Optional[Request] = None) -> None:
        return module_create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: ModuleVO, request: Optional[Request] = None) -> None:
        return module_modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return module_remove(self.db, ids)


class ResourceService:
    def __init__(self, db: Session):
        self.db = db
        self.dao = ResourceDao(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: ResourcePageParam) -> dict:
        return resource_page(self.db, param)

    def detail(self, id: str):
        return resource_detail(self.db, id)

    async def create(self, vo: ResourceVO, request: Optional[Request] = None) -> None:
        return resource_create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: ResourceVO, request: Optional[Request] = None) -> None:
        return resource_modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return resource_remove(self.db, ids)

    def tree(self) -> list:
        return resource_tree(self.db)
