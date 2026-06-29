from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql.elements import ColumnElement

from app.core.exceptions.business import NotFoundError
from app.modules.iam.account.model import SysAccount, SysAccountDeptRel, SysAccountGroupRel
from app.modules.iam.group.model import SysGroup, SysGroupRoleRel
from app.modules.iam.reference_guard import count_group_references, raise_if_referenced
from app.modules.iam.group.schema import (
    GroupAdminPageQuery,
    GroupCreateRequest,
    GroupGrantRoleRequest,
    GroupGrantUserRequest,
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
        if not unique_ids:
            return
        stmt = select(SysGroup.id).where(SysGroup.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Group not found")
        raise_if_referenced("Group", await count_group_references(self.db, unique_ids))
        await self.db.execute(delete(SysGroup).where(SysGroup.id.in_(unique_ids)))

    async def count_groups_in_scope(
        self,
        group_ids: list[str],
        data_scope_filter: ColumnElement[bool],
    ) -> int:
        unique_ids = list(dict.fromkeys(group_ids))
        if not unique_ids:
            return 0
        stmt = select(func.count(SysGroup.id)).where(SysGroup.id.in_(unique_ids), data_scope_filter)
        return int((await self.db.execute(stmt)).scalar_one())

    async def page_admin(
        self,
        query: GroupAdminPageQuery,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> tuple[list[SysGroup], int]:
        stmt: Select[tuple[SysGroup]] = select(SysGroup)
        count_stmt = select(func.count(SysGroup.id))
        filters = []
        if query.name:
            filters.append(SysGroup.name.contains(query.name))
        if query.status:
            filters.append(SysGroup.status == query.status)
        if data_scope_filter is not None:
            filters.append(data_scope_filter)
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

    async def list_accounts(
        self,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[SysAccount]:
        stmt = select(SysAccount).order_by(SysAccount.id.desc())
        if data_scope_filter is not None:
            stmt = (
                stmt.outerjoin(SysAccountDeptRel, SysAccountDeptRel.account_id == SysAccount.id)
                .where(data_scope_filter)
            )
        return list((await self.db.execute(stmt)).unique().scalars().all())

    async def list_group_accounts(
        self,
        group_id: str,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[SysAccount]:
        await self.get_required(group_id)
        stmt = (
            select(SysAccount)
            .join(SysAccountGroupRel, SysAccountGroupRel.account_id == SysAccount.id)
            .where(SysAccountGroupRel.group_id == group_id)
            .order_by(SysAccount.id.desc())
        )
        if data_scope_filter is not None:
            stmt = stmt.outerjoin(SysAccountDeptRel, SysAccountDeptRel.account_id == SysAccount.id).where(data_scope_filter)
        return list((await self.db.execute(stmt)).unique().scalars().all())

    async def list_account_ids_by_group(self, group_id: str) -> list[str]:
        await self.get_required(group_id)
        stmt = select(SysAccountGroupRel.account_id).where(SysAccountGroupRel.group_id == group_id)
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def replace_group_accounts(self, payload: GroupGrantUserRequest) -> None:
        await self.get_required(payload.id)
        account_ids = list(dict.fromkeys(payload.account_ids))
        if account_ids:
            stmt = select(SysAccount.id).where(SysAccount.id.in_(account_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(account_ids):
                raise NotFoundError("Account not found")
        await self.db.execute(delete(SysAccountGroupRel).where(SysAccountGroupRel.group_id == payload.id))
        for account_id in account_ids:
            self.db.add(SysAccountGroupRel(account_id=account_id, group_id=payload.id))
        await self.db.flush()

    async def list_all_roles(
        self,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[SysRole]:
        stmt = select(SysRole).order_by(SysRole.sort.asc(), SysRole.id.desc())
        if data_scope_filter is not None:
            stmt = stmt.where(data_scope_filter)
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_group_role_ids(
        self,
        group_id: str,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[str]:
        await self.get_required(group_id)
        stmt = select(SysGroupRoleRel.role_id).where(SysGroupRoleRel.group_id == group_id)
        if data_scope_filter is not None:
            stmt = stmt.join(SysRole, SysRole.id == SysGroupRoleRel.role_id).where(data_scope_filter)
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def replace_group_roles(self, payload: GroupGrantRoleRequest) -> None:
        await self.get_required(payload.id)
        role_ids = list(dict.fromkeys(payload.role_ids))
        if role_ids:
            stmt = select(SysRole.id).where(SysRole.id.in_(role_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(role_ids):
                raise NotFoundError("Role not found")
        await self.db.execute(delete(SysGroupRoleRel).where(SysGroupRoleRel.group_id == payload.id))
        for role_id in role_ids:
            self.db.add(SysGroupRoleRel(group_id=payload.id, role_id=role_id))
        await self.db.flush()
