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
from app.modules.message.enums import TodoStatus
from app.modules.message.todo.schema import (
    MyTodoPageQuery,
    TodoAdminPageQuery,
    TodoCreateRequest,
    TodoSchema,
    TodoStatusRequest,
    TodoUpdateRequest,
)
from app.modules.message.todo.service import TodoService

admin_router = APIRouter()
portal_router = APIRouter()


def register_current_user_routes(router: APIRouter, account_type: AccountType) -> None:
    dependencies = [Depends(require_account_type(account_type))]

    @router.get(
        "/message/todos/my-page",
        dependencies=dependencies,
        response_model=ApiResponse[PageData[TodoSchema]],
    )
    async def my_todos(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
        current: Current = 1,
        size: Size = 20,
        include_done: bool = False,
    ) -> ApiResponse[PageData[TodoSchema]]:
        query = MyTodoPageQuery(
            pagination=PageQuery(current=current, size=size),
            include_done=include_done,
        )
        return success(await TodoService(db).page_my_todos(query, session))

    @router.post(
        "/message/todos/start",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def start_todo(
        payload: TodoStatusRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await TodoService(db).start_todo(payload, session)
        return success()

    @router.post(
        "/message/todos/complete",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def complete_todo(
        payload: TodoStatusRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await TodoService(db).complete_todo(payload, session)
        return success()

    @router.post(
        "/message/todos/cancel",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def cancel_todo(
        payload: TodoStatusRequest,
        db: Annotated[AsyncSession, Depends(get_db_session)],
        session: Annotated[SessionPayload, Depends(get_current_session)],
    ) -> ApiResponse[None]:
        await TodoService(db).cancel_todo(payload, session)
        return success()


register_current_user_routes(admin_router, AccountType.ADMIN)
register_current_user_routes(portal_router, AccountType.PORTAL)


@admin_router.post(
    "/message/todos/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:todo:create")),
    ],
    response_model=ApiResponse[None],
)
async def create_todo(
    payload: TodoCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[None]:
    await TodoService(db).create_todo(payload, session)
    return success()


@admin_router.post(
    "/message/todos/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:todo:update")),
    ],
    response_model=ApiResponse[None],
)
async def update_todo(
    payload: TodoUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await TodoService(db).update_todo(payload)
    return success()


@admin_router.post(
    "/message/todos/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:todo:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete_todo(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await TodoService(db).delete_todos(payload)
    return success()


@admin_router.post(
    "/message/todos/cancel-admin",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:todo:cancel")),
    ],
    response_model=ApiResponse[None],
)
async def cancel_todo_admin(
    payload: IdQuery,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await TodoService(db).cancel_todo_admin(payload)
    return success()


@admin_router.get(
    "/message/todos/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:todo:detail")),
    ],
    response_model=ApiResponse[TodoSchema],
)
async def todo_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[TodoSchema]:
    return success(await TodoService(db).todo_detail(IdQuery(id=id)))


@admin_router.get(
    "/message/todos/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("message:todo:page")),
    ],
    response_model=ApiResponse[PageData[TodoSchema]],
)
async def todo_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    title: str | None = Query(default=None, max_length=255),
    status: TodoStatus | None = None,
    target_account_type: AccountType | None = None,
) -> ApiResponse[PageData[TodoSchema]]:
    query = TodoAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        title=title,
        status=status,
        target_account_type=target_account_type,
    )
    return success(await TodoService(db).page_todos_admin(query))
