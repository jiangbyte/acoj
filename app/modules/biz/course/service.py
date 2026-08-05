"""Course business logic."""

from __future__ import annotations

from datetime import datetime, timezone

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.core.security.session import SessionPayload
from app.modules.biz.clazz.repository import OjClassRepository
from app.modules.biz.course.enums import (
    AnnouncementStatus,
    CourseAccessScope,
    CourseBindingMode,
    CourseStatus,
    ProgressStatus,
    TaskMode,
    TaskStatus,
)
from app.modules.biz.course.model import OjCourse, OjCourseTask
from app.modules.biz.course.repository import OjCourseRepository
from app.modules.biz.course.schema import (
    OjCourseAdminPageQuery,
    OjCourseAnnouncementCreateRequest,
    OjCourseAnnouncementSchema,
    OjCourseAnnouncementUpdateRequest,
    OjCourseClassBriefSchema,
    OjCourseCreateRequest,
    OjCourseCreateResult,
    OjCoursePortalPageQuery,
    OjCourseSchema,
    OjCourseTaskCreateRequest,
    OjCourseTaskProblemSchema,
    OjCourseTaskProgressBoardQuery,
    OjCourseTaskProgressSchema,
    OjCourseTaskRecordSubmissionRequest,
    OjCourseTaskSchema,
    OjCourseTaskSetProblemsRequest,
    OjCourseTaskUpdateRequest,
    OjCourseUpdateRequest,
)
from app.modules.biz.submission.submission.model import OjSubmission
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


def _utcnow() -> datetime:
    return datetime.now(timezone.utc)


