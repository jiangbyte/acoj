"""Admin clarification routes."""

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import ContestIdsRequest
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
    payload: OjContestClarificationCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[str]:
    return success(await OjContestClarificationService(db).create_broadcast(payload))


@router.post(
    "/biz/contest/clarification/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:update")),
    ],
    response_model=ApiResponse[None],
)
async def update_broadcast(
    payload: OjContestClarificationUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestClarificationService(db).update_broadcast(payload)
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
    payload: ContestIdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestClarificationService(db).delete_broadcast(payload)
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
    query: Annotated[OjContestClarificationAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[OjContestClarificationSchema]]:
    return success(await OjContestClarificationService(db).page_broadcasts(query))


@router.get(
    "/biz/contest/clarification/thread/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:page")),
    ],
    response_model=ApiResponse[PageData[OjContestClarificationThreadSchema]],
)
async def page_threads(
    query: Annotated[OjContestClarificationThreadAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[OjContestClarificationThreadSchema]]:
    return success(await OjContestClarificationService(db).page_threads(query))


@router.post(
    "/biz/contest/clarification/thread/reply",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:clarification:update")),
    ],
    response_model=ApiResponse[OjContestClarificationThreadSchema],
)
async def reply_thread(
    payload: OjContestClarificationThreadReplyRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjContestClarificationThreadSchema]:
    return success(
        await OjContestClarificationService(db).reply(session.account_id, payload)
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
    payload: OjContestClarificationThreadStatusRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjContestClarificationService(db).set_status(payload)
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
    payload: OjContestClarificationThreadPromoteRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[str]:
    return success(await OjContestClarificationService(db).promote(payload))
