from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import ConflictError, NotFoundError
from app.modules.iam.account.model import (
    SysAccount,
    SysAccountDeptRel,
    SysAccountGroupRel,
    SysAccountRoleRel,
)
from app.modules.iam.account.schema import (
    AccountCreateRequest,
    AccountAdminPageQuery,
    AccountDeptAssignRequest,
    AccountGroupAssignRequest,
    AccountRoleAssignRequest,
    AccountUpdateRequest,
)
from app.modules.iam.dept.model import SysDept
from app.modules.iam.group.model import SysGroup, SysGroupRoleRel
from app.modules.iam.role.model import SysRole


class AccountRepository:
    """账户仓储，负责账户主表、账户归属和账户直接授权关系。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_by_id(self, account_id: str) -> SysAccount | None:
        return await self.db.get(SysAccount, account_id)

    async def get_required(self, account_id: str) -> SysAccount:
        entity = await self.get_by_id(account_id)
        if entity is None:
            raise NotFoundError("Account not found")
        return entity

    async def get_account_by_id(self, account_id: str) -> SysAccount | None:
        return await self.get_by_id(account_id)

    async def get_account_by_account(self, account: str) -> SysAccount | None:
        stmt = select(SysAccount).where(SysAccount.account == account)
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def create(self, payload: AccountCreateRequest, password_hash: str) -> None:
        existing = await self.get_account_by_account(payload.account)
        if existing:
            raise ConflictError("Account already exists")
        data = payload.model_dump(exclude={"password"})
        account = SysAccount(
            password_hash=password_hash,
            **data,
        )
        self.db.add(account)
        await self.db.flush()

    async def update(self, payload: AccountUpdateRequest, password_hash: str | None = None) -> None:
        entity = await self.get_required(payload.id)
        if payload.account != entity.account:
            existing = await self.get_account_by_account(payload.account)
            if existing and existing.id != payload.id:
                raise ConflictError("Account already exists")
        data = payload.model_dump(exclude={"id", "password"})
        for key, value in data.items():
            setattr(entity, key, value)
        if password_hash:
            entity.password_hash = password_hash
        await self.db.flush()

    async def delete_many(self, account_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(account_ids))
        stmt = select(SysAccount.id).where(SysAccount.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Account not found")
        await self.db.execute(delete(SysAccount).where(SysAccount.id.in_(unique_ids)))

    async def page_admin(self, query: AccountAdminPageQuery) -> tuple[list[SysAccount], int]:
        stmt: Select[tuple[SysAccount]] = select(SysAccount)
        count_stmt = select(func.count(SysAccount.id))
        filters = []
        if query.account:
            filters.append(SysAccount.account.contains(query.account))
        if query.name:
            filters.append(SysAccount.name.contains(query.name))
        if query.phone:
            filters.append(SysAccount.phone.contains(query.phone))
        if query.email:
            filters.append(SysAccount.email.contains(query.email))
        if query.account_type:
            filters.append(SysAccount.account_type == query.account_type.value)
        if query.account_status:
            filters.append(SysAccount.account_status == query.account_status.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(SysAccount.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        accounts = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return accounts, total

    async def assign_account_to_role(self, payload: AccountRoleAssignRequest) -> SysAccountRoleRel:
        if not await self.db.get(SysAccount, payload.account_id):
            raise NotFoundError("Account not found")
        if not await self.db.get(SysRole, payload.role_id):
            raise NotFoundError("Role not found")
        relation = SysAccountRoleRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def assign_account_to_group(
        self,
        payload: AccountGroupAssignRequest,
    ) -> SysAccountGroupRel:
        if not await self.db.get(SysAccount, payload.account_id):
            raise NotFoundError("Account not found")
        if not await self.db.get(SysGroup, payload.group_id):
            raise NotFoundError("Group not found")
        relation = SysAccountGroupRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def assign_account_to_dept(self, payload: AccountDeptAssignRequest) -> SysAccountDeptRel:
        if not await self.db.get(SysAccount, payload.account_id):
            raise NotFoundError("Account not found")
        if not await self.db.get(SysDept, payload.dept_id):
            raise NotFoundError("Dept not found")
        relation = SysAccountDeptRel(**payload.model_dump())
        self.db.add(relation)
        await self.db.flush()
        return relation

    async def get_account_role_ids(self, account_id: str) -> list[str]:
        direct_stmt = select(SysAccountRoleRel.role_id).where(
            SysAccountRoleRel.account_id == account_id
        )
        group_stmt = (
            select(SysGroupRoleRel.role_id)
            .join(SysAccountGroupRel, SysAccountGroupRel.group_id == SysGroupRoleRel.group_id)
            .where(SysAccountGroupRel.account_id == account_id)
        )
        direct = [str(value) for value in (await self.db.execute(direct_stmt)).scalars().all()]
        group = [str(value) for value in (await self.db.execute(group_stmt)).scalars().all()]
        return sorted(set(direct + group))

    async def get_account_role_codes(self, account_id: str) -> list[str]:
        direct_stmt = (
            select(SysRole.code)
            .join(SysAccountRoleRel, SysAccountRoleRel.role_id == SysRole.id)
            .where(SysAccountRoleRel.account_id == account_id)
        )
        group_stmt = (
            select(SysRole.code)
            .join(SysGroupRoleRel, SysGroupRoleRel.role_id == SysRole.id)
            .join(SysAccountGroupRel, SysAccountGroupRel.group_id == SysGroupRoleRel.group_id)
            .where(SysAccountGroupRel.account_id == account_id)
        )
        direct = [str(value) for value in (await self.db.execute(direct_stmt)).scalars().all()]
        group = [str(value) for value in (await self.db.execute(group_stmt)).scalars().all()]
        return sorted(set(direct + group))

    async def get_account_group_ids(self, account_id: str) -> list[str]:
        stmt = select(SysAccountGroupRel.group_id).where(
            SysAccountGroupRel.account_id == account_id
        )
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def get_account_dept_ids(self, account_id: str) -> list[str]:
        stmt = select(SysAccountDeptRel.dept_id).where(SysAccountDeptRel.account_id == account_id)
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]
