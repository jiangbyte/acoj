from sqlalchemy.ext.asyncio import AsyncSession

from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema
from app.core.security.session import SessionPayload
from app.modules.message.enums import TodoAssigneeStatus
from app.modules.message.todo.model import MsgTodo
from app.modules.message.todo.repository import TodoRepository
from app.modules.message.todo.schema import (
    MyTodoPageQuery,
    TodoAdminPageQuery,
    TodoCreateRequest,
    TodoSchema,
    TodoStatusRequest,
    TodoUpdateRequest,
)
from app.platform.db.transaction import transactional


class TodoService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = TodoRepository(db)

    async def create_todo(self, payload: TodoCreateRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.create_todo(
                payload,
                creator_account_type=str(session.account_type),
                creator_account_id=session.account_id,
            )

    async def update_todo(self, payload: TodoUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update_todo(payload)

    async def delete_todos(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_todos(payload.ids)

    async def cancel_todo_admin(self, payload: IdQuery) -> None:
        async with transactional(self.db):
            await self.repo.cancel_todo(payload.id)

    async def todo_detail(self, payload: IdQuery) -> TodoSchema:
        return _todo_schema(await self.repo.get_todo_required(payload.id), None)

    async def page_todos_admin(self, query: TodoAdminPageQuery) -> PageData[TodoSchema]:
        items, total = await self.repo.page_todos_admin(query)
        return build_page(query.pagination, total, [_todo_schema(item, None) for item in items])

    async def page_my_todos(self, query: MyTodoPageQuery, session: SessionPayload) -> PageData[TodoSchema]:
        items, total, status_map = await self.repo.page_my_todos(
            query,
            account_type=str(session.account_type),
            account_id=session.account_id,
        )
        return build_page(query.pagination, total, [_todo_schema(item, status_map.get(item.id)) for item in items])

    async def start_todo(self, payload: TodoStatusRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.update_my_todo_status(
                todo_id=payload.todo_id,
                account_type=str(session.account_type),
                account_id=session.account_id,
                status=TodoAssigneeStatus.IN_PROGRESS,
            )

    async def complete_todo(self, payload: TodoStatusRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.update_my_todo_status(
                todo_id=payload.todo_id,
                account_type=str(session.account_type),
                account_id=session.account_id,
                status=TodoAssigneeStatus.COMPLETED,
            )

    async def cancel_todo(self, payload: TodoStatusRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.update_my_todo_status(
                todo_id=payload.todo_id,
                account_type=str(session.account_type),
                account_id=session.account_id,
                status=TodoAssigneeStatus.CANCELLED,
            )


def _todo_schema(item: MsgTodo, assignee_status: str | None) -> TodoSchema:
    schema = to_schema(TodoSchema, item)
    schema.assignee_status = assignee_status
    return schema

