from datetime import datetime, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import to_schema, to_schema_list
from app.core.security.session import SessionPayload
from app.platform.db.transaction import transactional
from app.modules.message.message.model import MsgMessage
from app.modules.message.message.repository import MessageRepository
from app.modules.message.message.schema import (
    MessagePageQuery,
    MessageReadRequest,
    MessageSchema,
    MessageAttachmentSchema,
    RevokeMessageRequest,
    SendMessageRequest,
    UnreadCountResponse,
)
from app.modules.message.conversation.service import MsgConversationService
from app.modules.message.terminal.repository import MsgTerminalRepository


def _message_schema(item: MsgMessage, attachments: list) -> MessageSchema:
    schema = to_schema(MessageSchema, item)
    schema.attachments = [to_schema(MessageAttachmentSchema, att) for att in attachments]
    return schema


class MessageService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = MessageRepository(db)

    async def send(self, payload: SendMessageRequest, session: SessionPayload) -> MessageSchema:
        """Send a message. Auto-creates conversation if needed (direct with participant_refs)."""
        async with transactional(self.db):
            conversation_id = payload.conversation_id
            if not conversation_id and payload.group_id:
                from app.modules.message.conversation.model import MsgConversation
                stmt = select(MsgConversation).where(
                    MsgConversation.group_id == payload.group_id,
                    MsgConversation.status == "ACTIVE",
                )
                conv = (await self.db.execute(stmt)).scalar_one_or_none()
                if conv is None:
                    raise BusinessError("No active conversation found for this group")
                conversation_id = conv.id

            if not conversation_id:
                from app.modules.message.conversation.service import MsgConversationService
                conv_service = MsgConversationService(self.db)
                participants = [{"account_type": str(session.account_type), "account_id": session.account_id}]
                for ref in (payload.participant_refs or []):
                    participants.append({"account_type": ref.get("account_type", "PORTAL"), "account_id": ref.get("account_id")})
                conv = await conv_service.find_or_create_direct(participants, session)
                conversation_id = conv.id

            msg = await self.repo.create_message(
                payload, conversation_id,
                sender_account_type=str(session.account_type),
                sender_account_id=session.account_id,
                sender_type="USER",
            )

            # Update conversation last_message
            from app.modules.message.conversation.repository import MsgConversationRepository
            conv_repo = MsgConversationRepository(self.db)
            await conv_repo.update_last_message(conversation_id, msg.id, msg.created_at)
            # Increment unread for other participants
            await conv_repo.increment_unread(conversation_id, str(session.account_type), session.account_id)

            attachments = await self.repo.map_attachments([msg.id])
            return _message_schema(msg, attachments.get(msg.id, []))

    async def reply(self, payload: SendMessageRequest, session: SessionPayload) -> MessageSchema:
        if not payload.parent_id:
            raise BusinessError("parent_id is required for reply")
        return await self.send(payload, session)

    async def revoke(self, payload: RevokeMessageRequest, session: SessionPayload) -> None:
        async with transactional(self.db):
            msg = await self.repo.get_required(payload.message_id)
            if msg.is_revoked:
                raise BusinessError("Message already revoked")
            if msg.sender_account_type != str(session.account_type) or msg.sender_account_id != session.account_id:
                raise BusinessError("Can only revoke your own messages")
            await self.repo.revoke_message(payload.message_id)

    async def page_messages(self, query: MessagePageQuery, session: SessionPayload | None = None) -> PageData[MessageSchema]:
        """Paginate messages in a conversation, newest first."""
        if session:
            from app.modules.message.conversation.repository import MsgConversationRepository
            member = await MsgConversationRepository(self.db).get_member(
                query.conversation_id, str(session.account_type), session.account_id
            )
            if member is None:
                raise BusinessError("Not a participant of this conversation")
        items, total = await self.repo.page_messages(query.conversation_id, query.pagination.offset, query.pagination.size)
        attachment_map = await self.repo.map_attachments([m.id for m in items])
        schemas = []
        for item in items:
            schemas.append(_message_schema(item, attachment_map.get(item.id, [])))
        return build_page(query.pagination, total, schemas)

    async def mark_read(self, payload: MessageReadRequest, session: SessionPayload) -> None:
        """Mark conversation as read. Finds the latest message and uses it as cursor."""
        async with transactional(self.db):
            items, _ = await self.repo.page_messages(payload.conversation_id, 0, 1)
            if not items:
                return
            latest = items[0]
            await self.repo.mark_read(
                payload.conversation_id,
                str(session.account_type),
                session.account_id,
                latest.id,
                terminal_id=payload.terminal_id,
            )
            from app.modules.message.conversation.repository import MsgConversationRepository
            await MsgConversationRepository(self.db).reset_unread(
                payload.conversation_id, str(session.account_type), session.account_id
            )

    async def unread_count(self, conversation_id: str, session: SessionPayload) -> UnreadCountResponse:
        count = await self.repo.count_unread(conversation_id, str(session.account_type), session.account_id)
        return UnreadCountResponse(unread_count=count)
