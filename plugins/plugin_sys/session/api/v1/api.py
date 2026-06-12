from typing import List
from fastapi import APIRouter, Depends, Query
from sdk.web.result import Result, PageData, success
from sdk.auth.decorator import HeiCheckPermission
from ...params import SessionAnalysisResult, SessionPageResult, SessionPageParam, SessionExitParam, SessionExitTokenParam, SessionTokenResult, SessionChartData
from ...service import SessionService, get_session_service

router = APIRouter()


@router.get(
    "/api/v1/sys/session/analysis",
    summary="获取会话分析统计",
    response_model=Result[SessionAnalysisResult],
)
@HeiCheckPermission("sys:session:page")
async def analysis(
    service: SessionService = Depends(get_session_service),
):
    result = await service.analysis()
    return success(result)


@router.get(
    "/api/v1/sys/session/page",
    summary="获取B端在线用户分页",
    response_model=Result[PageData[SessionPageResult]],
)
@HeiCheckPermission("sys:session:page")
async def page(
    param: SessionPageParam = Depends(),
    service: SessionService = Depends(get_session_service),
):
    result = await service.page(param)
    return success({
        "records": result["records"],
        "total": result["total"],
        "page": param.current,
        "size": param.size,
    })


@router.post(
    "/api/v1/sys/session/exit",
    summary="强退B端用户会话",
    response_model=Result,
)
@HeiCheckPermission("sys:session:exit")
async def exit_session(
    param: SessionExitParam,
    service: SessionService = Depends(get_session_service),
):
    await service.exit_session(param.user_id)
    return success()


@router.get(
    "/api/v1/sys/session/tokens",
    summary="获取B端用户令牌列表",
    response_model=Result[List[SessionTokenResult]],
)
@HeiCheckPermission("sys:session:page")
async def token_list(
    user_id: str = Query(..., description="用户ID"),
    service: SessionService = Depends(get_session_service),
):
    result = await service.token_list(user_id)
    return success(result)


@router.post(
    "/api/v1/sys/session/exit-token",
    summary="强退B端指定令牌",
    response_model=Result,
)
@HeiCheckPermission("sys:session:exit")
async def exit_token(
    param: SessionExitTokenParam,
    service: SessionService = Depends(get_session_service),
):
    await service.exit_token(param.user_id, param.token)
    return success()


@router.get(
    "/api/v1/sys/session/chart-data",
    summary="获取会话图表数据",
    response_model=Result[SessionChartData],
)
@HeiCheckPermission("sys:session:page")
async def chart_data(
    service: SessionService = Depends(get_session_service),
):
    result = await service.chart_data()
    return success(result)
