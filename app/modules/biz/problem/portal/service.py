"""Portal problem bank service."""

from __future__ import annotations

from math import ceil

from sqlalchemy import Select, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import BusinessError, NotFoundError
from app.modules.biz.problem.enums import ProblemDifficulty, ProblemStatus
from app.modules.biz.problem.group.model import OjProblemGroup
from app.modules.biz.problem.language.model import OjProblemLanguage
from app.modules.biz.problem.portal.recommend import recommend_problems
from app.modules.biz.problem.portal.schema import (
    PortalProblemDetailSchema,
    PortalProblemGroupItem,
    PortalProblemLanguageSchema,
    PortalProblemListSchema,
    PortalProblemPageData,
    PortalProblemPageQuery,
    PortalProblemRecommendData,
    PortalProblemRecommendItem,
    PortalProblemSubmitRequest,
    PortalProblemTypeItem,
)
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.problem.repository import OjProblemRepository
from app.modules.biz.problem.problem.schema import OjProblemTrialJudgeResult
from app.modules.biz.problem.type.model import OjProblemType, OjProblemTypeRel
from app.modules.biz.problem.worker_languages import list_worker_languages
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission
from app.modules.biz.submission.submission.service import OjSubmissionService


class PortalProblemService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemRepository(db)

    def _assert_public(self, entity: OjProblem) -> None:
        if entity.status != ProblemStatus.PUBLISHED.value or not entity.is_public:
            raise NotFoundError("题目不存在")

    def _public_filters(self):
        return [
            OjProblem.status == ProblemStatus.PUBLISHED.value,
            OjProblem.is_public.is_(True),
        ]

    def _difficulty(self, value: str | None) -> ProblemDifficulty:
        try:
            return ProblemDifficulty(value or ProblemDifficulty.MEDIUM.value)
        except ValueError:
            return ProblemDifficulty.MEDIUM

    async def _solved_problem_ids(self, account_id: str, problem_ids: list[str] | None = None) -> set[str]:
        if not account_id:
            return set()
        filters = [
            OjSubmission.user_id == account_id,
            OjSubmission.result == SubmissionResult.AC.value,
            OjSubmission.kind != SubmissionKind.TRIAL.value,
        ]
        if problem_ids is not None:
            if not problem_ids:
                return set()
            filters.append(OjSubmission.problem_id.in_(problem_ids))
        rows = (await self.db.execute(select(OjSubmission.problem_id).where(*filters).distinct())).scalars().all()
        return set(rows)

    async def list_groups(self) -> list[PortalProblemGroupItem]:
        count_stmt = (
            select(OjProblem.group_id, func.count(OjProblem.id))
            .where(*self._public_filters(), OjProblem.group_id.is_not(None))
            .group_by(OjProblem.group_id)
        )
        count_map = dict((await self.db.execute(count_stmt)).all())
        groups = list(
            (
                await self.db.execute(
                    select(OjProblemGroup).order_by(OjProblemGroup.sort.asc(), OjProblemGroup.code.asc())
                )
            )
            .scalars()
            .all()
        )
        return [
            PortalProblemGroupItem(
                id=item.id,
                code=item.code,
                name=item.name,
                sort=item.sort,
                problem_count=int(count_map.get(item.id, 0)),
            )
            for item in groups
        ]

    async def list_types(self) -> list[PortalProblemTypeItem]:
        count_stmt = (
            select(OjProblemTypeRel.type_id, func.count(OjProblem.id))
            .select_from(OjProblemTypeRel)
            .join(OjProblem, OjProblem.id == OjProblemTypeRel.problem_id)
            .where(*self._public_filters())
            .group_by(OjProblemTypeRel.type_id)
        )
        count_map = dict((await self.db.execute(count_stmt)).all())
        types = list(
            (
                await self.db.execute(
                    select(OjProblemType).order_by(OjProblemType.sort.asc(), OjProblemType.code.asc())
                )
            )
            .scalars()
            .all()
        )
        return [
            PortalProblemTypeItem(
                id=item.id,
                code=item.code,
                name=item.name,
                sort=item.sort,
                problem_count=int(count_map.get(item.id, 0)),
            )
            for item in types
        ]

    def _list_filters(self, query: PortalProblemPageQuery):
        filters = self._public_filters()
        if query.code:
            filters.append(OjProblem.code.ilike(f"%{query.code}%"))
        if query.name:
            filters.append(OjProblem.name.ilike(f"%{query.name}%"))
        if query.keyword:
            like = f"%{query.keyword}%"
            filters.append(or_(OjProblem.code.ilike(like), OjProblem.name.ilike(like)))
        if query.group_id is not None:
            filters.append(OjProblem.group_id == query.group_id)
        if query.type_id:
            filters.append(
                OjProblem.id.in_(
                    select(OjProblemTypeRel.problem_id).where(OjProblemTypeRel.type_id == query.type_id)
                )
            )
        return filters

    async def page(
        self,
        query: PortalProblemPageQuery,
        *,
        account_id: str | None = None,
    ) -> PortalProblemPageData:
        filters = self._list_filters(query)
        stmt: Select[tuple[OjProblem]] = select(OjProblem).where(*filters)
        count_stmt = select(func.count(OjProblem.id)).where(*filters)
        stmt = stmt.order_by(OjProblem.code.asc()).offset(query.pagination.offset).limit(query.pagination.size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = int((await self.db.execute(count_stmt)).scalar_one() or 0)
        type_map = await self.repo.map_type_ids([item.id for item in items])
        all_type_ids = [tid for tids in type_map.values() for tid in tids]
        name_map = await self.repo.map_type_names(all_type_ids)
        group_map = await self.repo.map_group_names([item.group_id for item in items if item.group_id])

        solved_ids: set[str] = set()
        solved_count = 0
        if account_id:
            solved_ids = await self._solved_problem_ids(account_id)
            if solved_ids:
                solved_count = int(
                    (
                        await self.db.execute(
                            select(func.count(OjProblem.id)).where(
                                *filters,
                                OjProblem.id.in_(solved_ids),
                            )
                        )
                    ).scalar_one()
                    or 0
                )

        schemas: list[PortalProblemListSchema] = []
        for item in items:
            type_ids = type_map.get(item.id, [])
            schemas.append(
                PortalProblemListSchema(
                    id=item.id,
                    code=item.code,
                    name=item.name,
                    summary=item.summary,
                    group_id=item.group_id,
                    group_name=group_map.get(item.group_id) if item.group_id else None,
                    time_limit_ms=item.time_limit_ms,
                    memory_limit_kb=item.memory_limit_kb,
                    points=float(item.points),
                    partial=bool(item.partial),
                    difficulty=self._difficulty(getattr(item, "difficulty", None)),
                    user_count=int(item.user_count or 0),
                    ac_rate=float(item.ac_rate or 0),
                    solved=item.id in solved_ids,
                    type_ids=type_ids,
                    type_names=[name_map[t] for t in type_ids if t in name_map],
                )
            )
        pages = ceil(total / query.pagination.size) if total else 0
        return PortalProblemPageData(
            size=query.pagination.size,
            current=query.pagination.current,
            total=total,
            pages=pages,
            records=schemas,
            solved_count=solved_count,
        )

    async def recommend(
        self,
        *,
        account_id: str | None = None,
        size: int = 8,
    ) -> PortalProblemRecommendData:
        result = await recommend_problems(self.db, account_id=account_id, size=size)
        items = [row.problem for row in result.items]
        type_map = await self.repo.map_type_ids([item.id for item in items])
        all_type_ids = [tid for tids in type_map.values() for tid in tids]
        name_map = await self.repo.map_type_names(all_type_ids)
        group_map = await self.repo.map_group_names([item.group_id for item in items if item.group_id])
        solved_ids: set[str] = set()
        if account_id:
            solved_ids = await self._solved_problem_ids(account_id, [item.id for item in items])

        records: list[PortalProblemRecommendItem] = []
        for scored in result.items:
            item = scored.problem
            type_ids = type_map.get(item.id, [])
            records.append(
                PortalProblemRecommendItem(
                    id=item.id,
                    code=item.code,
                    name=item.name,
                    summary=item.summary,
                    group_id=item.group_id,
                    group_name=group_map.get(item.group_id) if item.group_id else None,
                    time_limit_ms=item.time_limit_ms,
                    memory_limit_kb=item.memory_limit_kb,
                    points=float(item.points),
                    partial=bool(item.partial),
                    difficulty=self._difficulty(getattr(item, "difficulty", None)),
                    user_count=int(item.user_count or 0),
                    ac_rate=float(item.ac_rate or 0),
                    solved=item.id in solved_ids,
                    type_ids=type_ids,
                    type_names=[name_map[t] for t in type_ids if t in name_map],
                    reason=scored.reason,
                    score=round(scored.score, 3),
                )
            )
        return PortalProblemRecommendData(
            records=records,
            strategy=result.strategy,
            target_difficulty=result.target_difficulty,
        )

    async def detail(
        self,
        problem_id: str,
        *,
        account_id: str | None = None,
    ) -> PortalProblemDetailSchema:
        entity = await self.repo.get_by_id(problem_id)
        if entity is None:
            raise NotFoundError("题目不存在")
        self._assert_public(entity)
        type_ids = await self.repo.list_type_ids(entity.id)
        name_map = await self.repo.map_type_names(type_ids)
        group_map = await self.repo.map_group_names([entity.group_id] if entity.group_id else [])
        solved = False
        if account_id:
            solved = bool(await self._solved_problem_ids(account_id, [entity.id]))
        return PortalProblemDetailSchema(
            id=entity.id,
            code=entity.code,
            name=entity.name,
            summary=entity.summary,
            description=entity.description,
            group_id=entity.group_id,
            group_name=group_map.get(entity.group_id) if entity.group_id else None,
            time_limit_ms=entity.time_limit_ms,
            memory_limit_kb=entity.memory_limit_kb,
            points=float(entity.points),
            partial=bool(entity.partial),
            difficulty=self._difficulty(getattr(entity, "difficulty", None)),
            user_count=int(entity.user_count or 0),
            ac_rate=float(entity.ac_rate or 0),
            solved=solved,
            type_ids=type_ids,
            type_names=[name_map[t] for t in type_ids if t in name_map],
            submission_source_visibility=entity.submission_source_visibility,
            published_at=entity.published_at,
            extra=entity.extra or {},
        )

    async def languages(self, problem_id: str) -> list[PortalProblemLanguageSchema]:
        entity = await self.repo.get_by_id(problem_id)
        if entity is None:
            raise NotFoundError("题目不存在")
        self._assert_public(entity)
        rows = list(
            (
                await self.db.execute(
                    select(OjProblemLanguage).where(
                        OjProblemLanguage.problem_id == problem_id,
                        OjProblemLanguage.status == StatusEnum.ENABLED.value,
                    )
                )
            )
            .scalars()
            .all()
        )
        meta = {item["key"]: item for item in list_worker_languages()}
        return [
            PortalProblemLanguageSchema(
                language_key=row.language_key,
                label=(meta.get(row.language_key) or {}).get("label"),
                extension=(meta.get(row.language_key) or {}).get("extension"),
                time_limit_ms=row.time_limit_ms,
                memory_limit_kb=row.memory_limit_kb,
            )
            for row in rows
        ]

    async def submit(
        self,
        *,
        problem_id: str,
        account_id: str,
        payload: PortalProblemSubmitRequest,
    ) -> OjProblemTrialJudgeResult:
        entity = await self.repo.get_by_id(problem_id)
        if entity is None:
            raise NotFoundError("题目不存在")
        self._assert_public(entity)
        if not payload.source.strip():
            raise BusinessError("源代码不能为空")
        return await OjSubmissionService(self.db).create_official_and_judge(
            problem_id=problem_id,
            user_id=account_id,
            language_key=payload.language_key,
            source=payload.source,
            wait_timeout_sec=payload.wait_timeout_sec,
            wait=payload.wait,
        )
