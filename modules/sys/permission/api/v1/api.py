from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import Result, success
from core.db import get_db
from core.auth.decorator import HeiCheckPermission
from ...service import PermissionService

router = APIRouter()


@router.get(
    "/api/v1/sys/permission/modules",
    summary="获取权限模块列表")
@HeiCheckPermission("sys:permission:modules")
async def list_modules(
    request: Request,
    db: Session = Depends(get_db)
):
    service = PermissionService(db)
    return success(await service.list_modules())


@router.get(
    "/api/v1/sys/permission/by-module",
    summary="根据模块获取权限列表")
@HeiCheckPermission("sys:permission:by-module")
async def by_module(
    request: Request,
    module: str = Query(...),
    db: Session = Depends(get_db)
):
    service = PermissionService(db)
    return success(await service.list_permissions_by_module(module))
