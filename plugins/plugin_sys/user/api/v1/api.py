"""
User API — mirrors hei-gin's plugins/plugin-sys/user/api/v1/api.go 1:1.
No extra routes beyond what Go registers.
"""

from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.kernel.plugin import Perm
from sdk.auth.decorator import CheckLogin, NoRepeat
from sdk.log import SysLog
from ...params import (
    UserVO, UserPageParam, GrantRoleParam, GrantUserPermissionParam,
    RefreshSessionACLParam, BatchRefreshSessionACLParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from ...service import UserService, get_user_service

router = APIRouter()


@router.get("/api/v1/sys/user/page", summary="获取用户分页", response_model=Result[PageData[UserVO]])
@Perm("sys:user:page", "用户分页")
def page(param: UserPageParam = Depends(), service: UserService = Depends(get_user_service)):
    return success(service.page(param))


@router.post("/api/v1/sys/user/create", summary="添加用户", response_model=Result)
@SysLog("添加用户")
@Perm("sys:user:create", "添加用户")
@NoRepeat(interval=3000)
async def create(
    vo: UserVO,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/user/modify", summary="编辑用户", response_model=Result)
@SysLog("编辑用户")
@Perm("sys:user:modify", "编辑用户")
async def modify(
    vo: UserVO,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/user/remove", summary="删除用户", response_model=Result)
@SysLog("删除用户")
@Perm("sys:user:remove", "删除用户")
def remove(param: IdsParam, service: UserService = Depends(get_user_service)):
    service.remove(param)
    return success()


@router.get("/api/v1/sys/user/detail", summary="获取用户详情", response_model=Result[UserVO])
@Perm("sys:user:detail", "用户详情")
def detail(id: str = Query(...), service: UserService = Depends(get_user_service)):
    data = service.detail(type("P", (), {"id": id})())
    return success(data if data else None)


@router.post("/api/v1/sys/user/grant-role", summary="分配用户角色", response_model=Result)
@SysLog("分配用户角色")
@Perm("sys:user:grant-role", "分配用户角色")
@NoRepeat(interval=3000)
async def grant_role(param: GrantRoleParam, service: UserService = Depends(get_user_service)):
    await service.grant_role(param)
    return success()


@router.post("/api/v1/sys/user/grant-permission", summary="分配用户权限", response_model=Result)
@SysLog("分配用户权限")
@Perm("sys:user:grant-permission", "分配用户权限")
@NoRepeat(interval=3000)
async def grant_permission(param: GrantUserPermissionParam, service: UserService = Depends(get_user_service)):
    await service.grant_permission(param)
    return success()


@router.post("/api/v1/sys/user/refresh-session-acl", summary="刷新用户会话权限", response_model=Result)
@SysLog("刷新用户会话权限")
@Perm("sys:user:refresh-session-acl", "刷新用户会话权限")
@NoRepeat(interval=3000)
async def refresh_session_acl(
    param: RefreshSessionACLParam,
    service: UserService = Depends(get_user_service),
):
    await service.refresh_session_acl(param)
    return success()


@router.post("/api/v1/sys/user/batch-refresh-session-acl", summary="批量刷新用户会话权限", response_model=Result)
@SysLog("批量刷新用户会话权限")
@Perm("sys:user:batch-refresh-session-acl", "批量刷新用户会话权限")
@NoRepeat(interval=3000)
async def batch_refresh_session_acl(
    param: BatchRefreshSessionACLParam,
    service: UserService = Depends(get_user_service),
):
    await service.batch_refresh_session_acl(param)
    return success()


@router.get("/api/v1/sys/user/own-permission-detail", summary="获取用户已分配的权限详情")
@Perm("sys:user:own-permission-detail", "用户权限详情")
def own_permission_detail(user_id: str = Query(...), service: UserService = Depends(get_user_service)):
    return success(service.get_user_permission_details(user_id))


@router.get("/api/v1/sys/user/own-roles", summary="获取用户已分配的角色ID列表")
@Perm("sys:user:own-roles", "用户角色列表")
def own_roles(user_id: str = Query(...), service: UserService = Depends(get_user_service)):
    return success(service.get_user_role_ids(user_id))


@router.get("/api/v1/sys/user/current", summary="获取当前用户信息")
@CheckLogin
def get_current_user(
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    data = service.get_current_user(actor)
    return success(data)


@router.get("/api/v1/sys/user/menus", summary="获取当前用户菜单树")
@CheckLogin
async def get_current_user_menus(
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    data = await service.get_current_user_menus(actor)
    return success(data)


@router.get("/api/v1/sys/user/permissions", summary="获取当前用户权限码列表")
@CheckLogin
async def get_current_user_permissions(
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    data = await service.get_current_user_permissions(actor)
    return success(data)


@router.post("/api/v1/sys/user/update-profile", summary="更新当前用户个人信息", response_model=Result)
@SysLog("更新个人信息")
@CheckLogin
@NoRepeat(interval=3000)
async def update_profile(
    param: UpdateProfileParam,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.update_profile(param, actor)
    return success()


@router.post("/api/v1/sys/user/update-avatar", summary="更新当前用户头像（base64）", response_model=Result)
@SysLog("更新头像")
@CheckLogin
async def update_avatar(
    param: UpdateAvatarParam,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.update_avatar(param, actor)
    return success()


@router.post("/api/v1/sys/user/update-password", summary="修改当前用户密码", response_model=Result)
@SysLog("修改密码")
@CheckLogin
@NoRepeat(interval=3000)
async def update_password(
    param: UpdatePasswordParam,
    service: UserService = Depends(get_user_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.update_password(param, actor)
    return success()
