from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.oj.submission.source.schema import (
    OjSubmissionSourceAdminPageQuery,
    OjSubmissionSourceCreateRequest,
    OjSubmissionSourceSchema,
    OjSubmissionSourceUpdateRequest,
)
from app.modules.oj.submission.source.service import OjSubmissionSourceService

router = APIRouter()


@router.post(
    "/oj/submission-sources/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:submission-sources:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: OjSubmissionSourceCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjSubmissionSourceService(db).create(payload)
    return success()


@router.post(
    "/oj/submission-sources/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:submission-sources:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: OjSubmissionSourceUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjSubmissionSourceService(db).update(payload)
    return success()


@router.post(
    "/oj/submission-sources/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:submission-sources:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjSubmissionSourceService(db).delete(payload)
    return success()


@router.get(
    "/oj/submission-sources/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:submission-sources:detail")),
    ],
    response_model=ApiResponse[OjSubmissionSourceSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjSubmissionSourceSchema]:
    return success(await OjSubmissionSourceService(db).detail(IdQuery(id=id)))


@router.get(
    "/oj/submission-sources/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:submission-sources:page")),
    ],
    response_model=ApiResponse[PageData[OjSubmissionSourceSchema]],
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
) -> ApiResponse[PageData[OjSubmissionSourceSchema]]:
    query = OjSubmissionSourceAdminPageQuery(
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
    return success(await OjSubmissionSourceService(db).page_admin(query))
