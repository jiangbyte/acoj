"""Portal submission APIs — query params only."""

from __future__ import annotations

import asyncio
import json
import time
from collections.abc import AsyncIterator
from typing import Annotated

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import IdQuery, ProblemIdQuery
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.submission.enums import SubmissionStatus
from app.modules.biz.submission.events import submission_event_channel
from app.modules.biz.submission.performance.schema import (
    MyLatestPracticeAcOut,
    MyLatestPracticeAcQuery,
    MySubmissionStatsOut,
    SimilarSubmissionListOut,
    SimilarSubmissionQuery,
    SubmissionEventsQuery,
    SubmissionPerformanceOut,
)
from app.modules.biz.submission.performance.service import SubmissionPerformanceService
from app.modules.biz.submission.portal.service import PortalSubmissionService
from app.modules.biz.submission.submission.schema import (
    OjSubmissionAdminPageQuery,
    OjSubmissionDetailSchema,
    OjSubmissionListSchema,
)
from app.modules.biz.submission.submission.service import OjSubmissionService
from app.platform.cache.redis import get_redis

router = APIRouter()

_TERMINAL = {SubmissionStatus.COMPLETED.value, SubmissionStatus.FAILED.value}


def _sse_pack(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False, default=str)}\n\n"


@router.get(
    "/biz/submission/page",
    response_model=ApiResponse[PageData[OjSubmissionListSchema]],
)
async def submission_page(
    query: Annotated[OjSubmissionAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[OjSubmissionListSchema]]:
    return success(await PortalSubmissionService(db).page(query))


@router.get(
    "/biz/submission/detail",
    response_model=ApiResponse[OjSubmissionDetailSchema],
)
async def submission_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjSubmissionDetailSchema]:
    viewer = session.account_id if session else None
    return success(await PortalSubmissionService(db).detail(query.id, viewer_account_id=viewer))


@router.get(
    "/biz/submission/performance",
    response_model=ApiResponse[SubmissionPerformanceOut],
)
async def submission_performance(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[SubmissionPerformanceOut]:
    viewer = session.account_id if session else None
    return success(
        await SubmissionPerformanceService(db).get_performance(
            query, viewer=viewer, for_admin=False
        )
    )


@router.get(
    "/biz/submission/similar",
    response_model=ApiResponse[SimilarSubmissionListOut],
)
async def submission_similar(
    query: Annotated[SimilarSubmissionQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[SimilarSubmissionListOut]:
    viewer = session.account_id if session else None
    return success(
        await SubmissionPerformanceService(db).list_similar(
            query, viewer=viewer, for_admin=False
        )
    )


@router.get(
    "/biz/submission/my-latest-ac",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[MyLatestPracticeAcOut],
)
async def submission_my_latest_ac(
    query: Annotated[MyLatestPracticeAcQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[MyLatestPracticeAcOut]:
    submission_id = await SubmissionPerformanceService(db).my_latest_practice_ac(
        query, user_id=session.account_id
    )
    return success(MyLatestPracticeAcOut(submission_id=submission_id))


@router.get(
    "/biz/submission/my-stats",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
    response_model=ApiResponse[MySubmissionStatsOut],
)
async def submission_my_stats(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[MySubmissionStatsOut]:
    return success(await PortalSubmissionService(db).my_stats(session))


@router.get(
    "/biz/submission/events",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def submission_events(
    query: Annotated[SubmissionEventsQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> StreamingResponse:
    from app.platform.db.session import get_session_factory

    submission_id = query.id

    async with get_session_factory()() as db:
        await PortalSubmissionService(db).assert_owner(IdQuery(id=submission_id), session)

    async def _snapshot() -> dict:
        async with get_session_factory()() as sess:
            return await OjSubmissionService(sess).snapshot_for_events(submission_id)

    async def generate() -> AsyncIterator[str]:
        snap = await _snapshot()
        yield _sse_pack("snapshot", snap)
        if snap.get("status") in _TERMINAL:
            yield _sse_pack("done", snap)
            return

        redis = get_redis()
        pubsub = None
        channel = submission_event_channel(submission_id)
        if redis is not None:
            pubsub = redis.pubsub()
            await pubsub.subscribe(channel)

        deadline = time.monotonic() + query.max_wait_sec
        last_poll = 0.0
        try:
            while time.monotonic() < deadline:
                pushed = None
                if pubsub is not None:
                    message = await pubsub.get_message(ignore_subscribe_messages=True, timeout=1.0)
                    if message and message.get("type") == "message":
                        raw = message.get("data")
                        text = raw.decode("utf-8") if isinstance(raw, bytes) else str(raw)
                        try:
                            pushed = json.loads(text)
                        except json.JSONDecodeError:
                            pushed = None
                else:
                    await asyncio.sleep(0.8)

                now = time.monotonic()
                if pushed is None and now - last_poll >= 1.0:
                    last_poll = now
                    pushed = await _snapshot()

                if pushed is not None:
                    yield _sse_pack("update", pushed)
                    if pushed.get("status") in _TERMINAL:
                        yield _sse_pack("done", pushed)
                        return

                yield ": heartbeat\n\n"

            final = await _snapshot()
            yield _sse_pack("timeout", final)
        finally:
            if pubsub is not None:
                try:
                    await pubsub.unsubscribe(channel)
                    await pubsub.aclose()
                except Exception:  # noqa: BLE001
                    pass

    return StreamingResponse(
        generate(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )
