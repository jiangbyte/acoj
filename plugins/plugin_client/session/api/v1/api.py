"""Client session API — standalone, mirrors hei-gin plugin-client/session/api/v1/api.go."""

from typing import List
from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.db import get_db
from core.result import Result, PageData, success
from core.plugin import Perm
from ...params import (
    SessionAnalysisResult, SessionPageResult, SessionPageParam,
    SessionExitParam, SessionExitTokenParam, SessionTokenResult, SessionChartData,
)
from ...service import (
    analysis as svc_analysis,
    page as svc_page,
    exit_session as svc_exit,
    token_list as svc_token_list,
    exit_token as svc_exit_token,
    chart_data as svc_chart_data,
)

router = APIRouter()


@router.get(
    "/api/v1/client/session/analysis",
    summary="获取C端会话分析统计",
    response_model=Result[SessionAnalysisResult],
)
@Perm("sys:session:page", "会话分页")
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
@Perm("sys:session:page", "会话分页")
async def page(
    request: Request,
    param: SessionPageParam = Depends(),
    db: Session = Depends(get_db),
):
    result = await svc_page(db, param)
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
@Perm("sys:session:exit", "强退会话")
async def exit_session(
    request: Request,
    param: SessionExitParam,
):
    await svc_exit(param.user_id)
    return success()


@router.get(
    "/api/v1/client/session/tokens",
    summary="获取C端用户令牌列表",
    response_model=Result[List[SessionTokenResult]],
)
@Perm("sys:session:page", "会话分页")
async def token_list(
    request: Request,
    user_id: str = Query(..., description="用户ID"),
):
    result = await svc_token_list(user_id)
    return success(result)


@router.post(
    "/api/v1/client/session/exit-token",
    summary="强退C端指定令牌",
    response_model=Result,
)
@Perm("sys:session:exit", "强退会话")
async def exit_token(
    request: Request,
    param: SessionExitTokenParam,
):
    await svc_exit_token(param.user_id, param.token)
    return success()


@router.get(
    "/api/v1/client/session/chart-data",
    summary="获取C端会话图表数据",
    response_model=Result[SessionChartData],
)
@Perm("sys:session:page", "会话分页")
async def chart_data(
    request: Request,
):
    result = await svc_chart_data()
    return success(result)
