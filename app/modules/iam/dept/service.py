from collections.abc import Mapping, Sequence

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.iam.dept.repository import DeptRepository, DeptTreeRecord
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

    async def create(self, payload: DeptCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: DeptUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysDeptSchema:
        return to_schema(SysDeptSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: DeptAdminPageQuery) -> PageData[SysDeptSchema]:
        items, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysDeptSchema, items))

    async def list_dept_tree(self) -> list[DeptTreeNode]:
        return _build_dept_tree_nodes(await self.repo.get_dept_tree())


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
