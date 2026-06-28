from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.grant.schema import (
    SubjectPermissionGrantRequest,
    SubjectResourceGrantRequest,
    SysSubjectPermissionGrantRelSchema,
    SysSubjectResourceGrantRelSchema,
)
from app.modules.iam.grant.service import GrantService

router = APIRouter()


@router.post(
    "/resource-grants",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:grant:resource")),
    ],
    response_model=ApiResponse[SysSubjectResourceGrantRelSchema],
)
async def grant_subject_resource(
    payload: SubjectResourceGrantRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysSubjectResourceGrantRelSchema]:
    return success(await GrantService(db).grant_subject_resource(payload))


@router.post(
    "/permission-grants",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:grant:permission")),
    ],
    response_model=ApiResponse[SysSubjectPermissionGrantRelSchema],
)
async def grant_subject_permission(
    payload: SubjectPermissionGrantRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysSubjectPermissionGrantRelSchema]:
    return success(await GrantService(db).grant_subject_permission(payload))
