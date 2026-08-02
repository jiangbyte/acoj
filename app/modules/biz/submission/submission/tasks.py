"""Apply judge results on the API-only Celery queue (acoj_api).

Worker finishes judge.execute → Celery link delivers the return value here.
acoj-worker must NOT consume acoj_api (or default if unused for apply).
"""

from __future__ import annotations

import logging
from typing import Any

from celery import signature

from app.platform.cache.redis import init_redis
from app.platform.db.session import get_session_factory, init_engine
from app.platform.db.transaction import transactional
from app.platform.tasks.async_runner import worker_async_runner
from app.platform.tasks.celery_app import celery_app

logger = logging.getLogger(__name__)

# Only hei-fastapi celery consumes this queue (never acoj-worker).
API_APPLY_QUEUE = "acoj_api"


def apply_success_signature(submission_id: str):
    return signature(
        "submission.apply_judge_result",
        args=[submission_id],
        queue=API_APPLY_QUEUE,
        app=celery_app,
    )


def apply_failure_signature(submission_id: str):
    return signature(
        "submission.apply_judge_failure",
        args=[submission_id],
        queue=API_APPLY_QUEUE,
        app=celery_app,
    )


@celery_app.task(name="submission.apply_judge_result")
def apply_judge_result_task(raw_result: Any, submission_id: str) -> None:
    """Celery link target: first arg is judge.execute return value."""
    worker_async_runner.run(_apply(raw_result, submission_id))


@celery_app.task(name="submission.apply_judge_failure")
def apply_judge_failure_task(task_id: str, submission_id: str) -> None:
    """Celery link_error target: first arg is failed parent task id."""
    worker_async_runner.run(
        _apply(
            {
                "submission_id": submission_id,
                "status": "FAILED",
                "result": "SE",
                "error": f"Judge task failed: {task_id}",
                "cases": [],
            },
            submission_id,
        )
    )


async def _apply(raw_result: Any, submission_id: str) -> None:
    from app.modules.biz.submission.enums import SubmissionStatus
    from app.modules.biz.submission.events import publish_submission_event
    from app.modules.biz.submission.submission.service import OjSubmissionService

    init_engine()
    await init_redis()

    if not isinstance(raw_result, dict):
        raw_result = {
            "submission_id": submission_id,
            "status": SubmissionStatus.FAILED.value,
            "result": "SE",
            "error": f"Unexpected judge result type: {type(raw_result).__name__}",
            "cases": [],
        }

    session_factory = get_session_factory()
    async with session_factory() as session:
        service = OjSubmissionService(session)
        async with transactional(session):
            await service.apply_judge_result(submission_id, raw_result)
        snap = await service.snapshot_for_events(submission_id)
    await publish_submission_event(submission_id, snap)
    logger.info(
        "Applied judge result submission_id=%s status=%s result=%s",
        submission_id,
        snap.get("status"),
        snap.get("result"),
    )
