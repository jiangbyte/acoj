from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.oj.problem.tag_relation.schema import (
    OjProblemTagRelationAdminPageQuery,
    OjProblemTagRelationCreateRequest,
    OjProblemTagRelationSchema,
    OjProblemTagRelationUpdateRequest,
)
from app.modules.oj.problem.tag_relation.service import OjProblemTagRelationService

router = APIRouter()


@router.post(
    "/oj/problem-tag-relations/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:problemtagrelations:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: OjProblemTagRelationCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjProblemTagRelationService(db).create(payload)
    return success()


@router.post(
    "/oj/problem-tag-relations/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:problemtagrelations:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: OjProblemTagRelationUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjProblemTagRelationService(db).update(payload)
    return success()


@router.post(
    "/oj/problem-tag-relations/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:problemtagrelations:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjProblemTagRelationService(db).delete(payload)
    return success()


@router.get(
    "/oj/problem-tag-relations/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:problemtagrelations:detail")),
    ],
    response_model=ApiResponse[OjProblemTagRelationSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjProblemTagRelationSchema]:
    return success(await OjProblemTagRelationService(db).detail(IdQuery(id=id)))


@router.get(
    "/oj/problem-tag-relations/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("oj:problemtagrelations:page")),
    ],
    response_model=ApiResponse[PageData[OjProblemTagRelationSchema]],
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
) -> ApiResponse[PageData[OjProblemTagRelationSchema]]:
    query = OjProblemTagRelationAdminPageQuery(
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
    return success(await OjProblemTagRelationService(db).page_admin(query))
