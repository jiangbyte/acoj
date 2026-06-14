"""Client session API — standalone, mirrors hei-gin plugin-client/session/api/v1/api.go."""

from typing import List
from fastapi import APIRouter, Depends, Query
from sdk.web.result import Result, PageData, success
from sdk.kernel.plugin import Perm
from ...params import (
    SessionAnalysisResult, SessionPageResult, SessionPageParam,
    SessionExitParam, SessionExitTokenParam, SessionTokenResult, SessionChartData,
)
from ...service import ClientSessionService, get_client_session_service

router = APIRouter()


@router.get(
    "/api/v1/client/session/analysis",
    summary="获取C端会话分析统计",
    response_model=Result[SessionAnalysisResult],
)
@Perm("sys:session:page", "会话分页")
async def analysis(
    service: ClientSessionService = Depends(get_client_session_service),
):
    result = await service.analysis()
    return success(result)


@router.get(
    "/api/v1/client/session/page",
    summary="获取C端在线用户分页",
    response_model=Result[PageData[SessionPageResult]],
)
@Perm("sys:session:page", "会话分页")
async def page(
    param: SessionPageParam = Depends(),
    service: ClientSessionService = Depends(get_client_session_service),
):
    return success(await service.page(param))


@router.post(
    "/api/v1/client/session/exit",
    summary="强退C端用户会话",
    response_model=Result,
)
@Perm("sys:session:exit", "强退会话")
async def exit_session(
    param: SessionExitParam,
    service: ClientSessionService = Depends(get_client_session_service),
):
    await service.exit_session(param.user_id)
    return success()


@router.get(
    "/api/v1/client/session/tokens",
    summary="获取C端用户令牌列表",
    response_model=Result[List[SessionTokenResult]],
)
@Perm("sys:session:page", "会话分页")
async def token_list(
    user_id: str = Query(..., description="用户ID"),
    service: ClientSessionService = Depends(get_client_session_service),
):
    result = await service.token_list(user_id)
    return success(result)


@router.post(
    "/api/v1/client/session/exit-token",
    summary="强退C端指定令牌",
    response_model=Result,
)
@Perm("sys:session:exit", "强退会话")
async def exit_token(
    param: SessionExitTokenParam,
    service: ClientSessionService = Depends(get_client_session_service),
):
    await service.exit_token(param.user_id, param.token)
    return success()


@router.get(
    "/api/v1/client/session/chart-data",
    summary="获取C端会话图表数据",
    response_model=Result[SessionChartData],
)
@Perm("sys:session:page", "会话分页")
async def chart_data(
    service: ClientSessionService = Depends(get_client_session_service),
):
    result = await service.chart_data()
    return success(result)
