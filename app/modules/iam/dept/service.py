from collections.abc import Mapping, Sequence

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthorizationError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.core.security.data_scope import build_data_scope_filter, resolve_data_scope_dept_ids
from app.core.security.session import SessionPayload
from app.modules.iam.dept.repository import DeptRepository, DeptTreeRecord
from app.modules.iam.dept.model import SysDept
from app.modules.iam.dept.schema import (
    DeptAdminPageQuery,
    DeptCreateRequest,
    DeptTreeNode,
    DeptUpdateRequest,
    SysDeptSchema,
)
from app.platform.db.transaction import transactional


class DeptService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = DeptRepository(db)

    async def create(self, payload: DeptCreateRequest, session: SessionPayload | None = None) -> None:
        if session is not None and payload.parent_id:
            await self._ensure_dept_ids_visible(session, "iam:dept:create", [payload.parent_id])
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: DeptUpdateRequest, session: SessionPayload | None = None) -> None:
        if session is not None:
            await self._ensure_dept_records_visible(session, "iam:dept:update", [payload.id])
            if payload.parent_id:
                await self._ensure_dept_records_visible(session, "iam:dept:update", [payload.parent_id])
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest, session: SessionPayload | None = None) -> None:
        if session is not None:
            await self._ensure_dept_records_visible(session, "iam:dept:delete", payload.ids)
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery, session: SessionPayload | None = None) -> SysDeptSchema:
        if session is not None:
            await self._ensure_dept_records_visible(session, "iam:dept:detail", [query.id])
        return to_schema(SysDeptSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self,
        query: DeptAdminPageQuery,
        session: SessionPayload | None = None,
    ) -> PageData[SysDeptSchema]:
        data_scope_filter = (
            await self._dept_scope_filter(session, "iam:dept:page")
            if session is not None
            else None
        )
        items, total = await self.repo.page_admin(query, data_scope_filter)
        return build_page(query.pagination, total, to_schema_list(SysDeptSchema, items))

    async def list_dept_tree(self, session: SessionPayload | None = None) -> list[DeptTreeNode]:
        data_scope_filter = (
            await self._dept_scope_filter(session, "iam:dept:list")
            if session is not None
            else None
        )
        return _build_dept_tree_nodes(await self.repo.get_dept_tree(data_scope_filter))

    async def _dept_scope_filter(self, session: SessionPayload, permission_key: str):
        return await build_data_scope_filter(
            self.db,
            session,
            permission_key,
            owner_column=SysDept.created_by,
            dept_column=SysDept.id,
        )

    async def _ensure_dept_records_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        dept_ids: list[str],
    ) -> None:
        unique_ids = list(dict.fromkeys(dept_ids))
        if not unique_ids:
            return
        data_scope_filter = await self._dept_scope_filter(session, permission_key)
        if await self.repo.count_depts_in_scope(unique_ids, data_scope_filter) != len(unique_ids):
            raise AuthorizationError("Dept is outside current data scope")

    async def _ensure_dept_ids_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        dept_ids: list[str],
    ) -> None:
        unique_ids = list(dict.fromkeys(dept_ids))
        if not unique_ids:
            return
        visible_dept_ids = await resolve_data_scope_dept_ids(self.db, session, permission_key)
        if visible_dept_ids is None:
            return
        allowed_ids = set(visible_dept_ids)
        if any(dept_id not in allowed_ids for dept_id in unique_ids):
            raise AuthorizationError("Dept is outside current data scope")


def _build_dept_tree_nodes(
    items: Sequence[DeptTreeRecord | DeptTreeNode | Mapping[str, object]],
) -> list[DeptTreeNode]:
    nodes: list[DeptTreeNode] = []
    for item in items:
        raw_item: Mapping[str, object] = (
            item.model_dump() if isinstance(item, DeptTreeNode) else item
        )
        nodes.append(
            DeptTreeNode(
                id=str(raw_item["id"]),
                name=str(raw_item["name"]),
                code=str(raw_item["code"]),
                category=str(raw_item["category"]),
                children=_build_dept_tree_nodes(raw_item.get("children", [])),  # type: ignore[arg-type]
            )
        )
    return nodes
