from sqlalchemy import Select, delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.core.exceptions.business import NotFoundError
from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.submission.submission.model import (
    OjContestSubmission,
    OjSubmission,
    OjSubmissionCase,
    OjSubmissionSource,
)
from app.modules.biz.submission.submission.schema import OjSubmissionAdminPageQuery


class OjSubmissionRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_by_id(self, entity_id: str) -> OjSubmission | None:
        return await self.db.get(OjSubmission, entity_id)

    async def get_required(self, entity_id: str) -> OjSubmission:
        entity = await self.get_by_id(entity_id)
        if entity is None:
            raise NotFoundError("OjSubmission not found")
        return entity

    async def get_detail_with_relations(self, entity_id: str) -> OjSubmission:
        stmt = (
            select(OjSubmission)
            .where(OjSubmission.id == entity_id)
            .options(
                selectinload(OjSubmission.source_row),
                selectinload(OjSubmission.cases),
                selectinload(OjSubmission.contest_submission),
            )
        )
        entity = (await self.db.execute(stmt)).scalar_one_or_none()
        if entity is None:
            raise NotFoundError("OjSubmission not found")
        return entity

    async def page_admin(self, query: OjSubmissionAdminPageQuery) -> tuple[list[OjSubmission], int]:
        stmt: Select[tuple[OjSubmission]] = select(OjSubmission)
        count_stmt = select(func.count(OjSubmission.id))
        filters = []
        if query.problem_id:
            filters.append(OjSubmission.problem_id == query.problem_id)
        if query.contest_id:
            filters.append(OjSubmission.contest_id == query.contest_id)
        if query.user_id:
            filters.append(OjSubmission.user_id == query.user_id)
        if query.kind is not None:
            filters.append(OjSubmission.kind == query.kind.value)
        if query.status is not None:
            filters.append(OjSubmission.status == query.status.value)
        if query.result:
            filters.append(OjSubmission.result == query.result)
        if query.language_key:
            filters.append(OjSubmission.language_key == query.language_key)
        if query.problem_code:
            problem_ids_stmt = select(OjProblem.id).where(OjProblem.code == query.problem_code)
            filters.append(OjSubmission.problem_id.in_(problem_ids_stmt))
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(OjSubmission.created_at.desc(), OjSubmission.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def map_problem_labels(self, problem_ids: list[str]) -> dict[str, tuple[str, str]]:
        unique = list(dict.fromkeys([pid for pid in problem_ids if pid]))
        if not unique:
            return {}
        stmt = select(OjProblem.id, OjProblem.code, OjProblem.name).where(OjProblem.id.in_(unique))
        rows = (await self.db.execute(stmt)).all()
        return {row.id: (row.code, row.name) for row in rows}

    async def map_contest_labels(self, contest_ids: list[str]) -> dict[str, tuple[str, str]]:
        unique = list(dict.fromkeys([cid for cid in contest_ids if cid]))
        if not unique:
            return {}
        stmt = select(OjContest.id, OjContest.key, OjContest.name).where(OjContest.id.in_(unique))
        rows = (await self.db.execute(stmt)).all()
        return {row.id: (row.key, row.name) for row in rows}

    async def replace_cases(self, submission_id: str, rows: list[OjSubmissionCase]) -> None:
        await self.db.execute(delete(OjSubmissionCase).where(OjSubmissionCase.submission_id == submission_id))
        if rows:
            self.db.add_all(rows)
        await self.db.flush()

    async def delete_many(self, entity_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(entity_ids))
        if not unique_ids:
            return
        stmt = select(OjSubmission.id).where(OjSubmission.id.in_(unique_ids))
        existing = set((await self.db.execute(stmt)).scalars().all())
        if len(existing) != len(unique_ids):
            raise NotFoundError("OjSubmission not found")
        await self.db.execute(delete(OjContestSubmission).where(OjContestSubmission.submission_id.in_(unique_ids)))
        await self.db.execute(delete(OjSubmissionCase).where(OjSubmissionCase.submission_id.in_(unique_ids)))
        await self.db.execute(delete(OjSubmissionSource).where(OjSubmissionSource.submission_id.in_(unique_ids)))
        await self.db.execute(delete(OjSubmission).where(OjSubmission.id.in_(unique_ids)))
