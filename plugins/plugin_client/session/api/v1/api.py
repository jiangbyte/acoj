"""Client session API — standalone, mirrors hei-gin plugin-client/session/api/v1/api.go."""

from typing import List
from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import Result, PageData, success
from ...params import (
    SessionAnalysisResult, SessionPageResult, SessionPageParam,
    SessionExitParam, SessionExitTokenParam, SessionTokenResult, SessionChartData,
)
from ...service import ClientSessionService, get_client_session_service
from micosauth.decorators import require_permissions
from sdk.auth import CONSUMER_REALM_ID
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get(
    "/api/v1/client/session/analysis",
    summary="获取C端会话分析统计",
    response_model=Result[SessionAnalysisResult],
)
@require_permissions("sys:client-session:page", realm=BUSINESS_REALM_ID)
async def analysis(
    request: Request,
    service: ClientSessionService = Depends(get_client_session_service),
):
    result = await service.analysis()
    return success(result)


@router.get(
    "/api/v1/client/session/page",
    summary="获取C端在线用户分页",
    response_model=Result[PageData[SessionPageResult]],
)
@require_permissions("sys:client-session:page", realm=BUSINESS_REALM_ID)
async def page(
    request: Request,
    param: SessionPageParam = Depends(),
    service: ClientSessionService = Depends(get_client_session_service),
):
    return success(await service.page(param))


@router.post(
    "/api/v1/client/session/exit",
    summary="强退C端用户会话",
    response_model=Result,
)
@require_permissions("sys:client-session:exit", realm=BUSINESS_REALM_ID)
async def exit_session(
    request: Request,
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
@require_permissions("sys:client-session:page", realm=BUSINESS_REALM_ID)
async def token_list(
    request: Request,
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
@require_permissions("sys:client-session:exit", realm=BUSINESS_REALM_ID)
async def exit_token(
    request: Request,
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
@require_permissions("sys:client-session:page", realm=BUSINESS_REALM_ID)
async def chart_data(
    request: Request,
    service: ClientSessionService = Depends(get_client_session_service),
):
    result = await service.chart_data()
    return success(result)
