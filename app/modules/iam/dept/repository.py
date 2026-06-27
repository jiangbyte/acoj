from typing import TypedDict

from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.iam.dept.model import SysDept
from app.core.exceptions.business import NotFoundError
from app.modules.iam.dept.schema import DeptAdminPageQuery, DeptCreateRequest, DeptUpdateRequest


class DeptTreeRecord(TypedDict):
    id: str
    name: str
    code: str
    category: str
    children: list["DeptTreeRecord"]


class DeptRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: DeptCreateRequest) -> None:
        dept = SysDept(**payload.model_dump())
        self.db.add(dept)
        await self.db.flush()

    async def get_by_id(self, dept_id: str) -> SysDept | None:
        return await self.db.get(SysDept, dept_id)

    async def get_required(self, dept_id: str) -> SysDept:
        entity = await self.get_by_id(dept_id)
        if entity is None:
            raise NotFoundError("Dept not found")
        return entity

    async def update(self, payload: DeptUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, dept_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(dept_ids))
        stmt = select(SysDept.id).where(SysDept.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Dept not found")
        await self.db.execute(delete(SysDept).where(SysDept.id.in_(unique_ids)))

    async def page_admin(self, query: DeptAdminPageQuery) -> tuple[list[SysDept], int]:
        stmt: Select[tuple[SysDept]] = select(SysDept)
        count_stmt = select(func.count(SysDept.id))
        filters = []
        if query.name:
            filters.append(SysDept.name.contains(query.name))
        if query.code:
            filters.append(SysDept.code.contains(query.code))
        if query.category:
            filters.append(SysDept.category == query.category)
        if query.parent_id:
            filters.append(SysDept.parent_id == query.parent_id)
        if query.status:
            filters.append(SysDept.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(SysDept.sort.asc(), SysDept.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def list_depts(self) -> list[SysDept]:
        stmt = select(SysDept).order_by(SysDept.sort.asc(), SysDept.id.asc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def get_dept_tree(self) -> list[DeptTreeRecord]:
        depts = await self.list_depts()
        node_map: dict[str, DeptTreeRecord] = {
            dept.id: {
                "id": dept.id,
                "name": dept.name,
                "code": dept.code,
                "category": dept.category,
                "children": [],
            }
            for dept in depts
        }
        roots: list[DeptTreeRecord] = []
        for dept in depts:
            if dept.parent_id and dept.parent_id in node_map:
                node_map[dept.parent_id]["children"].append(node_map[dept.id])
            else:
                roots.append(node_map[dept.id])
        return roots
