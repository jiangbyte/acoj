"""Learning plan service."""

from __future__ import annotations

from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import IdsRequest
from app.modules.biz.problem.enums import ProblemDifficulty
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.study.learning_plan_schema import (
    LearningPlanAdminPageQuery,
    LearningPlanCreateRequest,
    LearningPlanSchema,
    LearningPlanSectionInput,
    LearningPlanSectionSchema,
    LearningPlanUpdateRequest,
)
from app.modules.biz.study.model import OjLearningPlan, OjLearningPlanItem, OjLearningPlanSection
from app.modules.biz.study.problem_list_schema import ProblemListProblemBrief, ProblemListProgress
from app.modules.biz.study.solve import attempted_problem_ids, solved_problem_ids
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


class LearningPlanService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def _get_required(self, plan_id: str) -> OjLearningPlan:
        entity = await self.db.get(OjLearningPlan, plan_id)
        if entity is None:
            raise NotFoundError("学习计划不存在")
        return entity

    async def _problem_count(self, plan_id: str) -> int:
        return int(
            (
                await self.db.execute(
                    select(func.count())
                    .select_from(OjLearningPlanItem)
                    .join(OjLearningPlanSection, OjLearningPlanSection.id == OjLearningPlanItem.section_id)
                    .where(OjLearningPlanSection.plan_id == plan_id)
                )
            ).scalar_one()
            or 0
        )

    async def _replace_sections(self, plan_id: str, sections: list[LearningPlanSectionInput]) -> None:
        old_sections = list(
            (await self.db.execute(select(OjLearningPlanSection).where(OjLearningPlanSection.plan_id == plan_id)))
            .scalars()
            .all()
        )
        old_ids = [s.id for s in old_sections]
        if old_ids:
            await self.db.execute(delete(OjLearningPlanItem).where(OjLearningPlanItem.section_id.in_(old_ids)))
            await self.db.execute(delete(OjLearningPlanSection).where(OjLearningPlanSection.plan_id == plan_id))
        for idx, sec in enumerate(sections):
            section = OjLearningPlanSection(
                id=generate_snowflake_id(),
                plan_id=plan_id,
                title=sec.title,
                sort=sec.sort if sec.sort else idx,
            )
            self.db.add(section)
            await self.db.flush()
            for j, pid in enumerate(sec.problem_ids):
                self.db.add(
                    OjLearningPlanItem(
                        id=generate_snowflake_id(),
                        section_id=section.id,
                        problem_id=pid,
                        sort=j,
                    )
                )
        await self.db.flush()

    async def _to_schema(
        self,
        entity: OjLearningPlan,
        *,
        with_sections: bool = False,
        viewer_id: str | None = None,
        include_related: bool = False,
    ) -> LearningPlanSchema:
        problem_count = await self._problem_count(entity.id)
        sections_out: list[LearningPlanSectionSchema] = []
        progress = ProblemListProgress(total=problem_count)
        all_problems: list[OjProblem] = []
        if with_sections:
            sections = list(
                (
                    await self.db.execute(
                        select(OjLearningPlanSection)
                        .where(OjLearningPlanSection.plan_id == entity.id)
                        .order_by(OjLearningPlanSection.sort.asc())
                    )
                )
                .scalars()
                .all()
            )
            for sec in sections:
                items = list(
                    (
                        await self.db.execute(
                            select(OjLearningPlanItem)
                            .where(OjLearningPlanItem.section_id == sec.id)
                            .order_by(OjLearningPlanItem.sort.asc())
                        )
                    )
                    .scalars()
                    .all()
                )
                pids = [i.problem_id for i in items]
                sort_map = {i.problem_id: i.sort for i in items}
                problems: list[OjProblem] = []
                if pids:
                    problems = list(
                        (await self.db.execute(select(OjProblem).where(OjProblem.id.in_(pids)))).scalars().all()
                    )
                    problems.sort(key=lambda p: sort_map.get(p.id, 0))
                all_problems.extend(problems)
                solved = await solved_problem_ids(self.db, viewer_id, pids) if viewer_id else set()
                attempted = await attempted_problem_ids(self.db, viewer_id, pids) if viewer_id else set()
                briefs = [
                    ProblemListProblemBrief(
                        id=p.id,
                        code=p.code,
                        name=p.name,
                        difficulty=p.difficulty or ProblemDifficulty.MEDIUM.value,
                        ac_rate=float(p.ac_rate or 0),
                        user_count=int(p.user_count or 0),
                        solved=p.id in solved,
                        attempted=p.id in attempted and p.id not in solved,
                        sort=sort_map.get(p.id, 0),
                    )
                    for p in problems
                ]
                sections_out.append(
                    LearningPlanSectionSchema(id=sec.id, title=sec.title, sort=sec.sort, problems=briefs)
                )
            if viewer_id and all_problems:
                solved_all = await solved_problem_ids(self.db, viewer_id, [p.id for p in all_problems])
                attempted_all = await attempted_problem_ids(self.db, viewer_id, [p.id for p in all_problems])
                progress = ProblemListProgress(total=len(all_problems))
                for p in all_problems:
                    diff = p.difficulty or ProblemDifficulty.MEDIUM.value
                    if diff == ProblemDifficulty.EASY.value:
                        progress.easy_total += 1
                        if p.id in solved_all:
                            progress.easy_solved += 1
                    elif diff == ProblemDifficulty.HARD.value:
                        progress.hard_total += 1
                        if p.id in solved_all:
                            progress.hard_solved += 1
                    else:
                        progress.medium_total += 1
                        if p.id in solved_all:
                            progress.medium_solved += 1
                    if p.id in solved_all:
                        progress.solved += 1
                    elif p.id in attempted_all:
                        progress.attempted += 1

        related: list[LearningPlanSchema] = []
        if include_related:
            related_rows = list(
                (
                    await self.db.execute(
                        select(OjLearningPlan)
                        .where(
                            OjLearningPlan.status == StatusEnum.ENABLED.value,
                            OjLearningPlan.id != entity.id,
                            OjLearningPlan.category == entity.category,
                        )
                        .order_by(OjLearningPlan.sort.asc())
                        .limit(5)
                    )
                )
                .scalars()
                .all()
            )
            for r in related_rows:
                related.append(await self._to_schema(r, with_sections=False))

        return LearningPlanSchema(
            id=entity.id,
            code=entity.code,
            title=entity.title,
            subtitle=entity.subtitle,
            overview=entity.overview,
            cover_url=entity.cover_url,
            category=entity.category,
            status=entity.status,
            sort=entity.sort,
            problem_count=problem_count,
            progress=progress if with_sections else None,
            sections=sections_out,
            related=related,
            extra=entity.extra or {},
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )

    async def create(self, payload: LearningPlanCreateRequest) -> str:
        async with transactional(self.db):
            exists = (
                await self.db.execute(select(OjLearningPlan.id).where(OjLearningPlan.code == payload.code))
            ).scalar_one_or_none()
            if exists:
                raise BusinessError("计划编码已存在")
            entity = OjLearningPlan(
                id=generate_snowflake_id(),
                code=payload.code,
                title=payload.title,
                subtitle=payload.subtitle,
                overview=payload.overview,
                cover_url=payload.cover_url,
                category=payload.category.value,
                status=payload.status,
                sort=payload.sort,
                extra=payload.extra or {},
            )
            self.db.add(entity)
            await self.db.flush()
            await self._replace_sections(entity.id, payload.sections)
            return entity.id

    async def update(self, payload: LearningPlanUpdateRequest) -> None:
        async with transactional(self.db):
            entity = await self._get_required(payload.id)
            entity.code = payload.code
            entity.title = payload.title
            entity.subtitle = payload.subtitle
            entity.overview = payload.overview
            entity.cover_url = payload.cover_url
            entity.category = payload.category.value
            entity.status = payload.status
            entity.sort = payload.sort
            entity.extra = payload.extra or {}
            await self._replace_sections(entity.id, payload.sections)
            await self.db.flush()

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            for pid in payload.ids:
                entity = await self._get_required(pid)
                sections = list(
                    (
                        await self.db.execute(
                            select(OjLearningPlanSection.id).where(OjLearningPlanSection.plan_id == pid)
                        )
                    )
                    .scalars()
                    .all()
                )
                if sections:
                    await self.db.execute(delete(OjLearningPlanItem).where(OjLearningPlanItem.section_id.in_(sections)))
                    await self.db.execute(delete(OjLearningPlanSection).where(OjLearningPlanSection.plan_id == pid))
                await self.db.delete(entity)
            await self.db.flush()

    async def page_admin(self, query: LearningPlanAdminPageQuery) -> PageData[LearningPlanSchema]:
        filters = []
        if query.title:
            filters.append(OjLearningPlan.title.ilike(f"%{query.title}%"))
        if query.code:
            filters.append(OjLearningPlan.code.ilike(f"%{query.code}%"))
        if query.category:
            filters.append(OjLearningPlan.category == query.category.value)
        if query.status:
            filters.append(OjLearningPlan.status == query.status)
        stmt = select(OjLearningPlan)
        count_stmt = select(func.count()).select_from(OjLearningPlan)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        total = int((await self.db.execute(count_stmt)).scalar_one() or 0)
        rows = list(
            (
                await self.db.execute(
                    stmt.order_by(OjLearningPlan.sort.asc(), OjLearningPlan.created_at.desc())
                    .offset(query.pagination.offset)
                    .limit(query.pagination.size)
                )
            )
            .scalars()
            .all()
        )
        return build_page(query.pagination, total, [await self._to_schema(r) for r in rows])

    async def detail_admin(self, plan_id: str) -> LearningPlanSchema:
        return await self._to_schema(await self._get_required(plan_id), with_sections=True)

    async def page_portal(
        self, pagination: PageQuery, category: str | None = None
    ) -> PageData[LearningPlanSchema]:
        filters = [OjLearningPlan.status == StatusEnum.ENABLED.value]
        if category:
            filters.append(OjLearningPlan.category == category)
        total = int(
            (await self.db.execute(select(func.count()).select_from(OjLearningPlan).where(*filters))).scalar_one()
            or 0
        )
        rows = list(
            (
                await self.db.execute(
                    select(OjLearningPlan)
                    .where(*filters)
                    .order_by(OjLearningPlan.sort.asc(), OjLearningPlan.created_at.desc())
                    .offset(pagination.offset)
                    .limit(pagination.size)
                )
            )
            .scalars()
            .all()
        )
        return build_page(pagination, total, [await self._to_schema(r) for r in rows])

    async def detail_portal(self, plan_id: str, viewer_id: str | None) -> LearningPlanSchema:
        entity = await self._get_required(plan_id)
        if entity.status != StatusEnum.ENABLED.value:
            raise NotFoundError("学习计划不存在")
        return await self._to_schema(entity, with_sections=True, viewer_id=viewer_id, include_related=True)
