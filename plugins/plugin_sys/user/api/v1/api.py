"""
User API — mirrors hei-gin's plugins/plugin-sys/user/api/v1/api.go 1:1.
No extra routes beyond what Go registers.
"""

from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import Result, PageData, success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.plugin import Perm
from core.auth.decorator import HeiCheckLogin, NoRepeat
from core.log import SysLog
from ...params import (
    UserVO, UserPageParam, GrantRoleParam, GrantUserPermissionParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from ...service import (
    user_page, user_detail, user_create, user_modify, user_remove,
    user_grant_roles, user_grant_permissions,
    user_get_permission_details, user_get_role_ids,
    user_get_current, user_get_menus, user_get_permissions,
    user_update_profile, user_update_avatar, user_update_password,
)

router = APIRouter()


@router.get("/api/v1/sys/user/page", summary="获取用户分页", response_model=Result[PageData[UserVO]])
@Perm("sys:user:page", "用户分页")
async def page(request: Request, param: UserPageParam = Depends(), db: Session = Depends(get_db)):
    return success(user_page(db, param))


@router.post("/api/v1/sys/user/create", summary="添加用户", response_model=Result)
@SysLog("添加用户")
@Perm("sys:user:create", "添加用户")
@NoRepeat(interval=3000)
async def create(request: Request, vo: UserVO, db: Session = Depends(get_db)):
    await user_create(db, vo, request)
    return success()


@router.post("/api/v1/sys/user/modify", summary="编辑用户", response_model=Result)
@SysLog("编辑用户")
@Perm("sys:user:modify", "编辑用户")
async def modify(request: Request, vo: UserVO, db: Session = Depends(get_db)):
    await user_modify(db, vo, request)
    return success()


@router.post("/api/v1/sys/user/remove", summary="删除用户", response_model=Result)
@SysLog("删除用户")
@Perm("sys:user:remove", "删除用户")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    user_remove(db, param.ids)
    return success()


@router.get("/api/v1/sys/user/detail", summary="获取用户详情", response_model=Result[UserVO])
@Perm("sys:user:detail", "用户详情")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    data = user_detail(db, id)
    return success(data if data else None)


@router.post("/api/v1/sys/user/grant-role", summary="分配用户角色", response_model=Result)
@SysLog("分配用户角色")
@Perm("sys:user:grant-role", "分配用户角色")
@NoRepeat(interval=3000)
async def grant_role(request: Request, param: GrantRoleParam, db: Session = Depends(get_db)):
    await user_grant_roles(db, param, request)
    return success()


@router.post("/api/v1/sys/user/grant-permission", summary="分配用户权限", response_model=Result)
@SysLog("分配用户权限")
@Perm("sys:user:grant-permission", "分配用户权限")
@NoRepeat(interval=3000)
async def grant_permission(request: Request, param: GrantUserPermissionParam, db: Session = Depends(get_db)):
    await user_grant_permissions(db, param, request)
    return success()


@router.get("/api/v1/sys/user/own-permission-detail", summary="获取用户已分配的权限详情")
@Perm("sys:user:own-permission-detail", "用户权限详情")
async def own_permission_detail(request: Request, user_id: str = Query(...), db: Session = Depends(get_db)):
    return success(user_get_permission_details(db, user_id))


@router.get("/api/v1/sys/user/own-roles", summary="获取用户已分配的角色ID列表")
@Perm("sys:user:own-roles", "用户角色列表")
async def own_roles(request: Request, user_id: str = Query(...), db: Session = Depends(get_db)):
    return success(user_get_role_ids(db, user_id))


@router.get("/api/v1/sys/user/current", summary="获取当前用户信息")
@HeiCheckLogin
async def get_current_user(request: Request, db: Session = Depends(get_db)):
    data = await user_get_current(db, request)
    return success(data)


@router.get("/api/v1/sys/user/menus", summary="获取当前用户菜单树")
@HeiCheckLogin
async def get_current_user_menus(request: Request, db: Session = Depends(get_db)):
    data = await user_get_menus(db, request)
    return success(data)


@router.get("/api/v1/sys/user/permissions", summary="获取当前用户权限码列表")
@HeiCheckLogin
async def get_current_user_permissions(request: Request, db: Session = Depends(get_db)):
    data = await user_get_permissions(db, request)
    return success(data)


@router.post("/api/v1/sys/user/update-profile", summary="更新当前用户个人信息", response_model=Result)
@SysLog("更新个人信息")
@HeiCheckLogin
@NoRepeat(interval=3000)
async def update_profile(request: Request, param: UpdateProfileParam, db: Session = Depends(get_db)):
    await user_update_profile(db, param, request)
    return success()


@router.post("/api/v1/sys/user/update-avatar", summary="更新当前用户头像（base64）", response_model=Result)
@SysLog("更新头像")
@HeiCheckLogin
async def update_avatar(request: Request, param: UpdateAvatarParam, db: Session = Depends(get_db)):
    await user_update_avatar(db, param, request)
    return success()


@router.post("/api/v1/sys/user/update-password", summary="修改当前用户密码", response_model=Result)
@SysLog("修改密码")
@HeiCheckLogin
@NoRepeat(interval=3000)
async def update_password(request: Request, param: UpdatePasswordParam, db: Session = Depends(get_db)):
    await user_update_password(db, param, request)
    return success()
