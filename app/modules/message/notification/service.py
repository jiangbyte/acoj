from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema
from app.core.security.session import SessionPayload
from app.modules.message.notification.model import MsgNotification
from app.modules.message.notification.repository import NotificationRepository
from app.modules.message.notification.schema import (
    MarkNotificationReadRequest,
    MyNotificationPageQuery,
    NotificationAdminPageQuery,
    NotificationCreateRequest,
    NotificationSchema,
    NotificationUpdateRequest,
)
from app.platform.db.transaction import transactional


class NotificationService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = NotificationRepository(db)

    async def create_notification(self, payload: NotificationCreateRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.create_notification(
                payload,
                sender_account_type=str(session.account_type),
                sender_account_id=session.account_id,
            )

    async def update_notification(self, payload: NotificationUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update_notification(payload)

    async def publish_notification(self, payload: IdQuery) -> None:
        async with transactional(self.db):
            await self.repo.publish_notification(payload.id)

    async def revoke_notification(self, payload: IdQuery) -> None:
        async with transactional(self.db):
            await self.repo.revoke_notification(payload.id)

    async def delete_notifications(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_notifications(payload.ids)

    async def notification_detail(self, payload: IdQuery) -> NotificationSchema:
        return _notification_schema(await self.repo.get_notification_required(payload.id), is_read=False)

    async def my_notification_detail(self, payload: IdQuery, session: SessionPayload) -> NotificationSchema:
        item, is_read = await self.repo.get_my_notification(
            payload.id,
            account_type=str(session.account_type),
            account_id=session.account_id,
        )
        return _notification_schema(item, is_read=is_read)

    async def page_notifications_admin(self, query: NotificationAdminPageQuery) -> PageData[NotificationSchema]:
        items, total = await self.repo.page_notifications_admin(query)
        return build_page(query.pagination, total, [_notification_schema(item, is_read=False) for item in items])

    async def page_my_notifications(self, query: MyNotificationPageQuery, session: SessionPayload) -> PageData[NotificationSchema]:
        items, total, read_ids = await self.repo.page_my_notifications(
            query,
            account_type=str(session.account_type),
            account_id=session.account_id,
        )
        return build_page(query.pagination, total, [_notification_schema(item, is_read=item.id in read_ids) for item in items])

    async def mark_notifications_read(self, payload: MarkNotificationReadRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.mark_notifications_read(
                payload.ids,
                account_type=str(session.account_type),
                account_id=session.account_id,
            )

    async def mark_all_notifications_read(self, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.mark_all_notifications_read(
                account_type=str(session.account_type),
                account_id=session.account_id,
            )


def _notification_schema(item: MsgNotification, is_read: bool) -> NotificationSchema:
    schema = to_schema(NotificationSchema, item)
    schema.is_read = is_read
    return schema
