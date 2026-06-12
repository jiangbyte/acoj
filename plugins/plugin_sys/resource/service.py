"""
Resource/Module service — aligned with hei-gin's service.go.
Explicit field-by-field construction.
"""

from typing import Optional, List
from datetime import datetime
from fastapi import Request
from sqlalchemy.orm import Session
from sqlalchemy import select, delete as sa_delete, update as sa_update
from .models import SysModule, SysResource
from .params import ModuleVO, ResourceVO, ModulePageParam, ResourcePageParam
from .repository import ModuleRepository, ResourceRepository
from sdk.web.exception import BusinessException
from sdk.utils import generate_id
from sdk.auth import HeiAuthTool
from sdk.web.result import page_data
from ..role.models import RelRoleResource, RelRolePermission
import json
import logging

logger = logging.getLogger(__name__)


# ═════════════════════════════════════════════════════════════════════
# Converters
# ═════════════════════════════════════════════════════════════════════

def _module_to_vo(entity: SysModule) -> dict:
    vo = {
        "id": entity.id,
        "code": entity.code,
        "name": entity.name,
        "category": entity.category,
    }
    if entity.icon is not None:
        vo["icon"] = entity.icon
    if entity.color is not None:
        vo["color"] = entity.color
    if entity.description is not None:
        vo["description"] = entity.description
    if entity.is_visible is not None:
        vo["is_visible"] = entity.is_visible
    if entity.status is not None:
        vo["status"] = entity.status
    if entity.sort_code is not None:
        vo["sort_code"] = entity.sort_code
    if entity.created_at is not None:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_by is not None:
        vo["created_by"] = entity.created_by
    if entity.updated_at is not None:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_by is not None:
        vo["updated_by"] = entity.updated_by
    return vo


def _resource_to_vo(entity: SysResource) -> dict:
    vo = {
        "id": entity.id,
        "code": entity.code,
        "name": entity.name,
        "category": entity.category,
        "type": entity.type,
        "route_path": entity.route_path,
        "component_path": entity.component_path,
        "redirect_path": entity.redirect_path,
        "icon": entity.icon,
        "color": entity.color,
        "is_visible": entity.is_visible,
        "is_cache": entity.is_cache,
        "is_affix": entity.is_affix,
        "is_breadcrumb": entity.is_breadcrumb,
        "external_url": entity.external_url,
        "sort_code": entity.sort_code,
        "status": entity.status,
    }
    if entity.parent_id is not None:
        vo["parent_id"] = entity.parent_id
    else:
        vo["parent_id"] = None
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


def _extract_perm_code(extra: Optional[str]) -> Optional[str]:
    if not extra:
        return None
    try:
        return json.loads(extra).get("permission_code")
    except (json.JSONDecodeError, TypeError):
        return None


def _sync_perm(db: Session, resource_id: str, old_extra: Optional[str], new_extra: Optional[str]) -> None:
    old_code = _extract_perm_code(old_extra)
    new_code = _extract_perm_code(new_extra)
    if old_code == new_code:
        return

    role_ids = list(
        db.execute(
            select(RelRoleResource.role_id).where(RelRoleResource.resource_id == resource_id)
        ).scalars().all()
    )
    if not role_ids:
        return

    if old_code:
        db.execute(
            sa_delete(RelRolePermission).where(
                RelRolePermission.role_id.in_(role_ids),
                RelRolePermission.permission_code == old_code,
            )
        )
    if new_code:
        existing_role_ids = set(
            db.execute(
                select(RelRolePermission.role_id).where(
                    RelRolePermission.role_id.in_(role_ids),
                    RelRolePermission.permission_code == new_code,
                )
            ).scalars().all()
        )
        for rid in role_ids:
            if rid not in existing_role_ids:
                rel = RelRolePermission(
                    id=generate_id(), role_id=rid,
                    permission_code=new_code, scope="ALL",
                )
                db.add(rel)
    db.commit()


# ═════════════════════════════════════════════════════════════════════
# Module functions
# ═════════════════════════════════════════════════════════════════════

def module_page(db: Session, param: ModulePageParam) -> dict:
    result = ModuleRepository(db).find_page(param)
    records = [_module_to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result["total"], page=param.current, size=param.size)


def module_detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = ModuleRepository(db).find_by_id(id)
    if not entity:
        return None
    return _module_to_vo(entity)


def module_create(db: Session, vo: ModuleVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysModule(
        id=generate_id(),
        code=vo.code,
        name=vo.name,
        category=vo.category,
        created_at=now,
        updated_at=now,
    )
    if vo.icon is not None:
        entity.icon = vo.icon
    if vo.color is not None:
        entity.color = vo.color
    if vo.description is not None:
        entity.description = vo.description
    if vo.is_visible is not None:
        entity.is_visible = vo.is_visible
    if vo.status is not None:
        entity.status = vo.status
    if vo.sort_code is not None:
        entity.sort_code = vo.sort_code
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    ModuleRepository(db).insert(entity)


def module_modify(db: Session, vo: ModuleVO, user_id: Optional[str] = None) -> None:
    repository = ModuleRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")

    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "sort_code": vo.sort_code if vo.sort_code is not None else 0,
        "updated_at": datetime.now(),
    }
    if vo.icon is not None:
        up["icon"] = vo.icon
    else:
        up["icon"] = None
    if vo.color is not None:
        up["color"] = vo.color
    else:
        up["color"] = None
    if vo.description is not None:
        up["description"] = vo.description
    else:
        up["description"] = None
    if vo.is_visible is not None:
        up["is_visible"] = vo.is_visible
    if vo.status is not None:
        up["status"] = vo.status
    if user_id:
        up["updated_by"] = user_id

    repository.db.execute(sa_update(SysModule).where(SysModule.id == vo.id).values(**up))
    repository.db.commit()


