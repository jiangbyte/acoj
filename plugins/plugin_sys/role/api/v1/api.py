"""Role API — mirrors hei-gin plugins/plugin-sys/role/api/v1/api.go 1:1."""

from fastapi import APIRouter, Depends, Query
from sdk.web.result import success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.kernel.plugin import Perm
from sdk.auth.decorator import NoRepeat
from sdk.log import SysLog
from ...params import RoleVO, RolePageParam, GrantPermissionParam, GrantResourceParam, RefreshRoleSessionACLParam
from ...service import RoleService, get_role_service

router = APIRouter()


@router.get("/api/v1/sys/role/page", summary="获取角色分页")
@Perm("sys:role:page", "角色分页")
async def page(param: RolePageParam = Depends(), service: RoleService = Depends(get_role_service)):
    return success(await service.page(param))


@router.post("/api/v1/sys/role/create", summary="添加角色")
@SysLog("添加角色")
@Perm("sys:role:create", "添加角色")
@NoRepeat(interval=3000)
async def create(
    vo: RoleVO,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/role/modify", summary="编辑角色")
@SysLog("编辑角色")
@Perm("sys:role:modify", "编辑角色")
async def modify(
    vo: RoleVO,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/role/remove", summary="删除角色")
@SysLog("删除角色")
@Perm("sys:role:remove", "删除角色")
async def remove(param: IdsParam, service: RoleService = Depends(get_role_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/role/detail", summary="获取角色详情")
@Perm("sys:role:detail", "角色详情")
async def detail(id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.detail(id))


@router.get("/api/v1/sys/role/own-permission", summary="角色拥有的权限码列表")
@Perm("sys:role:detail", "角色详情")
async def own_permission(id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.get_permission_codes(id))


@router.get("/api/v1/sys/role/own-permission-detail", summary="角色拥有的权限详情")
@Perm("sys:role:detail", "角色详情")
async def own_permission_detail(id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.get_permission_details(id))


@router.get("/api/v1/sys/role/own-resource", summary="角色拥有的资源ID列表")
@Perm("sys:role:detail", "角色详情")
async def own_resource(id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.get_resource_ids(id))


@router.post("/api/v1/sys/role/grant-permission", summary="分配角色权限")
@SysLog("分配角色权限")
@Perm("sys:role:grant", "分配角色权限")
async def grant_permission(
    param: GrantPermissionParam,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.grant_permissions(param.role_id, param.permissions, actor)
    return success()


@router.post("/api/v1/sys/role/grant-resource", summary="分配角色资源")
@SysLog("分配角色资源")
@Perm("sys:role:grant", "分配角色资源")
async def grant_resource(
    param: GrantResourceParam,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.grant_resources(param.role_id, param.resource_ids, param.permissions, actor)
    return success()


@router.post("/api/v1/sys/role/refresh-session-acl", summary="刷新角色会话权限")
@SysLog("刷新角色会话权限")
@Perm("sys:role:refresh-session-acl", "刷新角色会话权限")
@NoRepeat(interval=3000)
async def refresh_session_acl(
    param: RefreshRoleSessionACLParam,
    service: RoleService = Depends(get_role_service),
):
    await service.refresh_session_acl(param)
    return success()
