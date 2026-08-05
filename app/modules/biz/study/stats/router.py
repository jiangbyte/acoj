"""Portal routers for user study stats."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_optional_session
from app.deps.db import get_db_session
from app.modules.biz.study.stats.service import (
    RecentSolvedItem,
    UserHeatmapQuery,
    UserHeatmapSchema,
    UserRecentSolvedQuery,
    UserStatsQuery,
    UserStatsSchema,
    UserStatsService,
)

portal_router = APIRouter()


@portal_router.get("/biz/user/stats", response_model=ApiResponse[UserStatsSchema])
async def portal_user_stats(
    query: Annotated[UserStatsQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[UserStatsSchema]:
    return success(await UserStatsService(db).stats(query, session=session))


@portal_router.get("/biz/user/heatmap", response_model=ApiResponse[UserHeatmapSchema])
async def portal_user_heatmap(
    query: Annotated[UserHeatmapQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[UserHeatmapSchema]:
    return success(await UserStatsService(db).heatmap(query, session=session))


@portal_router.get("/biz/user/recent-solved", response_model=ApiResponse[PageData[RecentSolvedItem]])
async def portal_user_recent_solved(
    query: Annotated[UserRecentSolvedQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PageData[RecentSolvedItem]]:
    return success(await UserStatsService(db).recent_solved(query, session=session))
