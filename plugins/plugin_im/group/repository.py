from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import and_, func, or_, select, update as sa_update
from sqlalchemy.orm import Session

from plugins.plugin_im.group.constants import GroupNormal, MemberActive
from plugins.plugin_im.model.group import Group, GroupJoinRequest, GroupMember, GroupMessage, GroupMessageRead


class GroupRepository:
    def __init__(self, db: Session):
        self.db = db

    def add(self, entity) -> None:
        self.db.add(entity)

    def flush(self) -> None:
        self.db.flush()

    def commit(self) -> None:
        self.db.commit()

    def find_group(self, group_id: str) -> Optional[Group]:
        stmt = select(Group).where(Group.id == group_id)
        return self.db.execute(stmt).scalar_one_or_none()

    def find_active_member(self, group_id: str, user_id: str, user_type: str) -> Optional[GroupMember]:
        stmt = select(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
            GroupMember.status == MemberActive,
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def count_active_members(self, group_id: str) -> int:
        stmt = select(func.count()).select_from(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def count_existing_active_members(self, group_id: str, user_ids: list[str], user_type: str) -> int:
        if not user_ids:
            return 0
        stmt = select(func.count()).select_from(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id.in_(user_ids),
            GroupMember.user_type == user_type,
            GroupMember.status == MemberActive,
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def list_member_group_ids(self, user_id: str, user_type: str) -> list[str]:
        stmt = select(GroupMember.group_id).where(
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
            GroupMember.status == MemberActive,
        )
        return list(self.db.execute(stmt).scalars().all())

    def list_groups_by_ids(self, group_ids: list[str]) -> list[Group]:
        if not group_ids:
            return []
        stmt = select(Group).where(
            Group.id.in_(group_ids),
            Group.status == GroupNormal,
        )
        return list(self.db.execute(stmt).scalars().all())

    def active_member_counts(self, group_ids: list[str]) -> dict[str, int]:
        if not group_ids:
            return {}
        stmt = select(
            GroupMember.group_id,
            func.count(GroupMember.id),
        ).where(
            GroupMember.group_id.in_(group_ids),
            GroupMember.status == MemberActive,
        ).group_by(GroupMember.group_id)
        return {group_id: count for group_id, count in self.db.execute(stmt).all()}

    def update_group(self, group_id: str, updates: dict) -> None:
        if not updates:
            return
        self.db.execute(sa_update(Group).where(Group.id == group_id).values(**updates))

    def dissolve_group(self, group_id: str, updated_at: datetime, left_status: str, dissolved_status: str) -> None:
        self.db.execute(sa_update(Group).where(Group.id == group_id).values(
            status=dissolved_status,
            updated_at=updated_at,
        ))
        self.db.execute(sa_update(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
        ).values(status=left_status))

    def add_member(self, member: GroupMember) -> None:
        self.db.add(member)

    def add_members(self, members: list[GroupMember]) -> None:
        for member in members:
            self.db.add(member)

    def add_messages(self, messages: list[GroupMessage]) -> None:
        for message in messages:
            self.db.add(message)

    def list_members(self, group_id: str) -> list[GroupMember]:
        stmt = select(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
        )
        return list(self.db.execute(stmt).scalars().all())

    def list_other_active_members(self, group_id: str, sender_id: str, sender_type: str) -> list[GroupMember]:
        stmt = select(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.status == MemberActive,
            or_(GroupMember.user_id != sender_id, GroupMember.user_type != sender_type),
        )
        return list(self.db.execute(stmt).scalars().all())

    def create_join_request(self, entity: GroupJoinRequest) -> None:
        self.db.add(entity)

    def count_pending_join_request(self, group_id: str, user_id: str, user_type: str) -> int:
        stmt = select(func.count()).select_from(GroupJoinRequest).where(
            GroupJoinRequest.group_id == group_id,
            GroupJoinRequest.user_id == user_id,
            GroupJoinRequest.user_type == user_type,
            GroupJoinRequest.status == "pending",
        )
        return int(self.db.execute(stmt).scalar() or 0)

    def list_pending_join_requests(self, group_id: str) -> list[GroupJoinRequest]:
        stmt = select(GroupJoinRequest).where(
            GroupJoinRequest.group_id == group_id,
            GroupJoinRequest.status == "pending",
        )
        return list(self.db.execute(stmt).scalars().all())

    def find_join_request(self, request_id: str) -> Optional[GroupJoinRequest]:
        stmt = select(GroupJoinRequest).where(GroupJoinRequest.id == request_id)
        return self.db.execute(stmt).scalar_one_or_none()

    def update_member_status(self, group_id: str, user_id: str, user_type: str, status: str) -> None:
        self.db.execute(sa_update(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
        ).values(status=status))

    def update_member_role(self, group_id: str, user_id: str, user_type: str, role: str) -> None:
        self.db.execute(sa_update(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
        ).values(role=role))

    def update_member_nickname(self, group_id: str, user_id: str, user_type: str, nickname: str) -> None:
        self.db.execute(sa_update(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
        ).values(nickname=nickname))

    def update_member_muted_until(self, group_id: str, user_id: str, user_type: str, muted_until: Optional[datetime]) -> None:
        self.db.execute(sa_update(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
            GroupMember.user_type == user_type,
        ).values(muted_until=muted_until))

    def find_group_message(self, group_id: str, message_id: str) -> Optional[GroupMessage]:
        stmt = select(GroupMessage).where(
            GroupMessage.group_id == group_id,
            GroupMessage.id == message_id,
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def page_group_messages(self, group_id: str, keyword: str, cursor_dt: Optional[datetime], size: int) -> list[GroupMessage]:
        stmt = select(GroupMessage).where(GroupMessage.group_id == group_id)
        if keyword:
            stmt = stmt.where(GroupMessage.content.like(f"%{keyword}%"))
        if cursor_dt is not None:
            stmt = stmt.where(GroupMessage.created_at < cursor_dt)
        stmt = stmt.order_by(GroupMessage.created_at.desc()).limit(size + 1)
        return list(self.db.execute(stmt).scalars().all())

    def find_group_read(self, group_id: str, user_id: str, user_type: str) -> Optional[GroupMessageRead]:
        stmt = select(GroupMessageRead).where(
            GroupMessageRead.group_id == group_id,
            GroupMessageRead.user_id == user_id,
            GroupMessageRead.user_type == user_type,
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def list_group_last_messages(self, group_ids: list[str]) -> dict[str, GroupMessage]:
        if not group_ids:
            return {}
        subq = select(
            GroupMessage.group_id,
            func.max(GroupMessage.created_at).label("max_ct"),
        ).where(
            GroupMessage.group_id.in_(group_ids)
        ).group_by(GroupMessage.group_id).subquery()
        stmt = select(GroupMessage).join(
            subq,
            and_(
                subq.c.group_id == GroupMessage.group_id,
                subq.c.max_ct == GroupMessage.created_at,
            ),
        )
        return {row.group_id: row for row in self.db.execute(stmt).scalars().all()}

    def unread_group_counts(self, group_ids: list[str], user_id: str, user_type: str) -> dict[str, int]:
        if not group_ids:
            return {}
        read_sub = select(
            GroupMessageRead.group_id,
            func.max(GroupMessageRead.read_at).label("max_read"),
        ).where(
            GroupMessageRead.user_id == user_id,
            GroupMessageRead.user_type == user_type,
        ).group_by(GroupMessageRead.group_id).subquery()
        stmt = select(
            GroupMessage.group_id,
            func.count(GroupMessage.id).label("count"),
        ).outerjoin(
            read_sub,
            read_sub.c.group_id == GroupMessage.group_id,
        ).where(
            GroupMessage.group_id.in_(group_ids),
            or_(
                read_sub.c.max_read.is_(None),
                GroupMessage.created_at > read_sub.c.max_read,
            ),
        ).group_by(GroupMessage.group_id)
        return {group_id: count for group_id, count in self.db.execute(stmt).all()}

    def search_groups(self, keyword: str, limit: int) -> list[Group]:
        stmt = select(Group).where(
            Group.name.like(f"%{keyword}%"),
            Group.status == GroupNormal,
        ).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

