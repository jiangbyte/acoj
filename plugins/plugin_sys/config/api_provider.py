"""Config value access helpers for cross-module reads."""
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from .service import get_value_by_key


async def get_config_value(db: AsyncSession, key: str) -> Optional[str]:
    del db
    return await get_value_by_key(key)
