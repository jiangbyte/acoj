"""Create contest submissions and enqueue judging."""

from __future__ import annotations

from datetime import datetime, timezone
from typing import Any

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError, NotFoundError
from app.modules.biz.contest.banned_user.model import OjContestBannedUser
from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.enums import ContestLifecycleStatus, ContestParticipationVirtual
from app.modules.biz.contest.lifecycle import (
    contest_is_frozen,
    lifecycle_status,
    participation_active,
    utcnow,
)
from app.modules.biz.contest.participation.model import OjContestParticipation
from app.modules.biz.contest.private_contestant.model import OjContestPrivateContestant
from app.modules.biz.contest.problem.model import OjContestProblem
from app.modules.biz.problem.judge_bridge import build_admin_trial_payload
from app.modules.biz.problem.test_case.model import OjProblemTestCase
from app.modules.biz.submission.enums import SubmissionKind, SubmissionStatus
from app.modules.biz.submission.submission.model import OjContestSubmission, OjSubmission, OjSubmissionSource
from app.modules.biz.submission.submission.service import OjSubmissionService
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id


class ContestSubmitService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def ensure_can_submit(
        self,
        *,
        contest: OjContest,
        participation: OjContestParticipation,
        cproblem: OjContestProblem,
    ) -> None:
        status = lifecycle_status(contest)
        if status == ContestLifecycleStatus.LOCKED:
            raise BusinessError("竞赛已锁定，禁止提交")
        if status == ContestLifecycleStatus.SCHEDULED:
            raise BusinessError("竞赛尚未开始")
        if participation.is_disqualified:
            raise BusinessError("已被取消资格，无法提交")
        banned = (
            await self.db.execute(
                select(OjContestBannedUser.id).where(
                    OjContestBannedUser.contest_id == contest.id,
                    OjContestBannedUser.account_id == participation.account_id,
                )
            )
        ).scalar_one_or_none()
        if banned:
            raise BusinessError("已被禁止参赛，无法提交")

        if participation.virtual == ContestParticipationVirtual.LIVE:
            if status == ContestLifecycleStatus.ENDED:
                raise BusinessError("正式参赛已结束，请使用虚拟参赛")
            if not participation_active(contest, participation.real_start):
                raise BusinessError("个人比赛窗口已结束")
        elif participation.virtual == ContestParticipationVirtual.SPECTATE:
            raise BusinessError("观赛模式不能提交")
        # virtual > 0: allow while personal window active
        elif participation.virtual > 0:
            if not participation_active(contest, participation.real_start):
                raise BusinessError("虚拟参赛窗口已结束")

        if cproblem.max_submissions is not None:
            cnt = (
                await self.db.execute(
                    select(func.count())
                    .select_from(OjContestSubmission)
                    .where(
                        OjContestSubmission.participation_id == participation.id,
                        OjContestSubmission.contest_problem_id == cproblem.id,
                    )
                )
            ).scalar_one()
            if int(cnt) >= int(cproblem.max_submissions):
                raise BusinessError(f"已达到该题最大提交次数 {cproblem.max_submissions}")

    async def submit(
        self,
        *,
        contest_id: str,
        account_id: str,
        problem_id: str,
        language_key: str,
        source: str,
        participation_id: str | None = None,
        wait: bool = False,
        wait_timeout_sec: int = 60,
    ) -> dict[str, Any]:
        contest = await self.db.get(OjContest, contest_id)
        if contest is None:
            raise NotFoundError("竞赛不存在")

        if participation_id:
            participation = await self.db.get(OjContestParticipation, participation_id)
            if participation is None or participation.contest_id != contest_id:
                raise BusinessError("参赛记录无效")
            if participation.account_id != account_id:
                # admin proxy submit may pass target account via participation
                pass
        else:
            participation = (
                await self.db.execute(
                    select(OjContestParticipation).where(
                        OjContestParticipation.contest_id == contest_id,
                        OjContestParticipation.account_id == account_id,
                        OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
                    )
                )
            ).scalar_one_or_none()
            if participation is None:
                raise BusinessError("请先报名参赛")

        cproblem = (
            await self.db.execute(
                select(OjContestProblem).where(
                    OjContestProblem.contest_id == contest_id,
                    OjContestProblem.problem_id == problem_id,
                )
            )
        ).scalar_one_or_none()
        if cproblem is None:
            raise BusinessError("题目不在本场竞赛中")

        await self.ensure_can_submit(contest=contest, participation=participation, cproblem=cproblem)

        is_pretest = bool(contest.run_pretests_only and cproblem.is_pretested)
        case_ids: list[str] | None = None
        if is_pretest:
            case_ids = list(
                (
                    await self.db.execute(
                        select(OjProblemTestCase.id).where(
                            OjProblemTestCase.problem_id == problem_id,
                            OjProblemTestCase.is_pretest.is_(True),
                        )
                    )
                ).scalars().all()
            )
            if not case_ids:
                # fall back to all cases if no pretest marked
                case_ids = None
                is_pretest = False

        submission_id = generate_snowflake_id()
        judge_payload = await build_admin_trial_payload(
            self.db,
            problem_id=problem_id,
            language_key=language_key,
            source=source,
            case_ids=case_ids,
            submission_id=submission_id,
        )
        # Use contest problem points for scoring scale in worker display
        judge_payload["problem"]["points"] = float(cproblem.points or judge_payload["problem"]["points"])
        judge_payload["problem"]["partial"] = bool(cproblem.partial)

        locked_at = None
        if contest.locked_after is not None:
            locked_at = contest.locked_after

        async with transactional(self.db):
            self.db.add(
                OjSubmission(
                    id=submission_id,
                    user_id=participation.account_id,
                    problem_id=problem_id,
                    language_key=language_key,
                    kind=SubmissionKind.CONTEST.value,
                    status=SubmissionStatus.JUDGING.value,
                    score=0,
                    time_ms=0,
                    memory_kb=0,
                    contest_id=contest_id,
                    case_points=0,
                    case_total=0,
                    locked_at=locked_at if lifecycle_status(contest) == ContestLifecycleStatus.LOCKED else None,
                )
            )
            self.db.add(
                OjSubmissionSource(
                    id=generate_snowflake_id(),
                    submission_id=submission_id,
                    source=source,
                )
            )
            self.db.add(
                OjContestSubmission(
                    id=generate_snowflake_id(),
                    submission_id=submission_id,
                    contest_problem_id=cproblem.id,
                    participation_id=participation.id,
                    points=0,
                    is_pretest=is_pretest,
                )
            )
            await self.db.flush()

        sub_svc = OjSubmissionService(self.db)
        sub_svc.enqueue_judge(submission_id, judge_payload)

        if wait:
            snap = await sub_svc._wait_until_terminal(submission_id, wait_timeout_sec)
            return snap
        return await sub_svc.snapshot_for_events(submission_id)


