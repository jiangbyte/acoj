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
from sdk.utils.tree_utils import build_tree, collect_descendant_ids
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from ..role.models import RelRolePermission, RelRoleResource
from .models import SysModule, SysResource
from .params import ModulePageParam, ModuleVO, ResourceMenuVO, ResourcePageParam, ResourceVO
from .repository import ModuleRepository, ResourceRepository


def _normalize_parent_id(parent_id: Optional[str]) -> Optional[str]:
    return parent_id if parent_id not in (None, "", "0") else None


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


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

    def page(self, param: ModulePageParam) -> dict:
        return map_page_data(self.repository.find_page(param), ModuleVO.model_validate, param.current, param.size)

    def detail(self, id: str) -> Optional[ModuleVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return ModuleVO.model_validate(entity)

    def create(self, vo: ModuleVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysModule(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category,
            icon=vo.icon,
            color=vo.color,
            description=vo.description,
            created_at=now,
            updated_at=now,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        if vo.is_visible is not None:
            entity.is_visible = vo.is_visible
        if vo.status is not None:
            entity.status = vo.status
        if vo.sort_code is not None:
            entity.sort_code = vo.sort_code
        self.repository.insert(entity)

    def modify(self, vo: ModuleVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        actor_user_id = _actor_user_id(actor)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "sort_code": vo.sort_code if vo.sort_code is not None else 0,
            "updated_at": datetime.now(),
            "icon": vo.icon,
            "color": vo.color,
            "description": vo.description,
        }
        if vo.is_visible is not None:
            updates["is_visible"] = vo.is_visible
        if vo.status is not None:
            updates["status"] = vo.status
        if actor_user_id:
            updates["updated_by"] = actor_user_id

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

    def _collect_descendant_ids(self, ids: List[str]) -> List[str]:
        all_rows = self.db.execute(select(SysResource)).scalars().all()
        return collect_descendant_ids(
            all_rows,
            ids,
            get_id=lambda row: row.id,
            get_parent_id=lambda row: row.parent_id or "",
        )

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
        return map_page_data(self.repository.find_page(param), ResourceVO.model_validate, param.current, param.size)

    def detail(self, id: str) -> Optional[ResourceVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return ResourceVO.model_validate(entity)

    def create(self, vo: ResourceVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
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
            parent_id=_normalize_parent_id(vo.parent_id),
            description=vo.description,
            route_path=vo.route_path,
            component_path=vo.component_path,
            redirect_path=vo.redirect_path,
            icon=vo.icon,
            color=vo.color,
            external_url=vo.external_url,
            extra=vo.extra,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        if vo.is_visible is not None:
            entity.is_visible = vo.is_visible
        if vo.is_cache is not None:
            entity.is_cache = vo.is_cache
        if vo.is_affix is not None:
            entity.is_affix = vo.is_affix
        if vo.is_breadcrumb is not None:
            entity.is_breadcrumb = vo.is_breadcrumb
        self.repository.insert(entity)

    def modify(self, vo: ResourceVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        old_extra = entity.extra
        actor_user_id = _actor_user_id(actor)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "type": vo.type,
            "sort_code": vo.sort_code if vo.sort_code is not None else 0,
            "updated_at": datetime.now(),
            "parent_id": _normalize_parent_id(vo.parent_id),
            "description": vo.description,
            "route_path": vo.route_path,
            "component_path": vo.component_path,
            "redirect_path": vo.redirect_path,
            "icon": vo.icon,
            "color": vo.color,
            "external_url": vo.external_url,
            "extra": vo.extra,
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
        if actor_user_id:
            updates["updated_by"] = actor_user_id

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

    def tree(self) -> list[ResourceVO]:
        rows = self.db.execute(select(SysResource).order_by(SysResource.sort_code.asc())).scalars().all()
        nodes = [ResourceVO.model_validate(row) for row in rows]
        return build_tree(
            nodes,
            get_id=lambda node: node.id or "",
            get_parent_id=lambda node: node.parent_id or "",
            get_children=lambda node: node.children,
            get_sort_code=lambda node: node.sort_code,
        )

    def menu(self) -> list[ResourceMenuVO]:
        rows = self.db.execute(select(SysResource).order_by(SysResource.sort_code.asc())).scalars().all()
        nodes = [ResourceMenuVO.model_validate(row) for row in rows]
        return build_tree(
            nodes,
            get_id=lambda node: node.id or "",
            get_parent_id=lambda node: _normalize_parent_id(node.parent_id) or "",
            get_children=lambda node: node.children,
            get_sort_code=lambda node: node.sort_code,
        )


def get_module_service(db: Session = Depends(get_db)) -> ModuleService:
    return ModuleService(ModuleRepository(db))


def get_resource_service(db: Session = Depends(get_db)) -> ResourceService:
    return ResourceService(ResourceRepository(db))
