"""Role API — mirrors hei-gin plugins/plugin-sys/role/api/v1/api.go 1:1."""

from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from sdk.web.result import success
from sdk.shared.types import IdParam, IdsParam
from sdk.infra.db import get_db
from sdk.kernel.plugin import Perm
from sdk.auth.decorator import NoRepeat
from sdk.log import SysLog
from ...params import RoleVO, RolePageParam, GrantPermissionParam, GrantResourceParam
from ...service import RoleService

router = APIRouter()


@router.get("/api/v1/sys/role/page", summary="获取角色分页")
@Perm("sys:role:page", "角色分页")
async def page(request: Request, param: RolePageParam = Depends(), db: Session = Depends(get_db)):
    return success(RoleService(db).page(param))


@router.post("/api/v1/sys/role/create", summary="添加角色")
@SysLog("添加角色")
@Perm("sys:role:create", "添加角色")
@NoRepeat(interval=3000)
async def create(request: Request, vo: RoleVO, db: Session = Depends(get_db)):
    await RoleService(db).create(vo, request)
    return success()


@router.post("/api/v1/sys/role/modify", summary="编辑角色")
@SysLog("编辑角色")
@Perm("sys:role:modify", "编辑角色")
async def modify(request: Request, vo: RoleVO, db: Session = Depends(get_db)):
    await RoleService(db).modify(vo, request)
    return success()


@router.post("/api/v1/sys/role/remove", summary="删除角色")
@SysLog("删除角色")
@Perm("sys:role:remove", "删除角色")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    RoleService(db).remove(param.ids)
    return success()


@router.get("/api/v1/sys/role/detail", summary="获取角色详情")
@Perm("sys:role:detail", "角色详情")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    data = RoleService(db).detail(id)
    return success(data if data else None)


@router.get("/api/v1/sys/role/own-permission", summary="角色拥有的权限码列表")
@Perm("sys:role:detail", "角色详情")
async def own_permission(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    return success(RoleService(db).get_role_permission_codes(id))


@router.get("/api/v1/sys/role/own-permission-detail", summary="角色拥有的权限详情")
@Perm("sys:role:detail", "角色详情")
async def own_permission_detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    return success(RoleService(db).get_role_permission_details(id))


@router.get("/api/v1/sys/role/own-resource", summary="角色拥有的资源ID列表")
@Perm("sys:role:detail", "角色详情")
async def own_resource(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    return success(RoleService(db).get_role_resource_ids(id))


@router.post("/api/v1/sys/role/grant-permission", summary="分配角色权限")
@SysLog("分配角色权限")
@Perm("sys:role:grant", "分配角色权限")
async def grant_permission(request: Request, param: GrantPermissionParam, db: Session = Depends(get_db)):
    service = RoleService(db)
    await service.grant_permissions(param.role_id, param.permissions, request)
    return success()


@router.post("/api/v1/sys/role/grant-resource", summary="分配角色资源")
@SysLog("分配角色资源")
@Perm("sys:role:grant", "分配角色资源")
async def grant_resource(request: Request, param: GrantResourceParam, db: Session = Depends(get_db)):
    service = RoleService(db)
    await service.grant_resources(param.role_id, param.resource_ids, param.permissions, request)
    return success()
