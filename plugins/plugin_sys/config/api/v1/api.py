from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import ConfigVO, ConfigPageParam, ConfigListParam, ConfigBatchEditParam, ConfigCategoryEditParam
from ...service import ConfigService, get_config_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get("/api/v1/sys/config/page", summary="获取配置分页")
@require_permissions("sys:config:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: ConfigPageParam = Depends(), service: ConfigService = Depends(get_config_service)):
    return success(await service.page(param))


@router.get("/api/v1/sys/config/list-by-category", summary="根据分类获取配置列表")
@require_permissions("sys:config:list", realm=BUSINESS_REALM_ID)
async def list_by_category(request: Request, param: ConfigListParam = Depends(), service: ConfigService = Depends(get_config_service)):
    return success(await service.list_by_category(param.category))


@router.post("/api/v1/sys/config/create", summary="添加配置")
@SysLog("添加配置")
@require_permissions("sys:config:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat:sys:config:create", window=3, max_requests=1)
async def create(
    request: Request,
    vo: ConfigVO,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/config/modify", summary="编辑配置")
@SysLog("编辑配置")
@require_permissions("sys:config:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: ConfigVO,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/config/remove", summary="删除配置")
@SysLog("删除配置")
@require_permissions("sys:config:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: ConfigService = Depends(get_config_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/config/detail", summary="获取配置详情")
@require_permissions("sys:config:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: ConfigService = Depends(get_config_service)):
    return success(await service.detail(id))


@router.post("/api/v1/sys/config/edit-batch", summary="批量编辑配置")
@SysLog("批量编辑配置")
@require_permissions("sys:config:edit", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat:sys:config:edit-batch", window=3, max_requests=1)
async def edit_batch(
    request: Request,
    param: ConfigBatchEditParam,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    await service.edit_batch(param, actor)
    return success()


@router.post("/api/v1/sys/config/edit-by-category", summary="按分类批量编辑配置")
@SysLog("按分类批量编辑配置")
@require_permissions("sys:config:edit", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat:sys:config:edit-by-category", window=3, max_requests=1)
async def edit_by_category(
    request: Request,
    param: ConfigCategoryEditParam,
    actor: ActorContext = Depends(get_current_actor),
    service: ConfigService = Depends(get_config_service),
):
    await service.edit_by_category(param, actor)
    return success()
