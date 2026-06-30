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
from app.modules.message.enums import MessageGroupStatus, MessageThreadStatus, MessageThreadType
from app.modules.message.message.schema import (
    GroupCreateRequest,
    GroupMemberRequest,
    GroupPageQuery,
    GroupSchema,
    GroupMemberSchema,
    GroupUpdateRequest,
    MessageSchema,
    ReactMessageRequest,
    ReadThreadRequest,
    SendMessageRequest,
    ThreadMessagePageQuery,
    ThreadPageQuery,
    ThreadSchema,
)
from app.modules.message.message.service import MessageService

admin_router = APIRouter()
portal_router = APIRouter()


def register_current_user_routes(router: APIRouter, account_type: AccountType) -> None:
    dependencies = [Depends(require_account_type(account_type))]

    @router.get(
        "/message/messages/groups",
        dependencies=dependencies,
        response_model=ApiResponse[list[GroupSchema]],
    )
    async def my_groups(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[list[GroupSchema]]:
        return success(await MessageService(db).list_my_groups(session))

    @router.post(
        "/message/messages/groups/create",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def create_my_group(
        payload: GroupCreateRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await MessageService(db).create_group(payload, session)
        return success()

    @router.post(
        "/message/messages/groups/add-members",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def add_my_group_members(
        payload: GroupMemberRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[None]:
        await MessageService(db).add_group_members(payload)
        return success()

    @router.post(
        "/message/messages/groups/remove-members",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def remove_my_group_members(
        payload: GroupMemberRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[None]:
        await MessageService(db).remove_group_members(payload)
        return success()

    @router.get(
        "/message/messages/threads",
        dependencies=dependencies,
        response_model=ApiResponse[PageData[ThreadSchema]],
    )
    async def my_threads(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
        current: Current = 1,
        size: Size = 20,
        thread_type: MessageThreadType | None = None,
        status: MessageThreadStatus | None = None,
    ) -> ApiResponse[PageData[ThreadSchema]]:
        query = ThreadPageQuery(
            pagination=PageQuery(current=current, size=size),
            thread_type=thread_type,
            status=status,
        )
        return success(await MessageService(db).page_my_threads(query, session))

    @router.get(
        "/message/messages/thread-messages",
        dependencies=dependencies,
        response_model=ApiResponse[PageData[MessageSchema]],
    )
    async def thread_messages(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
        thread_id: str = Query(min_length=1, max_length=64),
        current: Current = 1,
        size: Size = 20,
    ) -> ApiResponse[PageData[MessageSchema]]:
        query = ThreadMessagePageQuery(
            pagination=PageQuery(current=current, size=size),
            thread_id=thread_id,
        )
        return success(await MessageService(db).page_thread_messages(query, session))

    @router.post(
        "/message/messages/send",
        dependencies=dependencies,
        response_model=ApiResponse[MessageSchema],
    )
    async def send_message(
        payload: SendMessageRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[MessageSchema]:
        return success(await MessageService(db).send_message(payload, session))

    @router.post(
        "/message/messages/reply",
        dependencies=dependencies,
        response_model=ApiResponse[MessageSchema],
    )
    async def reply_message(
        payload: SendMessageRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[MessageSchema]:
        return success(await MessageService(db).reply_message(payload, session))

    @router.post(
        "/message/messages/read-thread",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def read_thread(
        payload: ReadThreadRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await MessageService(db).read_thread(payload, session)
        return success()

    @router.post(
        "/message/messages/react",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def react_message(
        payload: ReactMessageRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await MessageService(db).react_message(payload, session)
        return success()


register_current_user_routes(admin_router, AccountType.ADMIN)
register_current_user_routes(portal_router, AccountType.PORTAL)


@admin_router.post(
    "/message/groups/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:create")),
    ],
    response_model=ApiResponse[None],
)
async def create_group(
    payload: GroupCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[None]:
    await MessageService(db).create_group(payload, session)
    return success()


@admin_router.post(
    "/message/groups/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:update")),
    ],
    response_model=ApiResponse[None],
)
async def update_group(
    payload: GroupUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await MessageService(db).update_group(payload)
    return success()


@admin_router.post(
    "/message/groups/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete_group(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await MessageService(db).delete_groups(payload)
    return success()


@admin_router.get(
    "/message/groups/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:page")),
    ],
    response_model=ApiResponse[PageData[GroupSchema]],
)
async def group_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    name: str | None = Query(default=None, max_length=128),
    status: MessageGroupStatus | None = None,
) -> ApiResponse[PageData[GroupSchema]]:
    query = GroupPageQuery(pagination=PageQuery(current=current, size=size), name=name, status=status)
    return success(await MessageService(db).page_groups(query))


@admin_router.get(
    "/message/groups/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:detail")),
    ],
    response_model=ApiResponse[GroupSchema],
)
async def group_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[GroupSchema]:
    return success(await MessageService(db).group_detail(IdQuery(id=id)))


@admin_router.get(
    "/message/groups/members",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:detail")),
    ],
    response_model=ApiResponse[list[GroupMemberSchema]],
)
async def group_members(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[list[GroupMemberSchema]]:
    return success(await MessageService(db).group_members(IdQuery(id=id)))


@admin_router.post(
    "/message/groups/add-members",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:update")),
    ],
    response_model=ApiResponse[None],
)
async def group_add_members(
    payload: GroupMemberRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await MessageService(db).add_group_members(payload)
    return success()


@admin_router.post(
    "/message/groups/remove-members",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:group:update")),
    ],
    response_model=ApiResponse[None],
)
async def group_remove_members(
    payload: GroupMemberRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await MessageService(db).remove_group_members(payload)
    return success()


@admin_router.get(
    "/message/threads/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:thread:page")),
    ],
    response_model=ApiResponse[PageData[ThreadSchema]],
)
async def thread_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    thread_type: MessageThreadType | None = None,
    status: MessageThreadStatus | None = None,
) -> ApiResponse[PageData[ThreadSchema]]:
    query = ThreadPageQuery(
        pagination=PageQuery(current=current, size=size),
        thread_type=thread_type,
        status=status,
    )
    return success(await MessageService(db).page_all_threads(query))


@admin_router.get(
    "/message/threads/messages",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:thread:detail")),
    ],
    response_model=ApiResponse[PageData[MessageSchema]],
)
async def admin_thread_messages(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    thread_id: str = Query(min_length=1, max_length=64),
    current: Current = 1,
    size: Size = 20,
) -> ApiResponse[PageData[MessageSchema]]:
    query = ThreadMessagePageQuery(
        pagination=PageQuery(current=current, size=size),
        thread_id=thread_id,
    )
    return success(await MessageService(db).page_thread_messages(query))


@admin_router.post(
    "/message/threads/send-system",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:thread:send")),
    ],
    response_model=ApiResponse[MessageSchema],
)
async def send_system_message(
    payload: SendMessageRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[MessageSchema]:
    return success(await MessageService(db).send_system_message(payload))
