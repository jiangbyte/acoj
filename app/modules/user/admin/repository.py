from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.user.admin.model import AdminUserProfile


class AdminUserProfileRepository:
    """管理端账户资料仓储，负责扩展资料的初始化与查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create_default(self, account_id: str) -> AdminUserProfile:
        """为管理端账户创建默认扩展资料记录。"""
        profile = AdminUserProfile(account_id=account_id)
        self.db.add(profile)
        await self.db.flush()
        return profile

    async def get_by_account_id(self, account_id: str) -> AdminUserProfile | None:
        """按账户 ID 查询管理端扩展资料。"""
        stmt = select(AdminUserProfile).where(AdminUserProfile.account_id == account_id)
        return (await self.db.execute(stmt)).scalar_one_or_none()
