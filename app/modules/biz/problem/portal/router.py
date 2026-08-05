"""Portal problem bank APIs (query params only, no path IDs)."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import IdQuery, ProblemIdQuery
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
    PortalProblemRecommendQuery,
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
    query: Annotated[PortalProblemRecommendQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PortalProblemRecommendData]:
    """个性化题目推荐（游客走热门入门策略）。"""
    return success(
        await PortalProblemService(db).recommend(
            query,
            account_id=session.account_id if session else None,
        )
    )


@router.get(
    "/biz/problem/page",
    response_model=ApiResponse[PortalProblemPageData],
)
async def problem_page(
    query: Annotated[PortalProblemPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PortalProblemPageData]:
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
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PortalProblemDetailSchema]:
    return success(
        await PortalProblemService(db).detail(
            query,
            account_id=session.account_id if session else None,
        )
    )


@router.get(
    "/biz/problem/languages",
    response_model=ApiResponse[list[PortalProblemLanguageSchema]],
)
async def problem_languages(
    query: Annotated[ProblemIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[PortalProblemLanguageSchema]]:
    return success(await PortalProblemService(db).languages(query))


@router.post(
    "/biz/problem/submit",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[OjProblemTrialJudgeResult],
)
async def problem_submit(
    payload: PortalProblemSubmitRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjProblemTrialJudgeResult]:
    return success(
        await PortalProblemService(db).submit(
            payload=payload,
            account_id=session.account_id,
        )
    )
