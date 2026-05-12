from typing import List, Optional, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import select, or_
from .models import SysUser, RalUserRole, RalUserGroup, RalUserPermission
from .params import UserPageParam
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime
from modules.sys.role.params import PermissionItem


class UserDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysUser)

    def find_page(self, param: UserPageParam) -> Dict[str, Any]:
        def builder(query):
            if param.keyword:
                keyword = f"%{param.keyword}%"
                query = query.where(
                    or_(SysUser.account.ilike(keyword), SysUser.nickname.ilike(keyword))
                )
            if param.status:
                query = query.where(SysUser.status == param.status)
            return query.order_by(SysUser.created_at.desc())
        return super().find_page(param, builder)

    def insert(self, entity: SysUser) -> SysUser:
        entity.id = generate_id()
        if self._can_apply_soft_delete():
            setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
        now = datetime.now()
        entity.created_at = now
        entity.updated_at = now
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def insert_batch(self, entities: List[SysUser]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysUser) -> SysUser:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity

    # ---- RAL: User Roles ----
    def get_role_ids_by_user_id(self, user_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalUserRole.role_id).where(
                RalUserRole.user_id == user_id, RalUserRole.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def grant_roles(self, user_id: str, role_ids: List[str], created_by: Optional[str] = None,
                    scope: Optional[str] = None, custom_scope_group_ids: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        existing = self.db.execute(
            select(RalUserRole).where(RalUserRole.user_id == user_id)
        ).scalars().all()
        existing_by_rid = {r.role_id: r for r in existing}

        for r in existing:
            if r.role_id not in role_ids and r.is_deleted == not_del:
                r.is_deleted = del_val

        for rid in role_ids:
            if rid in existing_by_rid:
                rel = existing_by_rid[rid]
                rel.is_deleted = not_del
                rel.scope = scope
                rel.custom_scope_group_ids = custom_scope_group_ids
                rel.created_by = created_by
            else:
                rel = RalUserRole(
                    id=generate_id(), user_id=user_id, role_id=rid,
                    scope=scope, custom_scope_group_ids=custom_scope_group_ids,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()

    # ---- RAL: User Groups ----
    def get_group_ids_by_user_id(self, user_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalUserGroup.group_id).where(
                RalUserGroup.user_id == user_id, RalUserGroup.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def grant_groups(self, user_id: str, group_ids: List[str], created_by: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        existing = self.db.execute(
            select(RalUserGroup).where(RalUserGroup.user_id == user_id)
        ).scalars().all()
        existing_by_gid = {r.group_id: r for r in existing}

        for r in existing:
            if r.group_id not in group_ids and r.is_deleted == not_del:
                r.is_deleted = del_val

        for gid in group_ids:
            if gid in existing_by_gid:
                rel = existing_by_gid[gid]
                rel.is_deleted = not_del
                rel.created_by = created_by
            else:
                rel = RalUserGroup(
                    id=generate_id(), user_id=user_id, group_id=gid,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()

    # ---- RAL: User Permissions (direct) ----
    def get_permission_details_by_user_id(self, user_id: str) -> list[dict]:
        """Return permission_code + scope + custom IDs for a user."""
        rows = self.db.execute(
            select(
                RalUserPermission.permission_code,
                RalUserPermission.scope,
                RalUserPermission.custom_scope_group_ids,
                RalUserPermission.custom_scope_org_ids,
            ).where(
                RalUserPermission.user_id == user_id,
                RalUserPermission.is_deleted == self._soft_delete_not_deleted
            )
        ).all()
        return [
            {
                "permission_code": r[0],
                "scope": r[1] or "ALL",
                "custom_scope_group_ids": r[2],
                "custom_scope_org_ids": r[3],
            }
            for r in rows
        ]

    def grant_permissions(self, user_id: str, permissions: List[PermissionItem], created_by: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted

        incoming_codes = [p.permission_code for p in permissions]

        existing = self.db.execute(
            select(RalUserPermission).where(RalUserPermission.user_id == user_id)
        ).scalars().all()
        existing_by_code = {r.permission_code: r for r in existing}

        for r in existing:
            if r.permission_code not in incoming_codes and r.is_deleted == not_del:
                r.is_deleted = del_val

        for p in permissions:
            if p.permission_code in existing_by_code:
                rel = existing_by_code[p.permission_code]
                rel.is_deleted = not_del
                rel.scope = p.scope
                rel.custom_scope_group_ids = p.custom_scope_group_ids
                rel.custom_scope_org_ids = p.custom_scope_org_ids
                rel.created_by = created_by
            else:
                rel = RalUserPermission(
                    id=generate_id(), user_id=user_id, permission_code=p.permission_code,
                    scope=p.scope, custom_scope_group_ids=p.custom_scope_group_ids,
                    custom_scope_org_ids=p.custom_scope_org_ids,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()
