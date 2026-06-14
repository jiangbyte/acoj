from typing import List

from fastapi import Depends
from micosauth.utils.permission import MicosPermissionUtil
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.kernel.plugin.core_plugins import get_current_app


class PermissionService:
    def _collect_permissions(self) -> list[dict]:
        app = get_current_app()
        if app is None:
            return []
        items = MicosPermissionUtil.collect_from_app(app)
        grouped = MicosPermissionUtil.group_by_route(items)

        permissions: list[dict] = []
        seen: set[str] = set()
        for item in grouped:
            realm = str((item.get("realms") or [""])[0] or "")
            name = str(item.get("name") or "")
            for code in item.get("permissions") or []:
                value = str(code or "")
                if not value or value in seen:
                    continue
                seen.add(value)
                permissions.append(
                    {
                        "code": value,
                        "module": ":".join(value.split(":")[:-1]) if ":" in value else value,
                        "name": name,
                        "realm": realm,
                    }
                )
        return permissions

    async def list_modules(self) -> List[str]:
        """获取已注册权限模块列表。"""
        permissions = self._collect_permissions()
        modules = {entry.get("module") or "" for entry in permissions}
        return sorted(item for item in modules if item)

    async def list_permissions_by_module(self, module: str) -> List[dict]:
        """获取指定模块下的已注册权限。"""
        permissions = self._collect_permissions()
        result = []
        for entry in permissions:
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
