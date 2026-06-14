"""Role service."""

from __future__ import annotations

import json
from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy import delete as sa_delete
from sqlalchemy import func, select, update as sa_update
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.auth import Business
from sdk.auth.enums import DataScope
from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from ..user.models import RelUserRole
from .models import RelRolePermission, RelRoleResource, SysRole
from .params import (
    ButtonPermissionScope,
    PermissionItem,
    RefreshRoleSessionACLParam,
    RolePageParam,
    RoleVO,
)
from .repository import RoleRepository


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


class RoleService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, RoleRepository):
            self.repository = repository_or_db
        else:
            self.repository = RoleRepository(repository_or_db)
        self.db = self.repository.db

    async def page(self, param: RolePageParam) -> dict:
        param.current = max(1, param.current)
        param.size = max(1, param.size)
        if param.size > 100:
            param.size = 100
        return map_page_data(await self.repository.find_page(param), RoleVO.model_validate, param.current, param.size)

    async def detail(self, id: str) -> Optional[RoleVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return RoleVO.model_validate(entity)

    async def get_permission_codes(self, role_id: str) -> list[str]:
        if not role_id:
            return []
        return await self.repository.get_permission_codes_by_role_id(role_id)

    async def get_permission_details(self, role_id: str) -> list[PermissionItem]:
        if not role_id:
            return []
        return await self.repository.get_permission_details_by_role_id(role_id)

    async def get_resource_ids(self, role_id: str) -> list[str]:
        if not role_id:
            return []
        return await self.repository.get_resource_ids_by_role_id(role_id)

    async def create(self, vo: RoleVO, actor: Optional[ActorContext] = None) -> None:
        if not vo.code or not vo.name or not vo.category:
            raise BusinessException("角色编码、名称、类别不能为空", 400)

        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysRole(
            id=generate_id(),
            code=vo.code,
            name=vo.name,
            category=vo.category,
            sort_code=vo.sort_code or 0,
            status=vo.status or "ENABLED",
            created_at=now,
            updated_at=now,
            description=vo.description,
            extra=vo.extra,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        await self.repository.insert(entity)

    async def modify(self, vo: RoleVO, actor: Optional[ActorContext] = None) -> None:
        if not vo.id:
            raise BusinessException("ID不能为空", 400)
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")

        actor_user_id = _actor_user_id(actor)
        updates = {
            "code": vo.code,
            "name": vo.name,
            "category": vo.category,
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "description": vo.description,
            "extra": vo.extra,
        }
        if vo.status:
            updates["status"] = vo.status
        if actor_user_id:
            updates["updated_by"] = actor_user_id
        await self.db.execute(sa_update(SysRole).where(SysRole.id == vo.id).values(**updates))
        await self.db.commit()

    async def remove(self, ids: list) -> None:
        if not ids:
            return
        count = (await self.db.execute(
            select(func.count()).select_from(RelUserRole).where(RelUserRole.role_id.in_(ids))
        )).scalar() or 0
        if count > 0:
            raise BusinessException("角色存在关联用户，无法删除")
        try:
            await self.db.execute(sa_delete(RelRolePermission).where(RelRolePermission.role_id.in_(ids)))
            await self.db.execute(sa_delete(RelRoleResource).where(RelRoleResource.role_id.in_(ids)))
            await self.db.execute(sa_delete(RelUserRole).where(RelUserRole.role_id.in_(ids)))
            await self.db.execute(sa_delete(SysRole).where(SysRole.id.in_(ids)))
            await self.db.commit()
        except Exception:
            await self.db.rollback()
            raise

    async def grant_permissions(
        self,
        role_id: str,
        permissions: list,
        actor: Optional[ActorContext] = None,
    ) -> None:
        if not role_id:
            raise BusinessException("角色ID不能为空", 400)
        await self.repository.grant_permissions(role_id, permissions, _actor_user_id(actor))
        await self.refresh_session_acl(RefreshRoleSessionACLParam(role_id=role_id))

    async def grant_resources(
        self,
        role_id: str,
        resource_ids: list,
        permissions: list[ButtonPermissionScope],
        actor: Optional[ActorContext] = None,
    ) -> None:
        if not role_id:
            raise BusinessException("角色ID不能为空", 400)
        await self.repository.grant_resources(role_id, resource_ids, _actor_user_id(actor))

        resources = await self.repository.find_resources_with_extra_by_ids(resource_ids)
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
                        scope=scope.scope if scope else DataScope.ALL.value,
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
        await self.repository.add_missing_permissions(role_id, unique_items)
        await self.refresh_session_acl(RefreshRoleSessionACLParam(role_id=role_id))

    async def refresh_session_acl(self, param: RefreshRoleSessionACLParam) -> None:
        if not param.role_id:
            raise BusinessException("角色ID不能为空", 400)
        user_ids = (await self.db.execute(
            select(RelUserRole.user_id).where(RelUserRole.role_id == param.role_id)
        )).scalars().all()
        for user_id in user_ids:
            if user_id:
                await Business.refresh_user_sessions_acl(str(user_id))


def get_role_service(db: AsyncSession = Depends(get_db)) -> RoleService:
    return RoleService(RoleRepository(db))
