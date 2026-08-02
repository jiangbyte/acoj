"""Redis pub/sub helpers for submission status push (SSE)."""

from __future__ import annotations

import json
import logging
from typing import Any

from app.platform.cache.redis import get_redis

logger = logging.getLogger(__name__)

SUBMISSION_EVENT_CHANNEL_PREFIX = "oj:submission:events:"


def submission_event_channel(submission_id: str) -> str:
    return f"{SUBMISSION_EVENT_CHANNEL_PREFIX}{submission_id}"


async def publish_submission_event(submission_id: str, payload: dict[str, Any]) -> bool:
    redis = get_redis()
    if redis is None:
        return False
    try:
        await redis.publish(
            submission_event_channel(submission_id),
            json.dumps(payload, ensure_ascii=False, default=str),
        )
        return True
    except Exception:
        logger.warning("Failed to publish submission event %s", submission_id, exc_info=True)
        return False
