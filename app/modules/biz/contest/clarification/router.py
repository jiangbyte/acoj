"""Admin clarification routes."""

from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.contest.clarification.schema import (
    OjContestClarificationAdminPageQuery,
    OjContestClarificationCreateRequest,
    OjContestClarificationSchema,
    OjContestClarificationThreadAdminPageQuery,
    OjContestClarificationThreadPromoteRequest,
    OjContestClarificationThreadReplyRequest,
    OjContestClarificationThreadSchema,
    OjContestClarificationThreadStatusRequest,
    OjContestClarificationUpdateRequest,
)
from app.modules.biz.contest.clarification.service import OjContestClarificationService
from app.modules.biz.contest.enums import ClarificationThreadStatus

router = APIRouter()


@router.post(
    "/biz/contest/clarification/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:create")),
    ],
    response_model=ApiResponse[str],
)
async def create_broadcast(
    contest_id: Annotated[Id, Query()],
    payload: OjContestClarificationCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[str]:
    return success(await OjContestClarificationService(db).create_broadcast(contest_id, payload))


@router.post(
    "/biz/contest/clarification/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:update")),
    ],
    response_model=ApiResponse[None],
)
async def update_broadcast(
    contest_id: Annotated[Id, Query()],
    payload: OjContestClarificationUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestClarificationService(db).update_broadcast(contest_id, payload)
    return success()


@router.post(
    "/biz/contest/clarification/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete_broadcast(
    contest_id: Annotated[Id, Query()],
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestClarificationService(db).delete_broadcast(contest_id, payload)
    return success()


@router.get(
    "/biz/contest/clarification/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:page")),
    ],
    response_model=ApiResponse[PageData[OjContestClarificationSchema]],
)
async def page_broadcasts(
    contest_id: Annotated[Id, Query()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    problem_id: str | None = Query(default=None),
) -> ApiResponse[PageData[OjContestClarificationSchema]]:
    query = OjContestClarificationAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        contest_id=contest_id,
        problem_id=problem_id,
    )
    return success(await OjContestClarificationService(db).page_broadcasts(contest_id, query))


@router.get(
    "/biz/contest/clarification/thread/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:page")),
    ],
    response_model=ApiResponse[PageData[OjContestClarificationThreadSchema]],
)
async def page_threads(
    contest_id: Annotated[Id, Query()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    status: ClarificationThreadStatus | None = Query(default=None),
    account_id: str | None = Query(default=None),
) -> ApiResponse[PageData[OjContestClarificationThreadSchema]]:
    query = OjContestClarificationThreadAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        contest_id=contest_id,
        status=status,
        account_id=account_id,
    )
    return success(await OjContestClarificationService(db).page_threads(contest_id, query))


@router.post(
    "/biz/contest/clarification/thread/reply",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:update")),
    ],
    response_model=ApiResponse[OjContestClarificationThreadSchema],
)
async def reply_thread(
    contest_id: Annotated[Id, Query()],
    payload: OjContestClarificationThreadReplyRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjContestClarificationThreadSchema]:
    return success(
        await OjContestClarificationService(db).reply(contest_id, session.account_id, payload)
    )


@router.post(
    "/biz/contest/clarification/thread/status",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:update")),
    ],
    response_model=ApiResponse[None],
)
async def set_thread_status(
    contest_id: Annotated[Id, Query()],
    payload: OjContestClarificationThreadStatusRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestClarificationService(db).set_status(contest_id, payload)
    return success()


@router.post(
    "/biz/contest/clarification/thread/promote",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:create")),
    ],
    response_model=ApiResponse[str],
)
async def promote_thread(
    contest_id: Annotated[Id, Query()],
    payload: OjContestClarificationThreadPromoteRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[str]:
    return success(await OjContestClarificationService(db).promote(contest_id, payload))
