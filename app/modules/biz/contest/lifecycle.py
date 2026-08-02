"""Contest lifecycle helpers (computed, not persisted)."""

from __future__ import annotations

from datetime import datetime, timedelta, timezone

from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.enums import ContestLifecycleStatus


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


def ensure_aware(dt: datetime) -> datetime:
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt


def lifecycle_status(contest: OjContest, *, now: datetime | None = None) -> ContestLifecycleStatus:
    now = ensure_aware(now or utcnow())
    locked_after = contest.locked_after
    if locked_after is not None and now >= ensure_aware(locked_after):
        return ContestLifecycleStatus.LOCKED
    start = ensure_aware(contest.start_time)
    end = ensure_aware(contest.end_time)
    if now < start:
        return ContestLifecycleStatus.SCHEDULED
    if now < end:
        return ContestLifecycleStatus.RUNNING
    return ContestLifecycleStatus.ENDED


def contest_is_frozen(contest: OjContest, *, now: datetime | None = None) -> bool:
    freeze = contest.freeze_seconds
    if not freeze or freeze <= 0:
        return False
    now = ensure_aware(now or utcnow())
    if lifecycle_status(contest, now=now) != ContestLifecycleStatus.RUNNING:
        return False
    end = ensure_aware(contest.end_time)
    return now >= (end - timedelta(seconds=int(freeze)))


def personal_end_time(contest: OjContest, real_start: datetime) -> datetime:
    end = ensure_aware(contest.end_time)
    if not contest.time_limit_seconds:
        return end
    personal = ensure_aware(real_start) + timedelta(seconds=int(contest.time_limit_seconds))
    return min(personal, end)


def participation_active(contest: OjContest, real_start: datetime, *, now: datetime | None = None) -> bool:
    now = ensure_aware(now or utcnow())
    return ensure_aware(real_start) <= now < personal_end_time(contest, real_start)
