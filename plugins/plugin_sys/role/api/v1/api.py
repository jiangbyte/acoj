"""Role API — mirrors hei-gin plugins/plugin-sys/role/api/v1/api.go 1:1."""

from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import RoleVO, RolePageParam, GrantPermissionParam, GrantResourceParam, RefreshRoleSessionACLParam
from ...service import RoleService, get_role_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get("/api/v1/sys/role/page", summary="获取角色分页")
@require_permissions("sys:role:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: RolePageParam = Depends(), service: RoleService = Depends(get_role_service)):
    return success(await service.page(param))


@router.post("/api/v1/sys/role/create", summary="添加角色")
@SysLog("添加角色")
@require_permissions("sys:role:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def create(
    request: Request,
    vo: RoleVO,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/role/modify", summary="编辑角色")
@SysLog("编辑角色")
@require_permissions("sys:role:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: RoleVO,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/role/remove", summary="删除角色")
@SysLog("删除角色")
@require_permissions("sys:role:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: RoleService = Depends(get_role_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/role/detail", summary="获取角色详情")
@require_permissions("sys:role:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.detail(id))


@router.get("/api/v1/sys/role/own-permission", summary="角色拥有的权限码列表")
@require_permissions("sys:role:detail", realm=BUSINESS_REALM_ID)
async def own_permission(request: Request, id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.get_permission_codes(id))


@router.get("/api/v1/sys/role/own-permission-detail", summary="角色拥有的权限详情")
@require_permissions("sys:role:detail", realm=BUSINESS_REALM_ID)
async def own_permission_detail(request: Request, id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.get_permission_details(id))


@router.get("/api/v1/sys/role/own-resource", summary="角色拥有的资源ID列表")
@require_permissions("sys:role:detail", realm=BUSINESS_REALM_ID)
async def own_resource(request: Request, id: str = Query(...), service: RoleService = Depends(get_role_service)):
    return success(await service.get_resource_ids(id))


@router.post("/api/v1/sys/role/grant-permission", summary="分配角色权限")
@SysLog("分配角色权限")
@require_permissions("sys:role:grant", realm=BUSINESS_REALM_ID)
async def grant_permission(
    request: Request,
    param: GrantPermissionParam,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.grant_permissions(param.role_id, param.permissions, actor)
    return success()


@router.post("/api/v1/sys/role/grant-resource", summary="分配角色资源")
@SysLog("分配角色资源")
@require_permissions("sys:role:grant", realm=BUSINESS_REALM_ID)
async def grant_resource(
    request: Request,
    param: GrantResourceParam,
    service: RoleService = Depends(get_role_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.grant_resources(param.role_id, param.resource_ids, param.permissions, actor)
    return success()


@router.post("/api/v1/sys/role/refresh-session-acl", summary="刷新角色会话权限")
@SysLog("刷新角色会话权限")
@require_permissions("sys:role:refresh-session-acl", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def refresh_session_acl(
    request: Request,
    param: RefreshRoleSessionACLParam,
    service: RoleService = Depends(get_role_service),
):
    await service.refresh_session_acl(param)
    return success()
