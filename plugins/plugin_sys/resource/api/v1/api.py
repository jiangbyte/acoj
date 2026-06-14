from fastapi import APIRouter, Depends, Query, Request
from micosauth.decorators import require_permissions

from sdk.auth import BUSINESS_REALM_ID
from sdk.log import SysLog
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.web.result import success
from ...params import ModulePageParam, ModuleVO, ResourcePageParam, ResourceVO
from ...service import ModuleService, ResourceService, get_module_service, get_resource_service

router = APIRouter()


@router.get("/api/v1/sys/module/page", summary="获取模块分页")
@require_permissions("sys:module:page", realm=BUSINESS_REALM_ID)
async def module_page(
    request: Request,
    param: ModulePageParam = Depends(),
    service: ModuleService = Depends(get_module_service),
):
    return success(await service.page(param))


@router.get("/api/v1/sys/module/detail", summary="获取模块详情")
@require_permissions("sys:module:detail", realm=BUSINESS_REALM_ID)
async def module_detail(
    request: Request,
    id: str = Query(...),
    service: ModuleService = Depends(get_module_service),
):
    return success(await service.detail(id))


@router.post("/api/v1/sys/module/create", summary="添加模块")
@SysLog("添加模块")
@require_permissions("sys:module:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def module_create(
    request: Request,
    vo: ModuleVO,
    service: ModuleService = Depends(get_module_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/module/modify", summary="编辑模块")
@SysLog("编辑模块")
@require_permissions("sys:module:modify", realm=BUSINESS_REALM_ID)
async def module_modify(
    request: Request,
    vo: ModuleVO,
    service: ModuleService = Depends(get_module_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/module/remove", summary="删除模块")
@SysLog("删除模块")
@require_permissions("sys:module:remove", realm=BUSINESS_REALM_ID)
async def module_remove(
    request: Request,
    param: IdsParam,
    service: ModuleService = Depends(get_module_service),
):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/resource/page", summary="获取资源分页")
@require_permissions("sys:resource:page", realm=BUSINESS_REALM_ID)
async def resource_page(
    request: Request,
    param: ResourcePageParam = Depends(),
    service: ResourceService = Depends(get_resource_service),
):
    return success(await service.page(param))


@router.get("/api/v1/sys/resource/detail", summary="获取资源详情")
@require_permissions("sys:resource:detail", realm=BUSINESS_REALM_ID)
async def resource_detail(
    request: Request,
    id: str = Query(...),
    service: ResourceService = Depends(get_resource_service),
):
    return success(await service.detail(id))


@router.post("/api/v1/sys/resource/create", summary="添加资源")
@SysLog("添加资源")
@require_permissions("sys:resource:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def resource_create(
    request: Request,
    vo: ResourceVO,
    service: ResourceService = Depends(get_resource_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/resource/modify", summary="编辑资源")
@SysLog("编辑资源")
@require_permissions("sys:resource:modify", realm=BUSINESS_REALM_ID)
async def resource_modify(
    request: Request,
    vo: ResourceVO,
    service: ResourceService = Depends(get_resource_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/resource/remove", summary="删除资源")
@SysLog("删除资源")
@require_permissions("sys:resource:remove", realm=BUSINESS_REALM_ID)
async def resource_remove(
    request: Request,
    param: IdsParam,
    service: ResourceService = Depends(get_resource_service),
):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/resource/tree", summary="获取资源树")
@require_permissions("sys:resource:tree", realm=BUSINESS_REALM_ID)
async def resource_tree(
    request: Request,
    service: ResourceService = Depends(get_resource_service),
):
    return success(await service.tree())
