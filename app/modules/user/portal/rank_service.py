"""Portal ranking: practice solved board + contest rating board."""

from __future__ import annotations

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.modules.biz.contest.rating.model import OjContestRating
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult
from app.modules.biz.submission.submission.model import OjSubmission
from app.modules.user.portal.model import PortalUserProfile
from app.modules.user.portal.repository import PortalUserProfileRepository
from app.modules.user.portal.schema import (
    PortalRankMeResponse,
    PortalRankSummaryResponse,
    PortalRatingRankItem,
    PortalSolvedRankItem,
)
from app.platform.storage.url import resolve_file_url

BOARD_SOLVED = "solved"
BOARD_RATING = "rating"
VALID_BOARDS = {BOARD_SOLVED, BOARD_RATING}


def normalize_board(board: str) -> str:
    value = (board or "").strip().lower()
    if value not in VALID_BOARDS:
        raise BusinessError("board 须为 solved 或 rating")
    return value


class PortalRankService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = PortalUserProfileRepository(db)

    def _display_name(self, profile: PortalUserProfile | None, account_id: str) -> str | None:
        if profile is None:
            return None
        return profile.nickname or profile.name

    def _avatar(self, profile: PortalUserProfile | None) -> str | None:
        if profile is None:
            return None
        return resolve_file_url(profile.avatar)

    def _solved_agg_subquery(self):
        return (
            select(
                OjSubmission.user_id.label("account_id"),
                func.count(func.distinct(OjSubmission.problem_id)).label("solved"),
            )
            .where(
                OjSubmission.result == SubmissionResult.AC.value,
                OjSubmission.kind != SubmissionKind.TRIAL.value,
            )
            .group_by(OjSubmission.user_id)
            .having(func.count(func.distinct(OjSubmission.problem_id)) > 0)
            .subquery()
        )

    async def page_solved_rank(self, pagination: PageQuery) -> PageData[PortalSolvedRankItem]:
        agg = self._solved_agg_subquery()
        total = int((await self.db.execute(select(func.count()).select_from(agg))).scalar_one() or 0)
        stmt = (
            select(agg.c.account_id, agg.c.solved, PortalUserProfile)
            .outerjoin(PortalUserProfile, PortalUserProfile.account_id == agg.c.account_id)
            .order_by(agg.c.solved.desc(), agg.c.account_id.asc())
            .offset(pagination.offset)
            .limit(pagination.size)
        )
        rows = (await self.db.execute(stmt)).all()
        items: list[PortalSolvedRankItem] = []
        for index, (account_id, solved, profile) in enumerate(rows):
            items.append(
                PortalSolvedRankItem(
                    rank=pagination.offset + index + 1,
                    account_id=account_id,
                    nickname=self._display_name(profile, account_id),
                    avatar=self._avatar(profile),
                    solved=int(solved or 0),
                )
            )
        return build_page(pagination, total, items)

    async def page_rating_rank(self, pagination: PageQuery) -> PageData[PortalRatingRankItem]:
        items, total = await self.repo.page_by_rating(offset=pagination.offset, size=pagination.size)
        extras = await self._rating_extras([p.account_id for p in items])
        schemas: list[PortalRatingRankItem] = []
        for index, profile in enumerate(items):
            contests, delta = extras.get(profile.account_id, (0, 0))
            schemas.append(
                PortalRatingRankItem(
                    rank=pagination.offset + index + 1,
                    account_id=profile.account_id,
                    nickname=self._display_name(profile, profile.account_id),
                    avatar=self._avatar(profile),
                    rating=int(profile.rating or 0),
                    contests=contests,
                    delta=delta,
                )
            )
        return build_page(pagination, total, schemas)

    async def get_me(self, account_id: str, board: str) -> PortalRankMeResponse:
        board = normalize_board(board)
        profile = await self.repo.get_by_account_id(account_id)
        nickname = self._display_name(profile, account_id)
        avatar = self._avatar(profile)

        if board == BOARD_SOLVED:
            solved = await self._my_solved(account_id)
            rank = await self._solved_rank_of(account_id, solved) if solved > 0 else None
            return PortalRankMeResponse(
                board=BOARD_SOLVED,
                rank=rank,
                score=solved,
                nickname=nickname,
                avatar=avatar,
            )

        if board == BOARD_RATING:
            rating = int(profile.rating) if profile and profile.rating is not None else 0
            extras = await self._rating_extras([account_id])
            contests, delta = extras.get(account_id, (0, 0))
            rank = None
            if profile and profile.rating is not None:
                rank = await self._rating_rank_of(account_id, int(profile.rating))
            return PortalRankMeResponse(
                board=BOARD_RATING,
                rank=rank,
                score=rating,
                nickname=nickname,
                avatar=avatar,
                contests=contests,
                delta=delta,
            )

        raise BusinessError("board 须为 solved 或 rating")

    async def summary(self, board: str) -> PortalRankSummaryResponse:
        board = normalize_board(board)
        if board == BOARD_SOLVED:
            agg = self._solved_agg_subquery()
            row = (
                await self.db.execute(
                    select(
                        func.count().label("total_users"),
                        func.coalesce(func.max(agg.c.solved), 0).label("top_score"),
                        func.coalesce(func.avg(agg.c.solved), 0).label("avg_score"),
                    ).select_from(agg)
                )
            ).one()
            return PortalRankSummaryResponse(
                board=BOARD_SOLVED,
                total_users=int(row.total_users or 0),
                top_score=int(row.top_score or 0),
                avg_score=int(round(float(row.avg_score or 0))),
            )

        if board == BOARD_RATING:
            filters = [PortalUserProfile.rating.is_not(None)]
            row = (
                await self.db.execute(
                    select(
                        func.count(PortalUserProfile.account_id).label("total_users"),
                        func.coalesce(func.max(PortalUserProfile.rating), 0).label("top_score"),
                        func.coalesce(func.avg(PortalUserProfile.rating), 0).label("avg_score"),
                    ).where(*filters)
                )
            ).one()
            max_delta = await self._max_latest_delta()
            return PortalRankSummaryResponse(
                board=BOARD_RATING,
                total_users=int(row.total_users or 0),
                top_score=int(row.top_score or 0),
                avg_score=int(round(float(row.avg_score or 0))),
                max_delta=max_delta,
            )

        raise BusinessError("board 须为 solved 或 rating")

    async def _my_solved(self, account_id: str) -> int:
        stmt = select(func.count(func.distinct(OjSubmission.problem_id))).where(
            OjSubmission.user_id == account_id,
            OjSubmission.result == SubmissionResult.AC.value,
            OjSubmission.kind != SubmissionKind.TRIAL.value,
        )
        return int((await self.db.execute(stmt)).scalar_one() or 0)

    async def _solved_rank_of(self, account_id: str, solved: int) -> int:
        agg = self._solved_agg_subquery()
        ahead = (
            await self.db.execute(
                select(func.count())
                .select_from(agg)
                .where(
                    (agg.c.solved > solved)
                    | ((agg.c.solved == solved) & (agg.c.account_id < account_id))
                )
            )
        ).scalar_one()
        return int(ahead or 0) + 1

    async def _rating_rank_of(self, account_id: str, rating: int) -> int:
        ahead = (
            await self.db.execute(
                select(func.count(PortalUserProfile.account_id)).where(
                    PortalUserProfile.rating.is_not(None),
                    (PortalUserProfile.rating > rating)
                    | (
                        (PortalUserProfile.rating == rating)
                        & (PortalUserProfile.account_id < account_id)
                    ),
                )
            )
        ).scalar_one()
        return int(ahead or 0) + 1

    async def _rating_extras(self, account_ids: list[str]) -> dict[str, tuple[int, int]]:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return {}

        count_rows = (
            await self.db.execute(
                select(OjContestRating.account_id, func.count(OjContestRating.id))
                .where(OjContestRating.account_id.in_(unique_ids))
                .group_by(OjContestRating.account_id)
            )
        ).all()
        counts = {aid: int(c or 0) for aid, c in count_rows}

        latest_subq = (
            select(
                OjContestRating.account_id.label("account_id"),
                OjContestRating.delta.label("delta"),
                func.row_number()
                .over(
                    partition_by=OjContestRating.account_id,
                    order_by=OjContestRating.rated_at.desc(),
                )
                .label("rn"),
            )
            .where(OjContestRating.account_id.in_(unique_ids))
            .subquery()
        )
        delta_rows = (
            await self.db.execute(
                select(latest_subq.c.account_id, latest_subq.c.delta).where(latest_subq.c.rn == 1)
            )
        ).all()
        deltas = {aid: int(d or 0) for aid, d in delta_rows}

        return {aid: (counts.get(aid, 0), deltas.get(aid, 0)) for aid in unique_ids}

    async def _max_latest_delta(self) -> int:
        latest_subq = (
            select(
                OjContestRating.account_id.label("account_id"),
                OjContestRating.delta.label("delta"),
                func.row_number()
                .over(
                    partition_by=OjContestRating.account_id,
                    order_by=OjContestRating.rated_at.desc(),
                )
                .label("rn"),
            ).subquery()
        )
        value = (
            await self.db.execute(
                select(func.coalesce(func.max(latest_subq.c.delta), 0)).where(latest_subq.c.rn == 1)
            )
        ).scalar_one()
        return int(value or 0)
