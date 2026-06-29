from datetime import UTC, datetime, timedelta

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import AuthorizationError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest
from app.core.schema.base import to_schema
from app.core.security.data_scope import build_data_scope_filter, resolve_data_scope_dept_ids
from app.core.security.password import hash_password
from app.core.security.permission_registry import list_registered_permission_keys
from app.core.security.session import SessionPayload
from app.modules.auth.service import AuthService
from app.modules.iam.enums import GrantSubjectType
from app.modules.iam.account.model import SysAccount, SysAccountDeptRel
from app.modules.iam.grant.repository import GrantRepository
from app.modules.iam.group.model import SysGroup
from app.modules.iam.permission.service import ensure_registered_permission
from app.modules.iam.resource.service import ResourceService
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.account.schema import (
    AccountCreateRequest,
    AccountAdminPageQuery,
    AccountDeptAssignRequest,
    AccountGrantDeptRequest,
    AccountGrantGroupRequest,
    AccountGrantPermissionRequest,
    AccountGrantResourceRequest,
    AccountGrantRoleRequest,
    AccountGroupAssignRequest,
    AccountOwnDeptResponse,
    AccountOwnGroupResponse,
    AccountOwnPermissionDetailResponse,
    AccountOwnPermissionResponse,
    AccountOwnResourceResponse,
    AccountOwnRoleResponse,
    AccountPermissionGrantInfo,
    AccountResourceGrantInfo,
    AccountRoleAssignRequest,
    AccountUpdateRequest,
    SysAccountDeptRelSchema,
    SysAccountGroupRelSchema,
    SysAccountRoleRelSchema,
    SysAccountSchema,
)
from app.modules.user.admin.service import AdminUserProfileService
from app.modules.user.admin.repository import AdminUserProfileRepository
from app.modules.user.admin.schema import AdminProfileUpsertPayload
from app.modules.user.portal.service import PortalUserProfileService
from app.modules.user.portal.repository import PortalUserProfileRepository
from app.modules.user.portal.schema import PortalProfileUpsertPayload
from app.modules.iam.role.model import SysRole
from app.platform.db.transaction import transactional


