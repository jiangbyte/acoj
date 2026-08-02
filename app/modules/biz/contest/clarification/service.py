"""Admin contest clarification broadcasts + Q&A threads."""

from __future__ import annotations

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdsRequest, to_schema, to_schema_list
from app.modules.biz.contest.clarification.model import (
    OjContestClarification,
    OjContestClarificationMessage,
    OjContestClarificationThread,
)
from app.modules.biz.contest.clarification.schema import (
    OjContestClarificationAdminPageQuery,
    OjContestClarificationCreateRequest,
    OjContestClarificationMessageSchema,
    OjContestClarificationSchema,
    OjContestClarificationThreadAdminPageQuery,
    OjContestClarificationThreadPromoteRequest,
    OjContestClarificationThreadReplyRequest,
    OjContestClarificationThreadSchema,
    OjContestClarificationThreadStatusRequest,
    OjContestClarificationUpdateRequest,
)
from app.modules.biz.contest.enums import ClarificationThreadStatus
from app.modules.biz.contest.lifecycle import utcnow
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


class OjContestClarificationService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create_broadcast(
        self, contest_id: str, payload: OjContestClarificationCreateRequest
    ) -> str:
        async with transactional(self.db):
            entity = OjContestClarification(
                id=generate_snowflake_id(),
                contest_id=contest_id,
                problem_id=payload.problem_id,
                title=payload.title,
                body=payload.body,
                published_at=payload.published_at or utcnow(),
            )
            self.db.add(entity)
            await self.db.flush()
            return entity.id

    async def update_broadcast(
        self, contest_id: str, payload: OjContestClarificationUpdateRequest
    ) -> None:
        async with transactional(self.db):
            entity = await self.db.get(OjContestClarification, payload.id)
            if entity is None or entity.contest_id != contest_id:
                raise NotFoundError("答疑广播不存在")
            entity.problem_id = payload.problem_id
            entity.title = payload.title
            entity.body = payload.body
            if payload.published_at is not None:
                entity.published_at = payload.published_at
            await self.db.flush()

    async def delete_broadcast(self, contest_id: str, payload: IdsRequest) -> None:
        async with transactional(self.db):
            rows = list(
                (
                    await self.db.execute(
                        select(OjContestClarification).where(
                            OjContestClarification.id.in_(payload.ids),
                            OjContestClarification.contest_id == contest_id,
                        )
                    )
                ).scalars().all()
            )
            if len(rows) != len(set(payload.ids)):
                raise NotFoundError("答疑广播不存在")
            for row in rows:
                await self.db.delete(row)
            await self.db.flush()

    async def page_broadcasts(
        self, contest_id: str, query: OjContestClarificationAdminPageQuery
    ) -> PageData[OjContestClarificationSchema]:
        filters = [OjContestClarification.contest_id == contest_id]
        if query.problem_id:
            filters.append(OjContestClarification.problem_id == query.problem_id)
        stmt = (
            select(OjContestClarification)
            .where(*filters)
            .order_by(OjContestClarification.published_at.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        count_stmt = select(func.count(OjContestClarification.id)).where(*filters)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return build_page(query.pagination, total, to_schema_list(OjContestClarificationSchema, items))

    async def page_threads(
        self, contest_id: str, query: OjContestClarificationThreadAdminPageQuery
    ) -> PageData[OjContestClarificationThreadSchema]:
        filters = [OjContestClarificationThread.contest_id == contest_id]
        if query.status:
            filters.append(OjContestClarificationThread.status == query.status.value)
        if query.account_id:
            filters.append(OjContestClarificationThread.account_id == query.account_id)
        stmt = (
            select(OjContestClarificationThread)
            .where(*filters)
            .order_by(OjContestClarificationThread.created_at.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        count_stmt = select(func.count(OjContestClarificationThread.id)).where(*filters)
        threads = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        schemas = await self._with_messages(threads)
        return build_page(query.pagination, total, schemas)

    async def reply(
        self,
        contest_id: str,
        account_id: str,
        payload: OjContestClarificationThreadReplyRequest,
    ) -> OjContestClarificationThreadSchema:
        async with transactional(self.db):
            thread = await self.db.get(OjContestClarificationThread, payload.thread_id)
            if thread is None or thread.contest_id != contest_id:
                raise NotFoundError("提问不存在")
            self.db.add(
                OjContestClarificationMessage(
                    id=generate_snowflake_id(),
                    thread_id=thread.id,
                    account_id=account_id,
                    body=payload.body,
                    is_staff=True,
                )
            )
            if payload.set_answered:
                thread.status = ClarificationThreadStatus.ANSWERED.value
            await self.db.flush()
            await self.db.refresh(thread)
            return (await self._with_messages([thread]))[0]

    async def set_status(
        self, contest_id: str, payload: OjContestClarificationThreadStatusRequest
    ) -> None:
        async with transactional(self.db):
            thread = await self.db.get(OjContestClarificationThread, payload.thread_id)
            if thread is None or thread.contest_id != contest_id:
                raise NotFoundError("提问不存在")
            thread.status = payload.status.value
            await self.db.flush()

    async def promote(
        self, contest_id: str, payload: OjContestClarificationThreadPromoteRequest
    ) -> str:
        async with transactional(self.db):
            thread = await self.db.get(OjContestClarificationThread, payload.thread_id)
            if thread is None or thread.contest_id != contest_id:
                raise NotFoundError("提问不存在")
            msgs = list(
                (
                    await self.db.execute(
                        select(OjContestClarificationMessage)
                        .where(OjContestClarificationMessage.thread_id == thread.id)
                        .order_by(OjContestClarificationMessage.created_at.asc())
                    )
                ).scalars().all()
            )
            if not msgs:
                raise BusinessError("提问无内容，无法转公开")
            staff_msg = next((m for m in reversed(msgs) if m.is_staff), None)
            body = payload.body or (staff_msg.body if staff_msg else msgs[0].body)
            title = payload.title or thread.title
            entity = OjContestClarification(
                id=generate_snowflake_id(),
                contest_id=contest_id,
                problem_id=thread.problem_id,
                title=title,
                body=body,
                published_at=utcnow(),
            )
            self.db.add(entity)
            thread.status = ClarificationThreadStatus.ANSWERED.value
            await self.db.flush()
            return entity.id

    async def _with_messages(
        self, threads: list[OjContestClarificationThread]
    ) -> list[OjContestClarificationThreadSchema]:
        if not threads:
            return []
        ids = [t.id for t in threads]
        msgs = list(
            (
                await self.db.execute(
                    select(OjContestClarificationMessage)
                    .where(OjContestClarificationMessage.thread_id.in_(ids))
                    .order_by(OjContestClarificationMessage.created_at.asc())
                )
            ).scalars().all()
        )
        by_t: dict[str, list] = {i: [] for i in ids}
        for m in msgs:
            by_t.setdefault(m.thread_id, []).append(to_schema(OjContestClarificationMessageSchema, m))
        result = []
        for t in threads:
            base = to_schema(OjContestClarificationThreadSchema, t)
            result.append(base.model_copy(update={"messages": by_t.get(t.id, [])}))
        return result
