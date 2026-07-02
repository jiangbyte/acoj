from typing import Annotated

from fastapi import APIRouter, Depends, File, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import IdQuery
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.account.service import AccountService
from app.modules.user.admin.schema import (
    AdminProfileResponse,
    AdminUserCenterAvatarUpdateResponse,
    AdminUserCenterEmailUpdateRequest,
    AdminUserCenterOrgInfoResponse,
    AdminUserCenterPasswordUpdateRequest,
    AdminUserCenterPhoneUpdateRequest,
    AdminUserCenterProfileUpdateRequest,
)
from app.modules.user.admin.service import AdminUserProfileService
from app.modules.user.schema import AdminMeResponse
from app.platform.storage.url import resolve_file_url

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
    avatar = resolve_file_url(account.avatar)
    role_id_names, dept_id_names, group_id_names = await AdminUserProfileService(db).get_id_name_groups(
        session.role_ids,
        session.dept_ids,
        session.group_ids,
    )
    return success(
        AdminMeResponse(
            account_id=session.account_id,
            account=account.account,
            account_type=AccountType(str(session.account_type)),
            name=account.name,
            nickname=account.nickname,
            avatar=avatar,
            role_ids=session.role_ids,
            dept_ids=session.dept_ids,
            group_ids=session.group_ids,
            role_id_names=role_id_names,
            dept_id_names=dept_id_names,
            group_id_names=group_id_names,
            permission_keys=session.permission_keys,
            button_codes=session.button_codes,
            profile=AdminProfileResponse(
                account_id=session.account_id,
                name=account.name,
                nickname=account.nickname,
                avatar=avatar,
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


@router.post(
    "/user-center/profile/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[None],
)
async def update_user_center_profile(
    payload: AdminUserCenterProfileUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AdminUserProfileService(db).update_current_profile(payload, session)
    return success()


@router.post(
    "/user-center/avatar/upload",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[AdminUserCenterAvatarUpdateResponse],
)
async def upload_user_center_avatar(
    file: Annotated[UploadFile, File(...)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[AdminUserCenterAvatarUpdateResponse]:
    content = await file.read()
    return success(
        await AdminUserProfileService(db).update_current_avatar(
            content=content,
            content_type=file.content_type or "",
            session=session,
        )
    )


@router.post(
    "/user-center/password/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[None],
)
async def update_user_center_password(
    payload: AdminUserCenterPasswordUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AdminUserProfileService(db).update_current_password(payload, session)
    return success()


@router.post(
    "/user-center/phone/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[None],
)
async def update_user_center_phone(
    payload: AdminUserCenterPhoneUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AdminUserProfileService(db).update_current_phone(payload, session)
    return success()


@router.post(
    "/user-center/email/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[None],
)
async def update_user_center_email(
    payload: AdminUserCenterEmailUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AdminUserProfileService(db).update_current_email(payload, session)
    return success()


@router.get(
    "/user-center/org-info",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[AdminUserCenterOrgInfoResponse],
)
async def get_user_center_org_info(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[AdminUserCenterOrgInfoResponse]:
    return success(await AdminUserProfileService(db).get_org_info(session))
