from datetime import UTC, datetime

from sqlalchemy import delete
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.core.schema.base import to_schema
from app.modules.judge.enums import JudgeTaskStatus, SubmissionStatus
from app.modules.judge.model import OjSubmission, OjSubmissionCase
from app.modules.judge.repository import JudgeRepository
from app.modules.judge.schema import (
    JudgeNodeHeartbeatRequest,
    JudgeNodeRegisterRequest,
    JudgeNodeSchema,
    JudgeTaskEventRequest,
    JudgeTaskFinishRequest,
    JudgeTaskPollRequest,
    JudgeTaskSchema,
)
from app.platform.db.transaction import transactional


class JudgeService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = JudgeRepository(db)

    async def register_node(self, payload: JudgeNodeRegisterRequest) -> JudgeNodeSchema:
        async with transactional(self.db):
            return to_schema(JudgeNodeSchema, await self.repo.upsert_node(payload))

    async def heartbeat(self, payload: JudgeNodeHeartbeatRequest) -> JudgeNodeSchema:
        async with transactional(self.db):
            return to_schema(JudgeNodeSchema, await self.repo.upsert_node(payload))

    async def poll_task(self, payload: JudgeTaskPollRequest) -> JudgeTaskSchema | None:
        async with transactional(self.db):
            task = await self.repo.poll_task(payload.node_id, payload.capacity)
            return to_schema(JudgeTaskSchema, task) if task else None

    async def record_event(self, task_id: str, payload: JudgeTaskEventRequest) -> dict:
        async with transactional(self.db):
            task = await self.repo.get_task(task_id)
            if task is None:
                raise NotFoundError("Judge task not found")
            if payload.type.upper() == "RUNNING":
                task.status = JudgeTaskStatus.RUNNING.value
                task.started_at = datetime.now(UTC)
            return {"task_id": task_id, "type": payload.type, "accepted": True}

    async def finish_task(self, task_id: str, payload: JudgeTaskFinishRequest) -> JudgeTaskSchema:
        async with transactional(self.db):
            task = await self.repo.get_task(task_id)
            if task is None:
                raise NotFoundError("Judge task not found")
            task.status = (
                JudgeTaskStatus.FINISHED.value
                if payload.status.upper() == "FINISHED"
                else JudgeTaskStatus.FAILED.value
            )
            task.error_message = payload.error_message
            task.finished_at = datetime.now(UTC)
            await self._persist_submission_result(task.submission_id, task.node_id, payload)
            return to_schema(JudgeTaskSchema, task)

    async def _persist_submission_result(
        self,
        submission_id: str,
        node_id: str | None,
        payload: JudgeTaskFinishRequest,
    ) -> None:
        submission = await self.db.get(OjSubmission, submission_id)
        if submission is None:
            return
        if payload.status.upper() != "FINISHED":
            submission.status = SubmissionStatus.SE.value
            return
        result = payload.result or {}
        status = str(result.get("status") or SubmissionStatus.SE.value).upper()
        submission.status = status
        submission.score = float(result.get("score") or 0)
        submission.time_ms = int(result.get("time_ms") or 0)
        submission.memory_kb = int(result.get("memory_kb") or 0)
        submission.compile_message = result.get("compile_message")
        submission.judger_id = node_id
        await self.db.execute(
            delete(OjSubmissionCase).where(OjSubmissionCase.submission_id == submission_id)
        )
        for case in result.get("cases") or []:
            self.db.add(
                OjSubmissionCase(
                    submission_id=submission_id,
                    case_no=int(case.get("case_no") or 0),
                    batch_no=case.get("batch_no"),
                    status=str(case.get("status") or SubmissionStatus.SE.value).upper(),
                    score=float(case.get("score") or 0),
                    total_score=float(case.get("total_score") or 0),
                    time_ms=int(case.get("time_ms") or 0),
                    memory_kb=int(case.get("memory_kb") or 0),
                    feedback=case.get("feedback"),
                    output_preview=case.get("output_preview"),
                )
            )
