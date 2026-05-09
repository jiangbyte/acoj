from typing import List
from sqlalchemy.orm import Session
from .models import ClientUser
from core.db.base_dao import BaseDAO
from core.utils import generate_id
from datetime import datetime


class ClientUserDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, ClientUser)

    def insert(self, entity: ClientUser) -> ClientUser:
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

    def insert_batch(self, entities: List[ClientUser]) -> None:
        now = datetime.now()
        for entity in entities:
            entity.id = generate_id()
            if self._can_apply_soft_delete():
                setattr(entity, self._soft_delete_field, self._soft_delete_not_deleted)
            entity.created_at = now
            entity.updated_at = now
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: ClientUser) -> ClientUser:
        entity.updated_at = datetime.now()
        self.db.commit()
        self.db.refresh(entity)
        return entity
