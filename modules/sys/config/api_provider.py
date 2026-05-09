"""ConfigApi — in-module provider for reading config values.

Used by FileService and other modules to read sys_config values
from the database with Redis caching. Callers pass a db session.
"""
from typing import Optional
from sqlalchemy.orm import Session
from .service import ConfigService


def get_config_value(db: Session, key: str) -> Optional[str]:
    """Read a config value by key, with Redis caching."""
    service = ConfigService(db)
    return service.get_value_by_key(key)
