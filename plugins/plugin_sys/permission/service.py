from typing import List

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db


class PermissionService:

    async def list_modules(self) -> List[str]:
        """Get distinct permission module prefixes from Redis cache."""
        from sdk.auth.permission_scan import get_modules_from_redis
        return await get_modules_from_redis()

    async def list_permissions_by_module(self, module: str) -> List[dict]:
        """Get permissions under a module from Redis cache."""
        from sdk.auth.permission_scan import get_permissions_by_module_from_redis
        return await get_permissions_by_module_from_redis(module)


def get_permission_service(db: Session = Depends(get_db)) -> PermissionService:
    return PermissionService()
