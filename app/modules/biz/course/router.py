"""Course admin + portal routers."""

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
from app.modules.biz.course.enums import CourseAccessScope, CourseBindingMode, CourseStatus, CourseVisibility
from app.modules.biz.course.schema import (
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
from app.modules.biz.course.service import OjCourseService

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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    class_id: str | None = None,
    name: str | None = None,
    status: CourseStatus | None = None,
    visibility: CourseVisibility | None = None,
    access_scope: CourseAccessScope | None = None,
    binding_mode: CourseBindingMode | None = None,
) -> ApiResponse[PageData[OjCourseSchema]]:
    return success(
        await OjCourseService(db).page_admin(
            OjCourseAdminPageQuery(
                pagination=PageQuery(current=current, size=size),
                class_id=class_id,
                name=name,
                status=status,
                visibility=visibility,
                access_scope=access_scope,
                binding_mode=binding_mode,
            )
        )
    )


@admin_router.get(
    "/biz/course/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[OjCourseSchema],
)
async def admin_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjCourseSchema]:
    return success(await OjCourseService(db).detail(IdQuery(id=id)))


@admin_router.post(
    "/biz/course/publish",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_publish(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    await OjCourseService(db).publish(id)
    return success()


@admin_router.post(
    "/biz/course/archive",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_archive(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    await OjCourseService(db).archive(id)
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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    await OjCourseService(db).delete_announcement(id)
    return success()


@admin_router.get(
    "/biz/course/announcement/list",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjCourseAnnouncementSchema]],
)
async def admin_announcement_list(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    course_id: Annotated[Id, Query()],
) -> ApiResponse[list[OjCourseAnnouncementSchema]]:
    return success(await OjCourseService(db).list_announcements_admin(course_id))


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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    await OjCourseService(db).delete_task(id)
    return success()


@admin_router.post(
    "/biz/course/task/publish",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_publish(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    await OjCourseService(db).publish_task(id)
    return success()


@admin_router.post(
    "/biz/course/task/close",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("update")],
)
async def admin_task_close(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    await OjCourseService(db).close_task(id)
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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    course_id: Annotated[Id, Query()],
) -> ApiResponse[list[OjCourseTaskSchema]]:
    return success(await OjCourseService(db).list_tasks_admin(course_id))


@admin_router.get(
    "/biz/course/task/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[OjCourseTaskSchema],
)
async def admin_task_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjCourseTaskSchema]:
    return success(await OjCourseService(db).task_detail_admin(id))


@admin_router.get(
    "/biz/course/task/progress-board",
    dependencies=[Depends(require_account_type(AccountType.ADMIN)), _perm("detail")],
    response_model=ApiResponse[list[OjCourseTaskProgressSchema]],
)
async def admin_task_progress_board(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    task_id: Annotated[Id, Query()],
) -> ApiResponse[list[OjCourseTaskProgressSchema]]:
    return success(await OjCourseService(db).progress_board(OjCourseTaskProgressBoardQuery(task_id=task_id)))


# ---------- Portal ----------
@portal_router.get(
    "/biz/course/page",
    response_model=ApiResponse[PageData[OjCourseSchema]],
)
async def portal_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    current: Current = 1,
    size: Size = 12,
    keyword: str | None = None,
) -> ApiResponse[PageData[OjCourseSchema]]:
    return success(
        await OjCourseService(db).page_public(
            OjCoursePortalPageQuery(
                pagination=PageQuery(current=current, size=size),
                keyword=keyword,
            ),
            session,
        )
    )


@portal_router.get(
    "/biz/course/list",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[OjCourseSchema]],
)
async def portal_list(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    class_id: Annotated[Id, Query()],
) -> ApiResponse[list[OjCourseSchema]]:
    return success(await OjCourseService(db).list_by_class_for_member(class_id, session))


@portal_router.get(
    "/biz/course/detail",
    response_model=ApiResponse[OjCourseSchema],
)
async def portal_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjCourseSchema]:
    return success(await OjCourseService(db).detail_portal(id, session))


@portal_router.get(
    "/biz/course/announcement/list",
    response_model=ApiResponse[list[OjCourseAnnouncementSchema]],
)
async def portal_announcement_list(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    course_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[list[OjCourseAnnouncementSchema]]:
    return success(await OjCourseService(db).list_announcements_portal(course_id, session))


@portal_router.get(
    "/biz/course/task/list",
    response_model=ApiResponse[list[OjCourseTaskSchema]],
)
async def portal_task_list(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    course_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[list[OjCourseTaskSchema]]:
    return success(await OjCourseService(db).list_tasks_portal(course_id, session))


@portal_router.get(
    "/biz/course/task/detail",
    response_model=ApiResponse[OjCourseTaskSchema],
)
async def portal_task_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjCourseTaskSchema]:
    return success(await OjCourseService(db).task_detail_portal(id, session))


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
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    task_id: Annotated[Id, Query()],
) -> ApiResponse[dict]:
    await OjCourseService(db).assert_can_submit(task_id, session.account_id)
    return success({"allowed": True})
