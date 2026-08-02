"""Contest rating settlement (Elo-MMR), DMOJ-compatible cascade strategy."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Any

from sqlalchemy import delete, func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError, NotFoundError
from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.enums import ContestLifecycleStatus, ContestParticipationVirtual
from app.modules.biz.contest.lifecycle import ensure_aware, lifecycle_status, utcnow
from app.modules.biz.contest.participation.model import OjContestParticipation
from app.modules.biz.contest.rating.algo import (
    MEAN_INIT,
    RATING_INIT,
    approximate_mean_from_rating,
    recalculate_ratings,
    tie_ranker,
)
from app.modules.biz.contest.rating.model import OjContestRating
from app.modules.biz.submission.submission.model import OjContestSubmission
from app.modules.user.portal.model import PortalUserProfile
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


@dataclass
class _PoolRow:
    participation_id: str
    account_id: str
    score: float
    cumtime: int
    tiebreaker: float
    last_rating: int
    last_mean: float
    times_ranked: int


class OjContestRatingService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def rate_contest(self, contest_id: str) -> dict[str, Any]:
        async with transactional(self.db):
            contest = await self.db.get(OjContest, contest_id)
            if contest is None:
                raise NotFoundError("竞赛不存在")
            if not contest.is_rated:
                raise BusinessError("该竞赛未开启 Rating")
            status = lifecycle_status(contest)
            if status not in {ContestLifecycleStatus.ENDED, ContestLifecycleStatus.LOCKED}:
                raise BusinessError("仅结束后的竞赛可结算 Rating")

            now = utcnow()
            end_time = ensure_aware(contest.end_time)

            # Contests in [end_time, now] that already have ratings, plus this contest.
            existing_rated_ids = set(
                (
                    await self.db.execute(
                        select(OjContestRating.contest_id)
                        .join(OjContest, OjContest.id == OjContestRating.contest_id)
                        .where(OjContest.end_time >= end_time, OjContest.end_time <= now)
                        .distinct()
                    )
                ).scalars().all()
            )
            existing_rated_ids.add(contest.id)

            cleared_accounts: list[str] = []
            cleared = 0
            if existing_rated_ids:
                cleared_accounts = list(
                    (
                        await self.db.execute(
                            select(OjContestRating.account_id)
                            .where(OjContestRating.contest_id.in_(list(existing_rated_ids)))
                            .distinct()
                        )
                    ).scalars().all()
                )
                result = await self.db.execute(
                    delete(OjContestRating).where(OjContestRating.contest_id.in_(list(existing_rated_ids)))
                )
                cleared = int(result.rowcount or 0)

            # Re-rate all is_rated contests in the window (DMOJ), ordered by end_time.
            to_rate = list(
                (
                    await self.db.execute(
                        select(OjContest)
                        .where(
                            OjContest.is_rated.is_(True),
                            OjContest.end_time >= end_time,
                            OjContest.end_time <= now,
                        )
                        .order_by(OjContest.end_time.asc(), OjContest.id.asc())
                    )
                ).scalars().all()
            )

            total_rated = 0
            for c in to_rate:
                if lifecycle_status(c, now=now) not in {
                    ContestLifecycleStatus.ENDED,
                    ContestLifecycleStatus.LOCKED,
                }:
                    continue
                total_rated += await self._rate_one(c)

            if cleared_accounts:
                await self._refresh_profile_ratings(cleared_accounts)

            return {
                "contest_id": contest_id,
                "rated": total_rated,
                "contests_rerated": len(to_rate),
                "ratings_cleared": cleared,
            }

    async def undo_rate(self, contest_id: str) -> dict[str, Any]:
        async with transactional(self.db):
            contest = await self.db.get(OjContest, contest_id)
            if contest is None:
                raise NotFoundError("竞赛不存在")

            account_ids = list(
                (
                    await self.db.execute(
                        select(OjContestRating.account_id).where(OjContestRating.contest_id == contest_id)
                    )
                ).scalars().all()
            )
            result = await self.db.execute(delete(OjContestRating).where(OjContestRating.contest_id == contest_id))
            deleted = int(result.rowcount or 0)
            if account_ids:
                await self._refresh_profile_ratings(account_ids)
            return {"contest_id": contest_id, "deleted": deleted}

    async def list_ratings(self, contest_id: str) -> list[OjContestRating]:
        contest = await self.db.get(OjContest, contest_id)
        if contest is None:
            raise NotFoundError("竞赛不存在")
        stmt = (
            select(OjContestRating)
            .where(OjContestRating.contest_id == contest_id)
            .order_by(OjContestRating.rank.asc(), OjContestRating.account_id.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def _rate_one(self, contest: OjContest) -> int:
        pool = await self._build_pool(contest)
        if not pool:
            return 0

        ranking = list(tie_ranker(pool, key=lambda r: (r.score, r.cumtime, r.tiebreaker)))
        old_mean = [r.last_mean for r in pool]
        times_ranked = [r.times_ranked for r in pool]
        historical_p = await self._historical_performances(
            [r.account_id for r in pool], before_end_time=ensure_aware(contest.end_time)
        )
        perf_ceiling = None
        if isinstance(contest.extra, dict):
            raw = contest.extra.get("performance_ceiling")
            if raw is not None:
                perf_ceiling = float(raw)

        ratings, _means, performances = recalculate_ratings(
            ranking, old_mean, times_ranked, historical_p, perf_ceiling
        )

        now = utcnow()
        affected_accounts: list[str] = []
        for row, rank_f, new_rating, perf in zip(pool, ranking, ratings, performances):
            self.db.add(
                OjContestRating(
                    id=generate_snowflake_id(),
                    contest_id=contest.id,
                    account_id=row.account_id,
                    participation_id=row.participation_id,
                    rank=max(1, int(rank_f)),
                    rating=int(new_rating),
                    delta=int(new_rating) - int(row.last_rating),
                    performance=max(1, int(round(perf))),
                    rated_at=now,
                )
            )
            affected_accounts.append(row.account_id)

        await self.db.flush()
        await self._refresh_profile_ratings(affected_accounts)
        return len(pool)

    async def _build_pool(self, contest: OjContest) -> list[_PoolRow]:
        sub_count = (
            select(func.count(OjContestSubmission.id))
            .where(OjContestSubmission.participation_id == OjContestParticipation.id)
            .correlate(OjContestParticipation)
            .scalar_subquery()
        )
        stmt = (
            select(OjContestParticipation, sub_count.label("submissions"))
            .where(
                OjContestParticipation.contest_id == contest.id,
                OjContestParticipation.virtual == int(ContestParticipationVirtual.LIVE),
                OjContestParticipation.is_disqualified.is_(False),
                OjContestParticipation.rate_exclude.is_(False),
            )
            .order_by(
                OjContestParticipation.score.desc(),
                OjContestParticipation.cumtime.asc(),
                OjContestParticipation.tiebreaker.asc(),
                OjContestParticipation.id.asc(),
            )
        )
        rows = (await self.db.execute(stmt)).all()
        if not contest.rate_all:
            rows = [r for r in rows if int(r.submissions or 0) > 0]

        account_ids = [p.account_id for p, _ in rows]
        prior = await self._prior_rating_stats(account_ids, before_end_time=ensure_aware(contest.end_time))

        pool: list[_PoolRow] = []
        for participation, _subs in rows:
            stats = prior.get(participation.account_id)
            if stats is None:
                last_rating = RATING_INIT
                last_mean = MEAN_INIT
                times_ranked = 0
            else:
                last_rating, last_mean, times_ranked = stats

            if contest.rating_floor is not None and last_rating < contest.rating_floor:
                continue
            if contest.rating_ceiling is not None and last_rating > contest.rating_ceiling:
                continue

            pool.append(
                _PoolRow(
                    participation_id=participation.id,
                    account_id=participation.account_id,
                    score=float(participation.score or 0),
                    cumtime=int(participation.cumtime or 0),
                    tiebreaker=float(participation.tiebreaker or 0),
                    last_rating=int(last_rating),
                    last_mean=float(last_mean),
                    times_ranked=int(times_ranked),
                )
            )
        return pool

    async def _prior_rating_stats(
        self,
        account_ids: list[str],
        *,
        before_end_time: datetime,
    ) -> dict[str, tuple[int, float, int]]:
        if not account_ids:
            return {}
        stmt = (
            select(
                OjContestRating.account_id,
                OjContestRating.rating,
                OjContest.end_time,
            )
            .join(OjContest, OjContest.id == OjContestRating.contest_id)
            .where(
                OjContestRating.account_id.in_(account_ids),
                OjContest.end_time < before_end_time,
            )
            .order_by(OjContestRating.account_id.asc(), OjContest.end_time.desc(), OjContestRating.id.desc())
        )
        rows = (await self.db.execute(stmt)).all()
        counts: dict[str, int] = {}
        latest: dict[str, int] = {}
        for account_id, rating, _end in rows:
            counts[account_id] = counts.get(account_id, 0) + 1
            if account_id not in latest:
                latest[account_id] = int(rating)

        out: dict[str, tuple[int, float, int]] = {}
        for account_id, rating in latest.items():
            times = counts[account_id]
            mean = approximate_mean_from_rating(rating, max(times - 1, 0))
            out[account_id] = (rating, mean, times)
        return out

    async def _historical_performances(
        self,
        account_ids: list[str],
        *,
        before_end_time: datetime,
    ) -> list[list[float]]:
        if not account_ids:
            return []
        stmt = (
            select(OjContestRating.account_id, OjContestRating.performance)
            .join(OjContest, OjContest.id == OjContestRating.contest_id)
            .where(
                OjContestRating.account_id.in_(account_ids),
                OjContest.end_time < before_end_time,
            )
            .order_by(OjContest.end_time.desc(), OjContestRating.id.desc())
        )
        rows = (await self.db.execute(stmt)).all()
        buckets: dict[str, list[float]] = {aid: [] for aid in account_ids}
        for account_id, performance in rows:
            buckets[account_id].append(float(performance))
        return [buckets[aid] for aid in account_ids]

    async def _refresh_profile_ratings(self, account_ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(account_ids))
        if not unique_ids:
            return

        latest_subq = (
            select(
                OjContestRating.account_id.label("account_id"),
                OjContestRating.rating.label("rating"),
                func.row_number()
                .over(
                    partition_by=OjContestRating.account_id,
                    order_by=(OjContest.end_time.desc(), OjContestRating.rated_at.desc()),
                )
                .label("rn"),
            )
            .join(OjContest, OjContest.id == OjContestRating.contest_id)
            .where(OjContestRating.account_id.in_(unique_ids))
            .subquery()
        )
        latest_rows = (
            await self.db.execute(
                select(latest_subq.c.account_id, latest_subq.c.rating).where(latest_subq.c.rn == 1)
            )
        ).all()
        latest_map = {aid: int(rating) for aid, rating in latest_rows}

        existing = list(
            (
                await self.db.execute(select(PortalUserProfile).where(PortalUserProfile.account_id.in_(unique_ids)))
            ).scalars().all()
        )
        existing_ids = {p.account_id for p in existing}
        for profile in existing:
            profile.rating = latest_map.get(profile.account_id)
        for account_id in unique_ids:
            if account_id not in existing_ids and account_id in latest_map:
                self.db.add(PortalUserProfile(account_id=account_id, rating=latest_map[account_id]))
        missing = [aid for aid in unique_ids if aid not in latest_map and aid in existing_ids]
        if missing:
            await self.db.execute(
                update(PortalUserProfile).where(PortalUserProfile.account_id.in_(missing)).values(rating=None)
            )
        await self.db.flush()


async def rate_contest(db: AsyncSession, contest_id: str) -> dict[str, Any]:
    return await OjContestRatingService(db).rate_contest(contest_id)


async def undo_rate(db: AsyncSession, contest_id: str) -> dict[str, Any]:
    return await OjContestRatingService(db).undo_rate(contest_id)


async def list_ratings(db: AsyncSession, contest_id: str) -> list[OjContestRating]:
    return await OjContestRatingService(db).list_ratings(contest_id)
