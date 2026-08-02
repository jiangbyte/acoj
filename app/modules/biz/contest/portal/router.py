"""Portal contest APIs: query params only; reads are public, writes need login."""

from __future__ import annotations

from typing import Annotated, Any

from fastapi import APIRouter, Depends, Query
from sqlalchemy import func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType, StatusEnum
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import Current, PageData, PageQuery, Size, build_page
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, to_schema
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.contest.clarification.model import (
    OjContestClarification,
    OjContestClarificationMessage,
    OjContestClarificationThread,
)
from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.enums import (
    ClarificationThreadStatus,
    ContestLifecycleStatus,
    ContestParticipationVirtual,
    ScoreboardVisibility,
)
from app.modules.biz.contest.lifecycle import lifecycle_status
from app.modules.biz.contest.participation.model import OjContestParticipation
from app.modules.biz.contest.portal.schema import (
    PortalClarificationMessageCreateRequest,
    PortalClarificationMessageSchema,
    PortalClarificationSchema,
    PortalClarificationThreadCreateRequest,
    PortalClarificationThreadSchema,
    PortalContestBriefSchema,
    PortalContestJoinRequest,
    PortalContestParticipationSchema,
    PortalContestProblemDetailSchema,
    PortalContestProblemMetaSchema,
    PortalContestSubmitRequest,
)
from app.modules.biz.contest.problem.model import OjContestProblem
from app.modules.biz.contest.scoring import build_scoreboard
from app.modules.biz.contest.submit.service import ContestSubmitService, join_contest
from app.modules.biz.problem.language.model import OjProblemLanguage
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.worker_languages import list_worker_languages
from app.modules.biz.submission.submission.model import OjContestSubmission, OjSubmission
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id

router = APIRouter()


def _brief(contest: OjContest, *, joined: bool = False, include_description: bool = False) -> PortalContestBriefSchema:
    return PortalContestBriefSchema(
        id=contest.id,
        key=contest.key,
        name=contest.name,
        summary=contest.summary,
        description=contest.description if include_description else None,
        start_time=contest.start_time,
        end_time=contest.end_time,
        format_name=contest.format_name,
        lifecycle_status=lifecycle_status(contest).value,
        is_rated=contest.is_rated,
        is_private=bool(contest.is_private),
        use_clarifications=contest.use_clarifications,
        scoreboard_visibility=contest.scoreboard_visibility,
        freeze_seconds=contest.freeze_seconds,
        user_count=int(contest.user_count or 0),
        joined=joined,
        extra=contest.extra or {},
    )


async def _is_joined(db: AsyncSession, contest_id: str, account_id: str | None) -> bool:
    if not account_id:
        return False
    part = (
        await db.execute(
            select(OjContestParticipation.id).where(
                OjContestParticipation.contest_id == contest_id,
                OjContestParticipation.account_id == account_id,
                OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
            )
        )
    ).scalar_one_or_none()
    return part is not None


async def _require_visible_contest(db: AsyncSession, contest_id: str) -> OjContest:
    contest = await db.get(OjContest, contest_id)
    if contest is None or not contest.is_visible:
        raise NotFoundError("竞赛不存在")
    return contest


def _can_view_statements(contest: OjContest, joined: bool) -> bool:
    status = lifecycle_status(contest)
    if status in (ContestLifecycleStatus.RUNNING, ContestLifecycleStatus.ENDED, ContestLifecycleStatus.LOCKED):
        return True
    if status == ContestLifecycleStatus.SCHEDULED:
        return joined
    return False


@router.get(
    "/biz/contest/page",
    response_model=ApiResponse[PageData[PortalContestBriefSchema]],
)
async def contest_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    keyword: str | None = Query(default=None),
) -> ApiResponse[PageData[PortalContestBriefSchema]]:
    pagination = PageQuery(current=current, size=size)
    filters = [OjContest.is_visible.is_(True)]
    if keyword:
        like = f"%{keyword}%"
        filters.append(or_(OjContest.name.ilike(like), OjContest.key.ilike(like)))
    count_stmt = select(func.count(OjContest.id)).where(*filters)
    stmt = (
        select(OjContest)
        .where(*filters)
        .order_by(OjContest.start_time.desc())
        .offset(pagination.offset)
        .limit(pagination.size)
    )
    items = list((await db.execute(stmt)).scalars().all())
    total = (await db.execute(count_stmt)).scalar_one()
    return success(build_page(pagination, total, [_brief(c) for c in items]))


