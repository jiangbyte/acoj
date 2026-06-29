from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.core.schema.base import IdQuery
from app.modules.iam.account.service import AccountService
from app.modules.user.portal.schema import PortalProfileResponse
from app.modules.user.schema import PortalMeResponse

router = APIRouter()


@router.get(
    "/me",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[PortalMeResponse],
)
async def get_me(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PortalMeResponse]:
    """查询当前门户用户的扩展资料信息。"""
    account = await AccountService(db).detail(IdQuery(id=session.account_id))
    return success(
        PortalMeResponse(
            account_id=session.account_id,
            account=account.account,
            account_type=AccountType(str(session.account_type)),
            profile=PortalProfileResponse(
                account_id=session.account_id,
                name=account.name,
                nickname=account.nickname,
                avatar=account.avatar,
                signature=account.signature,
                phone=account.phone,
                email=account.email,
                created_at=account.created_at,
                updated_at=account.updated_at,
            ),
        )
    )
