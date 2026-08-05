"""Admin contest registration APIs."""

from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.contest.enums import ContestRegistrationStatus
from app.modules.biz.contest.registration.schema import (
    OjContestRegistrationAddRequest,
    OjContestRegistrationAdminPageQuery,
    OjContestRegistrationIdsRequest,
    OjContestRegistrationRejectRequest,
    OjContestRegistrationSchema,
)
from app.modules.biz.contest.registration.service import ContestRegistrationService

router = APIRouter()


@router.get(
    "/biz/contest/registration/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:contest:detail")),
    ],
    response_model=ApiResponse[PageData[OjContestRegistrationSchema]],
)
async def page(
    contest_id: Annotated[Id, Query()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    account_id: str | None = Query(default=None),
    status: ContestRegistrationStatus | None = Query(default=None),
) -> ApiResponse[PageData[OjContestRegistrationSchema]]:
    query = OjContestRegistrationAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        contest_id=contest_id,
        account_id=account_id,
        status=status,
    )
    return success(await ContestRegistrationService(db).page_admin(query))


@router.post(
    "/biz/contest/registration/add",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:contest:update")),
    ],
    response_model=ApiResponse[str],
)
async def add(
    contest_id: Annotated[Id, Query()],
    payload: OjContestRegistrationAddRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[str]:
    reg_id = await ContestRegistrationService(db).add(contest_id, payload, session.account_id)
    return success(reg_id)


@router.post(
    "/biz/contest/registration/approve",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:contest:update")),
    ],
    response_model=ApiResponse[None],
)
async def approve(
    contest_id: Annotated[Id, Query()],
    payload: OjContestRegistrationIdsRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ContestRegistrationService(db).approve(contest_id, payload, session.account_id)
    return success()


@router.post(
    "/biz/contest/registration/reject",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:contest:update")),
    ],
    response_model=ApiResponse[None],
)
async def reject(
    contest_id: Annotated[Id, Query()],
    payload: OjContestRegistrationRejectRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ContestRegistrationService(db).reject(contest_id, payload, session.account_id)
    return success()


@router.post(
    "/biz/contest/registration/cancel",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:contest:update")),
    ],
    response_model=ApiResponse[None],
)
async def cancel(
    contest_id: Annotated[Id, Query()],
    payload: OjContestRegistrationIdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ContestRegistrationService(db).cancel(contest_id, payload)
    return success()
