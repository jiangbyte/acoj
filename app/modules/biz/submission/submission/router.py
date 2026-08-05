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
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.submission.enums import SubmissionKind, SubmissionStatus
from app.modules.biz.submission.events import submission_event_channel
from app.modules.biz.submission.performance.schema import (
    SimilarSubmissionListOut,
    SubmissionPerformanceOut,
)
from app.modules.biz.submission.performance.service import SubmissionPerformanceService
from app.modules.biz.submission.submission.schema import (
    OjSubmissionAdminPageQuery,
    OjSubmissionDetailSchema,
    OjSubmissionListSchema,
    OjSubmissionRejudgeRequest,
    OjSubmissionRejudgeResult,
)
from app.modules.biz.submission.submission.service import OjSubmissionService
from app.platform.cache.redis import get_redis

router = APIRouter()

_TERMINAL = {SubmissionStatus.COMPLETED.value, SubmissionStatus.FAILED.value}


def _sse_pack(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False, default=str)}\n\n"


@router.get(
    "/biz/submission/submission/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:page")),
    ],
    response_model=ApiResponse[PageData[OjSubmissionListSchema]],
)
async def page(
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
    return success(await OjSubmissionService(db).page_admin(query))


@router.get(
    "/biz/submission/submission/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:detail")),
    ],
    response_model=ApiResponse[OjSubmissionDetailSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[OjSubmissionDetailSchema]:
    return success(await OjSubmissionService(db).detail(IdQuery(id=id)))


@router.get(
    "/biz/submission/submission/performance",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:detail")),
    ],
    response_model=ApiResponse[SubmissionPerformanceOut],
)
async def performance(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SubmissionPerformanceOut]:
    return success(
        await SubmissionPerformanceService(db).get_performance(id, viewer=None, for_admin=True)
    )


@router.get(
    "/biz/submission/submission/similar",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:detail")),
    ],
    response_model=ApiResponse[SimilarSubmissionListOut],
)
async def similar(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    size: int = Query(default=10, ge=1, le=50),
) -> ApiResponse[SimilarSubmissionListOut]:
    return success(
        await SubmissionPerformanceService(db).list_similar(
            id, size=size, viewer=None, for_admin=True
        )
    )


@router.post(
    "/biz/submission/submission/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await OjSubmissionService(db).delete(payload)
    return success()


@router.post(
    "/biz/submission/submission/rejudge",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:rejudge")),
    ],
    response_model=ApiResponse[OjSubmissionRejudgeResult],
)
async def rejudge(
    payload: OjSubmissionRejudgeRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
    _session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[OjSubmissionRejudgeResult]:
    return success(await OjSubmissionService(db).rejudge(payload))


@router.get(
    "/biz/submission/submission/events",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:submission:submission:detail")),
    ],
)
async def events(
    id: Annotated[Id, Query()],
    max_wait_sec: int = Query(default=120, ge=5, le=600),
) -> StreamingResponse:
    """SSE stream for one submission until COMPLETED/FAILED.

    Prefer fetch()+ReadableStream (Authorization header). Browser EventSource cannot set Bearer.
    Uses Redis pub/sub with DB poll fallback; heartbeat comments keep proxies alive.
    """
    from app.platform.db.session import get_session_factory

    submission_id = id

    async def _snapshot() -> dict:
        async with get_session_factory()() as session:
            return await OjSubmissionService(session).snapshot_for_events(submission_id)

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
