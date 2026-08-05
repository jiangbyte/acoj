"""Admin + portal routers for daily problems."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.study.daily.service import (
    DailyAdminPageQuery,
    DailyCalendarQuery,
    DailyCalendarSchema,
    DailyProblemBrief,
    DailyService,
    DailyTodayQuery,
    DailyTodaySchema,
    DailyUpsertRequest,
)

admin_router = APIRouter()
portal_router = APIRouter()


@admin_router.post(
    "/biz/daily/upsert",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_daily_upsert(
    payload: DailyUpsertRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await DailyService(db).upsert(payload)})


@admin_router.post(
    "/biz/daily/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_daily_delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DailyService(db).delete(payload)
    return success()


@admin_router.get(
    "/biz/daily/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[PageData[DailyProblemBrief]],
)
async def admin_daily_page(
    query: Annotated[DailyAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[DailyProblemBrief]]:
    return success(await DailyService(db).page_admin(query))


@portal_router.get("/biz/daily/today", response_model=ApiResponse[DailyTodaySchema])
async def portal_daily_today(
    query: Annotated[DailyTodayQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[DailyTodaySchema]:
    return success(await DailyService(db).today(query, session=session))


@portal_router.get("/biz/daily/calendar", response_model=ApiResponse[DailyCalendarSchema])
async def portal_daily_calendar(
    query: Annotated[DailyCalendarQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[DailyCalendarSchema]:
    return success(await DailyService(db).calendar(query, session=session))
