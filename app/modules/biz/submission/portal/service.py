"""Portal submission list/detail with source visibility rules."""

from __future__ import annotations

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthorizationError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery
from app.core.security.session import SessionPayload
from app.modules.biz.problem.enums import SubmissionSourceVisibility
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult, SubmissionStatus
from app.modules.biz.submission.performance.schema import MySubmissionStatsOut
from app.modules.biz.submission.submission.model import OjSubmission
from app.modules.biz.submission.submission.repository import OjSubmissionRepository
from app.modules.biz.submission.submission.schema import (
    OjSubmissionAdminPageQuery,
    OjSubmissionDetailSchema,
    OjSubmissionListSchema,
)
from app.modules.biz.submission.submission.service import OjSubmissionService


class PortalSubmissionService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjSubmissionRepository(db)
        self.inner = OjSubmissionService(db)

    async def page(self, query: OjSubmissionAdminPageQuery) -> PageData[OjSubmissionListSchema]:
        # Hide admin trial submissions from public status board by default.
        if query.kind is None:
            query = OjSubmissionAdminPageQuery(
                pagination=query.pagination,
                problem_id=query.problem_id,
                problem_code=query.problem_code,
                contest_id=query.contest_id,
                user_id=query.user_id,
                kind=SubmissionKind.OFFICIAL,
                status=query.status,
                result=query.result,
                language_key=query.language_key,
            )
        elif query.kind == SubmissionKind.TRIAL:
            raise NotFoundError("不支持查看试判提交")
        return await self.inner.page_admin(query)

    async def detail(
        self,
        submission_id: str,
        *,
        viewer_account_id: str | None,
    ) -> OjSubmissionDetailSchema:
        entity = await self.repo.get_detail_with_relations(submission_id)
        if entity.kind == SubmissionKind.TRIAL.value:
            raise NotFoundError("提交不存在")
        schema = await self.inner._to_detail_schema(entity)
        if not await self._can_view_source(entity.problem_id, entity.user_id, viewer_account_id):
            schema.source = None
        return schema

    async def my_stats(self, session: SessionPayload) -> MySubmissionStatsOut:
        """Aggregate the viewer's non-trial submissions for the submissions board sidebar."""
        account_id = session.account_id
        base = [
            OjSubmission.user_id == account_id,
            OjSubmission.kind != SubmissionKind.TRIAL.value,
        ]
        total = int(
            (
                await self.db.execute(select(func.count()).select_from(OjSubmission).where(*base))
            ).scalar_one()
            or 0
        )
        ac_total = int(
            (
                await self.db.execute(
                    select(func.count())
                    .select_from(OjSubmission)
                    .where(
                        *base,
                        OjSubmission.status == SubmissionStatus.COMPLETED.value,
                        OjSubmission.result == SubmissionResult.AC.value,
                    )
                )
            ).scalar_one()
            or 0
        )
        judging_total = int(
            (
                await self.db.execute(
                    select(func.count())
                    .select_from(OjSubmission)
                    .where(
                        *base,
                        OjSubmission.status.in_(
                            [
                                SubmissionStatus.QUEUED.value,
                                SubmissionStatus.JUDGING.value,
                            ]
                        ),
                    )
                )
            ).scalar_one()
            or 0
        )
        fail_total = max(0, total - ac_total - judging_total)
        solved_problem_total = int(
            (
                await self.db.execute(
                    select(func.count(func.distinct(OjSubmission.problem_id)))
                    .where(
                        *base,
                        OjSubmission.status == SubmissionStatus.COMPLETED.value,
                        OjSubmission.result == SubmissionResult.AC.value,
                    )
                )
            ).scalar_one()
            or 0
        )
        judged = ac_total + fail_total
        ac_rate = round(ac_total / judged * 100, 1) if judged else 0.0
        return MySubmissionStatsOut(
            submission_total=total,
            ac_total=ac_total,
            fail_total=fail_total,
            judging_total=judging_total,
            ac_rate=ac_rate,
            solved_problem_total=solved_problem_total,
        )

    async def assert_owner(self, query: IdQuery, session: SessionPayload) -> None:
        entity = await self.repo.get_by_id(query.id)
        if entity is None or entity.kind == SubmissionKind.TRIAL.value:
            raise NotFoundError("提交不存在")
        if entity.user_id != session.account_id:
            raise AuthorizationError("只能查看自己的提交事件流")

    async def _can_view_source(
        self,
        problem_id: str,
        owner_id: str,
        viewer_account_id: str | None,
    ) -> bool:
        if viewer_account_id and viewer_account_id == owner_id:
            return True
        problem = await self.db.get(OjProblem, problem_id)
        if problem is None:
            return False
        vis = problem.submission_source_visibility or SubmissionSourceVisibility.ONLY_OWN.value
        if vis == SubmissionSourceVisibility.ALWAYS.value:
            return True
        if vis == SubmissionSourceVisibility.ONLY_OWN.value:
            return False
        if vis == SubmissionSourceVisibility.FOLLOW.value:
            # FOLLOW: treat as ONLY_OWN for portal MVP
            return False
        if vis == SubmissionSourceVisibility.SOLVED.value:
            if not viewer_account_id:
                return False
            return await self._user_solved(problem_id, viewer_account_id)
        return False

    async def _user_solved(self, problem_id: str, account_id: str) -> bool:
        from app.modules.biz.submission.submission.model import OjSubmission

        row = (
            await self.db.execute(
                select(OjSubmission.id)
                .where(
                    OjSubmission.problem_id == problem_id,
                    OjSubmission.user_id == account_id,
                    OjSubmission.status == SubmissionStatus.COMPLETED.value,
                    OjSubmission.result == SubmissionResult.AC.value,
                    OjSubmission.kind != SubmissionKind.TRIAL.value,
                )
                .limit(1)
            )
        ).scalar_one_or_none()
        return row is not None
