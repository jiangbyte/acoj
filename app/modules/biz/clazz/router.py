"""Class admin + portal routers."""

from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.clazz.enums import ClassStatus, ClassVisibility
from app.modules.biz.clazz.schema import (
    OjClassAdminPageQuery,
    OjClassCreateRequest,
    OjClassJoinRequest,
    OjClassMemberAddRequest,
    OjClassMemberRemoveRequest,
    OjClassMemberSchema,
    OjClassPortalPageQuery,
    OjClassPublicSchema,
    OjClassRefreshInviteRequest,
    OjClassSchema,
    OjClassUpdateRequest,
)
from app.modules.biz.clazz.service import OjClassService

admin_router = APIRouter()
portal_router = APIRouter()


def _perm(action: str):
    return Depends(require_permission(f"biz:clazz:{action}"))


# ---------- Admin ----------
@admin_router.post(
    "/biz/clazz/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("create")],
)
async def admin_create(
    payload: OjClassCreateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await OjClassService(db).create(payload, session)})


@admin_router.post(
    "/biz/clazz/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_update(
    payload: OjClassUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjClassService(db).update(payload)
    return success()


@admin_router.post(
    "/biz/clazz/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("delete")],
)
async def admin_delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjClassService(db).delete(payload)
    return success()


@admin_router.get(
    "/biz/clazz/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("page")],
    response_model=ApiResponse[PageData[OjClassSchema]],
)
async def admin_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    code: str | None = None,
    name: str | None = None,
    status: ClassStatus | None = None,
    visibility: ClassVisibility | None = None,
) -> ApiResponse[PageData[OjClassSchema]]:
    return success(
        await OjClassService(db).page_admin(
            OjClassAdminPageQuery(
                pagination=PageQuery(current=current, size=size),
                code=code,
                name=name,
                status=status,
                visibility=visibility,
            )
        )
    )


@admin_router.get(
    "/biz/clazz/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[OjClassSchema],
)
async def admin_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjClassSchema]:
    return success(await OjClassService(db).detail(IdQuery(id=id)))


@admin_router.post(
    "/biz/clazz/member/add",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_member_add(
    payload: OjClassMemberAddRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjClassService(db).add_members(payload)
    return success()


@admin_router.post(
    "/biz/clazz/member/remove",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_member_remove(
    payload: OjClassMemberRemoveRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjClassService(db).remove_members(payload)
    return success()


@admin_router.post(
    "/biz/clazz/invite/refresh",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_refresh_invite(
    payload: OjClassRefreshInviteRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    code = await OjClassService(db).refresh_invite_code(payload)
    return success({"invite_code": code})


@admin_router.get(
    "/biz/clazz/members",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjClassMemberSchema]],
)
async def admin_members(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    class_id: Annotated[Id, Query()],
) -> ApiResponse[list[OjClassMemberSchema]]:
    return success(await OjClassService(db).member_list(class_id, admin=True))


# ---------- Portal ----------
@portal_router.get(
    "/biz/clazz/page",
    response_model=ApiResponse[PageData[OjClassPublicSchema]],
)
async def portal_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    current: Current = 1,
    size: Size = 12,
    keyword: str | None = None,
) -> ApiResponse[PageData[OjClassPublicSchema]]:
    return success(
        await OjClassService(db).page_public(
            OjClassPortalPageQuery(
                pagination=PageQuery(current=current, size=size),
                keyword=keyword,
            ),
            session,
        )
    )


@portal_router.get(
    "/biz/clazz/my",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjClassSchema]],
)
async def portal_my(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjClassSchema]]:
    return success(await OjClassService(db).my_classes(session))


@portal_router.post(
    "/biz/clazz/join",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_join(
    payload: OjClassJoinRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    class_id = await OjClassService(db).join_by_invite(payload, session)
    return success({"id": class_id})


@portal_router.get(
    "/biz/clazz/detail",
    response_model=ApiResponse[OjClassSchema],
)
async def portal_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjClassSchema]:
    return success(await OjClassService(db).detail_portal(id, session))


@portal_router.get(
    "/biz/clazz/members",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjClassMemberSchema]],
)
async def portal_members(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    class_id: Annotated[Id, Query()],
) -> ApiResponse[list[OjClassMemberSchema]]:
    return success(await OjClassService(db).member_list(class_id, session=session))


@portal_router.get(
    "/biz/clazz/courses",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list],
)
async def portal_courses_stub(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    class_id: Annotated[Id, Query()],
) -> ApiResponse[list]:
    """Deprecated stub — use course module list-by-class instead."""
    from app.modules.biz.course.service import OjCourseService

    return success(await OjCourseService(db).list_by_class_for_member(class_id, session))