def module_remove(db: Session, ids: list) -> None:
    if not ids:
        return
    ModuleRepository(db).delete_by_ids(ids)


# ═════════════════════════════════════════════════════════════════════
# Resource functions
# ═════════════════════════════════════════════════════════════════════

def resource_page(db: Session, param: ResourcePageParam) -> dict:
    result = ResourceRepository(db).find_page(param)
    records = [_resource_to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result["total"], page=param.current, size=param.size)


def resource_detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = ResourceRepository(db).find_by_id(id)
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
    if vo.description is not None:
        entity.description = vo.description
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
    if vo.extra is not None:
        entity.extra = vo.extra
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    ResourceRepository(db).insert(entity)


def resource_modify(db: Session, vo: ResourceVO, user_id: Optional[str] = None) -> None:
    repository = ResourceRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")

    old_extra = entity.extra
    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "type": vo.type,
        "sort_code": vo.sort_code if vo.sort_code is not None else 0,
        "updated_at": datetime.now(),
    }
    if vo.parent_id is not None:
        up["parent_id"] = vo.parent_id if vo.parent_id not in ("", "0") else None
    else:
        up["parent_id"] = None
    if vo.description is not None:
        up["description"] = vo.description
    else:
        up["description"] = None
    if vo.route_path is not None:
        up["route_path"] = vo.route_path
    else:
        up["route_path"] = None
    if vo.component_path is not None:
        up["component_path"] = vo.component_path
    else:
        up["component_path"] = None
    if vo.redirect_path is not None:
        up["redirect_path"] = vo.redirect_path
    else:
        up["redirect_path"] = None
    if vo.icon is not None:
        up["icon"] = vo.icon
    else:
        up["icon"] = None
    if vo.color is not None:
        up["color"] = vo.color
    else:
        up["color"] = None
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
    else:
        up["external_url"] = None
    if vo.extra is not None:
        up["extra"] = vo.extra
    else:
        up["extra"] = None
    if vo.status is not None:
        up["status"] = vo.status
    if user_id:
        up["updated_by"] = user_id

    repository.db.execute(sa_update(SysResource).where(SysResource.id == vo.id).values(**up))
    repository.db.commit()

    # Sync permission code change
    if vo.extra is not None or old_extra is not None:
        _sync_perm(db, vo.id, old_extra, vo.extra)


def resource_remove(db: Session, ids: list) -> None:
    if not ids:
        return
    all_ids = _collect_descendant_ids(db, ids)

    # Collect role IDs before deleting relations
    role_ids = list(
        db.execute(
            select(RelRoleResource.role_id).where(RelRoleResource.resource_id.in_(all_ids))
        ).scalars().all()
    )
    # Delete role-permission relations for affected roles
    if role_ids:
        db.execute(
            sa_delete(RelRolePermission).where(RelRolePermission.role_id.in_(role_ids))
        )
    # Delete role-resource relations
    db.execute(sa_delete(RelRoleResource).where(RelRoleResource.resource_id.in_(all_ids)))
    # Delete resources
    ResourceRepository(db).delete_by_ids(all_ids)


def resource_tree(db: Session) -> list:
    rows = db.execute(
        select(SysResource).order_by(SysResource.sort_code.asc())
    ).scalars().all()
    nodes = [_resource_to_vo(r) for r in rows]
    children_map = {}
    for n in nodes:
        pid = n.get("parent_id") or ""
        children_map.setdefault(pid, []).append(n)

    def build(pid: str, depth: int = 0) -> list:
        if depth > 50:
            return []
        result = []
        for n in children_map.get(pid, []):
            n["children"] = build(n["id"], depth + 1)
            result.append(n)
        return result

    return build("")


def resource_menu(db: Session) -> list:
    """Build menu tree for frontend rendering (aligned with hei-gin's ResourceMenu)."""
    rows = db.execute(
        select(SysResource).order_by(SysResource.sort_code.asc())
    ).scalars().all()

    children_map = {}
    for r in rows:
        pid = ""
        if r.parent_id and r.parent_id not in ("", "0"):
            pid = r.parent_id
        children_map.setdefault(pid, []).append(r)

    def _menu_node(r: SysResource) -> dict:
        n = {
            "id": r.id,
            "code": r.code,
            "name": r.name,
            "type": r.type,
            "category": r.category,
            "route_path": r.route_path,
            "component_path": r.component_path,
            "redirect_path": r.redirect_path,
            "icon": r.icon,
            "color": r.color,
            "is_visible": r.is_visible,
            "is_cache": r.is_cache,
            "is_affix": r.is_affix,
            "is_breadcrumb": r.is_breadcrumb,
            "external_url": r.external_url,
            "sort_code": r.sort_code,
            "status": r.status,
        }
        if r.parent_id is not None:
            n["parent_id"] = r.parent_id
        else:
            n["parent_id"] = None
        if r.description is not None:
            n["description"] = r.description
        return n

    def _build_tree(cm: dict, pid: str, depth: int = 0) -> list:
        if depth > 50:
            return []
        cs = cm.get(pid, [])
        if not cs:
            return []
        result = []
        for c in cs:
            n = _menu_node(c)
            n["children"] = _build_tree(cm, c.id, depth + 1)
            result.append(n)
        return result

    roots = children_map.get("", [])
    result = []
    for rt in roots:
        n = _menu_node(rt)
        n["children"] = _build_tree(children_map, rt.id, 0)
        result.append(n)
    return result


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible classes
# ═════════════════════════════════════════════════════════════════════

class ModuleService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = ModuleRepository(db)

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
        self.repository = ResourceRepository(db)

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

    def menu(self) -> list:
        return resource_menu(self.db)
