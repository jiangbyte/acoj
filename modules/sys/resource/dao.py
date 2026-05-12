from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import SysModule, SysResource, RalResourcePermission
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime


class ModuleDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysModule)

    def insert(self, entity: SysModule) -> SysModule:
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

    def insert_batch(self, entities: List[SysModule]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysModule) -> SysModule:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity


class ResourceDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysResource)

    def insert(self, entity: SysResource) -> SysResource:
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

    def insert_batch(self, entities: List[SysResource]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: SysResource) -> SysResource:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity

    # ---- RAL: Resource Permissions ----
    def get_permission_ids_by_resource_id(self, resource_id: str) -> List[str]:
        rows = self.db.execute(
            select(RalResourcePermission.permission_id).where(
                RalResourcePermission.resource_id == resource_id,
                RalResourcePermission.is_deleted == self._soft_delete_not_deleted
            )
        ).scalars().all()
        return list(rows)

    def bind_permissions(self, resource_id: str, permission_ids: List[str], created_by: Optional[str] = None):
        now = datetime.now()
        not_del = self._soft_delete_not_deleted
        del_val = self._soft_delete_deleted
        permission_ids = list(dict.fromkeys(permission_ids))

        existing = self.db.execute(
            select(RalResourcePermission).where(RalResourcePermission.resource_id == resource_id)
        ).scalars().all()
        existing_by_pid = {r.permission_id: r for r in existing}

        for r in existing:
            if r.permission_id not in permission_ids and r.is_deleted == not_del:
                r.is_deleted = del_val

        for pid in permission_ids:
            if pid in existing_by_pid:
                rel = existing_by_pid[pid]
                rel.is_deleted = not_del
                rel.created_by = created_by
            else:
                rel = RalResourcePermission(
                    id=generate_id(), resource_id=resource_id, permission_id=pid,
                    is_deleted=not_del, created_at=now, created_by=created_by
                )
                self.db.add(rel)
        self.db.commit()
