from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.oj.submission.submission.schema_portal import (
    OjPortalSubmitRequest,
    OjPortalSubmitResponse,
)
from app.modules.oj.submission.submission.service_portal import OjPortalSubmitService

router = APIRouter()


@router.post(
    "/oj/submit",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[OjPortalSubmitResponse],
)
async def submit(
    payload: OjPortalSubmitRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjPortalSubmitResponse]:
    submission_id = await OjPortalSubmitService(db).submit(
        problem_id=payload.problem_id,
        language_id=payload.language_id,
        source=payload.source,
        account_type=session.account_type,
        account_id=session.account_id,
    )
    return success(OjPortalSubmitResponse(submission_id=submission_id, status="QUEUED"))
