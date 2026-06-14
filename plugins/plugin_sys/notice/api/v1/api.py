from fastapi import APIRouter, Depends, Query
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import NoticeVO, NoticePageParam, NoticeLatestParam
from ...service import NoticeService, get_notice_service

router = APIRouter()


@router.get("/api/v1/sys/notice/page", summary="获取通知分页")
@CheckPermission("sys:notice:page")
def page(param: NoticePageParam = Depends(), service: NoticeService = Depends(get_notice_service)):
    return success(service.page(param))


@router.post("/api/v1/sys/notice/create", summary="添加通知")
@SysLog("添加通知")
@CheckPermission("sys:notice:create")
@NoRepeat(interval=3000)
def create(
    vo: NoticeVO,
    actor: ActorContext = Depends(get_current_actor),
    service: NoticeService = Depends(get_notice_service),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/notice/modify", summary="编辑通知")
@SysLog("编辑通知")
@CheckPermission("sys:notice:modify")
def modify(
    vo: NoticeVO,
    actor: ActorContext = Depends(get_current_actor),
    service: NoticeService = Depends(get_notice_service),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/notice/remove", summary="删除通知")
@SysLog("删除通知")
@CheckPermission("sys:notice:remove")
def remove(param: IdsParam, service: NoticeService = Depends(get_notice_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/notice/detail", summary="获取通知详情")
@CheckPermission("sys:notice:detail")
def detail(id: str = Query(...), service: NoticeService = Depends(get_notice_service)):
    return success(service.detail(id))


@router.get("/api/v1/public/c/notice/latest", summary="公开-最新通知列表")
def public_latest(param: NoticeLatestParam = Depends(), service: NoticeService = Depends(get_notice_service)):
    return success(service.latest(param))


@router.get("/api/v1/public/c/notice/page", summary="公开-通知分页")
def public_page(param: NoticePageParam = Depends(), service: NoticeService = Depends(get_notice_service)):
    return success(service.public_page(param))


@router.get("/api/v1/public/c/notice/detail", summary="公开-通知详情")
def public_detail(id: str = Query(...), service: NoticeService = Depends(get_notice_service)):
    return success(service.public_detail(id))
