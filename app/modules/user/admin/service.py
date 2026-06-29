from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.user.admin.repository import AdminUserProfileRepository
from app.modules.user.admin.schema import AdminProfileUpsertPayload


class AdminUserProfileService:
    """管理端账户资料服务，负责资料初始化和显式查询。"""

    def __init__(self, db: AsyncSession):
        self.repo = AdminUserProfileRepository(db)

    async def create_default_profile(self, account_id: str):
        """为管理端账户创建默认资料记录，关联存在性由上层服务保证。"""
        return await self.repo.create_default(account_id)

    async def upsert_profile(self, payload: AdminProfileUpsertPayload):
        """创建或更新管理端资料。"""
        return await self.repo.upsert(payload)

    async def get_profile(self, account_id: str):
        """按账户 ID 查询管理端资料，不依赖 ORM relationship 自动装配。"""
        return await self.repo.get_by_account_id(account_id)
