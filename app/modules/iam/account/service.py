from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import UserType
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest
from app.core.schema.base import to_schema, to_schema_list
from app.core.security.password import hash_password
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.account.schema import (
    AccountCreateRequest,
    AccountAdminPageQuery,
    AccountDeptAssignRequest,
    AccountGroupAssignRequest,
    AccountRoleAssignRequest,
    AccountUpdateRequest,
    SysAccountDeptRelSchema,
    SysAccountGroupRelSchema,
    SysAccountRoleRelSchema,
    SysAccountSchema,
)
from app.modules.user.admin.service import AdminUserProfileService
from app.modules.user.portal.service import PortalUserProfileService
from app.platform.db.transaction import transactional


class AccountService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = AccountRepository(db)

    async def create(self, payload: AccountCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(
                payload,
                password_hash=hash_password(payload.password),
            )
            account = await self.repo.get_account_by_account(payload.account)
            assert account is not None
            if payload.account_type == UserType.ADMIN:
                await AdminUserProfileService(self.db).create_default_profile(account.id)
            elif payload.account_type == UserType.PORTAL:
                await PortalUserProfileService(self.db).create_default_profile(account.id)

    async def update(self, payload: AccountUpdateRequest) -> None:
        async with transactional(self.db):
            password_hash = hash_password(payload.password) if payload.password else None
            await self.repo.update(payload, password_hash)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> SysAccountSchema:
        return to_schema(SysAccountSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: AccountAdminPageQuery) -> PageData[SysAccountSchema]:
        accounts, total = await self.repo.page_admin(query)
        return build_page(query.pagination, total, to_schema_list(SysAccountSchema, accounts))

    async def assign_account_role(
        self,
        payload: AccountRoleAssignRequest,
    ) -> SysAccountRoleRelSchema:
        async with transactional(self.db):
            return to_schema(
                SysAccountRoleRelSchema,
                await self.repo.assign_account_to_role(payload),
            )

    async def assign_account_group(
        self,
        payload: AccountGroupAssignRequest,
    ) -> SysAccountGroupRelSchema:
        async with transactional(self.db):
            return to_schema(
                SysAccountGroupRelSchema,
                await self.repo.assign_account_to_group(payload),
            )

    async def assign_account_dept(
        self,
        payload: AccountDeptAssignRequest,
    ) -> SysAccountDeptRelSchema:
        async with transactional(self.db):
            return to_schema(
                SysAccountDeptRelSchema,
                await self.repo.assign_account_to_dept(payload),
            )
