"""
Role service — standalone functions mirroring hei-gin's service.go pattern.
Explicit field-by-field construction.
"""

from __future__ import annotations

import json
import logging
from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select, func, delete as sa_delete
from fastapi import Request
from .models import SysRole
from .params import (
    RoleVO, RolePageParam, GrantPermissionParam, GrantResourceParam,
    ButtonPermissionScope, PermissionItem, SysRoleToRoleVO,
)
from .repository import RoleRepository
from sdk.enums import DataScopeEnum
from sdk.shared.types import IdParam, IdsParam
from sdk.web.result import page_data, PageDataField
from sdk.web.exception import BusinessException
from sdk.utils import generate_id
from sdk.auth import HeiAuthTool
from ..user.models import RelUserRole
from .models import RelRolePermission, RelRoleResource

logger = logging.getLogger(__name__)
# ── Standalone service functions ──

async def _get_cur_user_id(request: Optional[Request] = None) -> Optional[str]:
    try:
        return await HeiAuthTool.getLoginIdDefaultNull(request)
    except Exception:
        return None


def role_page(db: Session, param: RolePageParam) -> dict:
    repository = RoleRepository(db)
    result = repository.find_page(param)
    records = [SysRoleToRoleVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def role_detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = RoleRepository(db).find_by_id(id)
    if not entity:
        return None
    return SysRoleToRoleVO(entity)


def role_create(db: Session, vo: RoleVO, user_id: Optional[str] = None) -> None:
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
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    RoleRepository(db).insert(entity)


def role_modify(db: Session, vo: RoleVO, user_id: Optional[str] = None) -> None:
    if not vo.id:
        raise BusinessException("ID不能为空", 400)
    repository = RoleRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up: dict = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "sort_code": vo.sort_code,
        "updated_at": now,
    }
    if vo.description is not None:
        up["description"] = vo.description
    if vo.status:
        up["status"] = vo.status
    if vo.extra is not None:
        up["extra"] = vo.extra
    if user_id:
        up["updated_by"] = user_id
    repository.db.execute(sa_update(SysRole).where(SysRole.id == vo.id).values(**up))
    repository.db.commit()


def role_remove(db: Session, ids: list) -> None:
    if not ids:
        return
    cnt = db.execute(select(func.count()).select_from(RelUserRole).where(RelUserRole.role_id.in_(ids))).scalar() or 0
    if cnt > 0:
        raise BusinessException("角色存在关联用户，无法删除")
    try:
        db.execute(sa_delete(RelRolePermission).where(RelRolePermission.role_id.in_(ids)))
        db.execute(sa_delete(RelRoleResource).where(RelRoleResource.role_id.in_(ids)))
        db.execute(sa_delete(RelUserRole).where(RelUserRole.role_id.in_(ids)))
        db.execute(sa_delete(SysRole).where(SysRole.id.in_(ids)))
        db.commit()
    except Exception:
        db.rollback()
        raise


def role_grant_permissions(db: Session, role_id: str, permissions: list, created_by: Optional[str] = None) -> None:
    RoleRepository(db).grant_permissions(role_id, permissions, created_by)


def role_grant_resources(db: Session, role_id: str, resource_ids: list, permissions: list[ButtonPermissionScope], created_by: Optional[str] = None) -> None:
    repository = RoleRepository(db)
    repository.grant_resources(role_id, resource_ids, created_by)

    resources = repository.find_resources_with_extra_by_ids(resource_ids)
    scope_map = {p.permission_code: p for p in permissions}
    permission_items = []
    for r in resources:
        try:
            extra = json.loads(r.extra)
            pcode = extra.get("permission_code")
            if not pcode:
                continue
            scope = scope_map.get(pcode)
            permission_items.append(PermissionItem(
                permission_code=pcode,
                scope=scope.scope if scope else DataScopeEnum.ALL.value,
                custom_scope_group_ids=scope.custom_scope_group_ids if scope else None,
                custom_scope_org_ids=scope.custom_scope_org_ids if scope else None,
            ))
        except (json.JSONDecodeError, TypeError):
            continue

    if permission_items:
        seen = set()
        unique_items = []
        for item in permission_items:
            if item.permission_code not in seen:
                seen.add(item.permission_code)
                unique_items.append(item)
        repository.add_missing_permissions(role_id, unique_items)


def role_permission_codes(db: Session, role_id: str) -> List[str]:
    return RoleRepository(db).get_permission_codes_by_role_id(role_id)


def role_permission_details(db: Session, role_id: str) -> list[dict]:
    return RoleRepository(db).get_permission_details_by_role_id(role_id)


def role_resource_ids(db: Session, role_id: str) -> List[str]:
    return RoleRepository(db).get_resource_ids_by_role_id(role_id)


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class
# ═════════════════════════════════════════════════════════════════════

class RoleService:
    def __init__(self, db: Session):
        self.repository = RoleRepository(db)
        self.db = db

    def page(self, param: RolePageParam) -> dict:
        return role_page(self.db, param)

    def detail(self, id: str):
        return role_detail(self.db, id)

    async def create(self, vo: RoleVO, request: Optional[Request] = None) -> None:
        return role_create(self.db, vo, await _get_cur_user_id(request))

    async def modify(self, vo: RoleVO, request: Optional[Request] = None) -> None:
        return role_modify(self.db, vo, await _get_cur_user_id(request))

    def remove(self, ids: list) -> None:
        return role_remove(self.db, ids)

    async def grant_permissions(self, role_id: str, permissions: list, request: Optional[Request] = None) -> None:
        return role_grant_permissions(self.db, role_id, permissions, await _get_cur_user_id(request))

    async def grant_resources(self, role_id: str, resource_ids: list, permissions: list, request: Optional[Request] = None) -> None:
        return role_grant_resources(self.db, role_id, resource_ids, permissions, await _get_cur_user_id(request))

    def get_role_permission_codes(self, role_id: str) -> List[str]:
        return role_permission_codes(self.db, role_id)

    def get_role_permission_details(self, role_id: str) -> list[dict]:
        return role_permission_details(self.db, role_id)

    def get_role_resource_ids(self, role_id: str) -> List[str]:
        return role_resource_ids(self.db, role_id)
