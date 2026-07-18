from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.oj.contest.problem_result.schema import (
    OjContestProblemResultAdminPageQuery,
    OjContestProblemResultCreateRequest,
    OjContestProblemResultSchema,
    OjContestProblemResultUpdateRequest,
)
from app.modules.oj.contest.problem_result.service import OjContestProblemResultService

router = APIRouter()


@router.post(
    "/oj/contest-problem-results/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:contest-problem-results:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: OjContestProblemResultCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestProblemResultService(db).create(payload)
    return success()


@router.post(
    "/oj/contest-problem-results/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:contest-problem-results:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: OjContestProblemResultUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestProblemResultService(db).update(payload)
    return success()


@router.post(
    "/oj/contest-problem-results/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:contest-problem-results:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestProblemResultService(db).delete(payload)
    return success()


@router.get(
    "/oj/contest-problem-results/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:contest-problem-results:detail")),
    ],
    response_model=ApiResponse[OjContestProblemResultSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjContestProblemResultSchema]:
    return success(await OjContestProblemResultService(db).detail(IdQuery(id=id)))


@router.get(
    "/oj/contest-problem-results/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:contest-problem-results:page")),
    ],
    response_model=ApiResponse[PageData[OjContestProblemResultSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    problem_id: str | None = Query(default=None, max_length=64),
    contest_id: str | None = Query(default=None, max_length=64),
    submission_id: str | None = Query(default=None, max_length=64),
    participation_id: str | None = Query(default=None, max_length=64),
    account_type: str | None = Query(default=None, max_length=32),
    account_id: str | None = Query(default=None, max_length=64),
    target_type: str | None = Query(default=None, max_length=32),
    target_id: str | None = Query(default=None, max_length=64),
    code: str | None = Query(default=None, max_length=64),
    key: str | None = Query(default=None, max_length=64),
    name: str | None = Query(default=None, max_length=255),
    title: str | None = Query(default=None, max_length=255),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[OjContestProblemResultSchema]]:
    query = OjContestProblemResultAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        problem_id=problem_id,
        contest_id=contest_id,
        submission_id=submission_id,
        participation_id=participation_id,
        account_type=account_type,
        account_id=account_id,
        target_type=target_type,
        target_id=target_id,
        code=code,
        key=key,
        name=name,
        title=title,
        status=status,
    )
    return success(await OjContestProblemResultService(db).page_admin(query))
