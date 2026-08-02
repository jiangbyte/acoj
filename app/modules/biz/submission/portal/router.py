"""Portal submission APIs — query params only."""

from __future__ import annotations

import asyncio
import json
import time
from collections.abc import AsyncIterator
from typing import Annotated

from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.submission.enums import SubmissionKind, SubmissionStatus
from app.modules.biz.submission.events import submission_event_channel
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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    problem_id: str | None = Query(default=None),
    problem_code: str | None = Query(default=None),
    contest_id: str | None = Query(default=None),
    user_id: str | None = Query(default=None),
    kind: SubmissionKind | None = Query(default=None),
    status: SubmissionStatus | None = Query(default=None),
    result: str | None = Query(default=None),
    language_key: str | None = Query(default=None),
) -> ApiResponse[PageData[OjSubmissionListSchema]]:
    query = OjSubmissionAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        problem_id=problem_id,
        problem_code=problem_code,
        contest_id=contest_id,
        user_id=user_id,
        kind=kind,
        status=status,
        result=result,
        language_key=language_key,
    )
    return success(await PortalSubmissionService(db).page(query))


@router.get(
    "/biz/submission/detail",
    response_model=ApiResponse[OjSubmissionDetailSchema],
)
async def submission_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[OjSubmissionDetailSchema]:
    viewer = session.account_id if session else None
    return success(await PortalSubmissionService(db).detail(id, viewer_account_id=viewer))


@router.get(
    "/biz/submission/events",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def submission_events(
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    max_wait_sec: int = Query(default=120, ge=5, le=600),
) -> StreamingResponse:
    from app.platform.db.session import get_session_factory

    submission_id = id

    async with get_session_factory()() as db:
        await PortalSubmissionService(db).assert_owner(submission_id, session.account_id)

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

        deadline = time.monotonic() + max_wait_sec
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
