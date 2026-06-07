"""
Generic CRUD — mirrors hei-gin's ``sdk/crud/crud.go``.

Provides reusable ``page()``, ``detail()``, ``options()``, ``remove()``
functions that eliminate DAO boilerplate for standard operations.

Usage::

    from core.crud import page, simple_page, detail, options, remove

    # Custom filter:
    def banner_page(db: Session, param: BannerPageParam) -> dict:
        def build_query(q):
            if param.keyword:
                q = q.where(SysBanner.name.like(f"%{param.keyword}%"))
            return q
        return page(db, SysBanner, param, build_query)

    # No custom filter:
    def role_page(db: Session, param: RolePageParam) -> dict:
        return simple_page(db, SysRole, param)
"""

from __future__ import annotations

import math
from typing import Any, Callable, Optional, Protocol, Sequence, TypeVar

from sqlalchemy import func, select, delete as sa_delete
from sqlalchemy.orm import Session

from core.result import page_data, PageDataField
from core.exception import BusinessException

T = TypeVar("T")


# ── PageParam protocol ──────────────────────────────────────────────

class PageParamProto(Protocol):
    """Protocol matching hei-gin's ``crud.PageParam`` interface."""
    current: int
    size: int


def _page_num(param: Any) -> tuple[int, int]:
    """Extract and clamp current/size from any param object.

    Mirrors hei-gin's ``crud.pageNum(p PageParam)``.
    """
    current = max(1, getattr(param, "current", 1))
    size = max(1, min(getattr(param, "size", 10), 100))
    return current, size


# ── CRUD functions ──────────────────────────────────────────────────

def page(
    db: Session,
    model_class: type[T],
    param: Any,
    build_query: Callable | None = None,
    to_vo: Callable[[Any], Any] = lambda r: r,
    default_order: Any | None = None,
) -> dict:
    """Generic paginated query with optional custom filters.

    Args:
        db: SQLAlchemy session.
        model_class: ORM model class.
        param: Object with ``current`` and ``size`` attributes.
        build_query: Optional callable that receives and returns a
                     select() statement for adding custom filters.
        to_vo: Callable to convert ORM entity to a view object.
        default_order: Sort column/expression (default: model.created_at).

    Returns:
        A ``page_data()`` dict with records, total, page, size, pages.

    Mirrors hei-gin's ``crud.Page[T, P](c, model, param, buildQuery, order, toVO)``.
    """
    current, size = _page_num(param)
    offset = (current - 1) * size

    # Default ordering
    if default_order is None:
        default_order = getattr(model_class, "created_at", None)
    if default_order is not None:
        default_order = default_order.desc()

    # Count
    count_stmt = select(func.count()).select_from(model_class)
    if build_query:
        count_stmt = build_query(count_stmt)
    total = db.execute(count_stmt).scalar() or 0

    # Data
    data_stmt = select(model_class).offset(offset).limit(size)
    if default_order is not None:
        data_stmt = data_stmt.order_by(default_order)
    if build_query:
        data_stmt = build_query(data_stmt)
    records = list(db.execute(data_stmt).scalars().all())

    vos = [to_vo(r) for r in records]
    return page_data(records=vos, total=total, current=current, size=size)


def simple_page(
    db: Session,
    model_class: type[T],
    param: Any,
    to_vo: Callable[[Any], Any] = lambda r: r,
) -> dict:
    """Paginated query without custom filters.

    A convenience wrapper around ``page()``.

    Mirrors hei-gin's pattern of using ``Page()`` without ``buildQuery``.
    """
    return page(db, model_class, param, build_query=None, to_vo=to_vo)


def detail(
    db: Session,
    model_class: type[T],
    id: str,
    name: str = "数据",
    to_vo: Callable[[Any], Any] = lambda r: r,
) -> Any:
    """Retrieve a single record by ID.

    Raises ``BusinessException`` if not found.

    Mirrors hei-gin's ``crud.Detail[T](c, model, id, name)``.
    """
    stmt = select(model_class).where(model_class.id == id)
    entity = db.execute(stmt).scalar_one_or_none()
    if not entity:
        raise BusinessException(f"{name}不存在", 500)
    return to_vo(entity)


def detail_or_none(
    db: Session,
    model_class: type[T],
    id: str,
    to_vo: Callable[[Any], Any] = lambda r: r,
) -> Optional[Any]:
    """Retrieve a single record by ID, returning None if not found."""
    stmt = select(model_class).where(model_class.id == id)
    entity = db.execute(stmt).scalar_one_or_none()
    if not entity:
        return None
    return to_vo(entity)


def options(
    db: Session,
    model_class: type[T],
    to_vo: Callable[[Any], Any] = lambda r: r,
    order: str = "sort_code",
) -> list[Any]:
    """Return a sorted list of all records (for dropdowns / select options).

    Mirrors hei-gin's ``crud.Options[T](c, model, order, toVO)``.
    """
    stmt = select(model_class).order_by(order)
    records = list(db.execute(stmt).scalars().all())
    return [to_vo(r) for r in records]


def remove(
    db: Session,
    model_class: type[T],
    ids: list[str],
) -> int:
    """Delete records by IDs. Returns the number of deleted rows.

    Mirrors hei-gin's ``crud.Remove[T](c, model, ids)``.
    """
    if not ids:
        return 0
    stmt = sa_delete(model_class).where(model_class.id.in_(ids))
    result = db.execute(stmt)
    db.commit()
    return result.rowcount


__all__ = [
    "page", "simple_page", "detail", "detail_or_none",
    "options", "remove",
    "PageParamProto",
]
