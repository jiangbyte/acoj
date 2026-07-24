"""用户资料批量查询工具。

提供按 account_type + account_ids 批量查询 profile 的公共函数，
供消息模块等需要解析 created_by / updated_by 的场景复用。
"""

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import BusinessError
from app.modules.user.admin.model import AdminUserProfile
from app.modules.user.admin.repository import AdminUserProfileRepository
from app.modules.user.portal.model import PortalUserProfile
from app.modules.user.portal.repository import PortalUserProfileRepository


def _pick_profile_repo(db: AsyncSession, account_type: AccountType):
    """根据账户类型返回对应的 profile 仓库。"""
    if account_type == AccountType.ADMIN:
        return AdminUserProfileRepository(db)
    if account_type == AccountType.PORTAL:
        return PortalUserProfileRepository(db)
    raise ValueError(f"Unsupported account type: {account_type}")


def pick_profile_model(account_type: AccountType):
    """根据账户类型返回对应的 profile 模型类。"""
    if account_type == AccountType.ADMIN:
        return AdminUserProfile
    if account_type == AccountType.PORTAL:
        return PortalUserProfile
    raise BusinessError(f"Unsupported account type: {account_type}")


async def get_profile(
    db: AsyncSession, account_type: AccountType, account_id: str
) -> object | None:
    """获取单个用户的资料。"""
    repo = _pick_profile_repo(db, account_type)
    return await repo.get_by_account_id(account_id)


async def get_profiles_batch(
    db: AsyncSession, account_type: AccountType, account_ids: list[str],
) -> dict[str, object]:
    """批量获取同一类型用户的资料，返回 {account_id: profile} 映射。"""
    if not account_ids:
        return {}
    repo = _pick_profile_repo(db, account_type)
    profiles = await repo.list_by_account_ids(list(dict.fromkeys(account_ids)))
    return {p.account_id: p for p in profiles}
