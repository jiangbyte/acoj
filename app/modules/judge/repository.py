from datetime import UTC, datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.modules.judge.enums import JudgeNodeStatus, JudgeTaskStatus
from app.modules.judge.model import OjJudgeNode, OjJudgeTask
from app.modules.judge.schema import JudgeNodeHeartbeatRequest, JudgeNodeRegisterRequest


class JudgeRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def upsert_node(
        self,
        payload: JudgeNodeRegisterRequest | JudgeNodeHeartbeatRequest,
    ) -> OjJudgeNode:
        stmt = select(OjJudgeNode).where(OjJudgeNode.node_id == payload.node_id)
        node = (await self.db.execute(stmt)).scalar_one_or_none()
        if node is None:
            node = OjJudgeNode(node_id=payload.node_id, name=payload.name)
            self.db.add(node)
        node.name = payload.name
        node.base_url = payload.base_url
        node.version = payload.version
        node.cpu_core = payload.cpu_core
        node.supported_languages = payload.supported_languages
        node.supported_features = payload.supported_features
        node.status = JudgeNodeStatus.ONLINE.value
        node.last_heartbeat_at = datetime.now(UTC)
        if isinstance(payload, JudgeNodeHeartbeatRequest):
            node.load = payload.load
            node.running_tasks = payload.running_tasks
        await self.db.flush()
        await self.db.refresh(node)
        return node

    async def poll_task(self, node_id: str, capacity: int) -> OjJudgeTask | None:
        if capacity <= 0:
            return None
        stmt = (
            select(OjJudgeTask)
            .where(OjJudgeTask.status == JudgeTaskStatus.PENDING.value)
            .order_by(OjJudgeTask.priority.desc(), OjJudgeTask.created_at.asc())
            .limit(1)
            .with_for_update(skip_locked=True)
        )
        task = (await self.db.execute(stmt)).scalar_one_or_none()
        if task is None:
            return None
        task.node_id = node_id
        task.status = JudgeTaskStatus.DISPATCHED.value
        task.attempt += 1
        task.locked_at = datetime.now(UTC)
        await self.db.flush()
        await self.db.refresh(task)
        return task

    async def get_task(self, task_id: str) -> OjJudgeTask | None:
        return await self.db.get(OjJudgeTask, task_id)
