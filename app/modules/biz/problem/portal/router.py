"""Portal problem bank APIs (query params only, no path IDs)."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.problem.portal.schema import (
    PortalProblemDetailSchema,
    PortalProblemLanguageSchema,
    PortalProblemListSchema,
    PortalProblemPageQuery,
    PortalProblemSubmitRequest,
)
from app.modules.biz.problem.portal.service import PortalProblemService
from app.modules.biz.problem.problem.schema import OjProblemTrialJudgeResult

router = APIRouter()


@router.get(
    "/biz/problem/page",
    response_model=ApiResponse[PageData[PortalProblemListSchema]],
)
async def problem_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    keyword: str | None = Query(default=None),
    code: str | None = Query(default=None),
    name: str | None = Query(default=None),
    group_id: str | None = Query(default=None),
    type_id: str | None = Query(default=None),
) -> ApiResponse[PageData[PortalProblemListSchema]]:
    query = PortalProblemPageQuery(
        pagination=PageQuery(current=current, size=size),
        keyword=keyword,
        code=code,
        name=name,
        group_id=group_id,
        type_id=type_id,
    )
    return success(await PortalProblemService(db).page(query))


@router.get(
    "/biz/problem/detail",
    response_model=ApiResponse[PortalProblemDetailSchema],
)
async def problem_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[PortalProblemDetailSchema]:
    return success(await PortalProblemService(db).detail(id))


@router.get(
    "/biz/problem/languages",
    response_model=ApiResponse[list[PortalProblemLanguageSchema]],
)
async def problem_languages(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    problem_id: Annotated[Id, Query()],
) -> ApiResponse[list[PortalProblemLanguageSchema]]:
    return success(await PortalProblemService(db).languages(problem_id))


@router.post(
    "/biz/problem/submit",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[OjProblemTrialJudgeResult],
)
async def problem_submit(
    payload: PortalProblemSubmitRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    problem_id: Annotated[Id, Query()],
) -> ApiResponse[OjProblemTrialJudgeResult]:
    return success(
        await PortalProblemService(db).submit(
            problem_id=problem_id,
            account_id=session.account_id,
            payload=payload,
        )
    )
