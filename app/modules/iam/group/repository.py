from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.iam.group.model import SysGroup, SysGroupRoleRel
from app.modules.iam.group.schema import (
    GroupAdminPageQuery,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    GroupUpdateRequest,
)
from app.modules.iam.role.model import SysRole


class GroupRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: GroupCreateRequest) -> None:
        group = SysGroup(**payload.model_dump())
        self.db.add(group)
        await self.db.flush()

    async def get_by_id(self, group_id: str) -> SysGroup | None:
        return await self.db.get(SysGroup, group_id)

    async def get_required(self, group_id: str) -> SysGroup:
        entity = await self.get_by_id(group_id)
        if entity is None:
            raise NotFoundError("Group not found")
        return entity

    async def update(self, payload: GroupUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, group_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(group_ids))
        stmt = select(SysGroup.id).where(SysGroup.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Group not found")
        await self.db.execute(delete(SysGroup).where(SysGroup.id.in_(unique_ids)))

    async def page_admin(self, query: GroupAdminPageQuery) -> tuple[list[SysGroup], int]:
        stmt: Select[tuple[SysGroup]] = select(SysGroup)
        count_stmt = select(func.count(SysGroup.id))
        filters = []
        if query.name:
            filters.append(SysGroup.name.contains(query.name))
        if query.status:
            filters.append(SysGroup.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(SysGroup.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        groups = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return groups, total

    async def assign_group_to_role(self, payload: GroupRoleAssignRequest) -> SysGroupRoleRel:
        if not await self.db.get(SysGroup, payload.group_id):
            raise NotFoundError("Group not found")
        if not await self.db.get(SysRole, payload.role_id):
            raise NotFoundError("Role not found")
        relation = SysGroupRoleRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation
