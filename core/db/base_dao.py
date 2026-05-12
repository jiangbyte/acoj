from typing import TypeVar, Type, List, Optional, Dict, Any, Callable
from sqlalchemy.orm import Session
from sqlalchemy import select, func
from sqlalchemy.sql.selectable import Select
from config.settings import settings
from core.pojo import PageBounds
from .query_wrapper import QueryWrapper
from .meta_object_handler import MetaObjectHandler, DefaultMetaObjectHandler

T = TypeVar('T')


class BaseDAO:
    """Generic Data Access Object with MyBatis-Plus style features.

    - Pluggable MetaObjectHandler for field auto-fill on insert/update
    - Pluggable IdGenerator for ID generation strategy
    - QueryWrapper for dynamic query building

    Usage:
        # Default (auto-fills id, is_deleted, created_at, updated_at)
        dao = UserDao(db)

        # Custom MetaObjectHandler per DAO
        class MyHandler(MetaObjectHandler):
            def insert_fill(self, dao, entity):
                entity.id = self.id_generator.generate_id()
                entity.created_at = datetime.now()
        dao = UserDao(db, meta_object_handler=MyHandler())

        # Custom IdGenerator
        class UuidGenerator(IdGenerator):
            def generate_id(self):
                return str(uuid.uuid4())
        dao = UserDao(db, meta_object_handler=DefaultMetaObjectHandler(id_generator=UuidGenerator()))
    """

    def __init__(self, db: Session, model: Type[T],
                 meta_object_handler: Optional[MetaObjectHandler] = None):
        self.db = db
        self.model = model
        self._soft_delete_enabled_global = settings.db.soft_delete_enabled
        self._soft_delete_field = settings.db.soft_delete_field
        self._soft_delete_not_deleted = settings.db.soft_delete_value_not_deleted
        self._soft_delete_deleted = settings.db.soft_delete_value_deleted
        self._has_soft_delete_field = hasattr(model, self._soft_delete_field)
        self.meta_object_handler = meta_object_handler or DefaultMetaObjectHandler()

    def _can_apply_soft_delete(self) -> bool:
        return self._soft_delete_enabled_global and self._has_soft_delete_field

    def _apply_soft_delete_filter(self, query):
        if self._can_apply_soft_delete():
            return query.where(getattr(self.model, self._soft_delete_field) == self._soft_delete_not_deleted)
        return query

    # ---- base CRUD (now with auto-fill) ----

    def find_by_id(self, id: str) -> Optional[T]:
        query = select(self.model).where(self.model.id == id)
        query = self._apply_soft_delete_filter(query)
        return self.db.execute(query).scalar_one_or_none()

    def find_by_field(self, field_name: str, value: Any) -> Optional[T]:
        query = select(self.model).where(getattr(self.model, field_name) == value)
        query = self._apply_soft_delete_filter(query)
        return self.db.execute(query).scalar_one_or_none()

    def find_all(self) -> List[T]:
        query = select(self.model)
        query = self._apply_soft_delete_filter(query)
        return list(self.db.execute(query).scalars().all())

    def find_by_ids(self, ids: List[str]) -> List[T]:
        query = select(self.model).where(self.model.id.in_(ids))
        query = self._apply_soft_delete_filter(query)
        return list(self.db.execute(query).scalars().all())

    def find_by_field_list(self, field_name: str, values: List[Any]) -> List[T]:
        query = select(self.model).where(getattr(self.model, field_name).in_(values))
        query = self._apply_soft_delete_filter(query)
        return list(self.db.execute(query).scalars().all())

    def count_all(self) -> int:
        query = select(func.count()).select_from(self.model)
        query = self._apply_soft_delete_filter(query)
        return self.db.execute(query).scalar() or 0

    def find_page(self, page_bounds: PageBounds,
                  query_builder: Optional[Callable[[Select], Select]] = None) -> Dict[str, Any]:
        """Paginated query with optional callback for custom filters (legacy API)."""
        current = max(1, page_bounds.current)
        size = max(1, page_bounds.size)
        offset = (current - 1) * size

        query = select(self.model)
        query = self._apply_soft_delete_filter(query)
        if query_builder:
            query = query_builder(query)

        count_query = select(func.count()).select_from(query.subquery())
        total = self.db.execute(count_query).scalar() or 0

        records = list(self.db.execute(query.offset(offset).limit(size)).scalars().all())
        return {"records": records, "total": total}

    def insert(self, entity: T, user_id: Optional[str] = None) -> T:
        self.meta_object_handler.insert_fill(self, entity, created_by=user_id)
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def insert_batch(self, entities: List[T], user_id: Optional[str] = None) -> None:
        for entity in entities:
            self.meta_object_handler.insert_fill(self, entity, created_by=user_id)
        self.db.add_all(entities)
        self.db.commit()

    def update(self, entity: T, user_id: Optional[str] = None) -> T:
        self.meta_object_handler.update_fill(self, entity, updated_by=user_id)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_id(self, id: str) -> bool:
        entity = self.find_by_id(id)
        if not entity:
            return False
        if self._can_apply_soft_delete():
            setattr(entity, self._soft_delete_field, self._soft_delete_deleted)
            self.db.commit()
        else:
            self.db.delete(entity)
            self.db.commit()
        return True

    def delete_by_ids(self, ids: List[str]) -> int:
        entities = self.find_by_ids(ids)
        if not entities:
            return 0
        if self._can_apply_soft_delete():
            for entity in entities:
                setattr(entity, self._soft_delete_field, self._soft_delete_deleted)
            self.db.commit()
        else:
            for entity in entities:
                self.db.delete(entity)
            self.db.commit()
        return len(entities)

    # ---- QueryWrapper-based methods ----

    def _apply_wrapper(self, wrapper: QueryWrapper) -> QueryWrapper:
        """Apply soft-delete filter to a QueryWrapper if applicable."""
        if self._can_apply_soft_delete() and not wrapper._include_deleted:
            wrapper.eq(getattr(self.model, self._soft_delete_field), self._soft_delete_not_deleted)
        return wrapper

    def select_one(self, wrapper: QueryWrapper) -> Optional[T]:
        wrapper = self._apply_wrapper(wrapper)
        stmt = wrapper.build().limit(1)
        return self.db.execute(stmt).scalar_one_or_none()

    def select_list(self, wrapper: QueryWrapper) -> List[T]:
        wrapper = self._apply_wrapper(wrapper)
        stmt = wrapper.build()
        return list(self.db.execute(stmt).scalars().all())

    def select_count(self, wrapper: QueryWrapper) -> int:
        wrapper = self._apply_wrapper(wrapper)
        stmt = wrapper.build_count()
        return self.db.execute(stmt).scalar() or 0

    def select_page(self, wrapper: QueryWrapper, page_bounds: PageBounds) -> Dict[str, Any]:
        """Paginated query using QueryWrapper instead of callback pattern."""
        wrapper = self._apply_wrapper(wrapper)

        current = max(1, page_bounds.current)
        size = max(1, page_bounds.size)
        offset = (current - 1) * size

        count_stmt = wrapper.build_count()
        total = self.db.execute(count_stmt).scalar() or 0

        query_stmt = wrapper.build().offset(offset).limit(size)
        records = list(self.db.execute(query_stmt).scalars().all())

        return {"records": records, "total": total}
