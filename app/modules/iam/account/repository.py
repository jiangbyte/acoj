from datetime import UTC, datetime

from sqlalchemy import Select, case, delete, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql.elements import ColumnElement

from app.core.config.enums import AccountStatusEnum
from app.core.exceptions.business import ConflictError, NotFoundError
from app.modules.iam.account.model import (
    SysAccount,
    SysAccountDeptRel,
    SysAccountGroupRel,
    SysAccountIdentity,
    SysAccountRoleRel,
)
from app.modules.iam.account.schema import (
    AccountAdminPageQuery,
    AccountCancelPayload,
    AccountCreateRequest,
    AccountDeptAssignRequest,
    AccountDeptGrantInfo,
    AccountGrantDeptRequest,
    AccountGrantGroupRequest,
    AccountGrantPermissionRequest,
    AccountGrantRoleRequest,
    AccountGroupAssignRequest,
    AccountIdentityUpsertPayload,
    AccountPermissionGrantInfo,
    AccountRoleAssignRequest,
    AccountUpdateRequest,
)
from app.modules.iam.dept.model import SysDept
from app.modules.iam.enums import AccountIdentityBindStatus, AccountIdentityType, GrantSubjectType
from app.modules.iam.grant.model import SysSubjectPermissionGrantRel, SysSubjectResourceGrantRel
from app.modules.iam.group.model import SysGroup, SysGroupRoleRel
from app.modules.iam.role.model import SysRole
from app.modules.user.admin.model import AdminUserProfile
from app.modules.user.portal.model import PortalUserProfile


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

    async def list_accounts_by_ids(self, account_ids: list[str]) -> list[SysAccount]:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return []
        stmt = select(SysAccount).where(SysAccount.id.in_(unique_ids))
        return list((await self.db.execute(stmt)).scalars().all())

    async def get_account_by_account(self, account: str) -> SysAccount | None:
        stmt = (
            select(SysAccount)
            .join(SysAccountIdentity, SysAccountIdentity.account_id == SysAccount.id)
            .where(
                SysAccountIdentity.identity_type == AccountIdentityType.ACCOUNT.value,
                SysAccountIdentity.identifier == account,
                SysAccountIdentity.bind_status == AccountIdentityBindStatus.BOUND.value,
            )
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def get_account_by_identifier(
        self,
        identifier: str,
        identity_types: list[AccountIdentityType] | None = None,
    ) -> SysAccount | None:
        types = identity_types or [AccountIdentityType.ACCOUNT]
        stmt = (
            select(SysAccount)
            .join(SysAccountIdentity, SysAccountIdentity.account_id == SysAccount.id)
            .where(
                SysAccountIdentity.identity_type.in_([item.value for item in types]),
                SysAccountIdentity.identifier == identifier,
                SysAccountIdentity.bind_status == AccountIdentityBindStatus.BOUND.value,
                or_(
                    SysAccountIdentity.identity_type == AccountIdentityType.ACCOUNT.value,
                    SysAccountIdentity.verified.is_(True),
                ),
            )
            .order_by(
                case(
                    (SysAccountIdentity.identity_type == AccountIdentityType.ACCOUNT.value, 1),
                    (SysAccountIdentity.identity_type == AccountIdentityType.EMAIL.value, 2),
                    (SysAccountIdentity.identity_type == AccountIdentityType.PHONE.value, 3),
                    else_=9,
                )
            )
            .limit(1)
        )
        return (await self.db.execute(stmt)).scalars().first()

    async def create(self, payload: AccountCreateRequest, password_hash: str) -> SysAccount:
        existing = await self.get_account_by_account(payload.account)
        if existing:
            raise ConflictError("Account already exists")
        account = SysAccount(
            password_hash=password_hash,
            account_type=payload.account_type.value,
            account_status=payload.account_status.value,
        )
        self.db.add(account)
        await self.db.flush()
        await self.replace_account_identities(account.id, payload)
        return account

    async def update(self, payload: AccountUpdateRequest, password_hash: str | None = None) -> None:
        entity = await self.get_required(payload.id)
        existing = await self.get_account_by_account(payload.account)
        if existing and existing.id != payload.id:
            raise ConflictError("Account already exists")
        entity.account_type = payload.account_type.value
        entity.account_status = payload.account_status.value
        if password_hash:
            entity.password_hash = password_hash
        await self.replace_account_identities(payload.id, payload)
        await self.db.flush()

    async def update_password_hash(self, account_id: str, password_hash: str) -> None:
        entity = await self.get_required(account_id)
        entity.password_hash = password_hash
        await self.db.flush()

    async def replace_account_identities(
        self,
        account_id: str,
        payload: AccountCreateRequest | AccountUpdateRequest,
    ) -> None:
        identity_specs = [
            (
                AccountIdentityType.ACCOUNT,
                payload.account,
                True,
                True,
                AccountIdentityBindStatus.BOUND,
            ),
            (
                AccountIdentityType.EMAIL,
                (payload.email_identity or payload.email) if payload.email_login_enabled else None,
                False,
                payload.email_identity_verified,
                payload.email_identity_bind_status,
            ),
            (
                AccountIdentityType.PHONE,
                (payload.phone_identity or payload.phone) if payload.phone_login_enabled else None,
                False,
                payload.phone_identity_verified,
                payload.phone_identity_bind_status,
            ),
        ]
        identities = [
            AccountIdentityUpsertPayload(
                account_id=account_id,
                identity_type=identity_type,
                identifier=str(identifier).strip(),
                is_primary=is_primary,
                verified=verified,
                bind_status=bind_status,
            )
            for identity_type, identifier, is_primary, verified, bind_status in identity_specs
            if str(identifier or "").strip()
        ]
        if identities:
            conflict_conditions = [
                (SysAccountIdentity.identity_type == item.identity_type.value)
                & (SysAccountIdentity.identifier == item.identifier)
                for item in identities
            ]
            stmt = (
                select(SysAccountIdentity.id)
                .where(
                    SysAccountIdentity.account_id != account_id,
                    or_(*conflict_conditions),
                )
                .limit(1)
            )
            if (await self.db.execute(stmt)).scalar_one_or_none():
                raise ConflictError("Account identity already exists")
        await self.db.execute(
            delete(SysAccountIdentity).where(SysAccountIdentity.account_id == account_id)
        )
        for item in identities:
            self.db.add(
                SysAccountIdentity(
                    account_id=item.account_id,
                    identity_type=item.identity_type.value,
                    identifier=item.identifier,
                    verified=item.verified,
                    is_primary=item.is_primary,
                    bind_status=item.bind_status.value,
                )
            )
        await self.db.flush()

    async def list_identities_by_account_ids(
        self,
        account_ids: list[str],
    ) -> list[SysAccountIdentity]:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return []
        stmt = (
            select(SysAccountIdentity)
            .where(SysAccountIdentity.account_id.in_(unique_ids))
            .order_by(
                SysAccountIdentity.account_id.asc(),
                SysAccountIdentity.is_primary.desc(),
                SysAccountIdentity.id.asc(),
            )
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def upsert_account_identity(
        self,
        account_id: str,
        identity_type: AccountIdentityType,
        identifier: str | None,
        verified: bool = True,
        enabled: bool = True,
    ) -> None:
        await self.get_required(account_id)
        normalized_identifier = str(identifier or "").strip() if enabled else ""
        await self.db.execute(
            delete(SysAccountIdentity).where(
                SysAccountIdentity.account_id == account_id,
                SysAccountIdentity.identity_type == identity_type.value,
            )
        )
        if not normalized_identifier:
            await self.db.flush()
            return
        stmt = select(SysAccountIdentity).where(
            SysAccountIdentity.identity_type == identity_type.value,
            SysAccountIdentity.identifier == normalized_identifier,
            SysAccountIdentity.account_id != account_id,
        )
        if (await self.db.execute(stmt)).scalar_one_or_none():
            raise ConflictError("Account identity already exists")
        self.db.add(
            SysAccountIdentity(
                account_id=account_id,
                identity_type=identity_type.value,
                identifier=normalized_identifier,
                verified=verified,
                is_primary=False,
                bind_status=AccountIdentityBindStatus.BOUND.value,
            )
        )
        await self.db.flush()

    async def delete_many(self, account_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return
        stmt = select(SysAccount.id).where(SysAccount.id.in_(unique_ids))
        existing_ids = set((await self.db.execute(stmt)).scalars().all())
        if len(existing_ids) != len(unique_ids):
            raise NotFoundError("Account not found")
        await self.purge_many(unique_ids)

    async def cancel(
        self,
        payload: AccountCancelPayload,
        cancelled_by: str | None = None,
    ) -> SysAccount:
        entity = await self.get_required(payload.id)
        now = datetime.now(UTC)
        entity.account_status = AccountStatusEnum.CANCELLED.value
        entity.cancelled_at = entity.cancelled_at or now
        entity.cancelled_by = cancelled_by
        entity.cancel_reason = payload.cancel_reason
        await self.db.flush()
        return entity

    async def list_expired_cancelled_account_ids(self, cutoff: datetime) -> list[str]:
        stmt = select(SysAccount.id).where(
            SysAccount.cancelled_at.is_not(None),
            SysAccount.cancelled_at <= cutoff,
            or_(
                SysAccount.latest_login_time.is_(None),
                SysAccount.latest_login_time <= cutoff,
            ),
        )
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def purge_many(self, account_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return
        await self.db.execute(
            delete(SysSubjectPermissionGrantRel).where(
                SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ACCOUNT.value,
                SysSubjectPermissionGrantRel.subject_id.in_(unique_ids),
            )
        )
        await self.db.execute(
            delete(SysSubjectResourceGrantRel).where(
                SysSubjectResourceGrantRel.subject_type == GrantSubjectType.ACCOUNT.value,
                SysSubjectResourceGrantRel.subject_id.in_(unique_ids),
            )
        )
        await self.db.execute(
            delete(SysAccountRoleRel).where(SysAccountRoleRel.account_id.in_(unique_ids))
        )
        await self.db.execute(
            delete(SysAccountDeptRel).where(SysAccountDeptRel.account_id.in_(unique_ids))
        )
        await self.db.execute(
            delete(SysAccountGroupRel).where(SysAccountGroupRel.account_id.in_(unique_ids))
        )
        await self.db.execute(
            delete(SysAccountIdentity).where(SysAccountIdentity.account_id.in_(unique_ids))
        )
        await self.db.execute(
            delete(AdminUserProfile).where(AdminUserProfile.account_id.in_(unique_ids))
        )
        await self.db.execute(
            delete(PortalUserProfile).where(PortalUserProfile.account_id.in_(unique_ids))
        )
        await self.db.execute(delete(SysAccount).where(SysAccount.id.in_(unique_ids)))
        await self.db.flush()

    async def count_accounts_in_scope(
        self,
        account_ids: list[str],
        data_scope_filter: ColumnElement[bool],
    ) -> int:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return 0
        stmt = (
            select(func.count(func.distinct(SysAccount.id)))
            .outerjoin(SysAccountDeptRel, SysAccountDeptRel.account_id == SysAccount.id)
            .where(SysAccount.id.in_(unique_ids), data_scope_filter)
        )
        return int((await self.db.execute(stmt)).scalar_one())

    async def page_admin(
        self,
        query: AccountAdminPageQuery,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> tuple[list[SysAccount], int]:
        account_identity = SysAccountIdentity
        stmt: Select[tuple[SysAccount]] = (
            select(SysAccount)
            .join(
                account_identity,
                (account_identity.account_id == SysAccount.id)
                & (account_identity.identity_type == AccountIdentityType.ACCOUNT.value),
            )
            .outerjoin(AdminUserProfile, AdminUserProfile.account_id == SysAccount.id)
            .outerjoin(PortalUserProfile, PortalUserProfile.account_id == SysAccount.id)
        )
        count_stmt = (
            select(func.count(func.distinct(SysAccount.id)))
            .join(
                account_identity,
                (account_identity.account_id == SysAccount.id)
                & (account_identity.identity_type == AccountIdentityType.ACCOUNT.value),
            )
            .outerjoin(AdminUserProfile, AdminUserProfile.account_id == SysAccount.id)
            .outerjoin(PortalUserProfile, PortalUserProfile.account_id == SysAccount.id)
        )
        if data_scope_filter is not None:
            stmt = stmt.outerjoin(
                SysAccountDeptRel,
                SysAccountDeptRel.account_id == SysAccount.id,
            )
            count_stmt = count_stmt.outerjoin(
                SysAccountDeptRel,
                SysAccountDeptRel.account_id == SysAccount.id,
            )
        filters = []
        if query.account:
            filters.append(account_identity.identifier.contains(query.account))
        if query.name:
            filters.append(
                or_(
                    AdminUserProfile.name.contains(query.name),
                    PortalUserProfile.name.contains(query.name),
                )
            )
        if query.phone:
            filters.append(
                or_(
                    AdminUserProfile.phone.contains(query.phone),
                    PortalUserProfile.phone.contains(query.phone),
                )
            )
        if query.email:
            filters.append(
                or_(
                    AdminUserProfile.email.contains(query.email),
                    PortalUserProfile.email.contains(query.email),
                )
            )
        if query.account_type:
            filters.append(SysAccount.account_type == query.account_type.value)
        if query.account_status:
            filters.append(SysAccount.account_status == query.account_status.value)
        if data_scope_filter is not None:
            filters.append(data_scope_filter)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(SysAccount.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        accounts = list((await self.db.execute(stmt)).unique().scalars().all())
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

    async def list_all_roles(
        self,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[SysRole]:
        stmt = select(SysRole).order_by(SysRole.sort.asc(), SysRole.id.desc())
        if data_scope_filter is not None:
            stmt = stmt.where(data_scope_filter)
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_account_direct_role_ids(
        self,
        account_id: str,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[str]:
        await self.get_required(account_id)
        stmt = select(SysAccountRoleRel.role_id).where(SysAccountRoleRel.account_id == account_id)
        if data_scope_filter is not None:
            stmt = stmt.join(SysRole, SysRole.id == SysAccountRoleRel.role_id).where(
                data_scope_filter
            )
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def replace_account_roles(self, payload: AccountGrantRoleRequest) -> None:
        await self.get_required(payload.id)
        role_ids = list(dict.fromkeys(payload.role_ids))
        if role_ids:
            stmt = select(SysRole.id).where(SysRole.id.in_(role_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(role_ids):
                raise NotFoundError("Role not found")
        await self.db.execute(
            delete(SysAccountRoleRel).where(SysAccountRoleRel.account_id == payload.id)
        )
        for role_id in role_ids:
            self.db.add(SysAccountRoleRel(account_id=payload.id, role_id=role_id))
        await self.db.flush()

    async def list_all_groups(
        self,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[SysGroup]:
        stmt = select(SysGroup).order_by(SysGroup.id.desc())
        if data_scope_filter is not None:
            stmt = stmt.where(data_scope_filter)
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_account_direct_group_ids(
        self,
        account_id: str,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> list[str]:
        await self.get_required(account_id)
        stmt = select(SysAccountGroupRel.group_id).where(
            SysAccountGroupRel.account_id == account_id
        )
        if data_scope_filter is not None:
            stmt = stmt.join(SysGroup, SysGroup.id == SysAccountGroupRel.group_id).where(
                data_scope_filter
            )
        return [str(value) for value in (await self.db.execute(stmt)).scalars().all()]

    async def replace_account_groups(self, payload: AccountGrantGroupRequest) -> None:
        await self.get_required(payload.id)
        group_ids = list(dict.fromkeys(payload.group_ids))
        if group_ids:
            stmt = select(SysGroup.id).where(SysGroup.id.in_(group_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(group_ids):
                raise NotFoundError("Group not found")
        await self.db.execute(
            delete(SysAccountGroupRel).where(SysAccountGroupRel.account_id == payload.id)
        )
        for group_id in group_ids:
            self.db.add(SysAccountGroupRel(account_id=payload.id, group_id=group_id))
        await self.db.flush()

    async def list_account_dept_grants(
        self,
        account_id: str,
        visible_dept_ids: list[str] | None = None,
    ) -> list[AccountDeptGrantInfo]:
        await self.get_required(account_id)
        stmt = (
            select(SysAccountDeptRel)
            .where(SysAccountDeptRel.account_id == account_id)
            .order_by(SysAccountDeptRel.id.asc())
        )
        if visible_dept_ids is not None:
            stmt = stmt.where(SysAccountDeptRel.dept_id.in_(visible_dept_ids))
        grants = list((await self.db.execute(stmt)).scalars().all())
        return [
            AccountDeptGrantInfo(dept_id=grant.dept_id, is_primary=grant.is_primary)
            for grant in grants
        ]

    async def replace_account_depts(self, payload: AccountGrantDeptRequest) -> None:
        await self.get_required(payload.id)
        dept_ids = list(dict.fromkeys(item.dept_id for item in payload.grant_info_list))
        if dept_ids:
            stmt = select(SysDept.id).where(SysDept.id.in_(dept_ids))
            existing_ids = set((await self.db.execute(stmt)).scalars().all())
            if len(existing_ids) != len(dept_ids):
                raise NotFoundError("Dept not found")
        primary_seen = False
        await self.db.execute(
            delete(SysAccountDeptRel).where(SysAccountDeptRel.account_id == payload.id)
        )
        for item in payload.grant_info_list:
            is_primary = bool(item.is_primary) and not primary_seen
            primary_seen = primary_seen or is_primary
            self.db.add(
                SysAccountDeptRel(
                    account_id=payload.id,
                    dept_id=item.dept_id,
                    is_primary=is_primary,
                )
            )
        await self.db.flush()

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

    async def list_permission_grants(self, account_id: str) -> list[AccountPermissionGrantInfo]:
        await self.get_required(account_id)
        stmt = (
            select(SysSubjectPermissionGrantRel)
            .where(
                SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ACCOUNT.value,
                SysSubjectPermissionGrantRel.subject_id == account_id,
            )
            .order_by(SysSubjectPermissionGrantRel.id.asc())
        )
        grants = list((await self.db.execute(stmt)).scalars().all())
        return [
            AccountPermissionGrantInfo(
                permission_key=grant.permission_key,
                data_scope=grant.data_scope,
                custom_scope_dept_ids=list(grant.custom_scope_dept_ids),
            )
            for grant in grants
        ]

    async def replace_permission_grants(self, payload: AccountGrantPermissionRequest) -> None:
        await self.get_required(payload.id)
        await self.db.execute(
            delete(SysSubjectPermissionGrantRel).where(
                SysSubjectPermissionGrantRel.subject_type == GrantSubjectType.ACCOUNT.value,
                SysSubjectPermissionGrantRel.subject_id == payload.id,
            )
        )
        for grant in payload.grant_info_list:
            self.db.add(
                SysSubjectPermissionGrantRel(
                    subject_type=GrantSubjectType.ACCOUNT.value,
                    subject_id=payload.id,
                    permission_key=grant.permission_key,
                    data_scope=grant.data_scope.value,
                    custom_scope_dept_ids=list(grant.custom_scope_dept_ids),
                )
            )
        await self.db.flush()
