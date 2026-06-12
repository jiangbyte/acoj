from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from sdk.web.result import success
from sdk.shared.types import IdParam, IdsParam
from sdk.infra.db import get_db
from sdk.auth.decorator import HeiCheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import ResourceVO, ResourcePageParam, ModuleVO, ModulePageParam
from ...service import ResourceService, ModuleService

router = APIRouter()


# ═════════════════════════════════════════════════════════════════════
# Module routes
# ═════════════════════════════════════════════════════════════════════

@router.get("/api/v1/sys/module/page", summary="获取模块分页")
@HeiCheckPermission("sys:module:page")
async def module_page(request: Request, param: ModulePageParam = Depends(), db: Session = Depends(get_db)):
    service = ModuleService(db)
    return success(service.page(param))


@router.get("/api/v1/sys/module/detail", summary="获取模块详情")
@HeiCheckPermission("sys:module:detail")
async def module_detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = ModuleService(db)
    data = service.detail(id)
    return success(data if data else None)


@router.post("/api/v1/sys/module/create", summary="添加模块")
@SysLog("添加模块")
@HeiCheckPermission("sys:module:create")
@NoRepeat(interval=3000)
async def module_create(request: Request, vo: ModuleVO, db: Session = Depends(get_db)):
    service = ModuleService(db)
    await service.create(vo, request)
    return success()


@router.post("/api/v1/sys/module/modify", summary="编辑模块")
@SysLog("编辑模块")
@HeiCheckPermission("sys:module:modify")
async def module_modify(request: Request, vo: ModuleVO, db: Session = Depends(get_db)):
    service = ModuleService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/module/remove", summary="删除模块")
@SysLog("删除模块")
@HeiCheckPermission("sys:module:remove")
async def module_remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    service = ModuleService(db)
    service.remove(param.ids)
    return success()


# ═════════════════════════════════════════════════════════════════════
# Resource routes
# ═════════════════════════════════════════════════════════════════════

@router.get("/api/v1/sys/resource/page", summary="获取资源分页")
@HeiCheckPermission("sys:resource:page")
async def resource_page(request: Request, param: ResourcePageParam = Depends(), db: Session = Depends(get_db)):
    service = ResourceService(db)
    return success(service.page(param))


@router.get("/api/v1/sys/resource/detail", summary="获取资源详情")
@HeiCheckPermission("sys:resource:detail")
async def resource_detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = ResourceService(db)
    data = service.detail(id)
    return success(data if data else None)


@router.post("/api/v1/sys/resource/create", summary="添加资源")
@SysLog("添加资源")
@HeiCheckPermission("sys:resource:create")
@NoRepeat(interval=3000)
async def resource_create(request: Request, vo: ResourceVO, db: Session = Depends(get_db)):
    service = ResourceService(db)
    await service.create(vo, request)
    return success()


@router.post("/api/v1/sys/resource/modify", summary="编辑资源")
@SysLog("编辑资源")
@HeiCheckPermission("sys:resource:modify")
async def resource_modify(request: Request, vo: ResourceVO, db: Session = Depends(get_db)):
    service = ResourceService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/resource/remove", summary="删除资源")
@SysLog("删除资源")
@HeiCheckPermission("sys:resource:remove")
async def resource_remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    service = ResourceService(db)
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/resource/tree", summary="获取资源树")
@HeiCheckPermission("sys:resource:tree")
async def resource_tree(request: Request, db: Session = Depends(get_db)):
    service = ResourceService(db)
    return success(service.tree())


@router.get("/api/v1/sys/resource/menu", summary="获取资源菜单树")
@HeiCheckPermission("sys:resource:menu")
async def resource_menu(request: Request, db: Session = Depends(get_db)):
    service = ResourceService(db)
    return success(service.menu())
