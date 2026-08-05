"""Problem list service (admin official + portal personal)."""

from __future__ import annotations

from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import AuthorizationError, BusinessError, NotFoundError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, ProblemIdQuery
from app.core.security.session import SessionPayload
from app.modules.biz.problem.enums import ProblemDifficulty
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.study.enums import ProblemListKind, ProblemListVisibility
from app.modules.biz.study.problem_list.model import OjProblemList, OjProblemListItem
from app.modules.biz.study.problem_list.schema import (
    OfficialProblemListCreateRequest,
    ProblemListAdminPageQuery,
    ProblemListCreateRequest,
    ProblemListItemMutation,
    ProblemListOfficialPageQuery,
    ProblemListProblemBrief,
    ProblemListProgress,
    ProblemListReorderRequest,
    ProblemListSchema,
    ProblemListUpdateRequest,
)
from app.modules.biz.study.solve import attempted_problem_ids, solved_problem_ids
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id

FAVORITES_TITLE = "我的收藏"


class ProblemListService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def ensure_favorites(self, account_id: str) -> OjProblemList:
        """每人一份系统收藏题单；若曾被降级，按标题回收。"""
        row = (
            await self.db.execute(
                select(OjProblemList).where(
                    OjProblemList.owner_id == account_id,
                    OjProblemList.is_system.is_(True),
                    OjProblemList.kind == ProblemListKind.PERSONAL.value,
                )
            )
        ).scalar_one_or_none()
        if row is not None:
            if row.title != FAVORITES_TITLE:
                row.title = FAVORITES_TITLE
            return row

        legacy = (
            await self.db.execute(
                select(OjProblemList)
                .where(
                    OjProblemList.owner_id == account_id,
                    OjProblemList.kind == ProblemListKind.PERSONAL.value,
                    OjProblemList.title == FAVORITES_TITLE,
                )
                .order_by(OjProblemList.created_at.asc())
                .limit(1)
            )
        ).scalar_one_or_none()
        if legacy is not None:
            legacy.is_system = True
            legacy.visibility = ProblemListVisibility.PRIVATE.value
            legacy.summary = legacy.summary or "收藏的题目"
            await self.db.flush()
            return legacy

        entity = OjProblemList(
            id=generate_snowflake_id(),
            kind=ProblemListKind.PERSONAL.value,
            owner_id=account_id,
            title=FAVORITES_TITLE,
            summary="收藏的题目",
            visibility=ProblemListVisibility.PRIVATE.value,
            is_system=True,
            status=StatusEnum.ENABLED.value,
            sort=0,
            extra={},
        )
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def _item_count_map(self, list_ids: list[str]) -> dict[str, int]:
        if not list_ids:
            return {}
        rows = (
            await self.db.execute(
                select(OjProblemListItem.list_id, func.count())
                .where(OjProblemListItem.list_id.in_(list_ids))
                .group_by(OjProblemListItem.list_id)
            )
        ).all()
        return {lid: int(cnt) for lid, cnt in rows}

    async def _list_problem_ids(self, list_id: str) -> list[tuple[str, int]]:
        rows = (
            await self.db.execute(
                select(OjProblemListItem.problem_id, OjProblemListItem.sort)
                .where(OjProblemListItem.list_id == list_id)
                .order_by(OjProblemListItem.sort.asc(), OjProblemListItem.created_at.asc())
            )
        ).all()
        return [(pid, int(sort)) for pid, sort in rows]

    async def _replace_items(self, list_id: str, problem_ids: list[str]) -> None:
        await self.db.execute(delete(OjProblemListItem).where(OjProblemListItem.list_id == list_id))
        for idx, pid in enumerate(problem_ids):
            self.db.add(
                OjProblemListItem(
                    id=generate_snowflake_id(),
                    list_id=list_id,
                    problem_id=pid,
                    sort=idx,
                )
            )
        await self.db.flush()

    async def _progress(
        self,
        problems: list[OjProblem],
        solved: set[str],
        attempted: set[str],
    ) -> ProblemListProgress:
        progress = ProblemListProgress(total=len(problems))
        for p in problems:
            diff = p.difficulty or ProblemDifficulty.MEDIUM.value
            if diff == ProblemDifficulty.EASY.value:
                progress.easy_total += 1
                if p.id in solved:
                    progress.easy_solved += 1
            elif diff == ProblemDifficulty.HARD.value:
                progress.hard_total += 1
                if p.id in solved:
                    progress.hard_solved += 1
            else:
                progress.medium_total += 1
                if p.id in solved:
                    progress.medium_solved += 1
            if p.id in solved:
                progress.solved += 1
            elif p.id in attempted:
                progress.attempted += 1
        return progress

    async def _to_schema(
        self,
        entity: OjProblemList,
        *,
        with_problems: bool = False,
        viewer_id: str | None = None,
        problem_count: int | None = None,
    ) -> ProblemListSchema:
        pairs = await self._list_problem_ids(entity.id) if with_problems or problem_count is None else []
        count = problem_count if problem_count is not None else len(pairs)
        problems_out: list[ProblemListProblemBrief] = []
        progress = None
        if with_problems:
            problem_ids = [pid for pid, _ in pairs]
            sort_map = {pid: sort for pid, sort in pairs}
            entities: list[OjProblem] = []
            if problem_ids:
                entities = list(
                    (
                        await self.db.execute(select(OjProblem).where(OjProblem.id.in_(problem_ids)))
                    )
                    .scalars()
                    .all()
                )
                entities.sort(key=lambda x: sort_map.get(x.id, 0))
            solved: set[str] = set()
            attempted: set[str] = set()
            if viewer_id:
                solved = await solved_problem_ids(self.db, viewer_id, problem_ids)
                attempted = await attempted_problem_ids(self.db, viewer_id, problem_ids)
            progress = await self._progress(entities, solved, attempted)
            for p in entities:
                problems_out.append(
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
                )
        return ProblemListSchema(
            id=entity.id,
            kind=entity.kind,
            owner_id=entity.owner_id,
            code=entity.code,
            title=entity.title,
            summary=entity.summary,
            cover_url=entity.cover_url,
            visibility=entity.visibility,
            is_system=bool(entity.is_system),
            status=entity.status,
            sort=int(entity.sort or 0),
            problem_count=count,
            progress=progress,
            problems=problems_out,
            extra=entity.extra or {},
            created_at=entity.created_at,
            updated_at=entity.updated_at,
        )

    async def _get_required(self, list_id: str) -> OjProblemList:
        entity = await self.db.get(OjProblemList, list_id)
        if entity is None:
            raise NotFoundError("题单不存在")
        return entity

    def _assert_owner(self, entity: OjProblemList, account_id: str) -> None:
        if entity.kind != ProblemListKind.PERSONAL.value or entity.owner_id != account_id:
            raise AuthorizationError("无权操作该题单")

    # ---- admin official ----
    async def admin_create(self, payload: OfficialProblemListCreateRequest) -> str:
        async with transactional(self.db):
            exists = (
                await self.db.execute(select(OjProblemList.id).where(OjProblemList.code == payload.code))
            ).scalar_one_or_none()
            if exists:
                raise BusinessError("题单编码已存在")
            entity = OjProblemList(
                id=generate_snowflake_id(),
                kind=ProblemListKind.OFFICIAL.value,
                owner_id=None,
                code=payload.code,
                title=payload.title,
                summary=payload.summary,
                cover_url=payload.cover_url,
                visibility=payload.visibility.value,
                is_system=False,
                status=payload.status,
                sort=payload.sort,
                extra={},
            )
            self.db.add(entity)
            await self.db.flush()
            await self._replace_items(entity.id, payload.problem_ids)
            return entity.id

    async def admin_update(self, payload: ProblemListUpdateRequest) -> None:
        async with transactional(self.db):
            entity = await self._get_required(payload.id)
            if entity.kind != ProblemListKind.OFFICIAL.value:
                raise BusinessError("仅可编辑官方题单")
            entity.title = payload.title
            entity.summary = payload.summary
            entity.cover_url = payload.cover_url
            if payload.visibility is not None:
                entity.visibility = payload.visibility.value
            if payload.code is not None:
                entity.code = payload.code
            if payload.sort is not None:
                entity.sort = payload.sort
            if payload.status is not None:
                entity.status = payload.status
            if payload.problem_ids is not None:
                await self._replace_items(entity.id, payload.problem_ids)
            await self.db.flush()

    async def admin_delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            for lid in payload.ids:
                entity = await self._get_required(lid)
                if entity.kind != ProblemListKind.OFFICIAL.value:
                    raise BusinessError("仅可删除官方题单")
                await self.db.execute(delete(OjProblemListItem).where(OjProblemListItem.list_id == lid))
                await self.db.delete(entity)
            await self.db.flush()

    async def admin_page(self, query: ProblemListAdminPageQuery) -> PageData[ProblemListSchema]:
        filters = [OjProblemList.kind == ProblemListKind.OFFICIAL.value]
        if query.title:
            filters.append(OjProblemList.title.ilike(f"%{query.title}%"))
        if query.code:
            filters.append(OjProblemList.code.ilike(f"%{query.code}%"))
        if query.status:
            filters.append(OjProblemList.status == query.status)
        stmt = select(OjProblemList).where(*filters)
        count_stmt = select(func.count()).select_from(OjProblemList).where(*filters)
        total = int((await self.db.execute(count_stmt)).scalar_one() or 0)
        rows = list(
            (
                await self.db.execute(
                    stmt.order_by(OjProblemList.sort.asc(), OjProblemList.created_at.desc())
                    .offset(query.pagination.offset)
                    .limit(query.pagination.size)
                )
            )
            .scalars()
            .all()
        )
        counts = await self._item_count_map([r.id for r in rows])
        schemas = [
            await self._to_schema(r, problem_count=counts.get(r.id, 0)) for r in rows
        ]
        return build_page(query.pagination, total, schemas)

    async def admin_detail(self, query: IdQuery) -> ProblemListSchema:
        entity = await self._get_required(query.id)
        return await self._to_schema(entity, with_problems=True)

    # ---- portal ----
    async def mine(self, session: SessionPayload) -> list[ProblemListSchema]:
        account_id = session.account_id
        await self.ensure_favorites(account_id)
        rows = list(
            (
                await self.db.execute(
                    select(OjProblemList)
                    .where(
                        OjProblemList.kind == ProblemListKind.PERSONAL.value,
                        OjProblemList.owner_id == account_id,
                    )
                    .order_by(OjProblemList.is_system.desc(), OjProblemList.sort.asc(), OjProblemList.created_at.asc())
                )
            )
            .scalars()
            .all()
        )
        counts = await self._item_count_map([r.id for r in rows])
        return [await self._to_schema(r, problem_count=counts.get(r.id, 0)) for r in rows]

    async def official_page(self, query: ProblemListOfficialPageQuery) -> PageData[ProblemListSchema]:
        filters = [
            OjProblemList.kind == ProblemListKind.OFFICIAL.value,
            OjProblemList.status == StatusEnum.ENABLED.value,
            OjProblemList.visibility == ProblemListVisibility.PUBLIC.value,
        ]
        total = int(
            (await self.db.execute(select(func.count()).select_from(OjProblemList).where(*filters))).scalar_one()
            or 0
        )
        rows = list(
            (
                await self.db.execute(
                    select(OjProblemList)
                    .where(*filters)
                    .order_by(OjProblemList.sort.asc(), OjProblemList.created_at.desc())
                    .offset(query.pagination.offset)
                    .limit(query.pagination.size)
                )
            )
            .scalars()
            .all()
        )
        counts = await self._item_count_map([r.id for r in rows])
        return build_page(
            query.pagination,
            total,
            [await self._to_schema(r, problem_count=counts.get(r.id, 0)) for r in rows],
        )

    async def detail(
        self,
        query: IdQuery,
        *,
        session: SessionPayload | None = None,
    ) -> ProblemListSchema:
        entity = await self._get_required(query.id)
        viewer_id = session.account_id if session else None
        if entity.kind == ProblemListKind.PERSONAL.value:
            if not viewer_id or entity.owner_id != viewer_id:
                if entity.visibility != ProblemListVisibility.PUBLIC.value:
                    raise NotFoundError("题单不存在")
        elif entity.status != StatusEnum.ENABLED.value:
            raise NotFoundError("题单不存在")
        return await self._to_schema(entity, with_problems=True, viewer_id=viewer_id)

    async def portal_create(self, session: SessionPayload, payload: ProblemListCreateRequest) -> str:
        account_id = session.account_id
        async with transactional(self.db):
            if payload.title.strip() == FAVORITES_TITLE:
                raise BusinessError("「我的收藏」为系统题单，请换一个名称")
            await self.ensure_favorites(account_id)
            entity = OjProblemList(
                id=generate_snowflake_id(),
                kind=ProblemListKind.PERSONAL.value,
                owner_id=account_id,
                code=None,
                title=payload.title,
                summary=payload.summary,
                cover_url=payload.cover_url,
                visibility=payload.visibility.value,
                is_system=False,
                status=StatusEnum.ENABLED.value,
                sort=payload.sort,
                extra={},
            )
            self.db.add(entity)
            await self.db.flush()
            await self._replace_items(entity.id, payload.problem_ids)
            return entity.id

    async def portal_update(self, session: SessionPayload, payload: ProblemListUpdateRequest) -> None:
        account_id = session.account_id
        async with transactional(self.db):
            entity = await self._get_required(payload.id)
            self._assert_owner(entity, account_id)
            if entity.is_system:
                # 收藏夹：可改简介/封面/题目，标题与可见性锁定
                if payload.summary is not None:
                    entity.summary = payload.summary
                if payload.cover_url is not None:
                    entity.cover_url = payload.cover_url
                if payload.problem_ids is not None:
                    await self._replace_items(entity.id, payload.problem_ids)
            else:
                entity.title = payload.title
                entity.summary = payload.summary
                entity.cover_url = payload.cover_url
                if payload.visibility is not None:
                    entity.visibility = payload.visibility.value
                if payload.sort is not None:
                    entity.sort = payload.sort
                if payload.problem_ids is not None:
                    await self._replace_items(entity.id, payload.problem_ids)
            await self.db.flush()

    async def portal_delete(self, session: SessionPayload, payload: IdsRequest) -> None:
        account_id = session.account_id
        async with transactional(self.db):
            for lid in payload.ids:
                entity = await self._get_required(lid)
                self._assert_owner(entity, account_id)
                if entity.is_system:
                    raise BusinessError("「我的收藏」不可删除")
                await self.db.execute(delete(OjProblemListItem).where(OjProblemListItem.list_id == lid))
                await self.db.delete(entity)
            await self.db.flush()

    async def add_item(self, session: SessionPayload, payload: ProblemListItemMutation) -> None:
        account_id = session.account_id
        async with transactional(self.db):
            entity = await self._get_required(payload.list_id)
            self._assert_owner(entity, account_id)
            exists = (
                await self.db.execute(
                    select(OjProblemListItem.id).where(
                        OjProblemListItem.list_id == payload.list_id,
                        OjProblemListItem.problem_id == payload.problem_id,
                    )
                )
            ).scalar_one_or_none()
            if exists:
                return
            max_sort = (
                await self.db.execute(
                    select(func.max(OjProblemListItem.sort)).where(OjProblemListItem.list_id == payload.list_id)
                )
            ).scalar_one()
            self.db.add(
                OjProblemListItem(
                    id=generate_snowflake_id(),
                    list_id=payload.list_id,
                    problem_id=payload.problem_id,
                    sort=int(max_sort or 0) + 1,
                )
            )
            await self.db.flush()

    async def remove_item(self, session: SessionPayload, payload: ProblemListItemMutation) -> None:
        account_id = session.account_id
        async with transactional(self.db):
            entity = await self._get_required(payload.list_id)
            self._assert_owner(entity, account_id)
            await self.db.execute(
                delete(OjProblemListItem).where(
                    OjProblemListItem.list_id == payload.list_id,
                    OjProblemListItem.problem_id == payload.problem_id,
                )
            )
            await self.db.flush()

    async def is_favorited(self, session: SessionPayload, query: ProblemIdQuery) -> bool:
        account_id = session.account_id
        fav = await self.ensure_favorites(account_id)
        exists = (
            await self.db.execute(
                select(OjProblemListItem.id).where(
                    OjProblemListItem.list_id == fav.id,
                    OjProblemListItem.problem_id == query.problem_id,
                )
            )
        ).scalar_one_or_none()
        return exists is not None

    async def add_favorite(self, session: SessionPayload, payload: ProblemIdQuery) -> None:
        fav = await self.ensure_favorites(session.account_id)
        await self.add_item(session, ProblemListItemMutation(list_id=fav.id, problem_id=payload.problem_id))

    async def remove_favorite(self, session: SessionPayload, payload: ProblemIdQuery) -> None:
        fav = await self.ensure_favorites(session.account_id)
        await self.remove_item(session, ProblemListItemMutation(list_id=fav.id, problem_id=payload.problem_id))

    async def reorder(self, session: SessionPayload, payload: ProblemListReorderRequest) -> None:
        account_id = session.account_id
        async with transactional(self.db):
            entity = await self._get_required(payload.list_id)
            self._assert_owner(entity, account_id)
            for item in payload.items:
                pid = str(item.get("problem_id") or "")
                sort = int(item.get("sort") or 0)
                row = (
                    await self.db.execute(
                        select(OjProblemListItem).where(
                            OjProblemListItem.list_id == payload.list_id,
                            OjProblemListItem.problem_id == pid,
                        )
                    )
                ).scalar_one_or_none()
                if row:
                    row.sort = sort
            await self.db.flush()
