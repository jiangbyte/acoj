"""
User API — mirrors hei-gin's plugins/plugin-sys/user/api/v1/api.go 1:1.
No extra routes beyond what Go registers.
"""

from fastapi import APIRouter, Depends, Query, Request
from micosauth.decorators import require_login, require_permissions
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.auth import BUSINESS_REALM_ID
from sdk.log import SysLog
from sdk.shared.types import IdParam
from sdk.web.middleware import RateLimiter
from ...params import (
    UserVO, UserMenuVO, UserPageParam, GrantRoleParam, GrantUserPermissionParam,
    RefreshSessionACLParam, BatchRefreshSessionACLParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from ...service import UserService, get_user_service

router = APIRouter()


@router.get("/api/v1/sys/user/page", summary="获取用户分页", response_model=Result[PageData[UserVO]])
@require_permissions("sys:user:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: UserPageParam = Depends(), service: UserService = Depends(get_user_service)):
    return success(await service.page(param))


@router.post("/api/v1/sys/user/create", summary="添加用户", response_model=Result)
@SysLog("添加用户")
@require_permissions("sys:user:create", realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:create", window=3, max_requests=1)
async def create(
    request: Request,
    vo: UserVO,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/user/modify", summary="编辑用户", response_model=Result)
@SysLog("编辑用户")
@require_permissions("sys:user:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: UserVO,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/user/remove", summary="删除用户", response_model=Result)
@SysLog("删除用户")
@require_permissions("sys:user:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: UserService = Depends(get_user_service)):
    await service.remove(param)
    return success()


@router.get("/api/v1/sys/user/detail", summary="获取用户详情", response_model=Result[UserVO])
@require_permissions("sys:user:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: UserService = Depends(get_user_service)):
    return success(await service.detail(IdParam(id=id)))


@router.post("/api/v1/sys/user/grant-role", summary="分配用户角色", response_model=Result)
@SysLog("分配用户角色")
@require_permissions("sys:user:grant-role", realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:grant-role", window=3, max_requests=1)
async def grant_role(request: Request, param: GrantRoleParam, service: UserService = Depends(get_user_service)):
    await service.grant_role(param)
    return success()


@router.post("/api/v1/sys/user/grant-permission", summary="分配用户权限", response_model=Result)
@SysLog("分配用户权限")
@require_permissions("sys:user:grant-permission", realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:grant-permission", window=3, max_requests=1)
async def grant_permission(request: Request, param: GrantUserPermissionParam, service: UserService = Depends(get_user_service)):
    await service.grant_permission(param)
    return success()


@router.post("/api/v1/sys/user/refresh-session-acl", summary="刷新用户会话权限", response_model=Result)
@SysLog("刷新用户会话权限")
@require_permissions("sys:user:refresh-session-acl", realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:refresh-session-acl", window=3, max_requests=1)
async def refresh_session_acl(
    request: Request,
    param: RefreshSessionACLParam,
    service: UserService = Depends(get_user_service),
):
    await service.refresh_session_acl(param)
    return success()


@router.post("/api/v1/sys/user/batch-refresh-session-acl", summary="批量刷新用户会话权限", response_model=Result)
@SysLog("批量刷新用户会话权限")
@require_permissions("sys:user:batch-refresh-session-acl", realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:batch-refresh-session-acl", window=3, max_requests=1)
async def batch_refresh_session_acl(
    request: Request,
    param: BatchRefreshSessionACLParam,
    service: UserService = Depends(get_user_service),
):
    await service.batch_refresh_session_acl(param)
    return success()


@router.get("/api/v1/sys/user/own-permission-detail", summary="获取用户已分配的权限详情")
@require_permissions("sys:user:own-permission-detail", realm=BUSINESS_REALM_ID)
async def own_permission_detail(request: Request, user_id: str = Query(...), service: UserService = Depends(get_user_service)):
    return success(await service.get_user_permission_details(user_id))


@router.get("/api/v1/sys/user/own-roles", summary="获取用户已分配的角色ID列表")
@require_permissions("sys:user:own-roles", realm=BUSINESS_REALM_ID)
async def own_roles(request: Request, user_id: str = Query(...), service: UserService = Depends(get_user_service)):
    return success(await service.get_user_role_ids(user_id))


@router.get("/api/v1/sys/user/current", summary="获取当前用户信息")
@require_login(realm=BUSINESS_REALM_ID)
async def get_current_user(
    request: Request,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    data = await service.get_current_user(actor)
    return success(data)


@router.get("/api/v1/sys/user/menus", summary="获取当前用户菜单树", response_model=Result[list[UserMenuVO]])
@require_login(realm=BUSINESS_REALM_ID)
async def get_current_user_menus(
    request: Request,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    data = await service.get_current_user_menus(actor)
    return success(data)


@router.get("/api/v1/sys/user/permissions", summary="获取当前用户权限码列表")
@require_login(realm=BUSINESS_REALM_ID)
async def get_current_user_permissions(
    request: Request,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    data = await service.get_current_user_permissions(actor)
    return success(data)


@router.post("/api/v1/sys/user/update-profile", summary="更新当前用户个人信息", response_model=Result)
@SysLog("更新个人信息")
@require_login(realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:update-profile", window=3, max_requests=1)
async def update_profile(
    request: Request,
    param: UpdateProfileParam,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.update_profile(param, actor)
    return success()


@router.post("/api/v1/sys/user/update-avatar", summary="更新当前用户头像（base64）", response_model=Result)
@SysLog("更新头像")
@require_login(realm=BUSINESS_REALM_ID)
async def update_avatar(
    request: Request,
    param: UpdateAvatarParam,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.update_avatar(param, actor)
    return success()


@router.post("/api/v1/sys/user/update-password", summary="修改当前用户密码", response_model=Result)
@SysLog("修改密码")
@require_login(realm=BUSINESS_REALM_ID)
@RateLimiter("sys:user:update-password", window=3, max_requests=1)
async def update_password(
    request: Request,
    param: UpdatePasswordParam,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.update_password(param, actor)
    return success()
