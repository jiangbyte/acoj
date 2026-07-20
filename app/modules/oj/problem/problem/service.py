from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.problem.problem.repository import OjProblemRepository
from app.modules.oj.problem.problem.schema import (
    OjProblemAdminPageQuery,
    OjProblemCreateRequest,
    OjProblemSchema,
    OjProblemUpdateRequest,
    OjProblemWorkspaceResult,
)
from app.modules.oj.problem.asset.repository import OjProblemAssetRepository
from app.modules.oj.problem.asset.schema import OjProblemAssetSchema
from app.modules.oj.problem.dataset.repository import OjDatasetRepository
from app.modules.oj.problem.dataset.schema import OjDatasetSchema
from app.modules.oj.problem.member.repository import OjProblemMemberRepository
from app.modules.oj.problem.member.schema import OjProblemMemberSchema
from app.modules.oj.problem.objective_answer.repository import OjObjectiveAnswerRepository
from app.modules.oj.problem.objective_answer.schema import OjObjectiveAnswerSchema
from app.modules.oj.problem.sample.repository import OjProblemSampleRepository
from app.modules.oj.problem.sample.schema import OjProblemSampleSchema
from app.modules.oj.problem.tag.repository import OjProblemTagRepository
from app.modules.oj.problem.tag.schema import OjProblemTagSchema
from app.modules.oj.problem.tag_relation.repository import OjProblemTagRelationRepository
from app.modules.oj.problem.tag_relation.schema import OjProblemTagRelationSchema
from app.modules.oj.problem.test_case.repository import OjTestCaseRepository
from app.modules.oj.problem.test_case.schema import OjTestCaseSchema
from app.platform.db.transaction import transactional


class OjProblemService:
    """OJ problem 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjProblemRepository(db)

    async def create(self, payload: OjProblemCreateRequest) -> str:
        async with transactional(self.db):
            entity = await self.repo.create(payload)
            return entity.id

    async def update(self, payload: OjProblemUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjProblemSchema:
        return to_schema(OjProblemSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjProblemAdminPageQuery) -> PageData[OjProblemSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjProblemSchema, items))

    async def workspace(self, query: IdQuery) -> OjProblemWorkspaceResult:
        problem = await self.repo.get_by_id(query.id)
        if not problem:
            return OjProblemWorkspaceResult()
        problem_schema = to_schema(OjProblemSchema, problem)

        samples = await OjProblemSampleRepository(self.db).list_by_problem(query.id)
        datasets = await OjDatasetRepository(self.db).list_by_problem(query.id)
        dataset_ids = [d.id for d in datasets]
        test_cases = (
            await OjTestCaseRepository(self.db).list_by_datasets(dataset_ids)
            if dataset_ids
            else []
        )
        tag_relations = await OjProblemTagRelationRepository(self.db).list_by_problem(query.id)
        tag_ids = [tr.tag_id for tr in tag_relations]
        tags = (
            await OjProblemTagRepository(self.db).list_by_ids(tag_ids)
            if tag_ids
            else []
        )
        assets = await OjProblemAssetRepository(self.db).list_by_problem(query.id)
        members = await OjProblemMemberRepository(self.db).list_by_problem(query.id)
        objective_answers = await OjObjectiveAnswerRepository(self.db).list_by_problem(query.id)

        return OjProblemWorkspaceResult(
            problem=problem_schema,
            samples=to_schema_list(OjProblemSampleSchema, samples),
            datasets=to_schema_list(OjDatasetSchema, datasets),
            test_cases=to_schema_list(OjTestCaseSchema, test_cases),
            tags=to_schema_list(OjProblemTagSchema, tags),
            tag_relations=to_schema_list(OjProblemTagRelationSchema, tag_relations),
            assets=to_schema_list(OjProblemAssetSchema, assets),
            members=to_schema_list(OjProblemMemberSchema, members),
            objective_answers=to_schema_list(OjObjectiveAnswerSchema, objective_answers),
        )
