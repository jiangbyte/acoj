from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthorizationError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.core.security.data_scope import build_data_scope_filter, resolve_data_scope_dept_ids
from app.core.security.session import SessionPayload
from app.modules.auth.service import AuthService
from app.modules.iam.account.model import SysAccount, SysAccountDeptRel
from app.modules.iam.account.service import AccountService
from app.modules.iam.permission.service import PermissionService, ensure_registered_permission
from app.modules.iam.resource.service import ResourceService
from app.modules.iam.role.model import SysRole
from app.modules.iam.role.repository import RoleRepository
from app.modules.iam.role.schema import (
    RoleAdminPageQuery,
    RoleGrantResourceRequest,
    RoleGrantUserRequest,
    RoleOwnPermissionDetailResponse,
    RoleGrantPermissionRequest,
    RoleOwnPermissionResponse,
    RoleOwnResourceResponse,
    RoleOwnUserResponse,
    RoleCreateRequest,
    RoleUpdateRequest,
    SysRoleSchema,
)
from app.platform.db.transaction import transactional


class RoleService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = RoleRepository(db)

    async def create(self, payload: RoleCreateRequest, session: SessionPayload | None = None) -> None:
        if session is not None and payload.owner_dept_id:
            await self._ensure_depts_visible(session, "iam:role:create", [payload.owner_dept_id])
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: RoleUpdateRequest, session: SessionPayload | None = None) -> None:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:update", [payload.id])
            if payload.owner_dept_id:
                await self._ensure_depts_visible(session, "iam:role:update", [payload.owner_dept_id])
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest, session: SessionPayload | None = None) -> None:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:delete", payload.ids)
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery, session: SessionPayload | None = None) -> SysRoleSchema:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:detail", [query.id])
        return to_schema(SysRoleSchema, await self.repo.get_required(query.id))

    async def page_admin(
        self,
        query: RoleAdminPageQuery,
        session: SessionPayload | None = None,
    ) -> PageData[SysRoleSchema]:
        data_scope_filter = (
            await self._role_scope_filter(session, "iam:role:page")
            if session is not None
            else None
        )
        items, total = await self.repo.page_admin(query, data_scope_filter)
        return build_page(query.pagination, total, to_schema_list(SysRoleSchema, items))

    async def permission_tree_selector(self) -> list[str]:
        return await PermissionService().list_permission_resources()

    async def own_permission(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> RoleOwnPermissionResponse:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:ownpermission", [query.id])
        return RoleOwnPermissionResponse(
            id=query.id,
            grant_info_list=await self.repo.list_permission_grants(query.id),
        )

    async def own_permission_detail(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> RoleOwnPermissionDetailResponse:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:ownpermission", [query.id])
        return RoleOwnPermissionDetailResponse(
            id=query.id,
            permissions=await ResourceService(self.db).list_permission_registry_items(),
            grant_info_list=await self.repo.list_permission_grants(query.id),
        )

    async def grant_permission(
        self,
        payload: RoleGrantPermissionRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:grantpermission", [payload.id])
            await self._ensure_depts_visible(
                session,
                "iam:role:grantpermission",
                [dept_id for grant in payload.grant_info_list for dept_id in grant.custom_scope_dept_ids],
            )
        for grant in payload.grant_info_list:
            await ensure_registered_permission(grant.permission_key)
        async with transactional(self.db):
            old_account_ids = await self.repo.list_account_ids_by_role(payload.id)
            await self.repo.replace_permission_grants(payload)
        await self._refresh_accounts(old_account_ids)

    async def own_resource(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> RoleOwnResourceResponse:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:ownresource", [query.id])
        return RoleOwnResourceResponse(
            id=query.id,
            modules=await ResourceService(self.db).list_grant_modules(),
            grant_info_list=await self.repo.list_resource_grants(query.id),
        )

    async def grant_resource(
        self,
        payload: RoleGrantResourceRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:grantresource", [payload.id])
        async with transactional(self.db):
            old_account_ids = await self.repo.list_account_ids_by_role(payload.id)
            await self.repo.replace_resource_grants(payload)
        await self._refresh_accounts(old_account_ids)

    async def own_user(
        self,
        query: IdQuery,
        session: SessionPayload | None = None,
    ) -> RoleOwnUserResponse:
        account_filter = (
            await self._account_scope_filter(session, "iam:role:ownuser")
            if session is not None
            else None
        )
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:ownuser", [query.id])
        users = await self.repo.list_accounts(account_filter)
        role_users = await self.repo.list_role_accounts(query.id, account_filter)
        return RoleOwnUserResponse(
            id=query.id,
            users=await AccountService(self.db).build_account_schemas(users),
            account_ids=[account.id for account in role_users],
        )

    async def grant_user(
        self,
        payload: RoleGrantUserRequest,
        session: SessionPayload | None = None,
    ) -> None:
        if session is not None:
            await self._ensure_roles_visible(session, "iam:role:grantuser", [payload.id])
            await self._ensure_accounts_visible(session, "iam:role:grantuser", payload.account_ids)
        async with transactional(self.db):
            old_account_ids = await self.repo.list_account_ids_by_role(payload.id)
            await self.repo.replace_role_accounts(payload)
        await self._refresh_accounts(sorted(set(old_account_ids + payload.account_ids)))

    async def _refresh_accounts(self, account_ids: list[str]) -> None:
        await AuthService(self.db).refresh_accounts_sessions(sorted(set(account_ids)))

    async def _role_scope_filter(self, session: SessionPayload, permission_key: str):
        return await build_data_scope_filter(
            self.db,
            session,
            permission_key,
            owner_column=SysRole.created_by,
            dept_column=SysRole.owner_dept_id,
        )

    async def _account_scope_filter(self, session: SessionPayload, permission_key: str):
        return await build_data_scope_filter(
            self.db,
            session,
            permission_key,
            owner_column=SysAccount.id,
            dept_column=SysAccountDeptRel.dept_id,
        )

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
        if await self.repo.count_roles_in_scope(unique_ids, data_scope_filter) != len(unique_ids):
            raise AuthorizationError("Role is outside current data scope")

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
        if await AccountService(self.db).repo.count_accounts_in_scope(unique_ids, data_scope_filter) != len(unique_ids):
            raise AuthorizationError("Account is outside current data scope")

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
        allowed_ids = set(visible_dept_ids)
        if any(dept_id not in allowed_ids for dept_id in unique_ids):
            raise AuthorizationError("Dept is outside current data scope")
