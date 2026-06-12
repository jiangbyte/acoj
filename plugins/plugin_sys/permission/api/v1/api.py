"""Permission API — mirrors hei-gin plugin-sys/permission/api/v1/api.go."""

from typing import List
from fastapi import APIRouter, Query, Request
from sdk.web.result import Result, success
from sdk.kernel.plugin import Perm
from ...service import PermissionService

router = APIRouter()


@router.get(
    "/api/v1/sys/permission/modules",
    summary="获取权限模块列表",
    response_model=Result[List[str]],
)
@Perm("sys:permission:modules", "权限模块列表")
async def list_modules(
    request: Request,
):
    service = PermissionService()
    return success(await service.list_modules())


@router.get(
    "/api/v1/sys/permission/by-module",
    summary="根据模块获取权限列表",
    response_model=Result,
)
@Perm("sys:permission:by-module", "按模块查询权限")
async def by_module(
    request: Request,
    module: str = Query(...),
):
    service = PermissionService()
    return success(await service.list_permissions_by_module(module))
