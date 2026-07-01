from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.account.repository import AccountRepository
from app.modules.user.portal.schema import (
    PortalProfileResponse,
    PortalPublicProfileResponse,
    PortalUserCenterEmailUpdateRequest,
    PortalUserCenterPasswordUpdateRequest,
    PortalUserCenterPhoneUpdateRequest,
    PortalUserCenterProfileUpdateRequest,
)
from app.modules.user.portal.service import PortalUserProfileService
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
    account_repo = AccountRepository(db)
    await account_repo.get_required(session.account_id)
    identities = await account_repo.list_identities_by_account_ids([session.account_id])
    primary_identity = next(
        (
            item
            for item in identities
            if item.identity_type == "ACCOUNT" and item.is_primary
        ),
        None,
    ) or next((item for item in identities if item.identity_type == "ACCOUNT"), None)
    profile = await PortalUserProfileService(db).get_profile(session.account_id)
    return success(
        PortalMeResponse(
            account_id=session.account_id,
            account=getattr(primary_identity, "identifier", ""),
            account_type=AccountType(str(session.account_type)),
            name=profile.name if profile else None,
            nickname=profile.nickname if profile else None,
            avatar=profile.avatar if profile else None,
            role_ids=session.role_ids,
            dept_ids=session.dept_ids,
            group_ids=session.group_ids,
            permission_keys=session.permission_keys,
            button_codes=session.button_codes,
            profile=PortalProfileResponse(
                account_id=session.account_id,
                name=profile.name if profile else None,
                nickname=profile.nickname if profile else None,
                avatar=profile.avatar if profile else None,
                signature=profile.signature if profile else None,
                phone=profile.phone if profile else None,
                email=profile.email if profile else None,
                bio=profile.bio if profile else None,
                level=profile.level if profile else None,
                created_at=profile.created_at if profile else None,
                updated_at=profile.updated_at if profile else None,
            ),
        )
    )


@router.post(
    "/user-center/profile/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[None],
)
async def update_user_center_profile(
    payload: PortalUserCenterProfileUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PortalUserProfileService(db).update_current_profile(payload, session)
    return success()


@router.post(
    "/user-center/password/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[None],
)
async def update_user_center_password(
    payload: PortalUserCenterPasswordUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PortalUserProfileService(db).update_current_password(payload, session)
    return success()


@router.post(
    "/user-center/phone/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[None],
)
async def update_user_center_phone(
    payload: PortalUserCenterPhoneUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PortalUserProfileService(db).update_current_phone(payload, session)
    return success()


@router.post(
    "/user-center/email/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[None],
)
async def update_user_center_email(
    payload: PortalUserCenterEmailUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PortalUserProfileService(db).update_current_email(payload, session)
    return success()


@router.get(
    "/spaces/{account_id}",
    response_model=ApiResponse[PortalPublicProfileResponse],
)
async def get_public_space(
    account_id: str,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PortalPublicProfileResponse]:
    return success(await PortalUserProfileService(db).get_public_profile(account_id))
