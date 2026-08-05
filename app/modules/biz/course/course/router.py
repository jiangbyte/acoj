"""Course admin + portal routers."""

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import ClassIdQuery, CourseIdQuery, IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.course.enums import CourseAccessScope, CourseBindingMode, CourseStatus, CourseVisibility
from app.modules.biz.course.course.schema import (
    OjCourseAdminPageQuery,
    OjCourseAnnouncementCreateRequest,
    OjCourseAnnouncementSchema,
    OjCourseAnnouncementUpdateRequest,
    OjCourseCreateRequest,
    OjCourseCreateResult,
    OjCoursePortalPageQuery,
    OjCourseSchema,
    OjCourseTaskCreateRequest,
    OjCourseTaskProgressBoardQuery,
    OjCourseTaskProgressSchema,
    OjCourseTaskRecordSubmissionRequest,
    OjCourseTaskSchema,
    OjCourseTaskSetProblemsRequest,
    OjCourseTaskUpdateRequest,
    OjCourseUpdateRequest,
)
from app.modules.biz.course.course.service import OjCourseService

admin_router = APIRouter()
portal_router = APIRouter()


def _perm(action: str):
    return Depends(require_permission(f"biz:course:{action}"))


# ---------- Admin: course ----------
@admin_router.post(
    "/biz/course/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("create")],
    response_model=ApiResponse[OjCourseCreateResult],
)
async def admin_create(
    payload: OjCourseCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjCourseCreateResult]:
    return success(await OjCourseService(db).create(payload))


@admin_router.post(
    "/biz/course/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_update(
    payload: OjCourseUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).update(payload)
    return success()


@admin_router.post(
    "/biz/course/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("delete")],
)
async def admin_delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).delete(payload)
    return success()


@admin_router.get(
    "/biz/course/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("page")],
    response_model=ApiResponse[PageData[OjCourseSchema]],
)
async def admin_page(
    query: Annotated[OjCourseAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[OjCourseSchema]]:
    return success(await OjCourseService(db).page_admin(query))


@admin_router.get(
    "/biz/course/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[OjCourseSchema],
)
async def admin_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjCourseSchema]:
    return success(await OjCourseService(db).detail(query))


@admin_router.post(
    "/biz/course/publish",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_publish(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).publish(query)
    return success()


@admin_router.post(
    "/biz/course/archive",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_archive(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).archive(query)
    return success()


# ---------- Admin: announcement ----------
@admin_router.post(
    "/biz/course/announcement/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_announcement_create(
    payload: OjCourseAnnouncementCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await OjCourseService(db).create_announcement(payload)})


@admin_router.post(
    "/biz/course/announcement/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_announcement_update(
    payload: OjCourseAnnouncementUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).update_announcement(payload)
    return success()


@admin_router.post(
    "/biz/course/announcement/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_announcement_delete(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).delete_announcement(query)
    return success()


@admin_router.get(
    "/biz/course/announcement/list",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjCourseAnnouncementSchema]],
)
async def admin_announcement_list(
    query: Annotated[CourseIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjCourseAnnouncementSchema]]:
    return success(await OjCourseService(db).list_announcements_admin(query))


# ---------- Admin: task ----------
@admin_router.post(
    "/biz/course/task/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_create(
    payload: OjCourseTaskCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await OjCourseService(db).create_task(payload)})


@admin_router.post(
    "/biz/course/task/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_update(
    payload: OjCourseTaskUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).update_task(payload)
    return success()


@admin_router.post(
    "/biz/course/task/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_delete(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).delete_task(query)
    return success()


@admin_router.post(
    "/biz/course/task/publish",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_publish(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).publish_task(query)
    return success()


@admin_router.post(
    "/biz/course/task/close",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_close(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).close_task(query)
    return success()


@admin_router.post(
    "/biz/course/task/set-problems",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_set_problems(
    payload: OjCourseTaskSetProblemsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).set_task_problems(payload)
    return success()


@admin_router.get(
    "/biz/course/task/list",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjCourseTaskSchema]],
)
async def admin_task_list(
    query: Annotated[CourseIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjCourseTaskSchema]]:
    return success(await OjCourseService(db).list_tasks_admin(query))


@admin_router.get(
    "/biz/course/task/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[OjCourseTaskSchema],
)
async def admin_task_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjCourseTaskSchema]:
    return success(await OjCourseService(db).task_detail_admin(query))


@admin_router.get(
    "/biz/course/task/progress-board",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjCourseTaskProgressSchema]],
)
async def admin_task_progress_board(
    query: Annotated[OjCourseTaskProgressBoardQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjCourseTaskProgressSchema]]:
    return success(await OjCourseService(db).progress_board(query))


# ---------- Portal ----------
@portal_router.get(
    "/biz/course/page",
    response_model=ApiResponse[PageData[OjCourseSchema]],
)
async def portal_page(
    query: Annotated[OjCoursePortalPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PageData[OjCourseSchema]]:
    return success(
        await OjCourseService(db).page_public(query, session)
    )


@portal_router.get(
    "/biz/course/list",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjCourseSchema]],
)
async def portal_list(
    query: Annotated[ClassIdQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjCourseSchema]]:
    return success(await OjCourseService(db).list_by_class_for_member(query, session))


@portal_router.get(
    "/biz/course/detail",
    response_model=ApiResponse[OjCourseSchema],
)
async def portal_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjCourseSchema]:
    return success(await OjCourseService(db).detail_portal(query, session))


@portal_router.get(
    "/biz/course/announcement/list",
    response_model=ApiResponse[list[OjCourseAnnouncementSchema]],
)
async def portal_announcement_list(
    query: Annotated[CourseIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[list[OjCourseAnnouncementSchema]]:
    return success(await OjCourseService(db).list_announcements_portal(query, session))


@portal_router.get(
    "/biz/course/task/list",
    response_model=ApiResponse[list[OjCourseTaskSchema]],
)
async def portal_task_list(
    query: Annotated[CourseIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[list[OjCourseTaskSchema]]:
    return success(await OjCourseService(db).list_tasks_portal(query, session))


@portal_router.get(
    "/biz/course/task/detail",
    response_model=ApiResponse[OjCourseTaskSchema],
)
async def portal_task_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjCourseTaskSchema]:
    return success(await OjCourseService(db).task_detail_portal(query, session))


@portal_router.post(
    "/biz/course/task/record-submission",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_record_submission(
    payload: OjCourseTaskRecordSubmissionRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjCourseService(db).record_submission(payload, session.account_id)
    return success()


@portal_router.get(
    "/biz/course/task/can-submit",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_can_submit(
    query: Annotated[OjCourseTaskProgressBoardQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    await OjCourseService(db).assert_can_submit(query, session.account_id)
    return success({"allowed": True})
