"""Course repository."""

from datetime import datetime, timezone

from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.biz.clazz.model import OjClass, OjClassMember
from app.modules.biz.course.enums import AnnouncementStatus, CourseAccessScope, CourseStatus, TaskStatus
from app.modules.biz.course.model import (
    OjCourse,
    OjCourseAnnouncement,
    OjCourseClass,
    OjCourseTask,
    OjCourseTaskProblem,
    OjCourseTaskProgress,
    OjCourseTaskSubmission,
)
from app.modules.biz.course.schema import OjCourseAdminPageQuery, OjCourseUpdateRequest
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjCourseRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    # --- course ---

    async def create_course_entity(
        self,
        *,
        name: str,
        summary: str | None,
        cover_url: str | None,
        visibility: str,
        access_scope: str,
        binding_mode: str,
        sort: int,
        extra: dict,
    ) -> OjCourse:
        entity = OjCourse(
            id=generate_snowflake_id(),
            name=name,
            summary=summary,
            cover_url=cover_url,
            visibility=visibility,
            access_scope=access_scope,
            binding_mode=binding_mode,
            sort=sort,
            extra=extra,
        )
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def bind_classes(self, course_id: str, class_ids: list[str]) -> None:
        for class_id in class_ids:
            self.db.add(
                OjCourseClass(
                    id=generate_snowflake_id(),
                    course_id=course_id,
                    class_id=class_id,
                )
            )
        await self.db.flush()

    async def replace_classes(self, course_id: str, class_ids: list[str]) -> None:
        await self.db.execute(delete(OjCourseClass).where(OjCourseClass.course_id == course_id))
        await self.bind_classes(course_id, class_ids)

    async def list_class_ids(self, course_id: str) -> list[str]:
        stmt = (
            select(OjCourseClass.class_id)
            .where(OjCourseClass.course_id == course_id)
            .order_by(OjCourseClass.id.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def list_class_ids_map(self, course_ids: list[str]) -> dict[str, list[str]]:
        if not course_ids:
            return {}
        stmt = (
            select(OjCourseClass.course_id, OjCourseClass.class_id)
            .where(OjCourseClass.course_id.in_(course_ids))
            .order_by(OjCourseClass.id.asc())
        )
        result: dict[str, list[str]] = {cid: [] for cid in course_ids}
        for course_id, class_id in (await self.db.execute(stmt)).all():
            result.setdefault(course_id, []).append(class_id)
        return result

    async def list_class_briefs(self, class_ids: list[str]) -> list[OjClass]:
        if not class_ids:
            return []
        stmt = select(OjClass).where(OjClass.id.in_(class_ids))
        rows = list((await self.db.execute(stmt)).scalars().all())
        by_id = {row.id: row for row in rows}
        return [by_id[cid] for cid in class_ids if cid in by_id]

    async def is_account_in_course(self, course_id: str, account_id: str) -> bool:
        stmt = (
            select(OjCourseClass.id)
            .join(
                OjClassMember,
                (OjClassMember.class_id == OjCourseClass.class_id) & (OjClassMember.left_at.is_(None)),
            )
            .where(
                OjCourseClass.course_id == course_id,
                OjClassMember.account_id == account_id,
            )
            .limit(1)
        )
        return (await self.db.execute(stmt)).scalar_one_or_none() is not None

    async def get_course(self, course_id: str) -> OjCourse | None:
        return await self.db.get(OjCourse, course_id)

    async def get_course_required(self, course_id: str) -> OjCourse:
        entity = await self.get_course(course_id)
        if entity is None:
            raise NotFoundError("课程不存在")
        return entity

    async def update_course(self, payload: OjCourseUpdateRequest) -> OjCourse:
        entity = await self.get_course_required(payload.id)
        data = payload.model_dump(exclude={"id", "class_ids"}, exclude_none=True)
        for key, value in data.items():
            setattr(entity, key, value.value if hasattr(value, "value") else value)
        await self.db.flush()
        return entity

    async def delete_courses(self, course_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(course_ids))
        stmt = select(OjCourse.id).where(OjCourse.id.in_(unique_ids))
        existing = set((await self.db.execute(stmt)).scalars().all())
        if len(existing) != len(unique_ids):
            raise NotFoundError("课程不存在")
        await self.db.execute(delete(OjCourseClass).where(OjCourseClass.course_id.in_(unique_ids)))
        await self.db.execute(delete(OjCourse).where(OjCourse.id.in_(unique_ids)))

    async def page_courses(self, query: OjCourseAdminPageQuery) -> tuple[list[OjCourse], int]:
        stmt: Select[tuple[OjCourse]] = select(OjCourse)
        count_stmt = select(func.count(OjCourse.id))
        filters = []
        if query.class_id:
            course_ids_subq = select(OjCourseClass.course_id).where(OjCourseClass.class_id == query.class_id)
            filters.append(OjCourse.id.in_(course_ids_subq))
        if query.name:
            filters.append(OjCourse.name.ilike(f"%{query.name}%"))
        if query.status is not None:
            filters.append(OjCourse.status == query.status.value)
        if query.visibility is not None:
            filters.append(OjCourse.visibility == query.visibility.value)
        if query.access_scope is not None:
            filters.append(OjCourse.access_scope == query.access_scope.value)
        if query.binding_mode is not None:
            filters.append(OjCourse.binding_mode == query.binding_mode.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = stmt.order_by(OjCourse.sort.asc(), OjCourse.id.desc()).offset(query.pagination.offset).limit(
            query.pagination.size
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def list_by_class(self, class_id: str, published_only: bool = False) -> list[OjCourse]:
        stmt = (
            select(OjCourse)
            .join(OjCourseClass, OjCourseClass.course_id == OjCourse.id)
            .where(OjCourseClass.class_id == class_id)
        )
        if published_only:
            stmt = stmt.where(OjCourse.status == CourseStatus.PUBLISHED.value)
        stmt = stmt.order_by(OjCourse.sort.asc(), OjCourse.id.desc())
        return list((await self.db.execute(stmt)).scalars().unique().all())

    async def page_public_open(
        self, keyword: str | None, offset: int, size: int
    ) -> tuple[list[OjCourse], int]:
        filters = [
            OjCourse.access_scope == CourseAccessScope.OPEN.value,
            OjCourse.status == CourseStatus.PUBLISHED.value,
            OjCourse.visibility == "PUBLIC",
        ]
        stmt: Select[tuple[OjCourse]] = select(OjCourse).where(*filters)
        count_stmt = select(func.count(OjCourse.id)).where(*filters)
        if keyword:
            like = f"%{keyword}%"
            kw = (OjCourse.name.ilike(like)) | (OjCourse.summary.ilike(like))
            stmt = stmt.where(kw)
            count_stmt = count_stmt.where(kw)
        stmt = stmt.order_by(OjCourse.sort.asc(), OjCourse.id.desc()).offset(offset).limit(size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def set_course_status(self, course_id: str, status: str) -> OjCourse:
        entity = await self.get_course_required(course_id)
        entity.status = status
        await self.db.flush()
        return entity

    # --- announcement ---

    async def create_announcement(self, course_id: str, title: str, content: str | None) -> OjCourseAnnouncement:
        entity = OjCourseAnnouncement(
            id=generate_snowflake_id(),
            course_id=course_id,
            title=title,
            content=content,
            status=AnnouncementStatus.DRAFT.value,
        )
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def get_announcement(self, announcement_id: str) -> OjCourseAnnouncement | None:
        return await self.db.get(OjCourseAnnouncement, announcement_id)

    async def get_announcement_required(self, announcement_id: str) -> OjCourseAnnouncement:
        entity = await self.get_announcement(announcement_id)
        if entity is None:
            raise NotFoundError("公告不存在")
        return entity

    async def update_announcement(self, entity: OjCourseAnnouncement) -> None:
        await self.db.flush()

    async def delete_announcement(self, announcement_id: str) -> None:
        entity = await self.get_announcement_required(announcement_id)
        await self.db.delete(entity)

    async def list_announcements(self, course_id: str, published_only: bool = False) -> list[OjCourseAnnouncement]:
        stmt = select(OjCourseAnnouncement).where(OjCourseAnnouncement.course_id == course_id)
        if published_only:
            stmt = stmt.where(OjCourseAnnouncement.status == AnnouncementStatus.PUBLISHED.value)
        stmt = stmt.order_by(OjCourseAnnouncement.published_at.desc().nullslast(), OjCourseAnnouncement.id.desc())
        return list((await self.db.execute(stmt)).scalars().all())

    # --- task ---

    async def create_task(self, entity: OjCourseTask) -> OjCourseTask:
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def get_task(self, task_id: str) -> OjCourseTask | None:
        return await self.db.get(OjCourseTask, task_id)

    async def get_task_required(self, task_id: str) -> OjCourseTask:
        entity = await self.get_task(task_id)
        if entity is None:
            raise NotFoundError("任务不存在")
        return entity

    async def update_task(self, entity: OjCourseTask) -> None:
        await self.db.flush()

    async def delete_task(self, task_id: str) -> None:
        entity = await self.get_task_required(task_id)
        await self.db.delete(entity)

    async def list_tasks(self, course_id: str, published_only: bool = False) -> list[OjCourseTask]:
        stmt = select(OjCourseTask).where(OjCourseTask.course_id == course_id)
        if published_only:
            stmt = stmt.where(OjCourseTask.status == TaskStatus.PUBLISHED.value)
        stmt = stmt.order_by(OjCourseTask.sort.asc(), OjCourseTask.id.desc())
        return list((await self.db.execute(stmt)).scalars().all())

    async def set_task_status(self, task_id: str, status: str) -> OjCourseTask:
        entity = await self.get_task_required(task_id)
        entity.status = status
        await self.db.flush()
        return entity

    # --- task problems ---

    async def replace_task_problems(
        self, task_id: str, problem_ids: list[str], scores: dict[str, float] | None
    ) -> list[OjCourseTaskProblem]:
        await self.db.execute(delete(OjCourseTaskProblem).where(OjCourseTaskProblem.task_id == task_id))
        entities = []
        for idx, problem_id in enumerate(problem_ids):
            entities.append(
                OjCourseTaskProblem(
                    id=generate_snowflake_id(),
                    task_id=task_id,
                    problem_id=problem_id,
                    sort=idx,
                    score=scores.get(problem_id) if scores else None,
                )
            )
        if entities:
            self.db.add_all(entities)
        await self.db.flush()
        return entities

    async def list_task_problems(self, task_id: str) -> list[OjCourseTaskProblem]:
        stmt = (
            select(OjCourseTaskProblem)
            .where(OjCourseTaskProblem.task_id == task_id)
            .order_by(OjCourseTaskProblem.sort.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    # --- progress ---

    async def get_progress(self, task_id: str, account_id: str) -> OjCourseTaskProgress | None:
        stmt = select(OjCourseTaskProgress).where(
            OjCourseTaskProgress.task_id == task_id,
            OjCourseTaskProgress.account_id == account_id,
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def list_progress_by_task(self, task_id: str) -> list[OjCourseTaskProgress]:
        stmt = select(OjCourseTaskProgress).where(OjCourseTaskProgress.task_id == task_id)
        return list((await self.db.execute(stmt)).scalars().all())

    async def upsert_progress(
        self, task_id: str, account_id: str, solved_count: int, total_count: int, status: str
    ) -> OjCourseTaskProgress:
        progress = await self.get_progress(task_id, account_id)
        now = datetime.now(timezone.utc)
        if progress is None:
            progress = OjCourseTaskProgress(
                id=generate_snowflake_id(),
                task_id=task_id,
                account_id=account_id,
                solved_count=solved_count,
                total_count=total_count,
                status=status,
                finished_at=now if status == "DONE" else None,
            )
            self.db.add(progress)
        else:
            progress.solved_count = solved_count
            progress.total_count = total_count
            progress.status = status
            if status == "DONE" and progress.finished_at is None:
                progress.finished_at = now
        await self.db.flush()
        return progress

    # --- submission link ---

    async def get_task_submission(
        self, task_id: str, account_id: str, problem_id: str
    ) -> OjCourseTaskSubmission | None:
        stmt = select(OjCourseTaskSubmission).where(
            OjCourseTaskSubmission.task_id == task_id,
            OjCourseTaskSubmission.account_id == account_id,
            OjCourseTaskSubmission.problem_id == problem_id,
        )
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def has_task_submissions(self, task_id: str, account_id: str) -> bool:
        stmt = (
            select(OjCourseTaskSubmission.id)
            .where(
                OjCourseTaskSubmission.task_id == task_id,
                OjCourseTaskSubmission.account_id == account_id,
            )
            .limit(1)
        )
        return (await self.db.execute(stmt)).scalar_one_or_none() is not None

    async def upsert_task_submission(
        self, task_id: str, account_id: str, problem_id: str, submission_id: str
    ) -> OjCourseTaskSubmission:
        existing = await self.get_task_submission(task_id, account_id, problem_id)
        if existing is not None:
            existing.submission_id = submission_id
            await self.db.flush()
            return existing
        entity = OjCourseTaskSubmission(
            id=generate_snowflake_id(),
            task_id=task_id,
            account_id=account_id,
            problem_id=problem_id,
            submission_id=submission_id,
        )
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def count_ac_problems_for_task(self, task_id: str, account_id: str, ac_problem_ids: set[str]) -> int:
        if not ac_problem_ids:
            return 0
        stmt = select(OjCourseTaskSubmission.problem_id).where(
            OjCourseTaskSubmission.task_id == task_id,
            OjCourseTaskSubmission.account_id == account_id,
            OjCourseTaskSubmission.problem_id.in_(list(ac_problem_ids)),
        )
        rows = set((await self.db.execute(stmt)).scalars().all())
        return len(rows)
