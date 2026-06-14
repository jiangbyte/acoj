from fastapi import APIRouter, Depends, Query
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdParam, IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import (
    LogVO, LogPageParam,
    LogDeleteByCategoryParam, LogBarChartData, LogPieChartData,
)
from ...service import LogService, get_log_service

router = APIRouter()


@router.get(
    "/api/v1/sys/log/page",
    summary="获取操作日志分页",
    response_model=Result[PageData[LogVO]]
)
@CheckPermission("sys:log:page")
async def page(
    param: LogPageParam = Depends(),
    service: LogService = Depends(get_log_service),
):
    return success(await service.page(param))


@router.post(
    "/api/v1/sys/log/create",
    summary="添加操作日志",
    response_model=Result
)
@CheckPermission("sys:log:create")
async def create(
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
@CheckPermission("sys:log:modify")
async def modify(
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
@CheckPermission("sys:log:remove")
async def remove(
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
@CheckPermission("sys:log:detail")
async def detail(
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
@CheckPermission("sys:log:remove")
@NoRepeat(interval=5000)
async def delete_by_category(
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
@CheckPermission("sys:log:page")
async def vis_line_chart_data(
    service: LogService = Depends(get_log_service),
):
    return success(await service.vis_log_line_chart_data())


@router.get(
    "/api/v1/sys/log/vis/pie-chart-data",
    summary="登录登出总比例",
    response_model=Result[LogPieChartData],
)
@CheckPermission("sys:log:page")
async def vis_pie_chart_data(
    service: LogService = Depends(get_log_service),
):
    return success(await service.vis_log_pie_chart_data())


@router.get(
    "/api/v1/sys/log/op/bar-chart-data",
    summary="操作异常趋势（近7天）",
    response_model=Result[LogBarChartData],
)
@CheckPermission("sys:log:page")
async def op_bar_chart_data(
    service: LogService = Depends(get_log_service),
):
    return success(await service.op_log_bar_chart_data())


@router.get(
    "/api/v1/sys/log/op/pie-chart-data",
    summary="操作异常总比例",
    response_model=Result[LogPieChartData],
)
@CheckPermission("sys:log:page")
async def op_pie_chart_data(
    service: LogService = Depends(get_log_service),
):
    return success(await service.op_log_pie_chart_data())
