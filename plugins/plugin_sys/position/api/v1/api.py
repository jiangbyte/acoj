from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission, NoRepeat
from core.log import SysLog
from ...params import PositionVO, PositionPageParam
from ...service import PositionService

router = APIRouter()


@router.get("/api/v1/sys/position/page", summary="获取岗位分页")
@HeiCheckPermission("sys:position:page")
async def page(request: Request, param: PositionPageParam = Depends(), db: Session = Depends(get_db)):
    service = PositionService(db)
    return success(service.page(param))


@router.post("/api/v1/sys/position/create", summary="添加岗位")
@SysLog("添加职位")
@HeiCheckPermission("sys:position:create")
@NoRepeat(interval=3000)
async def create(request: Request, vo: PositionVO, db: Session = Depends(get_db)):
    service = PositionService(db)
    await service.create(vo, request)
    return success()


@router.post("/api/v1/sys/position/modify", summary="编辑岗位")
@SysLog("编辑职位")
@HeiCheckPermission("sys:position:modify")
async def modify(request: Request, vo: PositionVO, db: Session = Depends(get_db)):
    service = PositionService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/position/remove", summary="删除岗位")
@SysLog("删除职位")
@HeiCheckPermission("sys:position:remove")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    service = PositionService(db)
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/position/detail", summary="获取岗位详情")
@HeiCheckPermission("sys:position:detail")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = PositionService(db)
    data = service.detail(id)
    return success(data if data else None)
