"""Resource and module service."""

from datetime import datetime
import json
from typing import List, Optional

from fastapi import Depends
from sqlalchemy import delete as sa_delete
from sqlalchemy import select, update as sa_update
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import page_data

from ..role.models import RelRolePermission, RelRoleResource
from .models import SysModule, SysResource
from .params import ModulePageParam, ModuleVO, ResourcePageParam, ResourceVO
from .repository import ModuleRepository, ResourceRepository


def _module_to_vo(entity: SysModule) -> dict:
    data = {
        "id": entity.id,
        "code": entity.code,
        "name": entity.name,
        "category": entity.category,
    }
    if entity.icon is not None:
        data["icon"] = entity.icon
    if entity.color is not None:
        data["color"] = entity.color
    if entity.description is not None:
        data["description"] = entity.description
    if entity.is_visible is not None:
        data["is_visible"] = entity.is_visible
    if entity.status is not None:
        data["status"] = entity.status
    if entity.sort_code is not None:
        data["sort_code"] = entity.sort_code
    if entity.created_at is not None:
        data["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_by is not None:
        data["created_by"] = entity.created_by
    if entity.updated_at is not None:
        data["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_by is not None:
        data["updated_by"] = entity.updated_by
    return data


def _resource_to_vo(entity: SysResource) -> dict:
    data = {
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
        "parent_id": entity.parent_id if entity.parent_id is not None else None,
    }
    if entity.description is not None:
        data["description"] = entity.description
    if entity.extra is not None:
        data["extra"] = entity.extra
    if entity.created_at is not None:
        data["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_by is not None:
        data["created_by"] = entity.created_by
    if entity.updated_at is not None:
        data["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_by is not None:
        data["updated_by"] = entity.updated_by
    return data


def _extract_perm_code(extra: Optional[str]) -> Optional[str]:
    if not extra:
        return None
    try:
        return json.loads(extra).get("permission_code")
    except (json.JSONDecodeError, TypeError):
        return None


class ModuleService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, ModuleRepository):
            self.repository = repository_or_db
        else:
            self.repository = ModuleRepository(repository_or_db)
        self.db = self.repository.db

    @classmethod
    def from_db(cls, db: Session) -> "ModuleService":
        return cls(ModuleRepository(db))

    def page(self, param: ModulePageParam) -> dict:
        result = self.repository.find_page(param)
        records = [_module_to_vo(row) for row in result.get("records", [])]
        return page_data(records=records, total=result["total"], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return _module_to_vo(entity)

    def create(self, vo: ModuleVO, actor: Optional[ActorContext] = None) -> None:
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
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: ModuleVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "sort_code": vo.sort_code if vo.sort_code is not None else 0,
            "updated_at": datetime.now(),
            "icon": vo.icon if vo.icon is not None else None,
            "color": vo.color if vo.color is not None else None,
            "description": vo.description if vo.description is not None else None,
        }
        if vo.is_visible is not None:
            updates["is_visible"] = vo.is_visible
        if vo.status is not None:
            updates["status"] = vo.status
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id

        self.db.execute(sa_update(SysModule).where(SysModule.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list) -> None:
        if not ids:
            return
        self.repository.delete_by_ids(ids)


class ResourceService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, ResourceRepository):
            self.repository = repository_or_db
        else:
            self.repository = ResourceRepository(repository_or_db)
        self.db = self.repository.db

    @classmethod
    def from_db(cls, db: Session) -> "ResourceService":
        return cls(ResourceRepository(db))

    def _collect_descendant_ids(self, ids: List[str]) -> List[str]:
        all_rows = self.db.execute(select(SysResource)).scalars().all()
        children_map: dict[str, list[str]] = {}
        for row in all_rows:
            children_map.setdefault(row.parent_id or "", []).append(row.id)
        all_ids = set(ids)
        stack = list(ids)
        while stack:
            parent_id = stack.pop()
            for child_id in children_map.get(parent_id, []):
                if child_id not in all_ids:
                    all_ids.add(child_id)
                    stack.append(child_id)
        return list(all_ids)

    def _sync_perm(self, resource_id: str, old_extra: Optional[str], new_extra: Optional[str]) -> None:
        old_code = _extract_perm_code(old_extra)
        new_code = _extract_perm_code(new_extra)
        if old_code == new_code:
            return

        role_ids = list(
            self.db.execute(
                select(RelRoleResource.role_id).where(RelRoleResource.resource_id == resource_id)
            ).scalars().all()
        )
        if not role_ids:
            return

        if old_code:
            self.db.execute(
                sa_delete(RelRolePermission).where(
                    RelRolePermission.role_id.in_(role_ids),
                    RelRolePermission.permission_code == old_code,
                )
            )

        if new_code:
            existing_role_ids = set(
                self.db.execute(
                    select(RelRolePermission.role_id).where(
                        RelRolePermission.role_id.in_(role_ids),
                        RelRolePermission.permission_code == new_code,
                    )
                ).scalars().all()
            )
            for role_id in role_ids:
                if role_id in existing_role_ids:
                    continue
                self.db.add(
                    RelRolePermission(
                        id=generate_id(),
                        role_id=role_id,
                        permission_code=new_code,
                        scope="ALL",
                    )
                )
        self.db.commit()

    def page(self, param: ResourcePageParam) -> dict:
        result = self.repository.find_page(param)
        records = [_resource_to_vo(row) for row in result.get("records", [])]
        return page_data(records=records, total=result["total"], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return _resource_to_vo(entity)

    def create(self, vo: ResourceVO, actor: Optional[ActorContext] = None) -> None:
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
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: ResourceVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        old_extra = entity.extra
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "type": vo.type,
            "sort_code": vo.sort_code if vo.sort_code is not None else 0,
            "updated_at": datetime.now(),
            "parent_id": vo.parent_id if vo.parent_id not in ("", "0") else None,
            "description": vo.description if vo.description is not None else None,
            "route_path": vo.route_path if vo.route_path is not None else None,
            "component_path": vo.component_path if vo.component_path is not None else None,
            "redirect_path": vo.redirect_path if vo.redirect_path is not None else None,
            "icon": vo.icon if vo.icon is not None else None,
            "color": vo.color if vo.color is not None else None,
            "external_url": vo.external_url if vo.external_url is not None else None,
            "extra": vo.extra if vo.extra is not None else None,
        }
        if vo.is_visible is not None:
            updates["is_visible"] = vo.is_visible
        if vo.is_cache is not None:
            updates["is_cache"] = vo.is_cache
        if vo.is_affix is not None:
            updates["is_affix"] = vo.is_affix
        if vo.is_breadcrumb is not None:
            updates["is_breadcrumb"] = vo.is_breadcrumb
        if vo.status is not None:
            updates["status"] = vo.status
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id

        self.db.execute(sa_update(SysResource).where(SysResource.id == vo.id).values(**updates))
        self.db.commit()

        if vo.extra is not None or old_extra is not None:
            self._sync_perm(vo.id, old_extra, vo.extra)

    def remove(self, ids: list) -> None:
        if not ids:
            return

        all_ids = self._collect_descendant_ids(ids)
        role_ids = list(
            self.db.execute(
                select(RelRoleResource.role_id).where(RelRoleResource.resource_id.in_(all_ids))
            ).scalars().all()
        )
        if role_ids:
            self.db.execute(sa_delete(RelRolePermission).where(RelRolePermission.role_id.in_(role_ids)))
        self.db.execute(sa_delete(RelRoleResource).where(RelRoleResource.resource_id.in_(all_ids)))
        self.repository.delete_by_ids(all_ids)

    def tree(self) -> list:
        rows = self.db.execute(select(SysResource).order_by(SysResource.sort_code.asc())).scalars().all()
        nodes = [_resource_to_vo(row) for row in rows]
        children_map: dict[str, list[dict]] = {}
        for node in nodes:
            children_map.setdefault(node.get("parent_id") or "", []).append(node)

        def build(parent_id: str, depth: int = 0) -> list:
            if depth > 50:
                return []
            result = []
            for node in children_map.get(parent_id, []):
                node["children"] = build(node["id"], depth + 1)
                result.append(node)
            return result

        return build("")

    def menu(self) -> list:
        rows = self.db.execute(select(SysResource).order_by(SysResource.sort_code.asc())).scalars().all()
        children_map: dict[str, list[SysResource]] = {}
        for row in rows:
            parent_id = row.parent_id if row.parent_id and row.parent_id not in ("", "0") else ""
            children_map.setdefault(parent_id, []).append(row)

        def menu_node(row: SysResource) -> dict:
            data = {
                "id": row.id,
                "code": row.code,
                "name": row.name,
                "type": row.type,
                "category": row.category,
                "route_path": row.route_path,
                "component_path": row.component_path,
                "redirect_path": row.redirect_path,
                "icon": row.icon,
                "color": row.color,
                "is_visible": row.is_visible,
                "is_cache": row.is_cache,
                "is_affix": row.is_affix,
                "is_breadcrumb": row.is_breadcrumb,
                "external_url": row.external_url,
                "sort_code": row.sort_code,
                "status": row.status,
                "parent_id": row.parent_id if row.parent_id is not None else None,
            }
            if row.description is not None:
                data["description"] = row.description
            return data

        def build(parent_id: str, depth: int = 0) -> list:
            if depth > 50:
                return []
            result = []
            for row in children_map.get(parent_id, []):
                node = menu_node(row)
                node["children"] = build(row.id, depth + 1)
                result.append(node)
            return result

        return build("")


def get_module_service(db: Session = Depends(get_db)) -> ModuleService:
    return ModuleService.from_db(db)


def get_resource_service(db: Session = Depends(get_db)) -> ResourceService:
    return ResourceService.from_db(db)
