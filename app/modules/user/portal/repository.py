from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.user.portal.model import PortalUserProfile


class PortalUserProfileRepository:
    """门户账户资料仓储，负责门户资料主键写入与按账户查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create_default(self, account_id: str) -> PortalUserProfile:
        """为门户账户创建默认扩展资料记录。"""
        profile = PortalUserProfile(account_id=account_id)
        self.db.add(profile)
        await self.db.flush()
        return profile

    async def get_by_account_id(self, account_id: str) -> PortalUserProfile | None:
        """按账户 ID 查询门户资料记录。"""
        stmt = select(PortalUserProfile).where(PortalUserProfile.account_id == account_id)
        return (await self.db.execute(stmt)).scalar_one_or_none()
