from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import Result, success
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import BannerVO, BannerPageParam
from ...service import BannerService, get_banner_service

router = APIRouter()


@router.get("/api/v1/sys/banner/page", summary="获取Banner分页", response_model=Result)
@CheckPermission("sys:banner:page")
def page(request: Request, param: BannerPageParam = Depends(), service: BannerService = Depends(get_banner_service)):
    return success(service.page(param))


@router.post("/api/v1/sys/banner/create", summary="添加Banner", response_model=Result)
@SysLog("添加Banner")
@CheckPermission("sys:banner:create")
@NoRepeat(interval=3000)
def create(
    request: Request,
    vo: BannerVO,
    actor: ActorContext = Depends(get_current_actor),
    service: BannerService = Depends(get_banner_service),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/banner/modify", summary="编辑Banner", response_model=Result)
@SysLog("编辑Banner")
@CheckPermission("sys:banner:modify")
def modify(
    request: Request,
    vo: BannerVO,
    actor: ActorContext = Depends(get_current_actor),
    service: BannerService = Depends(get_banner_service),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/banner/remove", summary="删除Banner", response_model=Result)
@SysLog("删除Banner")
@CheckPermission("sys:banner:remove")
def remove(request: Request, param: IdsParam, service: BannerService = Depends(get_banner_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/banner/detail", summary="获取Banner详情", response_model=Result)
@CheckPermission("sys:banner:detail")
def detail(request: Request, id: str = Query(...), service: BannerService = Depends(get_banner_service)):
    data = service.detail(id)
    return success(data if data else None)