async def join_contest(
    db: AsyncSession,
    *,
    contest_id: str,
    account_id: str,
    access_code: str | None = None,
    spectate: bool = False,
) -> OjContestParticipation:
    contest = await db.get(OjContest, contest_id)
    if contest is None:
        raise NotFoundError("竞赛不存在")
    status = lifecycle_status(contest)
    if status == ContestLifecycleStatus.LOCKED:
        raise BusinessError("竞赛已锁定")

    banned = (
        await db.execute(
            select(OjContestBannedUser.id).where(
                OjContestBannedUser.contest_id == contest_id,
                OjContestBannedUser.account_id == account_id,
            )
        )
    ).scalar_one_or_none()
    if banned:
        raise BusinessError("你已被禁止参加本场竞赛")

    if contest.is_private:
        allowed = (
            await db.execute(
                select(OjContestPrivateContestant.id).where(
                    OjContestPrivateContestant.contest_id == contest_id,
                    OjContestPrivateContestant.account_id == account_id,
                )
            )
        ).scalar_one_or_none()
        if not allowed:
            raise BusinessError("私有竞赛：你不在选手名单中")

    if contest.access_code and (access_code or "").strip() != contest.access_code:
        raise BusinessError("准入码错误")

    if status == ContestLifecycleStatus.SCHEDULED and not spectate:
        raise BusinessError("竞赛尚未开始")

    virtual = ContestParticipationVirtual.SPECTATE if spectate else ContestParticipationVirtual.LIVE
    if status == ContestLifecycleStatus.ENDED and not spectate:
        # virtual attempt: next virtual id
        max_v = (
            await db.execute(
                select(func.max(OjContestParticipation.virtual)).where(
                    OjContestParticipation.contest_id == contest_id,
                    OjContestParticipation.account_id == account_id,
                )
            )
        ).scalar_one()
        virtual = 1 if max_v is None or int(max_v) < 1 else int(max_v) + 1

    if virtual == ContestParticipationVirtual.LIVE:
        existing = (
            await db.execute(
                select(OjContestParticipation).where(
                    OjContestParticipation.contest_id == contest_id,
                    OjContestParticipation.account_id == account_id,
                    OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
                )
            )
        ).scalar_one_or_none()
        if existing:
            return existing

    part = OjContestParticipation(
        id=generate_snowflake_id(),
        contest_id=contest_id,
        account_id=account_id,
        real_start=utcnow(),
        score=0,
        cumtime=0,
        tiebreaker=0,
        is_disqualified=False,
        virtual=int(virtual),
        rate_exclude=False,
        format_data={},
    )
    db.add(part)
    if int(virtual) == ContestParticipationVirtual.LIVE:
        contest.user_count = int(contest.user_count or 0) + 1
    await db.flush()
    return part
