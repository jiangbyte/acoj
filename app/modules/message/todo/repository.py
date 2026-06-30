from datetime import UTC, datetime

from sqlalchemy import and_, delete, func, or_, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import ConflictError, NotFoundError
from app.modules.message.enums import MessageTargetScope, TodoAssigneeStatus, TodoStatus
from app.modules.message.message.schema import AccountRef
from app.modules.message.todo.model import MsgTodo, MsgTodoAssignee
from app.modules.message.todo.schema import (
    MyTodoPageQuery,
    TodoAdminPageQuery,
    TodoCreateRequest,
    TodoUpdateRequest,
)


class TodoRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_todo_required(self, todo_id: str) -> MsgTodo:
        entity = await self.db.get(MsgTodo, todo_id)
        if entity is None:
            raise NotFoundError("Todo not found")
        return entity

    async def create_todo(
        self,
        payload: TodoCreateRequest,
        *,
        creator_account_type: str | None,
        creator_account_id: str | None,
    ) -> MsgTodo:
        todo = MsgTodo(
            **payload.model_dump(exclude={"assignee_refs"}),
            creator_account_type=creator_account_type,
            creator_account_id=creator_account_id,
        )
        self.db.add(todo)
        await self.db.flush()
        refs = payload.assignee_refs[:]
        if payload.target_scope == MessageTargetScope.SPECIFIC and payload.target_account_type and payload.target_account_id:
            refs.append(AccountRef(account_type=payload.target_account_type, account_id=payload.target_account_id))
        await self.add_todo_assignees(todo.id, refs)
        return todo

    async def update_todo(self, payload: TodoUpdateRequest) -> MsgTodo:
        todo = await self.get_todo_required(payload.id)
        data = payload.model_dump(exclude={"id", "assignee_refs"})
        for key, value in data.items():
            setattr(todo, key, value)
        await self.add_todo_assignees(todo.id, payload.assignee_refs)
        await self.db.flush()
        return todo

    async def cancel_todo(self, todo_id: str) -> None:
        todo = await self.get_todo_required(todo_id)
        todo.status = TodoStatus.CANCELLED.value
        await self.db.execute(
            update(MsgTodoAssignee)
            .where(MsgTodoAssignee.todo_id == todo_id, MsgTodoAssignee.status != TodoAssigneeStatus.COMPLETED.value)
            .values(status=TodoAssigneeStatus.CANCELLED.value, cancelled_at=datetime.now(UTC))
        )

    async def delete_todos(self, ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(ids))
        await self.db.execute(delete(MsgTodoAssignee).where(MsgTodoAssignee.todo_id.in_(unique_ids)))
        result = await self.db.execute(delete(MsgTodo).where(MsgTodo.id.in_(unique_ids)))
        if result.rowcount != len(unique_ids):
            raise NotFoundError("Todo not found")

    async def add_todo_assignees(self, todo_id: str, refs: list[AccountRef]) -> None:
        unique = {(ref.account_type.value, ref.account_id) for ref in refs}
        if not unique:
            return
        existing = set(
            (
                await self.db.execute(
                    select(MsgTodoAssignee.account_type, MsgTodoAssignee.account_id).where(
                        MsgTodoAssignee.todo_id == todo_id,
                        tuple_account_filter(MsgTodoAssignee.account_type, MsgTodoAssignee.account_id, unique),
                    )
                )
            ).all()
        )
        self.db.add_all(
            [
                MsgTodoAssignee(todo_id=todo_id, account_type=account_type, account_id=account_id)
                for account_type, account_id in unique
                if (account_type, account_id) not in existing
            ]
        )
        await self.db.flush()

    async def page_todos_admin(self, query: TodoAdminPageQuery) -> tuple[list[MsgTodo], int]:
        stmt = select(MsgTodo)
        count_stmt = select(func.count(MsgTodo.id))
        filters = []
        if query.title:
            filters.append(MsgTodo.title.contains(query.title))
        if query.status:
            filters.append(MsgTodo.status == query.status.value)
        if query.target_account_type:
            filters.append(MsgTodo.target_account_type == query.target_account_type.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = stmt.order_by(MsgTodo.updated_at.desc(), MsgTodo.id.desc()).offset(query.pagination.offset).limit(
            query.pagination.size
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def page_my_todos(
        self,
        query: MyTodoPageQuery,
        *,
        account_type: str,
        account_id: str,
    ) -> tuple[list[MsgTodo], int, dict[str, str | None]]:
        stmt = select(MsgTodo, MsgTodoAssignee.status).outerjoin(
            MsgTodoAssignee,
            and_(
                MsgTodoAssignee.todo_id == MsgTodo.id,
                MsgTodoAssignee.account_type == account_type,
                MsgTodoAssignee.account_id == account_id,
            ),
        )
        count_stmt = select(func.count(MsgTodo.id)).outerjoin(
            MsgTodoAssignee,
            and_(
                MsgTodoAssignee.todo_id == MsgTodo.id,
                MsgTodoAssignee.account_type == account_type,
                MsgTodoAssignee.account_id == account_id,
            ),
        )
        visible_filter = self._todo_visible_filter(account_type, account_id)
        stmt = stmt.where(visible_filter)
        count_stmt = count_stmt.where(visible_filter)
        if not query.include_done:
            done = [TodoAssigneeStatus.COMPLETED.value, TodoAssigneeStatus.CANCELLED.value]
            active = [TodoStatus.PENDING.value, TodoStatus.IN_PROGRESS.value]
            stmt = stmt.where(MsgTodo.status.in_(active), or_(MsgTodoAssignee.status.is_(None), MsgTodoAssignee.status.not_in(done)))
            count_stmt = count_stmt.where(
                MsgTodo.status.in_(active),
                or_(MsgTodoAssignee.status.is_(None), MsgTodoAssignee.status.not_in(done)),
            )
        stmt = stmt.order_by(MsgTodo.due_at.asc().nullslast(), MsgTodo.updated_at.desc()).offset(query.pagination.offset).limit(
            query.pagination.size
        )
        rows = (await self.db.execute(stmt)).all()
        todos = [row[0] for row in rows]
        status_map = {row[0].id: row[1] for row in rows}
        total = (await self.db.execute(count_stmt)).scalar_one()
        return todos, total, status_map

    async def count_my_todos(self, *, account_type: str, account_id: str) -> int:
        stmt = select(func.count(MsgTodo.id)).outerjoin(
            MsgTodoAssignee,
            and_(
                MsgTodoAssignee.todo_id == MsgTodo.id,
                MsgTodoAssignee.account_type == account_type,
                MsgTodoAssignee.account_id == account_id,
            ),
        )
        stmt = stmt.where(
            self._todo_visible_filter(account_type, account_id),
            MsgTodo.status == TodoStatus.PENDING.value,
            or_(MsgTodoAssignee.status.is_(None), MsgTodoAssignee.status == TodoAssigneeStatus.PENDING.value),
        )
        return int((await self.db.execute(stmt)).scalar_one())

    async def update_my_todo_status(
        self,
        *,
        todo_id: str,
        account_type: str,
        account_id: str,
        status: TodoAssigneeStatus,
    ) -> None:
        todo = await self.get_todo_required(todo_id)
        stmt = select(MsgTodoAssignee).where(
            MsgTodoAssignee.todo_id == todo_id,
            MsgTodoAssignee.account_type == account_type,
            MsgTodoAssignee.account_id == account_id,
        )
        assignee = (await self.db.execute(stmt)).scalar_one_or_none()
        if assignee is None:
            if todo.target_scope != MessageTargetScope.ALL.value:
                raise ConflictError("Todo is not assigned to current account")
            assignee = MsgTodoAssignee(todo_id=todo_id, account_type=account_type, account_id=account_id)
            self.db.add(assignee)
        now = datetime.now(UTC)
        assignee.status = status.value
        assignee.read_at = assignee.read_at or now
        if status == TodoAssigneeStatus.IN_PROGRESS:
            assignee.started_at = assignee.started_at or now
        elif status == TodoAssigneeStatus.COMPLETED:
            assignee.completed_at = now
        elif status == TodoAssigneeStatus.CANCELLED:
            assignee.cancelled_at = now
        await self.db.flush()

    def _todo_visible_filter(self, account_type: str, account_id: str):
        return or_(
            and_(
                MsgTodo.target_scope == MessageTargetScope.ALL.value,
                or_(MsgTodo.target_account_type.is_(None), MsgTodo.target_account_type == account_type),
            ),
            and_(
                MsgTodo.target_scope == MessageTargetScope.SPECIFIC.value,
                or_(
                    and_(MsgTodo.target_account_type == account_type, MsgTodo.target_account_id == account_id),
                    and_(MsgTodoAssignee.account_type == account_type, MsgTodoAssignee.account_id == account_id),
                ),
            ),
        )


def tuple_account_filter(account_type_column, account_id_column, refs: set[tuple[str, str]]):
    if not refs:
        return False
    return or_(*[and_(account_type_column == account_type, account_id_column == account_id) for account_type, account_id in refs])

