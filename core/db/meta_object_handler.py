from typing import Optional
from datetime import datetime


class IdGenerator:
    """Pluggable ID generation strategy.

    Replace with UUID generator, auto-increment, or any custom strategy.
    """

    def generate_id(self) -> str:
        raise NotImplementedError


class DefaultIdGenerator(IdGenerator):
    """Default: Snowflake-based ID generation."""

    def generate_id(self) -> str:
        from core.utils.snowflake_utils import generate_id
        return generate_id()


class MetaObjectHandler:
    """MyBatis-Plus style MetaObjectHandler.

    Plug into BaseDAO to auto-populate entity fields before insert/update.
    Override insert_fill() and update_fill() to customize behavior per DAO.

    Usage:
        class MyHandler(MetaObjectHandler):
            def insert_fill(self, dao, entity):
                entity.id = self.id_generator.generate_id()
                entity.created_at = datetime.now()
                entity.updated_at = datetime.now()

        class MyDao(BaseDAO):
            def __init__(self, db):
                super().__init__(db, MyModel, meta_object_handler=MyHandler())
    """

    def __init__(self, id_generator: Optional[IdGenerator] = None):
        self.id_generator = id_generator or DefaultIdGenerator()

    def insert_fill(self, dao, entity, created_by: Optional[str] = None):
        """Called before insert. Override to auto-populate fields."""
        pass

    def update_fill(self, dao, entity, updated_by: Optional[str] = None):
        """Called before update. Override to auto-populate fields."""
        pass


class DefaultMetaObjectHandler(MetaObjectHandler):
    """Default handler: auto-fills id, is_deleted, created_at, updated_at, created_by, updated_by.

    Matches the standard table design across this project:
      - id: Snowflake ID (via DefaultIdGenerator)
      - is_deleted: Soft delete flag set to 'NO'
      - created_at / updated_at: Current timestamp
      - created_by / updated_by: User ID (passed from service via dao.insert/update)
    """

    def insert_fill(self, dao, entity, created_by: Optional[str] = None):
        if not getattr(entity, 'id', None):
            entity.id = self.id_generator.generate_id()
        if dao._can_apply_soft_delete() and getattr(entity, dao._soft_delete_field, None) is None:
            setattr(entity, dao._soft_delete_field, dao._soft_delete_not_deleted)
        now = datetime.now()
        if hasattr(entity, 'created_at') and entity.created_at is None:
            entity.created_at = now
        if hasattr(entity, 'updated_at'):
            entity.updated_at = now
        if created_by is not None and hasattr(entity, 'created_by') and entity.created_by is None:
            entity.created_by = created_by

    def update_fill(self, dao, entity, updated_by: Optional[str] = None):
        if hasattr(entity, 'updated_at'):
            entity.updated_at = datetime.now()
        if updated_by is not None and hasattr(entity, 'updated_by'):
            entity.updated_by = updated_by
