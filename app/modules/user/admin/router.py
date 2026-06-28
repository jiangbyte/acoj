from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.user.admin.schema import AdminProfileResponse
from app.modules.user.schema import AdminMeResponse
from app.modules.user.admin.service import AdminUserProfileService

router = APIRouter()


@router.get(
    "/me",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[AdminMeResponse],
)
async def get_me(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[AdminMeResponse]:
    profile = await AdminUserProfileService(db).get_profile(session.account_id)
    return success(
        AdminMeResponse(
            account_id=session.account_id,
            account_type=AccountType(str(session.account_type)),
            profile=AdminProfileResponse(
                account_id=session.account_id,
                real_name=getattr(profile, "real_name", None),
                avatar_url=getattr(profile, "avatar_url", None),
                title=getattr(profile, "title", None),
                employee_no=getattr(profile, "employee_no", None),
                created_at=getattr(profile, "created_at", None),
                updated_at=getattr(profile, "updated_at", None),
            ),
        )
    )
