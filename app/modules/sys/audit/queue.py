from __future__ import annotations

import asyncio
import logging
from dataclasses import dataclass

from app.core.config.settings import settings
from app.modules.sys.audit.service import OperationAuditService
from app.platform.db.session import get_session_factory

logger = logging.getLogger(__name__)


@dataclass(frozen=True, slots=True)
class OperationAuditEvent:
    resource_type: str
    action: str
    method: str
    path: str
    status_code: int
    account_id: str | None
    account_type: str | None
    request_id: str | None
    ip: str | None
    user_agent: str | None


class OperationAuditQueue:
    def __init__(self) -> None:
        self._queue: asyncio.Queue[OperationAuditEvent] | None = None
        self._worker: asyncio.Task[None] | None = None
        self._lock = asyncio.Lock()

    async def start(self) -> None:
        async with self._lock:
            if self._worker and not self._worker.done():
                return
            self._queue = asyncio.Queue(maxsize=settings.audit.operation_queue_size)
            self._worker = asyncio.create_task(self._run(), name="operation-audit-writer")

    async def stop(self) -> None:
        async with self._lock:
            queue = self._queue
            worker = self._worker
            self._queue = None
            self._worker = None
        if queue is None or worker is None:
            return

        try:
            await asyncio.wait_for(
                queue.join(),
                timeout=settings.audit.operation_shutdown_timeout_seconds,
            )
        except TimeoutError:
            logger.warning("Timed out waiting for operation audit queue to drain")

        worker.cancel()
        try:
            await worker
        except asyncio.CancelledError:
            pass

    def enqueue(self, event: OperationAuditEvent) -> bool:
        queue = self._queue
        if queue is None:
            logger.debug("Operation audit queue is not started; dropping event")
            return False
        try:
            queue.put_nowait(event)
        except asyncio.QueueFull:
            logger.warning("Operation audit queue is full; dropping event")
            return False
        return True

    async def _run(self) -> None:
        queue = self._queue
        if queue is None:
            return
        while True:
            event = await queue.get()
            try:
                await _record_operation_audit(event)
            except Exception:
                logger.exception("Failed to write operation audit log")
            finally:
                queue.task_done()


async def _record_operation_audit(event: OperationAuditEvent) -> None:
    async with get_session_factory()() as session:
        await OperationAuditService(session).record(
            module="iam" if event.resource_type != "resources" else "resource",
            resource_type=event.resource_type,
            action=event.action,
            summary=f"{event.method} {event.path}",
            success=event.status_code < 400,
            error_message=None if event.status_code < 400 else str(event.status_code),
            account_id=event.account_id,
            account_type=event.account_type,
            request_id=event.request_id,
            ip=event.ip,
            user_agent=event.user_agent,
        )


operation_audit_queue = OperationAuditQueue()


async def start_operation_audit_queue() -> None:
    await operation_audit_queue.start()


async def stop_operation_audit_queue() -> None:
    await operation_audit_queue.stop()
