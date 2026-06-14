"""Permission API — mirrors hei-gin plugin-sys/permission/api/v1/api.go."""

from typing import List
from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import Result, success
from ...service import PermissionService, get_permission_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get(
    "/api/v1/sys/permission/modules",
    summary="获取权限模块列表",
    response_model=Result[List[str]],
)
@require_permissions("sys:permission:modules", realm=BUSINESS_REALM_ID)
async def list_modules(
    request: Request,
    service: PermissionService = Depends(get_permission_service),
):
    return success(await service.list_modules())


@router.get(
    "/api/v1/sys/permission/by-module",
    summary="根据模块获取权限列表",
    response_model=Result,
)
@require_permissions("sys:permission:by-module", realm=BUSINESS_REALM_ID)
async def by_module(
    request: Request,
    module: str = Query(...),
    service: PermissionService = Depends(get_permission_service),
):
    return success(await service.list_permissions_by_module(module))
