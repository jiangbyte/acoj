from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema
from app.core.security.session import SessionPayload
from app.modules.message.enums import MessageSenderType
from app.modules.message.message.model import MsgMessage, MsgThread
from app.modules.message.message.repository import MessageRepository
from app.modules.message.message.schema import (
    GroupCreateRequest,
    GroupMemberRequest,
    GroupPageQuery,
    GroupSchema,
    GroupMemberSchema,
    GroupUpdateRequest,
    MessageAttachmentSchema,
    MessageReactionSummary,
    MessageSchema,
    ReactMessageRequest,
    ReadThreadRequest,
    SendMessageRequest,
    ThreadMessagePageQuery,
    ThreadPageQuery,
    ThreadSchema,
)
from app.platform.db.transaction import transactional


class MessageService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = MessageRepository(db)

    async def create_group(self, payload: GroupCreateRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.create_group(
                payload,
                owner_account_type=str(session.account_type),
                owner_account_id=session.account_id,
            )

    async def update_group(self, payload: GroupUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update_group(payload)

    async def delete_groups(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_groups(payload.ids)

    async def add_group_members(self, payload: GroupMemberRequest) -> None:
        async with transactional(self.db):
            await self.repo.add_group_members(payload.group_id, payload.member_refs)

    async def remove_group_members(self, payload: GroupMemberRequest) -> None:
        async with transactional(self.db):
            await self.repo.remove_group_members(payload.group_id, payload.member_refs)

    async def page_groups(self, query: GroupPageQuery) -> PageData[GroupSchema]:
        items, total, counts = await self.repo.page_groups(query)
        return build_page(query.pagination, total, [_group_schema(item, counts.get(item.id, 0)) for item in items])

    async def list_my_groups(self, session: SessionPayload) -> list[GroupSchema]:
        items = await self.repo.list_my_groups(account_type=str(session.account_type), account_id=session.account_id)
        counts = await self.repo.count_group_members([item.id for item in items])
        return [_group_schema(item, counts.get(item.id, 0)) for item in items]

    async def group_detail(self, payload: IdQuery) -> GroupSchema:
        group = await self.repo.get_group_required(payload.id)
        counts = await self.repo.count_group_members([group.id])
        return _group_schema(group, counts.get(group.id, 0))

    async def group_members(self, payload: IdQuery) -> list[GroupMemberSchema]:
        return [to_schema(GroupMemberSchema, item) for item in await self.repo.list_group_members(payload.id)]

    async def send_message(self, payload: SendMessageRequest, session: SessionPayload) -> MessageSchema:
        async with transactional(self.db):
            thread = await self.repo.ensure_send_thread(
                payload,
                sender_account_type=str(session.account_type),
                sender_account_id=session.account_id,
            )
            message = await self.repo.send_message(
                thread,
                payload,
                sender_account_type=str(session.account_type),
                sender_account_id=session.account_id,
            )
            attachments = await self.repo.map_message_attachments([message.id])
            return _message_schema(message, attachments.get(message.id, []), [])

    async def send_system_message(self, payload: SendMessageRequest) -> MessageSchema:
        async with transactional(self.db):
            if payload.thread_id:
                thread = await self.repo.get_thread_required(payload.thread_id)
            else:
                thread = await self.repo.ensure_send_thread(
                    payload,
                    sender_account_type="ADMIN",
                    sender_account_id="0",
                )
            message = await self.repo.send_message(
                thread,
                payload,
                sender_account_type=None,
                sender_account_id=None,
                sender_type=MessageSenderType.SYSTEM.value,
            )
            attachments = await self.repo.map_message_attachments([message.id])
            return _message_schema(message, attachments.get(message.id, []), [])

    async def reply_message(self, payload: SendMessageRequest, session: SessionPayload) -> MessageSchema:
        if not payload.parent_id:
            raise BusinessError("parent_id is required")
        return await self.send_message(payload, session)

    async def page_my_threads(self, query: ThreadPageQuery, session: SessionPayload) -> PageData[ThreadSchema]:
        items, total, unread_map, latest_map = await self.repo.page_my_threads(
            query,
            account_type=str(session.account_type),
            account_id=session.account_id,
        )
        return build_page(
            query.pagination,
            total,
            [_thread_schema(item, unread_map.get(item.id, 0), latest_map.get(item.last_message_id or "")) for item in items],
        )

    async def page_all_threads(self, query: ThreadPageQuery) -> PageData[ThreadSchema]:
        items, total, latest_map = await self.repo.page_all_threads(query)
        return build_page(
            query.pagination,
            total,
            [_thread_schema(item, 0, latest_map.get(item.last_message_id or "")) for item in items],
        )

    async def page_thread_messages(
        self,
        query: ThreadMessagePageQuery,
        session: SessionPayload | None = None,
    ) -> PageData[MessageSchema]:
        if session:
            await self.repo.ensure_thread_participant(
                query.thread_id,
                account_type=str(session.account_type),
                account_id=session.account_id,
            )
        items, total, attachment_map, reaction_map = await self.repo.page_thread_messages(
            thread_id=query.thread_id,
            pagination=query.pagination,
            account_type=str(session.account_type) if session else None,
            account_id=session.account_id if session else None,
        )
        return build_page(
            query.pagination,
            total,
            [
                _message_schema(
                    item,
                    attachment_map.get(item.id, []),
                    reaction_map.get(item.id, []),
                )
                for item in items
            ],
        )

    async def read_thread(self, payload: ReadThreadRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.mark_thread_read(
                thread_id=payload.thread_id,
                account_type=str(session.account_type),
                account_id=session.account_id,
            )

    async def react_message(self, payload: ReactMessageRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            await self.repo.react_message(
                message_id=payload.message_id,
                account_type=str(session.account_type),
                account_id=session.account_id,
                reaction=payload.reaction,
            )


def _group_schema(item, member_count: int) -> GroupSchema:
    schema = to_schema(GroupSchema, item)
    schema.member_count = member_count
    return schema


def _thread_schema(item: MsgThread, unread_count: int, last_message: MsgMessage | None) -> ThreadSchema:
    schema = to_schema(ThreadSchema, item)
    schema.unread_count = unread_count
    if last_message:
        schema.last_message = _message_schema(last_message, [], [])
    return schema


def _message_schema(item: MsgMessage, attachments, reactions) -> MessageSchema:
    schema = to_schema(MessageSchema, item)
    schema.attachments = [to_schema(MessageAttachmentSchema, attachment) for attachment in attachments]
    schema.reactions = [
        MessageReactionSummary(reaction=reaction, count=count, reacted=reacted)
        for reaction, count, reacted in reactions
    ]
    return schema

