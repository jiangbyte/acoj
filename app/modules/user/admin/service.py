from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthenticationError
from app.core.security.password import hash_password, verify_password
from app.core.security.session import SessionPayload
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.enums import AccountIdentityType
from app.modules.iam.dept.repository import DeptRepository
from app.modules.iam.group.repository import GroupRepository
from app.modules.iam.role.repository import RoleRepository
from app.modules.user.admin.repository import AdminUserProfileRepository
from app.modules.user.admin.schema import (
    AdminProfileUpsertPayload,
    AdminUserCenterEmailUpdateRequest,
    AdminUserCenterOrgInfoResponse,
    AdminUserCenterPasswordUpdateRequest,
    AdminUserCenterPhoneUpdateRequest,
    AdminUserCenterProfileUpdateRequest,
    IdNameResponse,
)
from app.platform.db.transaction import transactional


class AdminUserProfileService:
    """管理端账户资料服务，负责资料初始化和显式查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = AdminUserProfileRepository(db)
        self.account_repo = AccountRepository(db)

    async def create_default_profile(self, account_id: str):
        """为管理端账户创建默认资料记录，关联存在性由上层服务保证。"""
        return await self.repo.create_default(account_id)

    async def upsert_profile(self, payload: AdminProfileUpsertPayload):
        """创建或更新管理端资料。"""
        return await self.repo.upsert(payload)

    async def get_profile(self, account_id: str):
        """按账户 ID 查询管理端资料，不依赖 ORM relationship 自动装配。"""
        return await self.repo.get_by_account_id(account_id)

    async def get_org_info(self, session: SessionPayload) -> AdminUserCenterOrgInfoResponse:
        """批量查询当前账户组织信息，避免前端或服务端逐个 ID 查询。"""
        role_id_names, dept_id_names, group_id_names = await self.get_id_name_groups(
            session.role_ids,
            session.dept_ids,
            session.group_ids,
        )
        return AdminUserCenterOrgInfoResponse(
            role_id_names=role_id_names,
            dept_id_names=dept_id_names,
            group_id_names=group_id_names,
        )

    async def get_id_name_groups(
        self,
        role_ids: list[str],
        dept_ids: list[str],
        group_ids: list[str],
    ) -> tuple[list[IdNameResponse], list[IdNameResponse], list[IdNameResponse]]:
        roles = await RoleRepository(self.db).list_by_ids(role_ids)
        depts = await DeptRepository(self.db).list_by_ids(dept_ids)
        groups = await GroupRepository(self.db).list_by_ids(group_ids)
        role_map = {item.id: item.name for item in roles}
        dept_map = {item.id: item.name for item in depts}
        group_map = {item.id: item.name for item in groups}
        return (
            self._build_id_names(role_ids, role_map),
            self._build_id_names(dept_ids, dept_map),
            self._build_id_names(group_ids, group_map),
        )

    async def update_current_profile(
        self,
        payload: AdminUserCenterProfileUpdateRequest,
        session: SessionPayload,
    ) -> None:
        profile = await self.repo.get_by_account_id(session.account_id)
        async with transactional(self.db):
            await self.repo.upsert(
                AdminProfileUpsertPayload(
                    account_id=session.account_id,
                    name=payload.name,
                    nickname=payload.nickname,
                    avatar=payload.avatar,
                    signature=payload.signature,
                    phone=profile.phone if profile else None,
                    email=profile.email if profile else None,
                    title=payload.title,
                    employee_no=payload.employee_no,
                    remark=payload.remark,
                )
            )

    async def update_current_password(
        self,
        payload: AdminUserCenterPasswordUpdateRequest,
        session: SessionPayload,
    ) -> None:
        account = await self.account_repo.get_required(session.account_id)
        self._ensure_password(account.password_hash, payload.old_password)
        async with transactional(self.db):
            await self.account_repo.update_password_hash(
                session.account_id,
                hash_password(payload.new_password),
            )
        from app.modules.auth.service import AuthService

        await AuthService(self.db).refresh_account_sessions(session.account_id)

    async def update_current_phone(
        self,
        payload: AdminUserCenterPhoneUpdateRequest,
        session: SessionPayload,
    ) -> None:
        account = await self.account_repo.get_required(session.account_id)
        self._ensure_password(account.password_hash, payload.password)
        profile = await self.repo.get_by_account_id(session.account_id)
        async with transactional(self.db):
            await self.account_repo.upsert_account_identity(
                session.account_id,
                AccountIdentityType.PHONE,
                payload.phone,
            )
            await self.repo.upsert(
                AdminProfileUpsertPayload(
                    account_id=session.account_id,
                    name=profile.name if profile else None,
                    nickname=profile.nickname if profile else None,
                    avatar=profile.avatar if profile else None,
                    signature=profile.signature if profile else None,
                    phone=payload.phone,
                    email=profile.email if profile else None,
                    title=profile.title if profile else None,
                    employee_no=profile.employee_no if profile else None,
                    remark=profile.remark if profile else None,
                )
            )

    async def update_current_email(
        self,
        payload: AdminUserCenterEmailUpdateRequest,
        session: SessionPayload,
    ) -> None:
        account = await self.account_repo.get_required(session.account_id)
        self._ensure_password(account.password_hash, payload.password)
        profile = await self.repo.get_by_account_id(session.account_id)
        async with transactional(self.db):
            await self.account_repo.upsert_account_identity(
                session.account_id,
                AccountIdentityType.EMAIL,
                payload.email,
            )
            await self.repo.upsert(
                AdminProfileUpsertPayload(
                    account_id=session.account_id,
                    name=profile.name if profile else None,
                    nickname=profile.nickname if profile else None,
                    avatar=profile.avatar if profile else None,
                    signature=profile.signature if profile else None,
                    phone=profile.phone if profile else None,
                    email=payload.email,
                    title=profile.title if profile else None,
                    employee_no=profile.employee_no if profile else None,
                    remark=profile.remark if profile else None,
                )
            )

    def _ensure_password(self, password_hash: str, password: str) -> None:
        if not verify_password(password, password_hash):
            raise AuthenticationError("Invalid account or password")

    def _build_id_names(
        self,
        ids: list[str],
        name_map: dict[str, str],
    ) -> list[IdNameResponse]:
        return [
            IdNameResponse(id=item_id, name=name_map[item_id])
            for item_id in ids
            if item_id in name_map
        ]
