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
from app.modules.user.admin.schema import AdminProfileResponse
from app.modules.user.schema import AdminMeResponse

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
    account = await AccountService(db).detail(IdQuery(id=session.account_id))
    return success(
        AdminMeResponse(
            account_id=session.account_id,
            account=account.account,
            account_type=AccountType(str(session.account_type)),
            name=account.name,
            nickname=account.nickname,
            avatar=account.avatar,
            role_ids=session.role_ids,
            dept_ids=session.dept_ids,
            group_ids=session.group_ids,
            permission_keys=session.permission_keys,
            button_codes=session.button_codes,
            profile=AdminProfileResponse(
                account_id=session.account_id,
                name=account.name,
                nickname=account.nickname,
                avatar=account.avatar,
                signature=account.signature,
                phone=account.phone,
                email=account.email,
                title=account.title,
                employee_no=account.employee_no,
                remark=account.remark,
                created_at=account.created_at,
                updated_at=account.updated_at,
            ),
        )
    )
