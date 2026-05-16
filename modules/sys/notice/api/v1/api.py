from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import Result, PageData, success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission, NoRepeat
from core.log import SysLog
from ...params import NoticeVO, NoticePageParam
from ...service import NoticeService

router = APIRouter()


@router.get(
    "/api/v1/sys/notice/page",
    summary="获取通知分页",
    response_model=Result[PageData[NoticeVO]]
)
@HeiCheckPermission("sys:notice:page")
async def page(
    request: Request,
    param: NoticePageParam = Depends(),
    db: Session = Depends(get_db)
):
    service = NoticeService(db)
    return success(service.page(param))


@router.post(
    "/api/v1/sys/notice/create",
    summary="添加通知",
    response_model=Result
)
@SysLog("添加通知")
@HeiCheckPermission("sys:notice:create")
@NoRepeat(interval=3000)
async def create(
    request: Request,
    vo: NoticeVO,
    db: Session = Depends(get_db)
):
    service = NoticeService(db)
    await service.create(vo, request)
    return success()


@router.post(
    "/api/v1/sys/notice/modify",
    summary="编辑通知",
    response_model=Result
)
@SysLog("编辑通知")
@HeiCheckPermission("sys:notice:modify")
async def modify(
    request: Request,
    vo: NoticeVO,
    db: Session = Depends(get_db)
):
    service = NoticeService(db)
    await service.modify(vo, request)
    return success()


@router.post(
    "/api/v1/sys/notice/remove",
    summary="删除通知",
    response_model=Result
)
@SysLog("删除通知")
@HeiCheckPermission("sys:notice:remove")
async def remove(
    request: Request,
    param: IdsParam,
    db: Session = Depends(get_db)
):
    service = NoticeService(db)
    service.remove(param)
    return success()


@router.get(
    "/api/v1/sys/notice/detail",
    summary="获取通知详情",
    response_model=Result[NoticeVO]
)
@HeiCheckPermission("sys:notice:detail")
async def detail(
    request: Request,
    id: str = Query(...),
    db: Session = Depends(get_db)
):
    service = NoticeService(db)
    data = service.detail(IdParam(id=id))
    return success(data if data else None)
