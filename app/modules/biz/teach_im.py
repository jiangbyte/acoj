"""Shared IM helpers for teaching domain (class / team bound MsgGroup)."""

from __future__ import annotations

from datetime import datetime, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.modules.message.conversation.model import MsgConversation, MsgConversationMember
from app.modules.message.enums import (
    ConversationMemberRole,
    ConversationStatus,
    ConversationType,
    GroupJoinMode,
    GroupStatus,
)
from app.modules.message.group.repository import MsgGroupRepository
from app.modules.message.group.schema import MsgGroupCreateRequest


async def create_bound_group(
    db: AsyncSession,
    name: str,
    owner_account_type: str,
    owner_account_id: str,
    source: str,
    source_id: str,
    max_members: int = 500,
) -> str:
    """Create MsgGroup + conversation bound to a teaching entity."""
    repo = MsgGroupRepository(db)
    group = await repo.create(
        MsgGroupCreateRequest(
            name=name,
            avatar=None,
            description=None,
            owner_account_type=owner_account_type,
            owner_account_id=owner_account_id,
            status=GroupStatus.ENABLED.value,
            join_mode=GroupJoinMode.INVITE_ONLY.value,
            max_members=max_members,
            member_count=1,
            extra={"source": source, "source_id": source_id},
        )
    )
    await repo.add_member(
        group_id=group.id,
        account_type=owner_account_type,
        account_id=owner_account_id,
        role=ConversationMemberRole.OWNER.value,
    )
    conversation = MsgConversation(
        conversation_type=ConversationType.GROUP.value,
        title=name,
        group_id=group.id,
        owner_account_type=owner_account_type,
        owner_account_id=owner_account_id,
        status=ConversationStatus.ACTIVE.value,
    )
    db.add(conversation)
    await db.flush()

    member = MsgConversationMember(
        conversation_id=conversation.id,
        account_type=owner_account_type,
        account_id=owner_account_id,
        role=ConversationMemberRole.OWNER.value,
        joined_at=datetime.now(timezone.utc),
    )
    db.add(member)
    await db.flush()
    return group.id


async def add_portal_member(db: AsyncSession, group_id: str, account_id: str) -> None:
    """Add a PORTAL user to bound group + conversation if not already active."""
    repo = MsgGroupRepository(db)
    account_type = AccountType.PORTAL.value
    existing = await repo.get_member(group_id, account_type, account_id)
    if existing is not None:
        return
    await repo.add_member(
        group_id=group_id,
        account_type=account_type,
        account_id=account_id,
        role=ConversationMemberRole.MEMBER.value,
    )
    await repo.increment_member_count(group_id)
    await _add_member_to_conversation(db, group_id, account_type, account_id)


async def remove_portal_member(db: AsyncSession, group_id: str, account_id: str) -> None:
    """Soft-remove a PORTAL user from bound group + conversation."""
    repo = MsgGroupRepository(db)
    account_type = AccountType.PORTAL.value
    member = await repo.get_member(group_id, account_type, account_id)
    if member is None:
        return
    await repo.remove_member(group_id, account_type, account_id)
    await repo.decrement_member_count(group_id)
    await _remove_member_from_conversation(db, group_id, account_type, account_id)


async def get_conversation_id(db: AsyncSession, group_id: str) -> str | None:
    """Return active GROUP conversation id for a MsgGroup, or None."""
    stmt = select(MsgConversation.id).where(
        MsgConversation.group_id == group_id,
        MsgConversation.conversation_type == ConversationType.GROUP.value,
        MsgConversation.status == ConversationStatus.ACTIVE.value,
    )
    return (await db.execute(stmt)).scalar_one_or_none()


async def _get_group_conversation(db: AsyncSession, group_id: str) -> MsgConversation | None:
    stmt = select(MsgConversation).where(
        MsgConversation.group_id == group_id,
        MsgConversation.conversation_type == ConversationType.GROUP.value,
        MsgConversation.status == ConversationStatus.ACTIVE.value,
    )
    return (await db.execute(stmt)).scalar_one_or_none()


async def _add_member_to_conversation(
    db: AsyncSession,
    group_id: str,
    account_type: str,
    account_id: str,
    role: str = ConversationMemberRole.MEMBER.value,
) -> None:
    conversation = await _get_group_conversation(db, group_id)
    if conversation is None:
        return
    existing = select(MsgConversationMember).where(
        MsgConversationMember.conversation_id == conversation.id,
        MsgConversationMember.account_type == account_type,
        MsgConversationMember.account_id == account_id,
        MsgConversationMember.left_at.is_(None),
    )
    if (await db.execute(existing)).scalar_one_or_none() is not None:
        return
    db.add(
        MsgConversationMember(
            conversation_id=conversation.id,
            account_type=account_type,
            account_id=account_id,
            role=role,
            joined_at=datetime.now(timezone.utc),
        )
    )


async def _remove_member_from_conversation(
    db: AsyncSession, group_id: str, account_type: str, account_id: str
) -> None:
    from app.modules.message.conversation.repository import MsgConversationRepository

    conversation = await _get_group_conversation(db, group_id)
    if conversation is None:
        return
    await MsgConversationRepository(db).remove_member(conversation.id, account_type, account_id)
