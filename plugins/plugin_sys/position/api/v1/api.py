from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import PositionVO, PositionPageParam
from ...service import PositionService, get_position_service

router = APIRouter()


@router.get("/api/v1/sys/position/page", summary="获取岗位分页")
@CheckPermission("sys:position:page")
def page(request: Request, param: PositionPageParam = Depends(), service: PositionService = Depends(get_position_service)):
    return success(service.page(param))


@router.post("/api/v1/sys/position/create", summary="添加岗位")
@SysLog("添加职位")
@CheckPermission("sys:position:create")
@NoRepeat(interval=3000)
def create(
    request: Request,
    vo: PositionVO,
    actor: ActorContext = Depends(get_current_actor),
    service: PositionService = Depends(get_position_service),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/position/modify", summary="编辑岗位")
@SysLog("编辑职位")
@CheckPermission("sys:position:modify")
def modify(
    request: Request,
    vo: PositionVO,
    actor: ActorContext = Depends(get_current_actor),
    service: PositionService = Depends(get_position_service),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/position/remove", summary="删除岗位")
@SysLog("删除职位")
@CheckPermission("sys:position:remove")
def remove(request: Request, param: IdsParam, service: PositionService = Depends(get_position_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/position/detail", summary="获取岗位详情")
@CheckPermission("sys:position:detail")
def detail(request: Request, id: str = Query(...), service: PositionService = Depends(get_position_service)):
    data = service.detail(id)
    return success(data if data else None)
