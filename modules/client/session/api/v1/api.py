from typing import List
from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.db import get_db
from core.result import Result, PageData, success
from core.auth.decorator import HeiCheckPermission
from core.constants import SESSION_PREFIX_CONSUMER, TOKEN_PREFIX_CONSUMER
from ...params import SessionAnalysisResult, SessionPageResult, SessionPageParam, SessionExitParam, SessionExitTokenParam, SessionTokenResult, SessionChartData
from ...service import analysis as svc_analysis, list_c_sessions as svc_list_c, exit_c_session as svc_exit_c, exit_c_session_token as svc_exit_c_token, chart_data as svc_chart_data, token_list as svc_token_list

router = APIRouter()


@router.get(
    "/api/v1/client/session/analysis",
    summary="获取C端会话分析统计",
    response_model=Result[SessionAnalysisResult],
)
@HeiCheckPermission("sys:session:page")
async def analysis(
    request: Request,
    db: Session = Depends(get_db),
):
    result = await svc_analysis(db)
    return success(result)


@router.get(
    "/api/v1/client/session/page",
    summary="获取C端在线用户分页",
    response_model=Result[PageData[SessionPageResult]],
)
@HeiCheckPermission("sys:session:page")
async def page(
    request: Request,
    param: SessionPageParam = Depends(),
    db: Session = Depends(get_db),
):
    result = await svc_list_c(db, param)
    return success({
        "records": result["records"],
        "total": result["total"],
        "page": param.current,
        "size": param.size,
    })


@router.post(
    "/api/v1/client/session/exit",
    summary="强退C端用户会话",
    response_model=Result,
)
@HeiCheckPermission("sys:session:exit")
async def exit_session(
    request: Request,
    param: SessionExitParam,
):
    await svc_exit_c(param.user_id)
    return success()


@router.get(
    "/api/v1/client/session/tokens",
    summary="获取C端用户令牌列表",
    response_model=Result[List[SessionTokenResult]],
)
@HeiCheckPermission("sys:session:page")
async def token_list(
    request: Request,
    user_id: str = Query(..., description="用户ID"),
):
    result = await svc_token_list(SESSION_PREFIX_CONSUMER, TOKEN_PREFIX_CONSUMER, user_id)
    return success(result)


@router.post(
    "/api/v1/client/session/exit-token",
    summary="强退C端指定令牌",
    response_model=Result,
)
@HeiCheckPermission("sys:session:exit")
async def exit_token(
    request: Request,
    param: SessionExitTokenParam,
):
    await svc_exit_c_token(param.user_id, param.token)
    return success()


@router.get(
    "/api/v1/client/session/chart-data",
    summary="获取C端会话图表数据",
    response_model=Result[SessionChartData],
)
@HeiCheckPermission("sys:session:page")
async def chart_data(
    request: Request,
    db: Session = Depends(get_db),
):
    result = await svc_chart_data(db)
    return success(result)
