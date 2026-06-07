from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission, NoRepeat
from core.log import SysLog
from ...params import NoticeVO, NoticePageParam, NoticeLatestParam
from ...service import NoticeService

router = APIRouter()


@router.get("/api/v1/sys/notice/page", summary="获取通知分页")
@HeiCheckPermission("sys:notice:page")
async def page(request: Request, param: NoticePageParam = Depends(), db: Session = Depends(get_db)):
    service = NoticeService(db)
    return success(service.page(param))


@router.post("/api/v1/sys/notice/create", summary="添加通知")
@SysLog("添加通知")
@HeiCheckPermission("sys:notice:create")
@NoRepeat(interval=3000)
async def create(request: Request, vo: NoticeVO, db: Session = Depends(get_db)):
    service = NoticeService(db)
    await service.create(vo, request)
    return success()


@router.post("/api/v1/sys/notice/modify", summary="编辑通知")
@SysLog("编辑通知")
@HeiCheckPermission("sys:notice:modify")
async def modify(request: Request, vo: NoticeVO, db: Session = Depends(get_db)):
    service = NoticeService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/notice/remove", summary="删除通知")
@SysLog("删除通知")
@HeiCheckPermission("sys:notice:remove")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    service = NoticeService(db)
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/notice/detail", summary="获取通知详情")
@HeiCheckPermission("sys:notice:detail")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = NoticeService(db)
    data = service.detail(id)
    return success(data if data else None)


@router.get("/api/v1/public/c/notice/latest", summary="公开-最新通知列表")
async def public_latest(request: Request, param: NoticeLatestParam = Depends(), db: Session = Depends(get_db)):
    service = NoticeService(db)
    return success(service.latest(param))


@router.get("/api/v1/public/c/notice/page", summary="公开-通知分页")
async def public_page(request: Request, param: NoticePageParam = Depends(), db: Session = Depends(get_db)):
    service = NoticeService(db)
    return success(service.public_page(param))


@router.get("/api/v1/public/c/notice/detail", summary="公开-通知详情")
async def public_detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = NoticeService(db)
    data = service.public_detail(id)
    return success(data if data else None)
