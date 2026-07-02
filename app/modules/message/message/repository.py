from datetime import UTC, datetime

from sqlalchemy import and_, delete, func, or_, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import ConflictError, NotFoundError
from app.modules.message.enums import (
    MessageGroupStatus,
    MessageSenderType,
    MessageThreadType,
)
from app.modules.message.message.model import (
    MsgGroup,
    MsgGroupMember,
    MsgMessage,
    MsgMessageAttachment,
    MsgMessageReaction,
    MsgThread,
    MsgThreadParticipant,
)
from app.modules.message.message.schema import (
    GroupCreateRequest,
    GroupPageQuery,
    GroupUpdateRequest,
    SendMessageRequest,
    ThreadCreateRequest,
    ThreadPageQuery,
)
from app.modules.message.schema import AccountRef


class MessageRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_group_required(self, group_id: str) -> MsgGroup:
        entity = await self.db.get(MsgGroup, group_id)
        if entity is None:
            raise NotFoundError("Message group not found")
        return entity

    async def create_group(
        self,
        payload: GroupCreateRequest,
        *,
        owner_account_type: str | None,
        owner_account_id: str | None,
    ) -> MsgGroup:
        group = MsgGroup(
            name=payload.name,
            avatar=payload.avatar,
            description=payload.description,
            owner_account_type=owner_account_type,
            owner_account_id=owner_account_id,
            extra=payload.extra,
        )
        self.db.add(group)
        await self.db.flush()
        members = payload.member_refs[:]
        if owner_account_type and owner_account_id:
            members.append(
                AccountRef(
                    account_type=AccountType(owner_account_type), account_id=owner_account_id
                )
            )
        await self.add_group_members(group.id, members)
        return group

    async def update_group(self, payload: GroupUpdateRequest) -> MsgGroup:
        group = await self.get_group_required(payload.id)
        data = payload.model_dump(exclude={"id"})
        for key, value in data.items():
            setattr(group, key, value)
        await self.db.flush()
        return group

    async def delete_groups(self, ids: list[str]) -> None:
        unique_ids = list(dict.fromkeys(ids))
        await self.db.execute(delete(MsgGroupMember).where(MsgGroupMember.group_id.in_(unique_ids)))
        result = await self.db.execute(delete(MsgGroup).where(MsgGroup.id.in_(unique_ids)))
        if result.rowcount != len(unique_ids):
            raise NotFoundError("Message group not found")

    async def add_group_members(self, group_id: str, refs: list[AccountRef]) -> None:
        if not refs:
            return
        await self.get_group_required(group_id)
        unique = {(ref.account_type.value, ref.account_id) for ref in refs}
        existing = set(
            (
                await self.db.execute(
                    select(MsgGroupMember.account_type, MsgGroupMember.account_id).where(
                        MsgGroupMember.group_id == group_id,
                        tuple_account_filter(
                            MsgGroupMember.account_type, MsgGroupMember.account_id, unique
                        ),
                    )
                )
            ).all()
        )
        now = datetime.now(UTC)
        self.db.add_all(
            [
                MsgGroupMember(
                    group_id=group_id,
                    account_type=account_type,
                    account_id=account_id,
                    joined_at=now,
                )
                for account_type, account_id in unique
                if (account_type, account_id) not in existing
            ]
        )
        await self.db.flush()

    async def remove_group_members(self, group_id: str, refs: list[AccountRef]) -> None:
        unique = {(ref.account_type.value, ref.account_id) for ref in refs}
        if not unique:
            return
        await self.db.execute(
            update(MsgGroupMember)
            .where(
                MsgGroupMember.group_id == group_id,
                tuple_account_filter(
                    MsgGroupMember.account_type, MsgGroupMember.account_id, unique
                ),
            )
            .values(left_at=datetime.now(UTC))
        )

    async def page_groups(
        self, query: GroupPageQuery
    ) -> tuple[list[MsgGroup], int, dict[str, int]]:
        stmt = select(MsgGroup)
        count_stmt = select(func.count(MsgGroup.id))
        filters = []
        if query.name:
            filters.append(MsgGroup.name.contains(query.name))
        if query.status:
            filters.append(MsgGroup.status == query.status.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(MsgGroup.updated_at.desc(), MsgGroup.id.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        items = list((await self.db.execute(stmt)).scalars().all())
        counts = await self.count_group_members([item.id for item in items])
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total, counts

    async def list_my_groups(self, *, account_type: str, account_id: str) -> list[MsgGroup]:
        stmt = (
            select(MsgGroup)
            .join(MsgGroupMember, MsgGroupMember.group_id == MsgGroup.id)
            .where(
                MsgGroup.status == MessageGroupStatus.ENABLED.value,
                MsgGroupMember.account_type == account_type,
                MsgGroupMember.account_id == account_id,
                MsgGroupMember.left_at.is_(None),
            )
            .order_by(MsgGroup.updated_at.desc(), MsgGroup.id.desc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def count_group_members(self, group_ids: list[str]) -> dict[str, int]:
        if not group_ids:
            return {}
        rows = (
            await self.db.execute(
                select(MsgGroupMember.group_id, func.count(MsgGroupMember.id))
                .where(MsgGroupMember.group_id.in_(group_ids), MsgGroupMember.left_at.is_(None))
                .group_by(MsgGroupMember.group_id)
            )
        ).all()
        return {str(group_id): int(count) for group_id, count in rows}

    async def list_group_members(self, group_id: str) -> list[MsgGroupMember]:
        await self.get_group_required(group_id)
        stmt = (
            select(MsgGroupMember)
            .where(MsgGroupMember.group_id == group_id, MsgGroupMember.left_at.is_(None))
            .order_by(MsgGroupMember.joined_at.asc(), MsgGroupMember.id.asc())
        )
        return list((await self.db.execute(stmt)).scalars().all())

    async def get_thread_required(self, thread_id: str) -> MsgThread:
        entity = await self.db.get(MsgThread, thread_id)
        if entity is None:
            raise NotFoundError("Message thread not found")
        return entity

    async def create_thread(
        self,
        payload: ThreadCreateRequest,
        *,
        created_account_type: str | None,
        created_account_id: str | None,
    ) -> MsgThread:
        thread = MsgThread(
            thread_type=payload.thread_type.value,
            title=payload.title,
            group_id=payload.group_id,
            created_account_type=created_account_type,
            created_account_id=created_account_id,
            extra=payload.extra,
        )
        self.db.add(thread)
        await self.db.flush()
        refs = payload.participant_refs[:]
        if payload.group_id:
            members = await self.list_group_members(payload.group_id)
            refs.extend(
                AccountRef(
                    account_type=AccountType(member.account_type), account_id=member.account_id
                )
                for member in members
            )
        if created_account_type and created_account_id:
            refs.append(
                AccountRef(
                    account_type=AccountType(created_account_type), account_id=created_account_id
                )
            )
        await self.add_thread_participants(thread.id, refs)
        return thread

    async def ensure_send_thread(
        self,
        payload: SendMessageRequest,
        *,
        sender_account_type: str,
        sender_account_id: str,
    ) -> MsgThread:
        if payload.thread_id:
            thread = await self.get_thread_required(payload.thread_id)
            await self.ensure_thread_participant(
                thread.id, account_type=sender_account_type, account_id=sender_account_id
            )
            return thread
        thread_type = MessageThreadType.GROUP if payload.group_id else MessageThreadType.DIRECT
        return await self.create_thread(
            ThreadCreateRequest(
                thread_type=thread_type,
                title=payload.title,
                group_id=payload.group_id,
                participant_refs=payload.participant_refs,
                extra=payload.extra,
            ),
            created_account_type=sender_account_type,
            created_account_id=sender_account_id,
        )

    async def add_thread_participants(self, thread_id: str, refs: list[AccountRef]) -> None:
        unique = {(ref.account_type.value, ref.account_id) for ref in refs}
        if not unique:
            return
        existing = set(
            (
                await self.db.execute(
                    select(
                        MsgThreadParticipant.account_type, MsgThreadParticipant.account_id
                    ).where(
                        MsgThreadParticipant.thread_id == thread_id,
                        tuple_account_filter(
                            MsgThreadParticipant.account_type,
                            MsgThreadParticipant.account_id,
                            unique,
                        ),
                    )
                )
            ).all()
        )
        now = datetime.now(UTC)
        self.db.add_all(
            [
                MsgThreadParticipant(
                    thread_id=thread_id,
                    account_type=account_type,
                    account_id=account_id,
                    joined_at=now,
                )
                for account_type, account_id in unique
                if (account_type, account_id) not in existing
            ]
        )
        await self.db.flush()

    async def ensure_thread_participant(
        self, thread_id: str, *, account_type: str, account_id: str
    ) -> MsgThreadParticipant:
        stmt = select(MsgThreadParticipant).where(
            MsgThreadParticipant.thread_id == thread_id,
            MsgThreadParticipant.account_type == account_type,
            MsgThreadParticipant.account_id == account_id,
            MsgThreadParticipant.left_at.is_(None),
        )
        participant = (await self.db.execute(stmt)).scalar_one_or_none()
        if participant is None:
            raise ConflictError("Message thread is not accessible")
        return participant

    async def send_message(
        self,
        thread: MsgThread,
        payload: SendMessageRequest,
        *,
        sender_account_type: str | None,
        sender_account_id: str | None,
        sender_type: str = MessageSenderType.USER.value,
    ) -> MsgMessage:
        if payload.parent_id:
            parent = await self.db.get(MsgMessage, payload.parent_id)
            if parent is None or parent.thread_id != thread.id:
                raise NotFoundError("Parent message not found")
            parent.reply_count += 1
        message = MsgMessage(
            thread_id=thread.id,
            parent_id=payload.parent_id,
            sender_type=sender_type,
            sender_account_type=sender_account_type,
            sender_account_id=sender_account_id,
            sender_name=payload.sender_name,
            content=payload.content,
            content_type=payload.content_type.value,
            extra=payload.extra,
        )
        self.db.add(message)
        await self.db.flush()
        if payload.attachments:
            self.db.add_all(
                [
                    MsgMessageAttachment(message_id=message.id, **attachment.model_dump())
                    for attachment in payload.attachments
                ]
            )
        now = datetime.now(UTC)
        thread.last_message_id = message.id
        thread.last_message_at = now
        await self.db.execute(
            update(MsgThreadParticipant)
            .where(
                MsgThreadParticipant.thread_id == thread.id,
                or_(
                    MsgThreadParticipant.account_type != sender_account_type,
                    MsgThreadParticipant.account_id != sender_account_id,
                ),
                MsgThreadParticipant.left_at.is_(None),
            )
            .values(unread_count=MsgThreadParticipant.unread_count + 1)
        )
        await self.db.execute(
            update(MsgThreadParticipant)
            .where(
                MsgThreadParticipant.thread_id == thread.id,
                MsgThreadParticipant.account_type == sender_account_type,
                MsgThreadParticipant.account_id == sender_account_id,
                MsgThreadParticipant.left_at.is_(None),
            )
            .values(last_read_message_id=message.id, last_read_at=now)
        )
        await self.db.flush()
        return message

    async def count_unread_messages(self, *, account_type: str, account_id: str) -> int:
        stmt = select(func.coalesce(func.sum(MsgThreadParticipant.unread_count), 0)).where(
            MsgThreadParticipant.account_type == account_type,
            MsgThreadParticipant.account_id == account_id,
            MsgThreadParticipant.left_at.is_(None),
        )
        return int((await self.db.execute(stmt)).scalar_one())

    async def page_my_threads(
        self,
        query: ThreadPageQuery,
        *,
        account_type: str,
        account_id: str,
    ) -> tuple[list[MsgThread], int, dict[str, int], dict[str, MsgMessage]]:
        stmt = select(MsgThread, MsgThreadParticipant.unread_count).join(
            MsgThreadParticipant,
            MsgThreadParticipant.thread_id == MsgThread.id,
        )
        count_stmt = select(func.count(MsgThread.id)).join(
            MsgThreadParticipant,
            MsgThreadParticipant.thread_id == MsgThread.id,
        )
        filters = [
            MsgThreadParticipant.account_type == account_type,
            MsgThreadParticipant.account_id == account_id,
            MsgThreadParticipant.left_at.is_(None),
        ]
        if query.thread_type:
            filters.append(MsgThread.thread_type == query.thread_type.value)
        if query.status:
            filters.append(MsgThread.status == query.status.value)
        stmt = stmt.where(*filters)
        count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(MsgThread.last_message_at.desc().nullslast(), MsgThread.updated_at.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        rows = (await self.db.execute(stmt)).all()
        threads = [row[0] for row in rows]
        unread_map = {row[0].id: int(row[1]) for row in rows}
        latest_map = await self.map_messages(
            [thread.last_message_id for thread in threads if thread.last_message_id]
        )
        total = (await self.db.execute(count_stmt)).scalar_one()
        return threads, total, unread_map, latest_map

    async def page_all_threads(
        self, query: ThreadPageQuery
    ) -> tuple[list[MsgThread], int, dict[str, MsgMessage]]:
        stmt = select(MsgThread)
        count_stmt = select(func.count(MsgThread.id))
        filters = []
        if query.thread_type:
            filters.append(MsgThread.thread_type == query.thread_type.value)
        if query.status:
            filters.append(MsgThread.status == query.status.value)
        if filters:
            stmt = stmt.where(*filters)
            count_stmt = count_stmt.where(*filters)
        stmt = (
            stmt.order_by(MsgThread.last_message_at.desc().nullslast(), MsgThread.updated_at.desc())
            .offset(query.pagination.offset)
            .limit(query.pagination.size)
        )
        threads = list((await self.db.execute(stmt)).scalars().all())
        latest_map = await self.map_messages(
            [thread.last_message_id for thread in threads if thread.last_message_id]
        )
        total = (await self.db.execute(count_stmt)).scalar_one()
        return threads, total, latest_map

    async def page_thread_messages(
        self,
        *,
        thread_id: str,
        pagination,
        account_type: str | None = None,
        account_id: str | None = None,
    ) -> tuple[
        list[MsgMessage],
        int,
        dict[str, list[MsgMessageAttachment]],
        dict[str, list[tuple[str, int, bool]]],
    ]:
        stmt = (
            select(MsgMessage)
            .where(MsgMessage.thread_id == thread_id)
            .order_by(MsgMessage.created_at.desc(), MsgMessage.id.desc())
            .offset(pagination.offset)
            .limit(pagination.size)
        )
        count_stmt = select(func.count(MsgMessage.id)).where(MsgMessage.thread_id == thread_id)
        items = list((await self.db.execute(stmt)).scalars().all())
        ids = [item.id for item in items]
        attachment_map = await self.map_message_attachments(ids)
        reaction_map = await self.map_message_reactions(
            ids, account_type=account_type, account_id=account_id
        )
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total, attachment_map, reaction_map

    async def map_messages(self, ids: list[str]) -> dict[str, MsgMessage]:
        unique_ids = [message_id for message_id in dict.fromkeys(ids) if message_id]
        if not unique_ids:
            return {}
        stmt = select(MsgMessage).where(MsgMessage.id.in_(unique_ids))
        return {message.id: message for message in (await self.db.execute(stmt)).scalars().all()}

    async def map_message_attachments(
        self, message_ids: list[str]
    ) -> dict[str, list[MsgMessageAttachment]]:
        if not message_ids:
            return {}
        rows = (
            (
                await self.db.execute(
                    select(MsgMessageAttachment)
                    .where(MsgMessageAttachment.message_id.in_(message_ids))
                    .order_by(MsgMessageAttachment.sort.asc(), MsgMessageAttachment.id.asc())
                )
            )
            .scalars()
            .all()
        )
        result: dict[str, list[MsgMessageAttachment]] = {}
        for item in rows:
            result.setdefault(item.message_id, []).append(item)
        return result

    async def map_message_reactions(
        self,
        message_ids: list[str],
        *,
        account_type: str | None,
        account_id: str | None,
    ) -> dict[str, list[tuple[str, int, bool]]]:
        if not message_ids:
            return {}
        rows = (
            await self.db.execute(
                select(
                    MsgMessageReaction.message_id,
                    MsgMessageReaction.reaction,
                    func.count(MsgMessageReaction.id),
                )
                .where(MsgMessageReaction.message_id.in_(message_ids))
                .group_by(MsgMessageReaction.message_id, MsgMessageReaction.reaction)
            )
        ).all()
        own: set[tuple[str, str]] = set()
        if account_type and account_id:
            own = set(
                (
                    await self.db.execute(
                        select(MsgMessageReaction.message_id, MsgMessageReaction.reaction).where(
                            MsgMessageReaction.message_id.in_(message_ids),
                            MsgMessageReaction.account_type == account_type,
                            MsgMessageReaction.account_id == account_id,
                        )
                    )
                ).all()
            )
        result: dict[str, list[tuple[str, int, bool]]] = {}
        for message_id, reaction, count in rows:
            result.setdefault(str(message_id), []).append(
                (str(reaction), int(count), (str(message_id), str(reaction)) in own)
            )
        return result

    async def mark_thread_read(self, *, thread_id: str, account_type: str, account_id: str) -> None:
        await self.ensure_thread_participant(
            thread_id, account_type=account_type, account_id=account_id
        )
        thread = await self.get_thread_required(thread_id)
        await self.db.execute(
            update(MsgThreadParticipant)
            .where(
                MsgThreadParticipant.thread_id == thread_id,
                MsgThreadParticipant.account_type == account_type,
                MsgThreadParticipant.account_id == account_id,
            )
            .values(
                unread_count=0,
                last_read_message_id=thread.last_message_id,
                last_read_at=datetime.now(UTC),
            )
        )

    async def react_message(
        self, *, message_id: str, account_type: str, account_id: str, reaction: str
    ) -> None:
        message = await self.db.get(MsgMessage, message_id)
        if message is None:
            raise NotFoundError("Message not found")
        await self.ensure_thread_participant(
            message.thread_id, account_type=account_type, account_id=account_id
        )
        stmt = select(MsgMessageReaction).where(
            MsgMessageReaction.message_id == message_id,
            MsgMessageReaction.account_type == account_type,
            MsgMessageReaction.account_id == account_id,
            MsgMessageReaction.reaction == reaction,
        )
        existing = (await self.db.execute(stmt)).scalar_one_or_none()
        if existing:
            await self.db.delete(existing)
        else:
            self.db.add(
                MsgMessageReaction(
                    message_id=message_id,
                    account_type=account_type,
                    account_id=account_id,
                    reaction=reaction,
                    created_at=datetime.now(UTC),
                )
            )
        await self.db.flush()


def tuple_account_filter(account_type_column, account_id_column, refs: set[tuple[str, str]]):
    if not refs:
        return False
    return or_(
        *[
            and_(account_type_column == account_type, account_id_column == account_id)
            for account_type, account_id in refs
        ]
    )
