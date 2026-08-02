"""Portal problem bank service."""

from __future__ import annotations

from sqlalchemy import Select, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.modules.biz.problem.enums import ProblemStatus
from app.modules.biz.problem.language.model import OjProblemLanguage
from app.modules.biz.problem.portal.schema import (
    PortalProblemDetailSchema,
    PortalProblemLanguageSchema,
    PortalProblemListSchema,
    PortalProblemPageQuery,
    PortalProblemSubmitRequest,
)
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.problem.repository import OjProblemRepository
from app.modules.biz.problem.problem.schema import OjProblemTrialJudgeResult
from app.modules.biz.problem.type.model import OjProblemTypeRel
from app.modules.biz.problem.worker_languages import list_worker_languages
from app.modules.biz.submission.submission.service import OjSubmissionService


class PortalProblemService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemRepository(db)

    def _assert_public(self, entity: OjProblem) -> None:
        if entity.status != ProblemStatus.PUBLISHED.value or not entity.is_public:
            raise NotFoundError("题目不存在")

    async def page(self, query: PortalProblemPageQuery) -> PageData[PortalProblemListSchema]:
        stmt: Select[tuple[OjProblem]] = select(OjProblem)
        count_stmt = select(func.count(OjProblem.id))
        filters = [
            OjProblem.status == ProblemStatus.PUBLISHED.value,
            OjProblem.is_public.is_(True),
        ]
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
        stmt = stmt.where(*filters)
        count_stmt = count_stmt.where(*filters)
        stmt = stmt.order_by(OjProblem.code.asc()).offset(query.pagination.offset).limit(query.pagination.size)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        type_map = await self.repo.map_type_ids([item.id for item in items])
        all_type_ids = [tid for tids in type_map.values() for tid in tids]
        name_map = await self.repo.map_type_names(all_type_ids)
        group_map = await self.repo.map_group_names([item.group_id for item in items if item.group_id])
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
                    user_count=int(item.user_count or 0),
                    ac_rate=float(item.ac_rate or 0),
                    type_ids=type_ids,
                    type_names=[name_map[t] for t in type_ids if t in name_map],
                )
            )
        return build_page(query.pagination, total, schemas)

    async def detail(self, problem_id: str) -> PortalProblemDetailSchema:
        entity = await self.repo.get_by_id(problem_id)
        if entity is None:
            raise NotFoundError("题目不存在")
        self._assert_public(entity)
        type_ids = await self.repo.list_type_ids(entity.id)
        name_map = await self.repo.map_type_names(type_ids)
        group_map = await self.repo.map_group_names([entity.group_id] if entity.group_id else [])
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
            user_count=int(entity.user_count or 0),
            ac_rate=float(entity.ac_rate or 0),
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
