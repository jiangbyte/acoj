from datetime import UTC, datetime

from sqlalchemy import Select, and_, delete, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import NotFoundError
from app.modules.message.enums import MessageTargetScope, NotificationStatus
from app.modules.message.notification.model import MsgNotification, MsgNotificationRead
from app.modules.message.notification.schema import (
    MyNotificationPageQuery,
    NotificationAdminPageQuery,
    NotificationCreateRequest,
    NotificationUpdateRequest,
)


class NotificationRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_notification_required(self, notification_id: str) -> MsgNotification:
        entity = await self.db.get(MsgNotification, notification_id)
        if entity is None:
            raise NotFoundError("Notification not found")
        return entity

    async def create_notification(
        self,
        payload: NotificationCreateRequest,
        *,
        sender_account_type: str | None,
        sender_account_id: str | None,
    ) -> MsgNotification:
        data = payload.model_dump()
        data["publish_at"] = payload.publish_at or (
            datetime.now(UTC) if payload.status == NotificationStatus.PUBLISHED else None
        )
        entity = MsgNotification(
            **data,
            sender_account_type=sender_account_type,
            sender_account_id=sender_account_id,
        )
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def update_notification(self, payload: NotificationUpdateRequest) -> MsgNotification:
        entity = await self.get_notification_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        if payload.status == NotificationStatus.PUBLISHED and data.get("publish_at") is None:
            data["publish_at"] = entity.publish_at or datetime.now(UTC)
        for key, value in data.items():
            setattr(entity, key, value)
        await self.db.flush()
        return entity

    async def publish_notification(self, notification_id: str) -> None:
        entity = await self.get_notification_required(notification_id)
        entity.status = NotificationStatus.PUBLISHED.value
        entity.publish_at = entity.publish_at or datetime.now(UTC)
        entity.revoked_at = None
        await self.db.flush()

    async def revoke_notification(self, notification_id: str) -> None:
        entity = await self.get_notification_required(notification_id)
        entity.status = NotificationStatus.REVOKED.value
        entity.revoked_at = datetime.now(UTC)
        await self.db.flush()

    async def delete_notifications(self, ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(ids))
        await self.db.execute(delete(MsgNotificationRead).where(MsgNotificationRead.notification_id.in_(unique_ids)))
        result = await self.db.execute(delete(MsgNotification).where(MsgNotification.id.in_(unique_ids)))
        if result.rowcount != len(unique_ids):
            raise NotFoundError("Notification not found")

    async def page_notifications_admin(
        self,
        query: NotificationAdminPageQuery,
    ) -> tuple[list[MsgNotification], int]:
        stmt: Select[tuple[MsgNotification]] = select(MsgNotification)
        count_stmt = select(func.count(MsgNotification.id))
        filters = []
        if query.title:
            filters.append(MsgNotification.title.contains(query.title))
        if query.status:
            filters.append(MsgNotification.status == query.status.value)
        if query.target_account_type:
            filters.append(MsgNotification.target_account_type == query.target_account_type.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = stmt.order_by(MsgNotification.updated_at.desc(), MsgNotification.id.desc()).offset(query.pagination.offset).limit(
            query.pagination.size
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def page_my_notifications(
        self,
        query: MyNotificationPageQuery,
        *,
        account_type: str,
        account_id: str,
    ) -> tuple[list[MsgNotification], int, set[str]]:
        visible_filter = self._notification_visible_filter(account_type, account_id)
        read_subquery = (
            select(MsgNotificationRead.notification_id)
            .where(
                MsgNotificationRead.account_type == account_type,
                MsgNotificationRead.account_id == account_id,
            )
            .subquery()
        )
        stmt = select(MsgNotification).where(visible_filter)
        count_stmt = select(func.count(MsgNotification.id)).where(visible_filter)
        if query.unread_only:
            stmt = stmt.where(MsgNotification.id.not_in(select(read_subquery.c.notification_id)))
            count_stmt = count_stmt.where(MsgNotification.id.not_in(select(read_subquery.c.notification_id)))
        stmt = stmt.order_by(MsgNotification.publish_at.desc().nullslast(), MsgNotification.id.desc()).offset(
            query.pagination.offset
        ).limit(query.pagination.size)
        items = list((await self.db.execute(stmt)).scalars().all())
        read_ids = await self.list_notification_read_ids(
            [item.id for item in items],
            account_type=account_type,
            account_id=account_id,
        )
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total, read_ids

    async def count_unread_notifications(self, *, account_type: str, account_id: str) -> int:
        read_subquery = (
            select(MsgNotificationRead.notification_id)
            .where(
                MsgNotificationRead.account_type == account_type,
                MsgNotificationRead.account_id == account_id,
            )
            .subquery()
        )
        stmt = (
            select(func.count(MsgNotification.id))
            .where(self._notification_visible_filter(account_type, account_id))
            .where(MsgNotification.id.not_in(select(read_subquery.c.notification_id)))
        )
        return int((await self.db.execute(stmt)).scalar_one())

    async def list_notification_read_ids(self, ids: list[str], *, account_type: str, account_id: str) -> set[str]:
        if not ids:
            return set()
        stmt = select(MsgNotificationRead.notification_id).where(
            MsgNotificationRead.notification_id.in_(ids),
            MsgNotificationRead.account_type == account_type,
            MsgNotificationRead.account_id == account_id,
        )
        return set((await self.db.execute(stmt)).scalars().all())

    async def mark_notifications_read(self, ids: list[str], *, account_type: str, account_id: str) -> None:
        unique_ids = list(dict.fromkeys(ids))
        if not unique_ids:
            return
        visible_ids = set(
            (
                await self.db.execute(
                    select(MsgNotification.id).where(
                        MsgNotification.id.in_(unique_ids),
                        self._notification_visible_filter(account_type, account_id),
                    )
                )
            )
            .scalars()
            .all()
        )
        if len(visible_ids) != len(unique_ids):
            raise NotFoundError("Notification not found")
        existing = await self.list_notification_read_ids(unique_ids, account_type=account_type, account_id=account_id)
        now = datetime.now(UTC)
        self.db.add_all(
            [
                MsgNotificationRead(
                    notification_id=notification_id,
                    account_type=account_type,
                    account_id=account_id,
                    read_at=now,
                )
                for notification_id in unique_ids
                if notification_id not in existing
            ]
        )
        await self.db.flush()

    async def mark_all_notifications_read(self, *, account_type: str, account_id: str) -> None:
        ids = list(
            (
                await self.db.execute(
                    select(MsgNotification.id).where(self._notification_visible_filter(account_type, account_id))
                )
            )
            .scalars()
            .all()
        )
        await self.mark_notifications_read(ids, account_type=account_type, account_id=account_id)

    def _notification_visible_filter(self, account_type: str, account_id: str):
        return and_(
            MsgNotification.status == NotificationStatus.PUBLISHED.value,
            or_(MsgNotification.publish_at.is_(None), MsgNotification.publish_at <= datetime.now(UTC)),
            or_(
                and_(
                    MsgNotification.target_scope == MessageTargetScope.ALL.value,
                    or_(MsgNotification.target_account_type.is_(None), MsgNotification.target_account_type == account_type),
                ),
                and_(
                    MsgNotification.target_scope == MessageTargetScope.SPECIFIC.value,
                    MsgNotification.target_account_type == account_type,
                    MsgNotification.target_account_id == account_id,
                ),
            ),
        )

