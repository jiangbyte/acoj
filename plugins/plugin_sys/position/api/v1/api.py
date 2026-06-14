from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import PositionVO, PositionPageParam
from ...service import PositionService, get_position_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get("/api/v1/sys/position/page", summary="获取岗位分页")
@require_permissions("sys:position:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: PositionPageParam = Depends(), service: PositionService = Depends(get_position_service)):
    return success(await service.page(param))


@router.post("/api/v1/sys/position/create", summary="添加岗位")
@SysLog("添加职位")
@require_permissions("sys:position:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def create(
    request: Request,
    vo: PositionVO,
    actor: ActorContext = Depends(get_current_actor),
    service: PositionService = Depends(get_position_service),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/position/modify", summary="编辑岗位")
@SysLog("编辑职位")
@require_permissions("sys:position:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: PositionVO,
    actor: ActorContext = Depends(get_current_actor),
    service: PositionService = Depends(get_position_service),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/position/remove", summary="删除岗位")
@SysLog("删除职位")
@require_permissions("sys:position:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: PositionService = Depends(get_position_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/position/detail", summary="获取岗位详情")
@require_permissions("sys:position:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: PositionService = Depends(get_position_service)):
    return success(await service.detail(id))
