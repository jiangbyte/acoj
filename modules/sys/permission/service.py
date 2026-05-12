from typing import Optional, List
from sqlalchemy.orm import Session
import logging

logger = logging.getLogger(__name__)


class PermissionService:
    def __init__(self, db: Session):
        self.db = db

    async def list_modules(self) -> List[str]:
        """Get distinct permission module prefixes from Redis cache."""
        from core.auth.permission_scan import get_modules_from_redis
        return await get_modules_from_redis()

    async def list_permissions_by_module(self, module: str) -> List[dict]:
        """Get permissions under a module from Redis cache."""
        from core.auth.permission_scan import get_permissions_by_module_from_redis
        return await get_permissions_by_module_from_redis(module)
