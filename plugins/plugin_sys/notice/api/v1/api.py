from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import NoticeVO, NoticePageParam, NoticeLatestParam
from ...service import NoticeService, get_notice_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get("/api/v1/sys/notice/page", summary="获取通知分页")
@require_permissions("sys:notice:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: NoticePageParam = Depends(), service: NoticeService = Depends(get_notice_service)):
    return success(await service.page(param))


@router.post("/api/v1/sys/notice/create", summary="添加通知")
@SysLog("添加通知")
@require_permissions("sys:notice:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def create(
    request: Request,
    vo: NoticeVO,
    actor: ActorContext = Depends(get_current_actor),
    service: NoticeService = Depends(get_notice_service),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/notice/modify", summary="编辑通知")
@SysLog("编辑通知")
@require_permissions("sys:notice:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: NoticeVO,
    actor: ActorContext = Depends(get_current_actor),
    service: NoticeService = Depends(get_notice_service),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/notice/remove", summary="删除通知")
@SysLog("删除通知")
@require_permissions("sys:notice:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: NoticeService = Depends(get_notice_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/notice/detail", summary="获取通知详情")
@require_permissions("sys:notice:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: NoticeService = Depends(get_notice_service)):
    return success(await service.detail(id))


@router.get("/api/v1/public/c/notice/latest", summary="公开-最新通知列表")
async def public_latest(request: Request, param: NoticeLatestParam = Depends(), service: NoticeService = Depends(get_notice_service)):
    return success(await service.latest(param))


@router.get("/api/v1/public/c/notice/page", summary="公开-通知分页")
async def public_page(request: Request, param: NoticePageParam = Depends(), service: NoticeService = Depends(get_notice_service)):
    return success(await service.public_page(param))


@router.get("/api/v1/public/c/notice/detail", summary="公开-通知详情")
async def public_detail(request: Request, id: str = Query(...), service: NoticeService = Depends(get_notice_service)):
    return success(await service.public_detail(id))
