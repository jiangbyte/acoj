from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.message.enums import NotificationStatus
from app.modules.message.notification.schema import (
    MarkNotificationReadRequest,
    MyNotificationPageQuery,
    NotificationAdminPageQuery,
    NotificationCreateRequest,
    NotificationSchema,
    NotificationUpdateRequest,
)
from app.modules.message.notification.service import NotificationService

admin_router = APIRouter()
portal_router = APIRouter()


def register_current_user_routes(router: APIRouter, account_type: AccountType) -> None:
    dependencies = [Depends(require_account_type(account_type))]

    @router.get(
        "/message/notifications/my-page",
        dependencies=dependencies,
        response_model=ApiResponse[PageData[NotificationSchema]],
    )
    async def my_notifications(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
        current: Current = 1,
        size: Size = 20,
        unread_only: bool = False,
    ) -> ApiResponse[PageData[NotificationSchema]]:
        query = MyNotificationPageQuery(
            pagination=PageQuery(current=current, size=size),
            unread_only=unread_only,
        )
        return success(await NotificationService(db).page_my_notifications(query, session))

    @router.get(
        "/message/notifications/my-detail",
        dependencies=dependencies,
        response_model=ApiResponse[NotificationSchema],
    )
    async def my_notification_detail(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
        id: Annotated[Id, Query()],
    ) -> ApiResponse[NotificationSchema]:
        return success(await NotificationService(db).my_notification_detail(IdQuery(id=id), session))

    @router.post(
        "/message/notifications/read",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def read_notifications(
        payload: MarkNotificationReadRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await NotificationService(db).mark_notifications_read(payload, session)
        return success()

    @router.post(
        "/message/notifications/read-all",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def read_all_notifications(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await NotificationService(db).mark_all_notifications_read(session)
        return success()


register_current_user_routes(admin_router, AccountType.ADMIN)
register_current_user_routes(portal_router, AccountType.PORTAL)


@admin_router.post(
    "/message/notifications/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:create")),
    ],
    response_model=ApiResponse[None],
)
async def create_notification(
    payload: NotificationCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[None]:
    await NotificationService(db).create_notification(payload, session)
    return success()


@admin_router.post(
    "/message/notifications/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:update")),
    ],
    response_model=ApiResponse[None],
)
async def update_notification(
    payload: NotificationUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await NotificationService(db).update_notification(payload)
    return success()


@admin_router.post(
    "/message/notifications/publish",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:publish")),
    ],
    response_model=ApiResponse[None],
)
async def publish_notification(
    payload: IdQuery,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await NotificationService(db).publish_notification(payload)
    return success()


@admin_router.post(
    "/message/notifications/revoke",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:revoke")),
    ],
    response_model=ApiResponse[None],
)
async def revoke_notification(
    payload: IdQuery,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await NotificationService(db).revoke_notification(payload)
    return success()


@admin_router.post(
    "/message/notifications/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete_notification(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await NotificationService(db).delete_notifications(payload)
    return success()


@admin_router.get(
    "/message/notifications/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:detail")),
    ],
    response_model=ApiResponse[NotificationSchema],
)
async def notification_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[NotificationSchema]:
    return success(await NotificationService(db).notification_detail(IdQuery(id=id)))


@admin_router.get(
    "/message/notifications/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:notification:page")),
    ],
    response_model=ApiResponse[PageData[NotificationSchema]],
)
async def notification_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    title: str | None = Query(default=None, max_length=255),
    status: NotificationStatus | None = None,
    target_account_type: AccountType | None = None,
) -> ApiResponse[PageData[NotificationSchema]]:
    query = NotificationAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        title=title,
        status=status,
        target_account_type=target_account_type,
    )
    return success(await NotificationService(db).page_notifications_admin(query))
