"""Config value access helpers for cross-module reads."""
from typing import Optional
from sqlalchemy.orm import Session
from .service import get_value_by_key


async def get_config_value(db: Session, key: str) -> Optional[str]:
    del db
    return await get_value_by_key(key)