class AccountService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = AccountRepository(db)
        self.grant_repo = GrantRepository(db)

    async def create(self, payload: AccountCreateRequest) -> None:
        async with transactional(self.db):
            account = await self.repo.create(
                payload,
                password_hash=hash_password(payload.password),
            )
            if payload.account_type == AccountType.ADMIN:
                await AdminUserProfileService(self.db).upsert_profile(
                    self._admin_profile_payload(account.id, payload),
                )
            elif payload.account_type == AccountType.PORTAL:
                await PortalUserProfileService(self.db).upsert_profile(
                    self._portal_profile_payload(account.id, payload),
                )

    async def update(self, payload: AccountUpdateRequest, session: SessionPayload | None = None) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:update", [payload.id])
        async with transactional(self.db):
            password_hash = hash_password(payload.password) if payload.password else None
            await self.repo.update(payload, password_hash)
            account = await self.repo.get_required(payload.id)
            if account.account_type == AccountType.ADMIN.value:
                await AdminUserProfileService(self.db).upsert_profile(
                    self._admin_profile_payload(payload.id, payload),
                )
            elif account.account_type == AccountType.PORTAL.value:
                await PortalUserProfileService(self.db).upsert_profile(
                    self._portal_profile_payload(payload.id, payload),
                )

    async def delete(self, payload: IdsRequest, session: SessionPayload | None = None) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:delete", payload.ids)
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def purge_expired_cancelled_accounts(self, retention_days: int = 15) -> int:
        cutoff = datetime.now(UTC) - timedelta(days=retention_days)
        account_ids = await self.repo.list_expired_cancelled_account_ids(cutoff)
        if not account_ids:
            return 0
        accounts = await self.repo.list_accounts_by_ids(account_ids)
        session_targets = [(account.account_type, account.id) for account in accounts]
        async with transactional(self.db):
            await self.repo.purge_many(account_ids)
        auth_service = AuthService(self.db)
        for account_type, account_id in session_targets:
            await auth_service.delete_account_sessions(account_type, account_id)
        return len(account_ids)

    async def detail(self, query: IdQuery, session: SessionPayload | None = None) -> SysAccountSchema:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:detail", [query.id])
        accounts = [await self.repo.get_required(query.id)]
        return (await self.build_account_schemas(accounts))[0]

    async def page_admin(
        self,
        query: AccountAdminPageQuery,
        session: SessionPayload | None = None,
    ) -> PageData[SysAccountSchema]:
        data_scope_filter = (
            await self._account_scope_filter(session, "iam:account:page")
            if session is not None
            else None
        )
        accounts, total = await self.repo.page_admin(query, data_scope_filter)
        return build_page(query.pagination, total, await self.build_account_schemas(accounts))

    async def assign_account_role(
        self,
        payload: AccountRoleAssignRequest,
        session: SessionPayload | None = None,
    ) -> SysAccountRoleRelSchema:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantrole", [payload.account_id])
            await self._ensure_roles_visible(session, "iam:account:grantrole", [payload.role_id])
        async with transactional(self.db):
            return to_schema(
                SysAccountRoleRelSchema,
                await self.repo.assign_account_to_role(payload),
            )

    async def assign_account_group(
        self,
        payload: AccountGroupAssignRequest,
        session: SessionPayload | None = None,
    ) -> SysAccountGroupRelSchema:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantgroup", [payload.account_id])
            await self._ensure_groups_visible(session, "iam:account:grantgroup", [payload.group_id])
        async with transactional(self.db):
            return to_schema(
                SysAccountGroupRelSchema,
                await self.repo.assign_account_to_group(payload),
            )

    async def assign_account_dept(
        self,
        payload: AccountDeptAssignRequest,
        session: SessionPayload | None = None,
    ) -> SysAccountDeptRelSchema:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantdept", [payload.account_id])
            await self._ensure_depts_visible(session, "iam:account:grantdept", [payload.dept_id])
        async with transactional(self.db):
            return to_schema(
                SysAccountDeptRelSchema,
                await self.repo.assign_account_to_dept(payload),
            )

    async def own_permission(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> AccountOwnPermissionResponse:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:ownpermission", [query.id])
        return AccountOwnPermissionResponse(
            id=query.id,
            grant_info_list=[
                AccountPermissionGrantInfo.model_validate(grant)
                for grant in await self.grant_repo.list_subject_permission_grants(
                    GrantSubjectType.ACCOUNT,
                    query.id,
                )
            ],
        )

    async def grant_permission(
        self,
        payload: AccountGrantPermissionRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantpermission", [payload.id])
            await self._ensure_grant_custom_depts_visible(
                session,
                "iam:account:grantpermission",
                [dept_id for grant in payload.grant_info_list for dept_id in grant.custom_scope_dept_ids],
            )
        await self._ensure_registered_permissions(
            [grant.permission_key for grant in payload.grant_info_list]
        )
        async with transactional(self.db):
            await self.grant_repo.replace_subject_permission_grants(
                GrantSubjectType.ACCOUNT,
                payload.id,
                payload.grant_info_list,
            )
        await self._refresh_accounts([payload.id])

    async def own_permission_detail(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> AccountOwnPermissionDetailResponse:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:ownpermission", [query.id])
        return AccountOwnPermissionDetailResponse(
            id=query.id,
            permissions=await ResourceService(self.db).list_permission_registry_items(),
            grant_info_list=[
                AccountPermissionGrantInfo.model_validate(grant)
                for grant in await self.grant_repo.list_subject_permission_grants(
                    GrantSubjectType.ACCOUNT,
                    query.id,
                )
            ],
        )

    async def own_resource(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> AccountOwnResourceResponse:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:ownresource", [query.id])
        return AccountOwnResourceResponse(
            id=query.id,
            modules=await ResourceService(self.db).list_grant_modules(),
            grant_info_list=[
                AccountResourceGrantInfo.model_validate(grant)
                for grant in await self.grant_repo.list_subject_resource_grants(
                    GrantSubjectType.ACCOUNT,
                    query.id,
                )
            ],
        )

    async def grant_resource(
        self,
        payload: AccountGrantResourceRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantresource", [payload.id])
        async with transactional(self.db):
            await self.grant_repo.replace_subject_resource_grant_infos(
                GrantSubjectType.ACCOUNT,
                payload.id,
                payload.grant_info_list,
            )
        await self._refresh_accounts([payload.id])

    async def own_role(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> AccountOwnRoleResponse:
        role_filter = (
            await self._role_scope_filter(session, "iam:account:ownrole")
            if session is not None
            else None
        )
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:ownrole", [query.id])
        return AccountOwnRoleResponse(
            id=query.id,
            roles=await self.repo.list_all_roles(role_filter),
            role_ids=await self.repo.list_account_direct_role_ids(query.id, role_filter),
        )

    async def grant_role(
        self,
        payload: AccountGrantRoleRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantrole", [payload.id])
            await self._ensure_roles_visible(session, "iam:account:grantrole", payload.role_ids)
        async with transactional(self.db):
            await self.repo.replace_account_roles(payload)
        await self._refresh_accounts([payload.id])

    async def own_group(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> AccountOwnGroupResponse:
        group_filter = (
            await self._group_scope_filter(session, "iam:account:owngroup")
            if session is not None
            else None
        )
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:owngroup", [query.id])
        return AccountOwnGroupResponse(
            id=query.id,
            groups=await self.repo.list_all_groups(group_filter),
            group_ids=await self.repo.list_account_direct_group_ids(query.id, group_filter),
        )

    async def grant_group(
        self,
        payload: AccountGrantGroupRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantgroup", [payload.id])
            await self._ensure_groups_visible(session, "iam:account:grantgroup", payload.group_ids)
        async with transactional(self.db):
            await self.repo.replace_account_groups(payload)
        await self._refresh_accounts([payload.id])

    async def own_dept(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> AccountOwnDeptResponse:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:owndept", [query.id])
        visible_dept_ids = (
            await resolve_data_scope_dept_ids(self.db, session, "iam:account:owndept")
            if session is not None
            else None
        )
        return AccountOwnDeptResponse(
            id=query.id,
            grant_info_list=await self.repo.list_account_dept_grants(query.id, visible_dept_ids),
        )

    async def grant_dept(
        self,
        payload: AccountGrantDeptRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_accounts_visible(session, "iam:account:grantdept", [payload.id])
            await self._ensure_depts_visible(
                session,
                "iam:account:grantdept",
                [item.dept_id for item in payload.grant_info_list],
            )
        async with transactional(self.db):
            await self.repo.replace_account_depts(payload)
        await self._refresh_accounts([payload.id])

    async def _ensure_registered_permissions(self, permission_keys: list[str]) -> None:
        unique_permission_keys = sorted(set(permission_keys))
        if not unique_permission_keys:
            return
        registered_permission_keys = await list_registered_permission_keys()
        for permission_key in unique_permission_keys:
            if permission_key not in registered_permission_keys:
                await ensure_registered_permission(permission_key)

    async def _refresh_accounts(self, account_ids: list[str]) -> None:
        await AuthService(self.db).refresh_accounts_sessions(sorted(set(account_ids)))

    async def _account_scope_filter(self, session: SessionPayload, permission_key: str):
        return await build_data_scope_filter(
            self.db,
            session,
            permission_key,
            owner_column=SysAccount.id,
            dept_column=SysAccountDeptRel.dept_id,
        )

    async def _role_scope_filter(self, session: SessionPayload, permission_key: str):
        return await build_data_scope_filter(
            self.db,
            session,
            permission_key,
            owner_column=SysRole.created_by,
            dept_column=SysRole.owner_dept_id,
        )

    async def _group_scope_filter(self, session: SessionPayload, permission_key: str):
        return await build_data_scope_filter(
            self.db,
            session,
            permission_key,
            owner_column=SysGroup.created_by,
            dept_column=SysGroup.owner_dept_id,
        )

    async def _ensure_accounts_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        account_ids: list[str],
    ) -> None:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return
        data_scope_filter = await self._account_scope_filter(session, permission_key)
        if await self.repo.count_accounts_in_scope(unique_ids, data_scope_filter) != len(unique_ids):
            raise AuthorizationError("Account is outside current data scope")

    async def _ensure_roles_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        role_ids: list[str],
    ) -> None:
        unique_ids = list(dict.fromkeys(role_ids))
        if not unique_ids:
            return
        data_scope_filter = await self._role_scope_filter(session, permission_key)
        from app.modules.iam.role.repository import RoleRepository

        if await RoleRepository(self.db).count_roles_in_scope(unique_ids, data_scope_filter) != len(unique_ids):
            raise AuthorizationError("Role is outside current data scope")

    async def _ensure_groups_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        group_ids: list[str],
    ) -> None:
        unique_ids = list(dict.fromkeys(group_ids))
        if not unique_ids:
            return
        data_scope_filter = await self._group_scope_filter(session, permission_key)
        from app.modules.iam.group.repository import GroupRepository

        if await GroupRepository(self.db).count_groups_in_scope(unique_ids, data_scope_filter) != len(unique_ids):
            raise AuthorizationError("Group is outside current data scope")

    async def _ensure_depts_visible(
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
        if any(dept_id not in set(visible_dept_ids) for dept_id in unique_ids):
            raise AuthorizationError("Dept is outside current data scope")

    async def _ensure_grant_custom_depts_visible(
        self,
        session: SessionPayload,
        permission_key: str,
        dept_ids: list[str],
    ) -> None:
        await self._ensure_depts_visible(session, permission_key, dept_ids)

    def _admin_profile_payload(
        self,
        account_id: str,
        payload: AccountCreateRequest | AccountUpdateRequest,
    ) -> AdminProfileUpsertPayload:
        return AdminProfileUpsertPayload(
            account_id=account_id,
            name=payload.name,
            nickname=payload.nickname,
            avatar=payload.avatar,
            signature=payload.signature,
            phone=payload.phone,
            email=payload.email,
            employee_no=payload.employee_no,
            title=payload.title,
            remark=payload.remark,
        )

    def _portal_profile_payload(
        self,
        account_id: str,
        payload: AccountCreateRequest | AccountUpdateRequest,
    ) -> PortalProfileUpsertPayload:
        return PortalProfileUpsertPayload(
            account_id=account_id,
            name=payload.name,
            nickname=payload.nickname,
            avatar=payload.avatar,
            signature=payload.signature,
            phone=payload.phone,
            email=payload.email,
        )

    async def build_account_schemas(self, accounts: list) -> list[SysAccountSchema]:
        account_ids = [account.id for account in accounts]
        identities = await self.repo.list_identities_by_account_ids(account_ids)
        admin_profiles = await AdminUserProfileRepository(self.db).list_by_account_ids(account_ids)
        portal_profiles = await PortalUserProfileRepository(self.db).list_by_account_ids(account_ids)
        identity_map: dict[str, list] = {}
        for identity in identities:
            identity_map.setdefault(identity.account_id, []).append(identity)
        admin_profile_map = {profile.account_id: profile for profile in admin_profiles}
        portal_profile_map = {profile.account_id: profile for profile in portal_profiles}

        items: list[SysAccountSchema] = []
        for account in accounts:
            account_identities = identity_map.get(account.id, [])
            primary_identity = next(
                (
                    item
                    for item in account_identities
                    if item.identity_type == "ACCOUNT" and item.is_primary
                ),
                None,
            ) or next(
                (item for item in account_identities if item.identity_type == "ACCOUNT"),
                None,
            )
            email_identity = next(
                (item for item in account_identities if item.identity_type == "EMAIL"),
                None,
            )
            phone_identity = next(
                (item for item in account_identities if item.identity_type == "PHONE"),
                None,
            )
            profile = (
                admin_profile_map.get(account.id)
                if account.account_type == AccountType.ADMIN.value
                else portal_profile_map.get(account.id)
            )
            items.append(
                SysAccountSchema(
                    id=account.id,
                    account=getattr(primary_identity, "identifier", ""),
                    account_type=account.account_type,
                    account_status=account.account_status,
                    name=getattr(profile, "name", None) or "",
                    nickname=getattr(profile, "nickname", None),
                    avatar=getattr(profile, "avatar", None),
                    signature=getattr(profile, "signature", None),
                    phone=getattr(profile, "phone", None),
                    email=getattr(profile, "email", None),
                    employee_no=getattr(profile, "employee_no", None),
                    title=getattr(profile, "title", None),
                    remark=getattr(profile, "remark", None),
                    email_identity=getattr(email_identity, "identifier", None),
                    phone_identity=getattr(phone_identity, "identifier", None),
                    email_identity_verified=bool(getattr(email_identity, "verified", False)),
                    phone_identity_verified=bool(getattr(phone_identity, "verified", False)),
                    email_identity_bind_status=getattr(email_identity, "bind_status", None),
                    phone_identity_bind_status=getattr(phone_identity, "bind_status", None),
                    identities=account_identities,
                    cancelled_at=account.cancelled_at,
                    cancelled_by=account.cancelled_by,
                    cancel_reason=account.cancel_reason,
                    last_login_ip=account.last_login_ip,
                    last_login_address=account.last_login_address,
                    last_login_time=account.last_login_time,
                    last_login_device=account.last_login_device,
                    latest_login_ip=account.latest_login_ip,
                    latest_login_address=account.latest_login_address,
                    latest_login_time=account.latest_login_time,
                    latest_login_device=account.latest_login_device,
                    created_at=account.created_at,
                    created_by=account.created_by,
                    updated_at=account.updated_at,
                    updated_by=account.updated_by,
                )
            )
        return items
