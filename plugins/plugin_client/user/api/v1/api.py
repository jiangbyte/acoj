"""
Client user API — uses Perm() and standalone service functions.
Mirrors hei-gin plugins/plugin-client/user/api/v1/api.go
"""

from fastapi import APIRouter, Depends, Query, Request
from sdk.auth.enums import RealmID
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_client_actor
from sdk.shared.types import IdsParam
from sdk.kernel.plugin import Perm
from sdk.auth.decorator import CheckLogin, NoRepeat
from sdk.log import SysLog
from ...params import (
    ClientUserVO, ClientUserPageParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from ...service import ClientUserService, get_client_user_service

router = APIRouter()


# ── Admin routes (管理端) ──

@router.get("/api/v1/client-user/page", summary="获取C端用户分页",
            response_model=Result[PageData[ClientUserVO]])
@Perm("client:user:page", "C端用户分页")
async def page(
    param: ClientUserPageParam = Depends(),
    service: ClientUserService = Depends(get_client_user_service),
):
    return success(service.page(param))


@router.post("/api/v1/client-user/create", summary="添加C端用户", response_model=Result)
@Perm("client:user:create", "添加C端用户")
async def create(
    vo: ClientUserVO,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/client-user/modify", summary="编辑C端用户", response_model=Result)
@Perm("client:user:modify", "编辑C端用户")
async def modify(
    vo: ClientUserVO,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/client-user/remove", summary="删除C端用户", response_model=Result)
@Perm("client:user:remove", "删除C端用户")
async def remove(param: IdsParam, service: ClientUserService = Depends(get_client_user_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/client-user/detail", summary="获取C端用户详情",
            response_model=Result[ClientUserVO])
@Perm("client:user:detail", "C端用户详情")
async def detail(id: str = Query(...), service: ClientUserService = Depends(get_client_user_service)):
    data = service.detail(id)
    return success(data)


# ── Self-service routes (C端) ──

@router.get("/api/v1/c/client-user/current", summary="获取当前C端用户信息")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def get_current_user(
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    data = service.get_current_user(actor)
    return success(data)


@router.post("/api/v1/c/client-user/update-profile", summary="更新当前C端用户个人信息",
             response_model=Result)
@SysLog("C端用户更新个人信息")
@CheckLogin(realm_id=RealmID.CONSUMER)
@NoRepeat(interval=3000)
async def update_profile(
    param: UpdateProfileParam,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    service.update_profile(param, actor)
    return success()


@router.post("/api/v1/c/client-user/update-avatar", summary="更新当前C端用户头像（base64）",
             response_model=Result)
@SysLog("C端用户更新头像")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def update_avatar(
    param: UpdateAvatarParam,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    service.update_avatar(param, actor)
    return success()


@router.post("/api/v1/c/client-user/update-password", summary="修改当前C端用户密码",
             response_model=Result)
@SysLog("C端用户修改密码")
@CheckLogin(realm_id=RealmID.CONSUMER)
@NoRepeat(interval=3000)
async def update_password(
    param: UpdatePasswordParam,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    service.update_password(param, actor)
    return success()