@router.get(
    "/biz/contest/detail",
    response_model=ApiResponse[PortalContestBriefSchema],
)
async def contest_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PortalContestBriefSchema]:
    contest = await _require_visible_contest(db, id)
    joined = await _is_joined(db, id, session.account_id if session else None)
    return success(_brief(contest, joined=joined, include_description=True))


@router.post(
    "/biz/contest/join",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[PortalContestParticipationSchema],
)
async def contest_join(
    payload: PortalContestJoinRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[PortalContestParticipationSchema]:
    async with transactional(db):
        part = await join_contest(
            db,
            contest_id=contest_id,
            account_id=session.account_id,
            access_code=payload.access_code,
            spectate=payload.spectate,
        )
    return success(to_schema(PortalContestParticipationSchema, part))


@router.post(
    "/biz/contest/leave",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[None],
)
async def contest_leave(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[None]:
    contest = await db.get(OjContest, contest_id)
    if contest is None:
        raise NotFoundError("竞赛不存在")
    if lifecycle_status(contest) != ContestLifecycleStatus.SCHEDULED:
        raise BusinessError("比赛已开始，无法取消报名")
    async with transactional(db):
        part = (
            await db.execute(
                select(OjContestParticipation).where(
                    OjContestParticipation.contest_id == contest_id,
                    OjContestParticipation.account_id == session.account_id,
                    OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
                )
            )
        ).scalar_one_or_none()
        if part is None:
            return success()
        await db.delete(part)
        contest.user_count = max(0, int(contest.user_count or 0) - 1)
        await db.flush()
    return success()


@router.post(
    "/biz/contest/submit",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[dict[str, Any]],
)
async def contest_submit(
    payload: PortalContestSubmitRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[dict[str, Any]]:
    snap = await ContestSubmitService(db).submit(
        contest_id=contest_id,
        account_id=session.account_id,
        problem_id=payload.problem_id,
        language_key=payload.language_key,
        source=payload.source,
        wait=payload.wait,
        wait_timeout_sec=payload.wait_timeout_sec,
    )
    return success(snap)


@router.get(
    "/biz/contest/problems",
    response_model=ApiResponse[list[PortalContestProblemMetaSchema]],
)
async def contest_problems(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[list[PortalContestProblemMetaSchema]]:
    contest = await _require_visible_contest(db, contest_id)
    joined = await _is_joined(db, contest_id, session.account_id if session else None)
    if lifecycle_status(contest) == ContestLifecycleStatus.SCHEDULED and not joined:
        return success([])
    rows = list(
        (
            await db.execute(
                select(OjContestProblem)
                .where(OjContestProblem.contest_id == contest_id)
                .order_by(OjContestProblem.sort.asc())
            )
        )
        .scalars()
        .all()
    )
    problem_ids = [p.problem_id for p in rows]
    problems = {}
    if problem_ids:
        for pr in (await db.execute(select(OjProblem).where(OjProblem.id.in_(problem_ids)))).scalars().all():
            problems[pr.id] = pr
    result: list[PortalContestProblemMetaSchema] = []
    for p in rows:
        pr = problems.get(p.problem_id)
        result.append(
            PortalContestProblemMetaSchema(
                id=p.id,
                problem_id=p.problem_id,
                label=p.label,
                points=float(p.points or 0),
                partial=bool(p.partial),
                sort=int(p.sort or 0),
                max_submissions=p.max_submissions,
                problem_code=None if contest.hide_problem_tags and lifecycle_status(contest) == ContestLifecycleStatus.RUNNING else (pr.code if pr else None),
                problem_name=pr.name if pr else None,
            )
        )
    return success(result)


@router.get(
    "/biz/contest/problem/detail",
    response_model=ApiResponse[PortalContestProblemDetailSchema],
)
async def contest_problem_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
    problem_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[PortalContestProblemDetailSchema]:
    contest = await _require_visible_contest(db, contest_id)
    joined = await _is_joined(db, contest_id, session.account_id if session else None)
    if not _can_view_statements(contest, joined):
        raise BusinessError("比赛尚未开始，暂不可查看题面")
    cproblem = (
        await db.execute(
            select(OjContestProblem).where(
                OjContestProblem.contest_id == contest_id,
                OjContestProblem.problem_id == problem_id,
            )
        )
    ).scalar_one_or_none()
    if cproblem is None:
        raise NotFoundError("竞赛题目不存在")
    problem = await db.get(OjProblem, problem_id)
    if problem is None:
        raise NotFoundError("题目不存在")
    lang_rows = list(
        (
            await db.execute(
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
    languages = [
        {
            "language_key": row.language_key,
            "label": (meta.get(row.language_key) or {}).get("label"),
            "extension": (meta.get(row.language_key) or {}).get("extension"),
            "time_limit_ms": row.time_limit_ms,
            "memory_limit_kb": row.memory_limit_kb,
        }
        for row in lang_rows
    ]
    return success(
        PortalContestProblemDetailSchema(
            id=cproblem.id,
            problem_id=cproblem.problem_id,
            label=cproblem.label,
            points=float(cproblem.points or 0),
            partial=bool(cproblem.partial),
            sort=int(cproblem.sort or 0),
            max_submissions=cproblem.max_submissions,
            problem_code=problem.code,
            problem_name=problem.name,
            description=problem.description,
            time_limit_ms=problem.time_limit_ms,
            memory_limit_kb=problem.memory_limit_kb,
            languages=languages,
        )
    )


@router.get(
    "/biz/contest/scoreboard",
    response_model=ApiResponse[dict[str, Any]],
)
async def contest_scoreboard(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[dict[str, Any]]:
    contest = await _require_visible_contest(db, contest_id)
    vis = contest.scoreboard_visibility
    status = lifecycle_status(contest)
    if vis == ScoreboardVisibility.HIDDEN:
        raise BusinessError("榜单已隐藏")
    if vis == ScoreboardVisibility.AFTER_CONTEST and status not in (
        ContestLifecycleStatus.ENDED,
        ContestLifecycleStatus.LOCKED,
    ):
        raise BusinessError("比赛结束后才可查看榜单")
    if vis == ScoreboardVisibility.AFTER_PARTICIPATION:
        joined = await _is_joined(db, contest_id, session.account_id if session else None)
        if not joined:
            raise BusinessError("报名参赛后才可查看榜单")
    board = await build_scoreboard(db, contest_id, ignore_freeze=False)
    return success(board)


@router.get(
    "/biz/contest/my-submissions",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[dict[str, Any]]],
)
async def my_submissions(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[list[dict[str, Any]]]:
    part = (
        await db.execute(
            select(OjContestParticipation).where(
                OjContestParticipation.contest_id == contest_id,
                OjContestParticipation.account_id == session.account_id,
                OjContestParticipation.virtual == ContestParticipationVirtual.LIVE,
            )
        )
    ).scalar_one_or_none()
    if part is None:
        return success([])
    rows = (
        await db.execute(
            select(OjContestSubmission, OjSubmission)
            .join(OjSubmission, OjSubmission.id == OjContestSubmission.submission_id)
            .where(OjContestSubmission.participation_id == part.id)
            .order_by(OjSubmission.created_at.desc())
            .limit(100)
        )
    ).all()
    return success(
        [
            {
                "submission_id": sub.id,
                "problem_id": sub.problem_id,
                "language_key": sub.language_key,
                "status": sub.status,
                "result": sub.result,
                "score": sub.score,
                "contest_points": cs.points,
                "is_pretest": cs.is_pretest,
                "created_at": sub.created_at.isoformat() if sub.created_at else None,
            }
            for cs, sub in rows
        ]
    )


@router.get(
    "/biz/contest/clarifications",
    response_model=ApiResponse[list[PortalClarificationSchema]],
)
async def list_clarifications(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[list[PortalClarificationSchema]]:
    contest = await _require_visible_contest(db, contest_id)
    if not contest.use_clarifications:
        return success([])
    rows = list(
        (
            await db.execute(
                select(OjContestClarification)
                .where(OjContestClarification.contest_id == contest_id)
                .order_by(OjContestClarification.published_at.desc())
            )
        )
        .scalars()
        .all()
    )
    return success([to_schema(PortalClarificationSchema, r) for r in rows])


@router.get(
    "/biz/contest/clarification-threads/mine",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[list[PortalClarificationThreadSchema]],
)
async def my_threads(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[list[PortalClarificationThreadSchema]]:
    threads = list(
        (
            await db.execute(
                select(OjContestClarificationThread).where(
                    OjContestClarificationThread.contest_id == contest_id,
                    OjContestClarificationThread.account_id == session.account_id,
                )
            )
        )
        .scalars()
        .all()
    )
    return success(await _threads_with_messages(db, threads))


@router.post(
    "/biz/contest/clarification-threads",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[PortalClarificationThreadSchema],
)
async def create_thread(
    payload: PortalClarificationThreadCreateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
) -> ApiResponse[PortalClarificationThreadSchema]:
    contest = await db.get(OjContest, contest_id)
    if contest is None:
        raise NotFoundError("竞赛不存在")
    if not contest.use_clarifications:
        raise BusinessError("本场竞赛未开启答疑")
    async with transactional(db):
        thread = OjContestClarificationThread(
            id=generate_snowflake_id(),
            contest_id=contest_id,
            problem_id=payload.problem_id,
            account_id=session.account_id,
            title=payload.title,
            status=ClarificationThreadStatus.OPEN.value,
        )
        db.add(thread)
        db.add(
            OjContestClarificationMessage(
                id=generate_snowflake_id(),
                thread_id=thread.id,
                account_id=session.account_id,
                body=payload.body,
                is_staff=False,
            )
        )
        await db.flush()
    return success((await _threads_with_messages(db, [thread]))[0])


@router.post(
    "/biz/contest/clarification-threads/messages",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[PortalClarificationMessageSchema],
)
async def add_thread_message(
    payload: PortalClarificationMessageCreateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    contest_id: Annotated[Id, Query()],
    thread_id: Annotated[Id, Query()],
) -> ApiResponse[PortalClarificationMessageSchema]:
    thread = await db.get(OjContestClarificationThread, thread_id)
    if thread is None or thread.contest_id != contest_id:
        raise NotFoundError("提问不存在")
    if thread.account_id != session.account_id:
        raise BusinessError("只能在自己的提问中追加消息")
    if thread.status == ClarificationThreadStatus.CLOSED.value:
        raise BusinessError("提问已关闭")
    async with transactional(db):
        msg = OjContestClarificationMessage(
            id=generate_snowflake_id(),
            thread_id=thread_id,
            account_id=session.account_id,
            body=payload.body,
            is_staff=False,
        )
        db.add(msg)
        await db.flush()
    return success(to_schema(PortalClarificationMessageSchema, msg))


async def _threads_with_messages(
    db: AsyncSession,
    threads: list[OjContestClarificationThread],
) -> list[PortalClarificationThreadSchema]:
    if not threads:
        return []
    ids = [t.id for t in threads]
    msgs = list(
        (
            await db.execute(
                select(OjContestClarificationMessage)
                .where(OjContestClarificationMessage.thread_id.in_(ids))
                .order_by(OjContestClarificationMessage.created_at.asc())
            )
        )
        .scalars()
        .all()
    )
    by_thread: dict[str, list[PortalClarificationMessageSchema]] = {i: [] for i in ids}
    for m in msgs:
        by_thread.setdefault(m.thread_id, []).append(to_schema(PortalClarificationMessageSchema, m))
    return [
        PortalClarificationThreadSchema(
            id=t.id,
            contest_id=t.contest_id,
            problem_id=t.problem_id,
            account_id=t.account_id,
            title=t.title,
            status=ClarificationThreadStatus(t.status),
            messages=by_thread.get(t.id, []),
        )
        for t in threads
    ]
