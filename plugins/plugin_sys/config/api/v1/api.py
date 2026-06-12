from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.auth.decorator import HeiCheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import ConfigVO, ConfigPageParam, ConfigListParam, ConfigBatchEditParam, ConfigCategoryEditParam
from ...service import ConfigService, get_config_service

router = APIRouter()


@router.get("/api/v1/sys/config/page", summary="获取配置分页")
@HeiCheckPermission("sys:config:page")
async def page(request: Request, param: ConfigPageParam = Depends(), service: ConfigService = Depends(get_config_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/config/list-by-category", summary="根据分类获取配置列表")
@HeiCheckPermission("sys:config:list")
async def list_by_category(request: Request, param: ConfigListParam = Depends(), service: ConfigService = Depends(get_config_service)):
    return success(service.list_by_category(param.category))


@router.post("/api/v1/sys/config/create", summary="添加配置")
@SysLog("添加配置")
@HeiCheckPermission("sys:config:create")
@NoRepeat(interval=3000)
async def create(
    request: Request,
    vo: ConfigVO,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/config/modify", summary="编辑配置")
@SysLog("编辑配置")
@HeiCheckPermission("sys:config:modify")
async def modify(
    request: Request,
    vo: ConfigVO,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/config/remove", summary="删除配置")
@SysLog("删除配置")
@HeiCheckPermission("sys:config:remove")
async def remove(request: Request, param: IdsParam, service: ConfigService = Depends(get_config_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/config/detail", summary="获取配置详情")
@HeiCheckPermission("sys:config:detail")
async def detail(request: Request, id: str = Query(...), service: ConfigService = Depends(get_config_service)):
    data = service.detail(id)
    return success(data if data else None)


@router.post("/api/v1/sys/config/edit-batch", summary="批量编辑配置")
@SysLog("批量编辑配置")
@HeiCheckPermission("sys:config:edit")
@NoRepeat(interval=3000)
async def edit_batch(
    request: Request,
    param: ConfigBatchEditParam,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    service.edit_batch(param, actor)
    return success()


@router.post("/api/v1/sys/config/edit-by-category", summary="按分类批量编辑配置")
@SysLog("按分类批量编辑配置")
@HeiCheckPermission("sys:config:edit")
@NoRepeat(interval=3000)
async def edit_by_category(
    request: Request,
    param: ConfigCategoryEditParam,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    service.edit_by_category(param, actor)
    return success()
