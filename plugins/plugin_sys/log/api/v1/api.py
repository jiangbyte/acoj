from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdParam, IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import (
    LogVO, LogPageParam,
    LogDeleteByCategoryParam, LogBarChartData, LogPieChartData,
)
from ...service import LogService, get_log_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get(
    "/api/v1/sys/log/page",
    summary="获取操作日志分页",
    response_model=Result[PageData[LogVO]]
)
@require_permissions("sys:log:page", realm=BUSINESS_REALM_ID)
async def page(
    request: Request,
    param: LogPageParam = Depends(),
    service: LogService = Depends(get_log_service),
):
    return success(await service.page(param))


@router.post(
    "/api/v1/sys/log/create",
    summary="添加操作日志",
    response_model=Result
)
@require_permissions("sys:log:create", realm=BUSINESS_REALM_ID)
async def create(
    request: Request,
    vo: LogVO,
    service: LogService = Depends(get_log_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post(
    "/api/v1/sys/log/modify",
    summary="编辑操作日志",
    response_model=Result
)
@require_permissions("sys:log:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: LogVO,
    service: LogService = Depends(get_log_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post(
    "/api/v1/sys/log/remove",
    summary="删除操作日志",
    response_model=Result
)
@SysLog("删除操作日志")
@require_permissions("sys:log:remove", realm=BUSINESS_REALM_ID)
async def remove(
    request: Request,
    param: IdsParam,
    service: LogService = Depends(get_log_service),
):
    await service.remove(param)
    return success()


@router.get(
    "/api/v1/sys/log/detail",
    summary="获取操作日志详情",
    response_model=Result[LogVO]
)
@require_permissions("sys:log:detail", realm=BUSINESS_REALM_ID)
async def detail(
    request: Request,
    id: str = Query(...),
    service: LogService = Depends(get_log_service),
):
    return success(await service.detail(IdParam(id=id)))


@router.post(
    "/api/v1/sys/log/delete-by-category",
    summary="按分类清空日志",
    response_model=Result
)
@SysLog("按分类清空日志")
@require_permissions("sys:log:remove", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=5, max_requests=1)
async def delete_by_category(
    request: Request,
    param: LogDeleteByCategoryParam,
    service: LogService = Depends(get_log_service),
):
    await service.delete_by_category(param)
    return success()


# ---- Chart / Statistics endpoints ----

@router.get(
    "/api/v1/sys/log/vis/line-chart-data",
    summary="登录登出趋势（近7天）",
    response_model=Result[LogBarChartData],
)
@require_permissions("sys:log:page", realm=BUSINESS_REALM_ID)
async def vis_line_chart_data(
    request: Request,
    service: LogService = Depends(get_log_service),
):
    return success(await service.vis_log_line_chart_data())


@router.get(
    "/api/v1/sys/log/vis/pie-chart-data",
    summary="登录登出总比例",
    response_model=Result[LogPieChartData],
)
@require_permissions("sys:log:page", realm=BUSINESS_REALM_ID)
async def vis_pie_chart_data(
    request: Request,
    service: LogService = Depends(get_log_service),
):
    return success(await service.vis_log_pie_chart_data())


@router.get(
    "/api/v1/sys/log/op/bar-chart-data",
    summary="操作异常趋势（近7天）",
    response_model=Result[LogBarChartData],
)
@require_permissions("sys:log:page", realm=BUSINESS_REALM_ID)
async def op_bar_chart_data(
    request: Request,
    service: LogService = Depends(get_log_service),
):
    return success(await service.op_log_bar_chart_data())


@router.get(
    "/api/v1/sys/log/op/pie-chart-data",
    summary="操作异常总比例",
    response_model=Result[LogPieChartData],
)
@require_permissions("sys:log:page", realm=BUSINESS_REALM_ID)
async def op_pie_chart_data(
    request: Request,
    service: LogService = Depends(get_log_service),
):
    return success(await service.op_log_pie_chart_data())
