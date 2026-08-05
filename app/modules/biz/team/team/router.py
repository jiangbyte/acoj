"""Team admin + portal routers."""

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import CourseIdQuery, IdQuery, TeamIdQuery
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.team.enums import TeamScope, TeamStatus, TeamVisibility
from app.modules.biz.team.team.schema import (
    OjTeamAdminPageQuery,
    OjTeamCreateCourseRequest,
    OjTeamCreateIndependentRequest,
    OjTeamInviteRefreshRequest,
    OjTeamJoinRequest,
    OjTeamMemberAddRequest,
    OjTeamMemberRemoveRequest,
    OjTeamMemberSchema,
    OjTeamOwnerUpdateRequest,
    OjTeamPortalPageQuery,
    OjTeamPublicSchema,
    OjTeamSchema,
    OjTeamUpdateRequest,
    OjTeamUserSearchItem,
    OjTeamUserSearchQuery,
)
from app.modules.biz.team.team.service import OjTeamService

admin_router = APIRouter()
portal_router = APIRouter()


def _perm(action: str):
    return Depends(require_permission(f"biz:team:{action}"))


# ---------- Admin ----------
@admin_router.get(
    "/biz/team/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("page")],
    response_model=ApiResponse[PageData[OjTeamSchema]],
)
async def admin_page(
    query: Annotated[OjTeamAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[OjTeamSchema]]:
    return success(await OjTeamService(db).page_admin(query))


@admin_router.get(
    "/biz/team/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[OjTeamSchema],
)
async def admin_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjTeamSchema]:
    return success(await OjTeamService(db).detail(query))


@admin_router.post(
    "/biz/team/course/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("create")],
)
async def admin_course_team_create(
    payload: OjTeamCreateCourseRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await OjTeamService(db).create_course_team(payload, session)})


@admin_router.post(
    "/biz/team/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_update(
    payload: OjTeamUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).update(payload)
    return success()


@admin_router.post(
    "/biz/team/disable",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_disable(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).disable(query)
    return success()


@admin_router.post(
    "/biz/team/dissolve",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_dissolve(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).admin_dissolve(query)
    return success()


@admin_router.post(
    "/biz/team/member/add",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_member_add(
    payload: OjTeamMemberAddRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).add_members(payload)
    return success()


@admin_router.post(
    "/biz/team/member/remove",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_member_remove(
    payload: OjTeamMemberRemoveRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).remove_members(payload)
    return success()


@admin_router.get(
    "/biz/team/members",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjTeamMemberSchema]],
)
async def admin_members(
    query: Annotated[TeamIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjTeamMemberSchema]]:
    return success(await OjTeamService(db).member_list(query, admin=True))


# ---------- Portal: independent ----------
@portal_router.get(
    "/biz/team/page",
    response_model=ApiResponse[PageData[OjTeamPublicSchema]],
)
async def portal_page(
    query: Annotated[OjTeamPortalPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PageData[OjTeamPublicSchema]]:
    return success(
        await OjTeamService(db).page_public(query, session)
    )


@portal_router.post(
    "/biz/team/create",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_create(
    payload: OjTeamCreateIndependentRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await OjTeamService(db).create_independent(payload, session)})


@portal_router.post(
    "/biz/team/join",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_join(
    payload: OjTeamJoinRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await OjTeamService(db).join_by_invite(payload, session)})


@portal_router.post(
    "/biz/team/leave",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_leave(
    query: Annotated[IdQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).leave(query, session)
    return success()


@portal_router.post(
    "/biz/team/dissolve",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_dissolve(
    query: Annotated[IdQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).dissolve(query, session)
    return success()


@portal_router.get(
    "/biz/team/my",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjTeamSchema]],
)
async def portal_my(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjTeamSchema]]:
    return success(await OjTeamService(db).my_teams(session))


@portal_router.get(
    "/biz/team/detail",
    response_model=ApiResponse[OjTeamSchema],
)
async def portal_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjTeamSchema]:
    return success(await OjTeamService(db).detail_portal(query, session))


@portal_router.get(
    "/biz/team/members",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjTeamMemberSchema]],
)
async def portal_members(
    query: Annotated[TeamIdQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjTeamMemberSchema]]:
    return success(await OjTeamService(db).member_list(query, session=session))


@portal_router.get(
    "/biz/team/course/list",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjTeamSchema]],
)
async def portal_course_teams(
    query: Annotated[CourseIdQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjTeamSchema]]:
    return success(await OjTeamService(db).list_by_course_portal(query, session))


@portal_router.post(
    "/biz/team/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_update(
    payload: OjTeamOwnerUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).update_by_owner(payload, session)
    return success()


@portal_router.post(
    "/biz/team/member/add",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_member_add(
    payload: OjTeamMemberAddRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).add_members_by_owner(payload, session)
    return success()


@portal_router.post(
    "/biz/team/member/remove",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_member_remove(
    payload: OjTeamMemberRemoveRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjTeamService(db).remove_member_by_owner(payload, session)
    return success()


@portal_router.post(
    "/biz/team/invite/refresh",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_invite_refresh(
    payload: OjTeamInviteRefreshRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    code = await OjTeamService(db).refresh_invite(payload.team_id, session)
    return success({"invite_code": code})


@portal_router.get(
    "/biz/team/user/search",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjTeamUserSearchItem]],
)
async def portal_user_search(
    query: Annotated[OjTeamUserSearchQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjTeamUserSearchItem]]:
    return success(await OjTeamService(db).search_portal_users(query, session))
