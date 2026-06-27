from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.iam.role.model import SysRole
from app.modules.iam.role.schema import RoleAdminPageQuery, RoleCreateRequest, RoleUpdateRequest


class RoleRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: RoleCreateRequest) -> None:
        role = SysRole(**payload.model_dump())
        self.db.add(role)
        await self.db.flush()

    async def get_by_id(self, role_id: str) -> SysRole | None:
        return await self.db.get(SysRole, role_id)

    async def get_required(self, role_id: str) -> SysRole:
        entity = await self.get_by_id(role_id)
        if entity is None:
            raise NotFoundError("Role not found")
        return entity

    async def update(self, payload: RoleUpdateRequest) -> None:
        entity = await self.get_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()

    async def delete_many(self, role_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(role_ids))
        stmt = select(SysRole.id).where(SysRole.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Role not found")
        await self.db.execute(delete(SysRole).where(SysRole.id.in_(unique_ids)))

    async def page_admin(self, query: RoleAdminPageQuery) -> tuple[list[SysRole], int]:
        stmt: Select[tuple[SysRole]] = select(SysRole)
        count_stmt = select(func.count(SysRole.id))
        filters = []
        if query.code:
            filters.append(SysRole.code.contains(query.code))
        if query.name:
            filters.append(SysRole.name.contains(query.name))
        if query.category:
            filters.append(SysRole.category == query.category)
        if query.scope_type:
            filters.append(SysRole.scope_type == query.scope_type.value)
        if query.status:
            filters.append(SysRole.status == query.status)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(SysRole.sort.asc(), SysRole.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        roles = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return roles, total
