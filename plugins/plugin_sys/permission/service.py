from typing import List

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.kernel.registry import get_registered_perm_entries


class PermissionService:

    async def list_modules(self) -> List[str]:
        """获取已注册权限模块列表。"""
        permissions = get_registered_perm_entries()
        modules = {entry.get("module") or "" for entry in permissions.values()}
        return sorted(item for item in modules if item)

    async def list_permissions_by_module(self, module: str) -> List[dict]:
        """获取指定模块下的已注册权限。"""
        permissions = get_registered_perm_entries()
        result = []
        for entry in permissions.values():
            if (entry.get("module") or "") == module:
                result.append(
                    {
                        "code": entry.get("code") or "",
                        "module": entry.get("module") or "",
                        "name": entry.get("name") or "",
                    }
                )
        return result


def get_permission_service(db: AsyncSession = Depends(get_db)) -> PermissionService:
    return PermissionService()
