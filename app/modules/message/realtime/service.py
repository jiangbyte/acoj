import asyncio
import json
from datetime import UTC, datetime
from typing import AsyncIterator

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import AuthenticationError
from app.core.security.session import SessionPayload, session_store
from app.modules.message.message.repository import MessageRepository
from app.modules.message.message.schema import ThreadPageQuery
from app.modules.message.notification.repository import NotificationRepository
from app.modules.message.notification.schema import MyNotificationPageQuery
from app.modules.message.realtime.schema import HeaderNoticeItem, MessageSummaryResponse
from app.modules.message.todo.repository import TodoRepository
from app.modules.message.todo.schema import MyTodoPageQuery


class RealtimeService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.notification_repo = NotificationRepository(db)
        self.message_repo = MessageRepository(db)
        self.todo_repo = TodoRepository(db)

    async def summary(self, session: SessionPayload) -> MessageSummaryResponse:
        account_type = str(session.account_type)
        account_id = session.account_id
        notification_unread = await self.notification_repo.count_unread_notifications(
            account_type=account_type,
            account_id=account_id,
        )
        message_unread = await self.message_repo.count_unread_messages(
            account_type=account_type,
            account_id=account_id,
        )
        todo_pending = await self.todo_repo.count_my_todos(
            account_type=account_type,
            account_id=account_id,
        )
        return MessageSummaryResponse(
            notification_unread=notification_unread,
            message_unread=message_unread,
            todo_pending=todo_pending,
        )

    async def header_items(self, session: SessionPayload) -> list[HeaderNoticeItem]:
        account_type = str(session.account_type)
        account_id = session.account_id
        notifications, _, read_ids = await self.notification_repo.page_my_notifications(
            MyNotificationPageQuery(pagination={"current": 1, "size": 8}),
            account_type=account_type,
            account_id=account_id,
        )
        threads, _, unread_map, latest_map = await self.message_repo.page_my_threads(
            ThreadPageQuery(pagination={"current": 1, "size": 8}),
            account_type=account_type,
            account_id=account_id,
        )
        todos, _, todo_status_map = await self.todo_repo.page_my_todos(
            MyTodoPageQuery(pagination={"current": 1, "size": 8}),
            account_type=account_type,
            account_id=account_id,
        )
        items: list[HeaderNoticeItem] = []
        for notification in notifications:
            items.append(
                HeaderNoticeItem(
                    id=f"notification:{notification.id}",
                    type=0,
                    title=notification.title,
                    icon="icon-park-outline:tips-one",
                    tag_title=str(notification.severity),
                    tag_type=_notice_tag_type(str(notification.severity)),
                    description=notification.content,
                    date=notification.publish_at or notification.created_at,
                    is_read=notification.id in read_ids,
                    source_type="notification",
                    source_id=notification.id,
                )
            )
        for thread in threads:
            latest = latest_map.get(thread.last_message_id or "")
            items.append(
                HeaderNoticeItem(
                    id=f"message:{thread.id}",
                    type=1,
                    title=thread.title or "Message",
                    icon="icon-park-outline:message",
                    tag_title=str(thread.thread_type),
                    tag_type="info",
                    description=latest.content if latest else None,
                    date=thread.last_message_at or thread.updated_at,
                    is_read=unread_map.get(thread.id, 0) <= 0,
                    source_type="message",
                    source_id=thread.id,
                )
            )
        for todo in todos:
            assignee_status = todo_status_map.get(todo.id)
            items.append(
                HeaderNoticeItem(
                    id=f"todo:{todo.id}",
                    type=2,
                    title=todo.title,
                    icon="icon-park-outline:checklist",
                    tag_title=str(todo.priority),
                    tag_type=_todo_tag_type(str(todo.priority)),
                    description=todo.content,
                    date=todo.due_at or todo.updated_at,
                    is_read=assignee_status is not None,
                    source_type="todo",
                    source_id=todo.id,
                )
            )
        return sorted(items, key=lambda item: item.date or datetime.min.replace(tzinfo=UTC), reverse=True)[:12]

    async def event_stream(self, session: SessionPayload) -> AsyncIterator[str]:
        previous = ""
        while True:
            summary = await self.summary(session)
            payload = summary.model_dump()
            current = json.dumps(payload, ensure_ascii=False, sort_keys=True)
            if current != previous:
                yield f"event: summary\ndata: {current}\n\n"
                previous = current
            else:
                yield "event: ping\ndata: {}\n\n"
            await asyncio.sleep(15)


async def get_session_by_token(token: str | None) -> SessionPayload:
    if not token:
        raise AuthenticationError("Missing authorization token")
    session = await session_store.get(token.strip())
    if not session:
        raise AuthenticationError("Invalid or expired token")
    return session


def _notice_tag_type(severity: str) -> str:
    return {
        "SUCCESS": "success",
        "WARNING": "warning",
        "ERROR": "error",
    }.get(severity, "info")


def _todo_tag_type(priority: str) -> str:
    return {
        "LOW": "default",
        "HIGH": "warning",
        "URGENT": "error",
    }.get(priority, "info")
