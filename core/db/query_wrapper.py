from typing import Any, List, Optional
from sqlalchemy import select, func, Select, desc, asc
from sqlalchemy.sql.expression import UnaryExpression


class QueryWrapper:
    """MyBatis-Plus style dynamic query builder.

    Usage:
        wrapper = QueryWrapper(SysConfig) \\
            .eq(SysConfig.category, 'SYS_BASE') \\
            .like(SysConfig.config_key, 'SYS_') \\
            .order_by_asc(SysConfig.sort_code)

        dao.select_list(wrapper)
        dao.select_one(wrapper)
        dao.select_page(wrapper, page_bounds)
    """

    def __init__(self, model, include_deleted: bool = False):
        self.model = model
        self._conditions: List[Any] = []
        self._order_by: List[UnaryExpression] = []
        self._group_by: List[Any] = []
        self._include_deleted = include_deleted

    # ---- conditions ----

    def eq(self, field, value: Any) -> "QueryWrapper":
        self._conditions.append(field == value)
        return self

    def ne(self, field, value: Any) -> "QueryWrapper":
        self._conditions.append(field != value)
        return self

    def gt(self, field, value: Any) -> "QueryWrapper":
        self._conditions.append(field > value)
        return self

    def ge(self, field, value: Any) -> "QueryWrapper":
        self._conditions.append(field >= value)
        return self

    def lt(self, field, value: Any) -> "QueryWrapper":
        self._conditions.append(field < value)
        return self

    def le(self, field, value: Any) -> "QueryWrapper":
        self._conditions.append(field <= value)
        return self

    def like(self, field, value: str) -> "QueryWrapper":
        self._conditions.append(field.like(f"%{value}%"))
        return self

    def like_left(self, field, value: str) -> "QueryWrapper":
        self._conditions.append(field.like(f"%{value}"))
        return self

    def like_right(self, field, value: str) -> "QueryWrapper":
        self._conditions.append(field.like(f"{value}%"))
        return self

    def in_(self, field, values: List[Any]) -> "QueryWrapper":
        self._conditions.append(field.in_(values))
        return self

    def not_in(self, field, values: List[Any]) -> "QueryWrapper":
        self._conditions.append(field.notin_(values))
        return self

    def is_null(self, field) -> "QueryWrapper":
        self._conditions.append(field.is_(None))
        return self

    def is_not_null(self, field) -> "QueryWrapper":
        self._conditions.append(field.isnot(None))
        return self

    def between(self, field, start: Any, end: Any) -> "QueryWrapper":
        self._conditions.append(field.between(start, end))
        return self

    def where(self, condition: Any) -> "QueryWrapper":
        """Add a raw SQLAlchemy condition (e.g. or_(a == 1, b == 2))."""
        self._conditions.append(condition)
        return self

    # ---- order & group ----

    def order_by_asc(self, field) -> "QueryWrapper":
        self._order_by.append(asc(field))
        return self

    def order_by_desc(self, field) -> "QueryWrapper":
        self._order_by.append(desc(field))
        return self

    def group_by(self, field) -> "QueryWrapper":
        self._group_by.append(field)
        return self

    # ---- build ----

    def build(self) -> Select:
        """Build a select() statement from accumulated conditions."""
        stmt = select(self.model)
        for cond in self._conditions:
            stmt = stmt.where(cond)
        for order in self._order_by:
            stmt = stmt.order_by(order)
        for group in self._group_by:
            stmt = stmt.group_by(group)
        return stmt

    def build_count(self) -> Select:
        """Build a count() statement from conditions (ignores order_by/group_by)."""
        stmt = select(func.count()).select_from(self.model)
        for cond in self._conditions:
            stmt = stmt.where(cond)
        return stmt
