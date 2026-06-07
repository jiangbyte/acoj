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
    ButtonPermissionScope, PermissionItem,
)
from .dao import RoleDao
from core.enums import DataScopeEnum
from core.pojo import IdParam, IdsParam
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils import generate_id
from core.auth import HeiAuthTool
from ..user.models import RelUserRole
from .models import RelRolePermission, RelRoleResource

logger = logging.getLogger(__name__)


# ── Helpers ──

def _to_vo(entity: SysRole) -> dict:
    vo = {
        "id": entity.id,
        "code": entity.code,
        "name": entity.name,
        "category": entity.category,
        "sort_code": entity.sort_code,
        "status": entity.status,
    }
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


# ── Standalone service functions ──

async def _get_cur_user_id(request: Optional[Request] = None) -> Optional[str]:
    try:
        return await HeiAuthTool.getLoginIdDefaultNull(request)
    except Exception:
        return None


def role_page(db: Session, param: RolePageParam) -> dict:
    dao = RoleDao(db)
    result = dao.find_page(param)
    records = [_to_vo(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def role_detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = RoleDao(db).find_by_id(id)
    if not entity:
        return None
    return _to_vo(entity)


def role_create(db: Session, vo: RoleVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysRole(
        id=generate_id(),
        code=vo.code,
        name=vo.name,
        category=vo.category,
        sort_code=vo.sort_code,
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
    RoleDao(db).insert(entity)


def role_modify(db: Session, vo: RoleVO, user_id: Optional[str] = None) -> None:
    dao = RoleDao(db)
    entity = dao.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {
        "code": vo.code,
        "name": vo.name,
        "category": vo.category,
        "sort_code": vo.sort_code,
        "status": vo.status,
        "updated_at": now,
    }
    if vo.description is not None:
        up["description"] = vo.description
    if vo.extra is not None:
        up["extra"] = vo.extra
    if user_id:
        up["updated_by"] = user_id
    dao.db.execute(sa_update(SysRole).where(SysRole.id == vo.id).values(**up))
    dao.db.commit()


def role_remove(db: Session, ids: list) -> None:
    if not ids:
        return
    dao = RoleDao(db)
    cnt = db.execute(select(func.count()).select_from(RelUserRole).where(RelUserRole.role_id.in_(ids))).scalar() or 0
    if cnt > 0:
        raise BusinessException("角色存在关联用户，无法删除")

    for model in [RelRolePermission, RelRoleResource, RelUserRole]:
        db.execute(sa_delete(model).where(model.role_id.in_(ids)))
    dao.delete_by_ids(ids)


def role_grant_permissions(db: Session, role_id: str, permissions: list, created_by: Optional[str] = None) -> None:
    RoleDao(db).grant_permissions(role_id, permissions, created_by)


def role_grant_resources(db: Session, role_id: str, resource_ids: list, permissions: list[ButtonPermissionScope], created_by: Optional[str] = None) -> None:
    dao = RoleDao(db)
    dao.grant_resources(role_id, resource_ids, created_by)

    resources = dao.find_resources_with_extra_by_ids(resource_ids)
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
        dao.add_missing_permissions(role_id, unique_items)


def role_permission_codes(db: Session, role_id: str) -> List[str]:
    return RoleDao(db).get_permission_codes_by_role_id(role_id)


def role_permission_details(db: Session, role_id: str) -> list[dict]:
    return RoleDao(db).get_permission_details_by_role_id(role_id)


def role_resource_ids(db: Session, role_id: str) -> List[str]:
    return RoleDao(db).get_resource_ids_by_role_id(role_id)


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class
# ═════════════════════════════════════════════════════════════════════

class RoleService:
    def __init__(self, db: Session):
        self.dao = RoleDao(db)
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