class OjCourseService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjCourseRepository(db)
        self.class_repo = OjClassRepository(db)

    async def _ensure_classes_exist(self, class_ids: list[str]) -> None:
        for class_id in class_ids:
            await self.class_repo.get_required(class_id)

    async def _can_participate(self, course: OjCourse, account_id: str | None) -> bool:
        if not account_id:
            return False
        if course.access_scope == CourseAccessScope.OPEN.value:
            return True
        return await self.repo.is_account_in_course(course.id, account_id)

    async def _ensure_portal_course_read(
        self, course_id: str, session: SessionPayload | None = None
    ) -> OjCourse:
        course = await self.repo.get_course_required(course_id)
        if course.status != CourseStatus.PUBLISHED.value:
            raise BusinessError("课程未发布")
        if course.access_scope == CourseAccessScope.OPEN.value:
            return course
        account_id = session.account_id if session and session.account_type == AccountType.PORTAL else None
        if not account_id or not await self.repo.is_account_in_course(course_id, account_id):
            raise BusinessError("您不是该课程关联班级的成员")
        return course

    async def _ensure_portal_course_write(self, course_id: str, account_id: str) -> OjCourse:
        course = await self.repo.get_course_required(course_id)
        if course.status != CourseStatus.PUBLISHED.value:
            raise BusinessError("课程未发布")
        if course.access_scope == CourseAccessScope.OPEN.value:
            return course
        if not await self.repo.is_account_in_course(course_id, account_id):
            raise BusinessError("您不是该课程关联班级的成员")
        return course

    async def _ensure_portal_course_access(self, course_id: str, account_id: str) -> None:
        await self._ensure_portal_course_write(course_id, account_id)

    async def _to_course_schema(
        self,
        course: OjCourse,
        class_ids: list[str] | None = None,
        *,
        account_id: str | None = None,
    ) -> OjCourseSchema:
        ids = class_ids if class_ids is not None else await self.repo.list_class_ids(course.id)
        briefs = await self.repo.list_class_briefs(ids)
        schema = to_schema(OjCourseSchema, course)
        schema.class_ids = ids
        schema.class_id = ids[0] if ids else None
        schema.classes = [
            OjCourseClassBriefSchema(id=item.id, code=item.code, name=item.name) for item in briefs
        ]
        schema.can_participate = await self._can_participate(course, account_id)
        return schema

    async def _to_course_schema_list(
        self, items: list[OjCourse], account_id: str | None = None
    ) -> list[OjCourseSchema]:
        class_map = await self.repo.list_class_ids_map([item.id for item in items])
        return [
            await self._to_course_schema(item, class_map.get(item.id, []), account_id=account_id)
            for item in items
        ]

    # --- course CRUD ---

    async def create(self, payload: OjCourseCreateRequest) -> OjCourseCreateResult:
        if payload.class_ids:
            await self._ensure_classes_exist(payload.class_ids)
        visibility = payload.visibility.value
        access_scope = payload.access_scope.value
        binding_mode = payload.binding_mode.value
        created_ids: list[str] = []
        async with transactional(self.db):
            if payload.access_scope == CourseAccessScope.OPEN:
                entity = await self.repo.create_course_entity(
                    name=payload.name,
                    summary=payload.summary,
                    cover_url=payload.cover_url,
                    visibility=visibility,
                    access_scope=access_scope,
                    binding_mode=CourseBindingMode.SHARED.value,
                    sort=payload.sort,
                    extra=payload.extra,
                )
                created_ids = [entity.id]
            elif payload.binding_mode == CourseBindingMode.SHARED:
                entity = await self.repo.create_course_entity(
                    name=payload.name,
                    summary=payload.summary,
                    cover_url=payload.cover_url,
                    visibility=visibility,
                    access_scope=access_scope,
                    binding_mode=binding_mode,
                    sort=payload.sort,
                    extra=payload.extra,
                )
                await self.repo.bind_classes(entity.id, payload.class_ids)
                created_ids = [entity.id]
            else:
                for class_id in payload.class_ids:
                    entity = await self.repo.create_course_entity(
                        name=payload.name,
                        summary=payload.summary,
                        cover_url=payload.cover_url,
                        visibility=visibility,
                        access_scope=CourseAccessScope.CLASS.value,
                        binding_mode=CourseBindingMode.PER_CLASS.value,
                        sort=payload.sort,
                        extra=payload.extra,
                    )
                    await self.repo.bind_classes(entity.id, [class_id])
                    created_ids.append(entity.id)
        return OjCourseCreateResult(id=created_ids[0], ids=created_ids)

    async def update(self, payload: OjCourseUpdateRequest) -> None:
        async with transactional(self.db):
            course = await self.repo.update_course(payload)
            if payload.access_scope is not None:
                course.access_scope = payload.access_scope.value
                if payload.access_scope == CourseAccessScope.OPEN:
                    await self.repo.replace_classes(course.id, [])
                    course.binding_mode = CourseBindingMode.SHARED.value
                await self.db.flush()

            scope = CourseAccessScope(course.access_scope)
            if payload.class_ids is not None:
                if scope == CourseAccessScope.OPEN:
                    await self.repo.replace_classes(course.id, [])
                else:
                    class_ids = list(
                        dict.fromkeys([cid.strip() for cid in payload.class_ids if cid and cid.strip()])
                    )
                    if not class_ids:
                        raise BusinessError("私有课至少保留一个班级")
                    if course.binding_mode == CourseBindingMode.PER_CLASS.value and len(class_ids) > 1:
                        raise BusinessError("分班课程只能关联一个班级；合班请使用「合班上课」模式创建")
                    await self._ensure_classes_exist(class_ids)
                    await self.repo.replace_classes(course.id, class_ids)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_courses(payload.ids)

    async def detail(self, query: IdQuery) -> OjCourseSchema:
        return await self._to_course_schema(await self.repo.get_course_required(query.id))

    async def page_admin(self, query: OjCourseAdminPageQuery) -> PageData[OjCourseSchema]:
        items, total = await self.repo.page_courses(query)
        return build_page(query.pagination, total, await self._to_course_schema_list(items))

    async def page_public(
        self, query: OjCoursePortalPageQuery, session: SessionPayload | None = None
    ) -> PageData[OjCourseSchema]:
        items, total = await self.repo.page_public_open(
            query.keyword, query.pagination.offset, query.pagination.size
        )
        account_id = session.account_id if session and session.account_type == AccountType.PORTAL else None
        return build_page(
            query.pagination, total, await self._to_course_schema_list(items, account_id=account_id)
        )

    async def publish(self, course_id: str) -> None:
        async with transactional(self.db):
            await self.repo.set_course_status(course_id, CourseStatus.PUBLISHED.value)

    async def archive(self, course_id: str) -> None:
        async with transactional(self.db):
            await self.repo.set_course_status(course_id, CourseStatus.ARCHIVED.value)

    async def list_by_class_for_member(self, class_id: str, session: SessionPayload) -> list[OjCourseSchema]:
        if not await self.class_repo.is_member(class_id, session.account_id):
            raise BusinessError("您不是该班级成员")
        items = await self.repo.list_by_class(class_id, published_only=True)
        return await self._to_course_schema_list(items, account_id=session.account_id)

    async def detail_portal(
        self, course_id: str, session: SessionPayload | None = None
    ) -> OjCourseSchema:
        course = await self._ensure_portal_course_read(course_id, session)
        account_id = session.account_id if session and session.account_type == AccountType.PORTAL else None
        return await self._to_course_schema(course, account_id=account_id)

    async def detail_for_member(self, course_id: str, session: SessionPayload) -> OjCourseSchema:
        return await self.detail_portal(course_id, session)

    # --- announcement ---

    async def create_announcement(self, payload: OjCourseAnnouncementCreateRequest) -> str:
        await self.repo.get_course_required(payload.course_id)
        async with transactional(self.db):
            entity = await self.repo.create_announcement(payload.course_id, payload.title, payload.content)
            entity.status = AnnouncementStatus.PUBLISHED.value
            entity.published_at = _utcnow()
            await self.repo.update_announcement(entity)
        return entity.id

    async def update_announcement(self, payload: OjCourseAnnouncementUpdateRequest) -> None:
        async with transactional(self.db):
            entity = await self.repo.get_announcement_required(payload.id)
            if payload.title is not None:
                entity.title = payload.title
            if payload.content is not None:
                entity.content = payload.content
            if payload.status is not None:
                entity.status = payload.status.value
                if payload.status == AnnouncementStatus.PUBLISHED:
                    entity.published_at = _utcnow()
            await self.repo.update_announcement(entity)

    async def delete_announcement(self, announcement_id: str) -> None:
        async with transactional(self.db):
            await self.repo.delete_announcement(announcement_id)

    async def list_announcements_admin(self, course_id: str) -> list[OjCourseAnnouncementSchema]:
        items = await self.repo.list_announcements(course_id, published_only=False)
        return to_schema_list(OjCourseAnnouncementSchema, items)

    async def list_announcements_portal(
        self, course_id: str, session: SessionPayload | None = None
    ) -> list[OjCourseAnnouncementSchema]:
        await self._ensure_portal_course_read(course_id, session)
        items = await self.repo.list_announcements(course_id, published_only=True)
        return to_schema_list(OjCourseAnnouncementSchema, items)

    # --- task ---

    async def create_task(self, payload: OjCourseTaskCreateRequest) -> str:
        await self.repo.get_course_required(payload.course_id)
        entity = OjCourseTask(
            id=generate_snowflake_id(),
            course_id=payload.course_id,
            title=payload.title,
            description=payload.description,
            mode=payload.mode.value,
            status=TaskStatus.DRAFT.value,
            open_at=payload.open_at,
            close_at=payload.close_at,
            due_at=payload.due_at,
            sort=payload.sort,
            extra=payload.extra,
        )
        async with transactional(self.db):
            await self.repo.create_task(entity)
        return entity.id

    async def update_task(self, payload: OjCourseTaskUpdateRequest) -> None:
        async with transactional(self.db):
            entity = await self.repo.get_task_required(payload.id)
            for key, value in payload.model_dump(exclude={"id"}, exclude_none=True).items():
                setattr(entity, key, value.value if hasattr(value, "value") else value)
            await self.repo.update_task(entity)

    async def delete_task(self, task_id: str) -> None:
        async with transactional(self.db):
            await self.repo.delete_task(task_id)

    async def publish_task(self, task_id: str) -> None:
        async with transactional(self.db):
            await self.repo.set_task_status(task_id, TaskStatus.PUBLISHED.value)

    async def close_task(self, task_id: str) -> None:
        async with transactional(self.db):
            await self.repo.set_task_status(task_id, TaskStatus.CLOSED.value)

    async def set_task_problems(self, payload: OjCourseTaskSetProblemsRequest) -> None:
        await self.repo.get_task_required(payload.task_id)
        async with transactional(self.db):
            await self.repo.replace_task_problems(payload.task_id, payload.problem_ids, payload.scores)

    async def list_tasks_admin(self, course_id: str) -> list[OjCourseTaskSchema]:
        tasks = await self.repo.list_tasks(course_id, published_only=False)
        return [await self._task_to_schema(t, None) for t in tasks]

    async def list_tasks_portal(
        self, course_id: str, session: SessionPayload | None = None
    ) -> list[OjCourseTaskSchema]:
        await self._ensure_portal_course_read(course_id, session)
        account_id = session.account_id if session and session.account_type == AccountType.PORTAL else None
        tasks = await self.repo.list_tasks(course_id, published_only=True)
        return [await self._task_to_schema(t, account_id) for t in tasks]

    async def task_detail_admin(self, task_id: str) -> OjCourseTaskSchema:
        task = await self.repo.get_task_required(task_id)
        return await self._task_to_schema(task, None)

    async def task_detail_portal(
        self, task_id: str, session: SessionPayload | None = None
    ) -> OjCourseTaskSchema:
        task = await self.repo.get_task_required(task_id)
        if task.status != TaskStatus.PUBLISHED.value:
            raise BusinessError("任务未发布")
        await self._ensure_portal_course_read(task.course_id, session)
        account_id = session.account_id if session and session.account_type == AccountType.PORTAL else None
        # Refresh progress from latest submission verdicts when user has attempts
        if account_id and await self.repo.has_task_submissions(task_id, account_id):
            await self.recompute_progress(task_id, account_id)
        return await self._task_to_schema(task, account_id)

    async def _task_to_schema(self, task: OjCourseTask, account_id: str | None) -> OjCourseTaskSchema:
        schema = to_schema(OjCourseTaskSchema, task)
        schema.problems = to_schema_list(
            OjCourseTaskProblemSchema,
            await self.repo.list_task_problems(task.id),
        )
        if account_id:
            progress = await self.repo.get_progress(task.id, account_id)
            if progress:
                schema.my_progress = to_schema(OjCourseTaskProgressSchema, progress)
        return schema

    # --- submit window ---

    def _check_task_window(self, task: OjCourseTask) -> None:
        if task.status != TaskStatus.PUBLISHED.value:
            raise BusinessError("任务未开放提交")
        now = _utcnow()
        if task.mode == TaskMode.REALTIME.value:
            if task.open_at and now < task.open_at:
                raise BusinessError("任务尚未开始")
            if task.close_at and now > task.close_at:
                raise BusinessError("任务已结束")
        elif task.mode == TaskMode.ASYNC.value:
            if task.due_at and now > task.due_at:
                raise BusinessError("任务已过截止时间")

    async def assert_can_submit(self, task_id: str, account_id: str) -> OjCourseTask:
        task = await self.repo.get_task_required(task_id)
        course = await self.repo.get_course_required(task.course_id)
        await self._ensure_portal_course_access(course.id, account_id)
        self._check_task_window(task)
        problem_ids = {p.problem_id for p in await self.repo.list_task_problems(task_id)}
        if not problem_ids:
            raise BusinessError("任务未配置题目")
        return task

    # --- submission + progress ---

    async def record_submission(self, payload: OjCourseTaskRecordSubmissionRequest, account_id: str) -> None:
        task = await self.assert_can_submit(payload.task_id, account_id)
        problem_ids = {p.problem_id for p in await self.repo.list_task_problems(task.id)}
        if payload.problem_id not in problem_ids:
            raise BusinessError("题目不在任务中")
        submission = await self.db.get(OjSubmission, payload.submission_id)
        if submission is None:
            raise BusinessError("提交不存在")
        if submission.user_id != account_id:
            raise BusinessError("提交不属于当前用户")
        if submission.problem_id != payload.problem_id:
            raise BusinessError("提交与题目不匹配")

        async with transactional(self.db):
            await self.repo.upsert_task_submission(
                payload.task_id, account_id, payload.problem_id, payload.submission_id
            )
            await self.recompute_progress(payload.task_id, account_id)

    async def recompute_progress(self, task_id: str, account_id: str) -> OjCourseTaskProgressSchema:
        problems = await self.repo.list_task_problems(task_id)
        total = len(problems)
        if total == 0:
            status = ProgressStatus.NOT_STARTED.value
            solved = 0
        else:
            ac_problem_ids = set()
            has_any_submission = False
            for p in problems:
                link = await self.repo.get_task_submission(task_id, account_id, p.problem_id)
                if link is None:
                    continue
                has_any_submission = True
                sub = await self.db.get(OjSubmission, link.submission_id)
                if sub and sub.result == "AC":
                    ac_problem_ids.add(p.problem_id)
            solved = len(ac_problem_ids)
            if solved >= total:
                status = ProgressStatus.DONE.value
            elif has_any_submission or solved > 0:
                status = ProgressStatus.IN_PROGRESS.value
            else:
                status = ProgressStatus.NOT_STARTED.value
        progress = await self.repo.upsert_progress(task_id, account_id, solved, total, status)
        return to_schema(OjCourseTaskProgressSchema, progress)

    async def progress_board(self, query: OjCourseTaskProgressBoardQuery) -> list[OjCourseTaskProgressSchema]:
        await self.repo.get_task_required(query.task_id)
        items = await self.repo.list_progress_by_task(query.task_id)
        return to_schema_list(OjCourseTaskProgressSchema, items)
