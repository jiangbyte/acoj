"""Batch helpers to avoid N+1 ownership checks and FK lookups on OJ admin CRUD."""

from __future__ import annotations

from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError


async def assert_ids_exist(
    db: AsyncSession,
    *,
    model: type,
    entity_ids: list[str],
    not_found_message: str,
) -> None:
    """Verify all ids exist with one SELECT (empty list is a no-op)."""
    unique_ids = list(dict.fromkeys(entity_id for entity_id in entity_ids if entity_id))
    if not unique_ids:
        return
    stmt = select(model.id).where(model.id.in_(unique_ids))
    existing_ids = set((await db.execute(stmt)).scalars().all())
    if len(existing_ids) != len(unique_ids):
        raise NotFoundError(not_found_message)


async def delete_owned_by_parent(
    db: AsyncSession,
    *,
    model: type,
    parent_attr: str,
    parent_id: str,
    entity_ids: list[str],
    not_found_message: str,
) -> None:
    """Verify all ids belong to parent with one SELECT, then one DELETE."""
    unique_ids = list(dict.fromkeys(entity_ids))
    if not unique_ids:
        return
    parent_col = getattr(model, parent_attr)
    stmt = select(model.id).where(model.id.in_(unique_ids), parent_col == parent_id)
    existing_ids = set((await db.execute(stmt)).scalars().all())
    if len(existing_ids) != len(unique_ids):
        raise NotFoundError(not_found_message)
    await db.execute(delete(model).where(model.id.in_(unique_ids)))


async def ensure_parent_exists(
    db: AsyncSession,
    *,
    model: type,
    parent_id: str,
    not_found_message: str,
) -> None:
    """One-shot parent existence check for nested child APIs."""
    entity = await db.get(model, parent_id)
    if entity is None:
        raise NotFoundError(not_found_message)


def ensure_belongs_to_parent(
    entity: object,
    *,
    parent_attr: str,
    parent_id: str,
    not_found_message: str,
) -> None:
    """Raise NotFound when a loaded child row does not belong to parent_id."""
    if getattr(entity, parent_attr, None) != parent_id:
        raise NotFoundError(not_found_message)
