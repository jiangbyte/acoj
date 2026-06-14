"""
Client user API — uses Perm() and standalone service functions.
Mirrors hei-gin plugins/plugin-client/user/api/v1/api.go
"""

from fastapi import APIRouter, Depends, Query, Request
from micosauth.decorators import require_login, require_permissions
from sdk.auth import CONSUMER_REALM_ID
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_client_actor
from sdk.shared.types import IdsParam
from sdk.log import SysLog
from sdk.web.middleware import RateLimiter
from ...params import (
    ClientUserVO, ClientUserPageParam,
    UpdateProfileParam, UpdateAvatarParam, UpdatePasswordParam,
)
from ...service import ClientUserService, get_client_user_service

router = APIRouter()


# ── Admin routes (管理端) ──

@router.get("/api/v1/client-user/page", summary="获取C端用户分页",
            response_model=Result[PageData[ClientUserVO]])
@require_permissions("client:user:page", realm=CONSUMER_REALM_ID)
async def page(
    request: Request,
    param: ClientUserPageParam = Depends(),
    service: ClientUserService = Depends(get_client_user_service),
):
    return success(await service.page(param))


@router.post("/api/v1/client-user/create", summary="添加C端用户", response_model=Result)
@require_permissions("client:user:create", realm=CONSUMER_REALM_ID)
async def create(
    request: Request,
    vo: ClientUserVO,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/client-user/modify", summary="编辑C端用户", response_model=Result)
@require_permissions("client:user:modify", realm=CONSUMER_REALM_ID)
async def modify(
    request: Request,
    vo: ClientUserVO,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/client-user/remove", summary="删除C端用户", response_model=Result)
@require_permissions("client:user:remove", realm=CONSUMER_REALM_ID)
async def remove(request: Request, param: IdsParam, service: ClientUserService = Depends(get_client_user_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/client-user/detail", summary="获取C端用户详情",
            response_model=Result[ClientUserVO])
@require_permissions("client:user:detail", realm=CONSUMER_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: ClientUserService = Depends(get_client_user_service)):
    data = await service.detail(id)
    return success(data)


# ── Self-service routes (C端) ──

@router.get("/api/v1/c/client-user/current", summary="获取当前C端用户信息")
@require_login(realm=CONSUMER_REALM_ID)
async def get_current_user(
    request: Request,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    data = await service.get_current_user(actor)
    return success(data)


@router.post("/api/v1/c/client-user/update-profile", summary="更新当前C端用户个人信息",
             response_model=Result)
@SysLog("C端用户更新个人信息")
@require_login(realm=CONSUMER_REALM_ID)
@RateLimiter("client:user:update-profile", window=3, max_requests=1)
async def update_profile(
    request: Request,
    param: UpdateProfileParam,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    await service.update_profile(param, actor)
    return success()


@router.post("/api/v1/c/client-user/update-avatar", summary="更新当前C端用户头像（base64）",
             response_model=Result)
@SysLog("C端用户更新头像")
@require_login(realm=CONSUMER_REALM_ID)
async def update_avatar(
    request: Request,
    param: UpdateAvatarParam,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    await service.update_avatar(param, actor)
    return success()


@router.post("/api/v1/c/client-user/update-password", summary="修改当前C端用户密码",
             response_model=Result)
@SysLog("C端用户修改密码")
@require_login(realm=CONSUMER_REALM_ID)
@RateLimiter("client:user:update-password", window=3, max_requests=1)
async def update_password(
    request: Request,
    param: UpdatePasswordParam,
    service: ClientUserService = Depends(get_client_user_service),
    actor: ActorContext = Depends(get_current_client_actor),
):
    await service.update_password(param, actor)
    return success()
