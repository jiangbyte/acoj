from fastapi import APIRouter, Depends, Query, Request
from micosauth.decorators import require_permissions
from sdk.shared.di import ActorContext, get_current_actor
from sdk.auth import BUSINESS_REALM_ID
from sdk.web.result import Result, success
from sdk.shared.types import IdsParam
from sdk.log import SysLog
from sdk.web.middleware import RateLimiter
from ...params import BannerVO, BannerPageParam
from ...service import BannerService, get_banner_service

router = APIRouter()


@router.get("/api/v1/sys/banner/page", summary="获取Banner分页", response_model=Result)
@require_permissions("sys:banner:page", realm=BUSINESS_REALM_ID)
async def page(
    request: Request,
    param: BannerPageParam = Depends(),
    service: BannerService = Depends(get_banner_service),
):
    return success(await service.page(param))


@router.post("/api/v1/sys/banner/create", summary="添加Banner", response_model=Result)
@SysLog("添加Banner")
@require_permissions("sys:banner:create", realm=BUSINESS_REALM_ID)
@RateLimiter("sys:banner:create", window=3, max_requests=1)
async def create(
    request: Request,
    vo: BannerVO,
    actor: ActorContext = Depends(get_current_actor),
    service: BannerService = Depends(get_banner_service),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/banner/modify", summary="编辑Banner", response_model=Result)
@SysLog("编辑Banner")
@require_permissions("sys:banner:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: BannerVO,
    actor: ActorContext = Depends(get_current_actor),
    service: BannerService = Depends(get_banner_service),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/banner/remove", summary="删除Banner", response_model=Result)
@SysLog("删除Banner")
@require_permissions("sys:banner:remove", realm=BUSINESS_REALM_ID)
async def remove(
    request: Request,
    param: IdsParam,
    service: BannerService = Depends(get_banner_service),
):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/banner/detail", summary="获取Banner详情", response_model=Result)
@require_permissions("sys:banner:detail", realm=BUSINESS_REALM_ID)
async def detail(
    request: Request,
    id: str = Query(...),
    service: BannerService = Depends(get_banner_service),
):
    return success(await service.detail(id))
