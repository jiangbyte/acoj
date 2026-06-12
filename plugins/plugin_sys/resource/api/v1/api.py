from fastapi import APIRouter, Depends, Query
from sdk.web.result import success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.auth.decorator import HeiCheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import ResourceVO, ResourcePageParam, ModuleVO, ModulePageParam
from ...service import (
    ModuleService,
    ResourceService,
    get_module_service,
    get_resource_service,
)

router = APIRouter()


# ═════════════════════════════════════════════════════════════════════
# Module routes
# ═════════════════════════════════════════════════════════════════════

@router.get("/api/v1/sys/module/page", summary="获取模块分页")
@HeiCheckPermission("sys:module:page")
async def module_page(param: ModulePageParam = Depends(), service: ModuleService = Depends(get_module_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/module/detail", summary="获取模块详情")
@HeiCheckPermission("sys:module:detail")
async def module_detail(id: str = Query(...), service: ModuleService = Depends(get_module_service)):
    data = service.detail(id)
    return success(data if data else None)


@router.post("/api/v1/sys/module/create", summary="添加模块")
@SysLog("添加模块")
@HeiCheckPermission("sys:module:create")
@NoRepeat(interval=3000)
async def module_create(
    vo: ModuleVO,
    service: ModuleService = Depends(get_module_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/module/modify", summary="编辑模块")
@SysLog("编辑模块")
@HeiCheckPermission("sys:module:modify")
async def module_modify(
    vo: ModuleVO,
    service: ModuleService = Depends(get_module_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/module/remove", summary="删除模块")
@SysLog("删除模块")
@HeiCheckPermission("sys:module:remove")
async def module_remove(param: IdsParam, service: ModuleService = Depends(get_module_service)):
    service.remove(param.ids)
    return success()


# ═════════════════════════════════════════════════════════════════════
# Resource routes
# ═════════════════════════════════════════════════════════════════════

@router.get("/api/v1/sys/resource/page", summary="获取资源分页")
@HeiCheckPermission("sys:resource:page")
async def resource_page(param: ResourcePageParam = Depends(), service: ResourceService = Depends(get_resource_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/resource/detail", summary="获取资源详情")
@HeiCheckPermission("sys:resource:detail")
async def resource_detail(id: str = Query(...), service: ResourceService = Depends(get_resource_service)):
    data = service.detail(id)
    return success(data if data else None)


@router.post("/api/v1/sys/resource/create", summary="添加资源")
@SysLog("添加资源")
@HeiCheckPermission("sys:resource:create")
@NoRepeat(interval=3000)
async def resource_create(
    vo: ResourceVO,
    service: ResourceService = Depends(get_resource_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/resource/modify", summary="编辑资源")
@SysLog("编辑资源")
@HeiCheckPermission("sys:resource:modify")
async def resource_modify(
    vo: ResourceVO,
    service: ResourceService = Depends(get_resource_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/resource/remove", summary="删除资源")
@SysLog("删除资源")
@HeiCheckPermission("sys:resource:remove")
async def resource_remove(param: IdsParam, service: ResourceService = Depends(get_resource_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/resource/tree", summary="获取资源树")
@HeiCheckPermission("sys:resource:tree")
async def resource_tree(service: ResourceService = Depends(get_resource_service)):
    return success(service.tree())


@router.get("/api/v1/sys/resource/menu", summary="获取资源菜单树")
@HeiCheckPermission("sys:resource:menu")
async def resource_menu(service: ResourceService = Depends(get_resource_service)):
    return success(service.menu())
