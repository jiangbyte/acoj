from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import AuthenticationError, NotFoundError
from app.core.security.password import hash_password, verify_password
from app.core.security.session import SessionPayload
from app.modules.iam.account.repository import AccountRepository
from app.modules.iam.enums import AccountIdentityType
from app.modules.user.portal.repository import PortalUserProfileRepository
from app.modules.user.portal.schema import (
    PortalProfileUpsertPayload,
    PortalPublicProfileResponse,
    PortalUserCenterEmailUpdateRequest,
    PortalUserCenterPasswordUpdateRequest,
    PortalUserCenterPhoneUpdateRequest,
    PortalUserCenterProfileUpdateRequest,
)
from app.platform.db.transaction import transactional


class PortalUserProfileService:
    """门户账户资料服务，负责资料初始化和显式查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = PortalUserProfileRepository(db)
        self.account_repo = AccountRepository(db)

    async def create_default_profile(self, account_id: str):
        """为门户账户创建默认资料记录，避免把关联维护下放给数据库。"""
        return await self.repo.create_default(account_id)

    async def upsert_profile(self, payload: PortalProfileUpsertPayload):
        """创建或更新门户资料。"""
        return await self.repo.upsert(payload)

    async def get_profile(self, account_id: str):
        """按账户 ID 查询门户资料。"""
        return await self.repo.get_by_account_id(account_id)

    async def get_public_profile(self, account_id: str) -> PortalPublicProfileResponse:
        """查询门户用户公开主页资料，不返回联系方式和授权信息。"""
        account = await self.account_repo.get_required(account_id)
        if account.account_type != AccountType.PORTAL.value:
            raise NotFoundError("Portal profile not found")
        profile = await self.repo.get_by_account_id(account_id)
        return PortalPublicProfileResponse(
            account_id=account_id,
            name=profile.name if profile else None,
            nickname=profile.nickname if profile else None,
            avatar=profile.avatar if profile else None,
            signature=profile.signature if profile else None,
            bio=profile.bio if profile else None,
            level=profile.level if profile else None,
        )

    async def update_current_profile(
        self,
        payload: PortalUserCenterProfileUpdateRequest,
        session: SessionPayload,
    ) -> None:
        profile = await self.repo.get_by_account_id(session.account_id)
        async with transactional(self.db):
            await self.repo.upsert(
                PortalProfileUpsertPayload(
                    account_id=session.account_id,
                    name=payload.name,
                    nickname=payload.nickname,
                    avatar=payload.avatar,
                    signature=payload.signature,
                    phone=profile.phone if profile else None,
                    email=profile.email if profile else None,
                    bio=payload.bio,
                    level=profile.level if profile else None,
                )
            )

    async def update_current_password(
        self,
        payload: PortalUserCenterPasswordUpdateRequest,
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
        payload: PortalUserCenterPhoneUpdateRequest,
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
                PortalProfileUpsertPayload(
                    account_id=session.account_id,
                    name=profile.name if profile else None,
                    nickname=profile.nickname if profile else None,
                    avatar=profile.avatar if profile else None,
                    signature=profile.signature if profile else None,
                    phone=payload.phone,
                    email=profile.email if profile else None,
                    bio=profile.bio if profile else None,
                    level=profile.level if profile else None,
                )
            )

    async def update_current_email(
        self,
        payload: PortalUserCenterEmailUpdateRequest,
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
                PortalProfileUpsertPayload(
                    account_id=session.account_id,
                    name=profile.name if profile else None,
                    nickname=profile.nickname if profile else None,
                    avatar=profile.avatar if profile else None,
                    signature=profile.signature if profile else None,
                    phone=profile.phone if profile else None,
                    email=payload.email,
                    bio=profile.bio if profile else None,
                    level=profile.level if profile else None,
                )
            )

    def _ensure_password(self, password_hash: str, password: str) -> None:
        if not verify_password(password, password_hash):
            raise AuthenticationError("Invalid account or password")
