from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.user.portal.repository import PortalUserProfileRepository
from app.modules.user.portal.schema import PortalProfileUpsertPayload


class PortalUserProfileService:
    """门户账户资料服务，负责资料初始化和显式查询。"""

    def __init__(self, db: AsyncSession):
        self.repo = PortalUserProfileRepository(db)

    async def create_default_profile(self, account_id: str):
        """为门户账户创建默认资料记录，避免把关联维护下放给数据库。"""
        return await self.repo.create_default(account_id)

    async def upsert_profile(self, payload: PortalProfileUpsertPayload):
        """创建或更新门户资料。"""
        return await self.repo.upsert(payload)

    async def get_profile(self, account_id: str):
        """按账户 ID 查询门户资料。"""
        return await self.repo.get_by_account_id(account_id)
