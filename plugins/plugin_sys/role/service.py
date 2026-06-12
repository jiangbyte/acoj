"""Role service."""

from __future__ import annotations

import json
from datetime import datetime
from typing import List, Optional

from fastapi import Depends
from sqlalchemy import delete as sa_delete
from sqlalchemy import func, select, update as sa_update
from sqlalchemy.orm import Session

from sdk.enums import DataScopeEnum
from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data

from ..user.models import RelUserRole
from .models import RelRolePermission, RelRoleResource, SysRole
from .params import (
    ButtonPermissionScope,
    PermissionItem,
    GrantPermissionParam,
    GrantResourceParam,
    RolePageParam,
    RoleVO,
    SysRoleToRoleVO,
)
from .repository import RoleRepository


class RoleService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, RoleRepository):
            self.repository = repository_or_db
        else:
            self.repository = RoleRepository(repository_or_db)
        self.db = self.repository.db

    @classmethod
    def from_db(cls, db: Session) -> "RoleService":
        return cls(RoleRepository(db))

    def page(self, param: RolePageParam) -> dict:
        result = self.repository.find_page(param)
        records = [SysRoleToRoleVO(row) for row in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[dict]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return SysRoleToRoleVO(entity)

    def create(self, vo: RoleVO, actor: Optional[ActorContext] = None) -> None:
        if not vo.code or not vo.name or not vo.category:
            raise BusinessException("角色编码、名称、类别不能为空", 400)

        now = datetime.now()
        entity = SysRole(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category,
            sort_code=vo.sort_code or 0,
            status=vo.status or "ENABLED",
            created_at=now,
            updated_at=now,
        )
        if vo.description is not None:
            entity.description = vo.description
        if vo.extra is not None:
            entity.extra = vo.extra
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: RoleVO, actor: Optional[ActorContext] = None) -> None:
        if not vo.id:
            raise BusinessException("ID不能为空", 400)
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
        }
        if vo.description is not None:
            updates["description"] = vo.description
        if vo.status:
            updates["status"] = vo.status
        if vo.extra is not None:
            updates["extra"] = vo.extra
        if actor and actor.user_id:
            updates["updated_by"] = actor.user_id
        self.db.execute(sa_update(SysRole).where(SysRole.id == vo.id).values(**updates))
        self.db.commit()

    def remove(self, ids: list) -> None:
        if not ids:
            return
        count = self.db.execute(
            select(func.count()).select_from(RelUserRole).where(RelUserRole.role_id.in_(ids))
        ).scalar() or 0
        if count > 0:
            raise BusinessException("角色存在关联用户，无法删除")
        try:
            self.db.execute(sa_delete(RelRolePermission).where(RelRolePermission.role_id.in_(ids)))
            self.db.execute(sa_delete(RelRoleResource).where(RelRoleResource.role_id.in_(ids)))
            self.db.execute(sa_delete(RelUserRole).where(RelUserRole.role_id.in_(ids)))
            self.db.execute(sa_delete(SysRole).where(SysRole.id.in_(ids)))
            self.db.commit()
        except Exception:
            self.db.rollback()
            raise

    def grant_permissions(
        self,
        role_id: str,
        permissions: list,
        actor: Optional[ActorContext] = None,
    ) -> None:
        self.repository.grant_permissions(role_id, permissions, actor.user_id if actor and actor.user_id else None)

    def grant_resources(
        self,
        role_id: str,
        resource_ids: list,
        permissions: list[ButtonPermissionScope],
        actor: Optional[ActorContext] = None,
    ) -> None:
        created_by = actor.user_id if actor and actor.user_id else None
        self.repository.grant_resources(role_id, resource_ids, created_by)

        resources = self.repository.find_resources_with_extra_by_ids(resource_ids)
        scope_map = {item.permission_code: item for item in permissions}
        permission_items = []
        for resource in resources:
            try:
                extra = json.loads(resource.extra)
                permission_code = extra.get("permission_code")
                if not permission_code:
                    continue
                scope = scope_map.get(permission_code)
                permission_items.append(
                    PermissionItem(
                        permission_code=permission_code,
                        scope=scope.scope if scope else DataScopeEnum.ALL.value,
                        custom_scope_group_ids=scope.custom_scope_group_ids if scope else None,
                        custom_scope_org_ids=scope.custom_scope_org_ids if scope else None,
                    )
                )
            except (json.JSONDecodeError, TypeError):
                continue

        if not permission_items:
            return

        seen = set()
        unique_items = []
        for item in permission_items:
            if item.permission_code in seen:
                continue
            seen.add(item.permission_code)
            unique_items.append(item)
        self.repository.add_missing_permissions(role_id, unique_items)

    def get_role_permission_codes(self, role_id: str) -> List[str]:
        return self.repository.get_permission_codes_by_role_id(role_id)

    def get_role_permission_details(self, role_id: str) -> list[dict]:
        return self.repository.get_permission_details_by_role_id(role_id)

    def get_role_resource_ids(self, role_id: str) -> List[str]:
        return self.repository.get_resource_ids_by_role_id(role_id)


def get_role_service(db: Session = Depends(get_db)) -> RoleService:
    return RoleService.from_db(db)
