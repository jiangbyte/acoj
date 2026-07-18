from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.modules.oj.submission.submission.repository import OjSubmissionRepository
from app.modules.oj.submission.submission.schema import (
    OjSubmissionAdminPageQuery,
    OjSubmissionCreateRequest,
    OjSubmissionSchema,
    OjSubmissionUpdateRequest,
)
from app.platform.db.transaction import transactional


class OjSubmissionService:
    """OJ submission 服务，负责基础维护查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjSubmissionRepository(db)

    async def create(self, payload: OjSubmissionCreateRequest) -> None:
        async with transactional(self.db):
            await self.repo.create(payload)

    async def update(self, payload: OjSubmissionUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def detail(self, query: IdQuery) -> OjSubmissionSchema:
        return to_schema(OjSubmissionSchema, await self.repo.get_required(query.id))

    async def page_admin(self, query: OjSubmissionAdminPageQuery) -> PageData[OjSubmissionSchema]:
        items, total = await self.repo.page(query)
        return build_page(query.pagination, total, to_schema_list(OjSubmissionSchema, items))
