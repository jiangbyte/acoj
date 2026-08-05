"""Portal problem bank APIs (query params only, no path IDs)."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.problem.portal.schema import (
    PortalProblemDetailSchema,
    PortalProblemGroupItem,
    PortalProblemLanguageSchema,
    PortalProblemPageData,
    PortalProblemPageQuery,
    PortalProblemRecommendData,
    PortalProblemSubmitRequest,
    PortalProblemTypeItem,
)
from app.modules.biz.problem.portal.service import PortalProblemService
from app.modules.biz.problem.problem.schema import OjProblemTrialJudgeResult

router = APIRouter()


@router.get(
    "/biz/problem/group/list",
    response_model=ApiResponse[list[PortalProblemGroupItem]],
)
async def problem_group_list(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[PortalProblemGroupItem]]:
    return success(await PortalProblemService(db).list_groups())


@router.get(
    "/biz/problem/type/list",
    response_model=ApiResponse[list[PortalProblemTypeItem]],
)
async def problem_type_list(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[PortalProblemTypeItem]]:
    return success(await PortalProblemService(db).list_types())


@router.get(
    "/biz/problem/recommend",
    response_model=ApiResponse[PortalProblemRecommendData],
)
async def problem_recommend(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    size: int = Query(default=8, ge=1, le=50),
) -> ApiResponse[PortalProblemRecommendData]:
    """个性化题目推荐（游客走热门入门策略）。"""
    return success(
        await PortalProblemService(db).recommend(
            account_id=session.account_id if session else None,
            size=size,
        )
    )


@router.get(
    "/biz/problem/page",
    response_model=ApiResponse[PortalProblemPageData],
)
async def problem_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    current: Current = 1,
    size: Size = 20,
    keyword: str | None = Query(default=None),
    code: str | None = Query(default=None),
    name: str | None = Query(default=None),
    group_id: str | None = Query(default=None),
    type_id: str | None = Query(default=None),
) -> ApiResponse[PortalProblemPageData]:
    query = PortalProblemPageQuery(
        pagination=PageQuery(current=current, size=size),
        keyword=keyword,
        code=code,
        name=name,
        group_id=group_id,
        type_id=type_id,
    )
    return success(
        await PortalProblemService(db).page(
            query,
            account_id=session.account_id if session else None,
        )
    )


@router.get(
    "/biz/problem/detail",
    response_model=ApiResponse[PortalProblemDetailSchema],
)
async def problem_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PortalProblemDetailSchema]:
    return success(
        await PortalProblemService(db).detail(
            id,
            account_id=session.account_id if session else None,
        )
    )


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
