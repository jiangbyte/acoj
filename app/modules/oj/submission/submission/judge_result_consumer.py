"""判题结果消费者 — 从 MQ 消费判题结果，写入数据库。"""

import json
import logging
from datetime import datetime, timezone

from app.platform.mq.consumer import MQConsumerWorker, worker_async_runner
from app.platform.mq.message import MQMessage

logger = logging.getLogger(__name__)


async def _handle_judge_result(message: MQMessage) -> None:
    from app.platform.db.session import get_session_factory, init_engine
    from app.platform.id_generator.snowflake import generate_snowflake_id
    from app.modules.oj.submission.submission.model import OjSubmission
    from app.modules.oj.submission.case.model import OjSubmissionCase

    payload = json.loads(message.body)
    submission_id = payload["submission_id"]

    init_engine()
    session_factory = get_session_factory()

    async with session_factory() as db:
        submission = await db.get(OjSubmission, submission_id)
        if not submission:
            logger.error("提交记录未找到: %s", submission_id)
            return

        submission.status = payload["status"]
        submission.result = payload["result"]
        submission.score = payload["score"]
        submission.time_ms = payload["time_ms"]
        submission.memory_kb = payload["memory_kb"]
        submission.compile_output = payload.get("compile_output")
        submission.judged_at = datetime.now(timezone.utc)

        total_case_points = 0.0
        for case_data in payload.get("cases", []):
            total_case_points += case_data.get("total", 0.0)
            db.add(
                OjSubmissionCase(
                    id=generate_snowflake_id(),
                    submission_id=submission_id,
                    case_no=case_data["case_no"],
                    status="COMPLETED",
                    result=case_data["result"],
                    time_ms=case_data["time_ms"],
                    memory_kb=case_data["memory_kb"],
                    points=case_data["points"],
                    total=case_data["total"],
                    output=case_data.get("stdout_preview"),
                    stderr=case_data.get("stderr_preview"),
                    sort=case_data["case_no"],
                )
            )

        submission.case_points = payload.get("score", 0.0)
        submission.case_total = total_case_points
        submission.current_case = len(payload.get("cases", []))

        await db.commit()
        logger.info("判题结果已写库: %s -> %s", submission_id, payload.get("result"))


def handle_result(message: MQMessage) -> None:
    worker_async_runner.run(_handle_judge_result(message))


def _setup_channel(channel) -> str:
    exchange = "oj.judge"
    queue = "oj.judge.result"
    channel.exchange_declare(exchange=exchange, exchange_type="direct", durable=True)
    channel.queue_declare(queue=queue, durable=True)
    channel.queue_bind(queue=queue, exchange=exchange, routing_key="result")
    return queue


result_consumer = MQConsumerWorker(
    name="judge-result",
    setup_channel=_setup_channel,
    handler=handle_result,
    auto_ack=False,
    prefetch_count=1,
)
