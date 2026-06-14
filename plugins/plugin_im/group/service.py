"""Group service."""

from __future__ import annotations

import json
from datetime import datetime, timedelta
from typing import Optional

from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from plugins.plugin_im.ws import Message as WSMessage, get_global_cross_hub
from plugins.plugin_im.ws.tasks import schedule as schedule_ws_task

from ..message.user_repository import IMUserRepository
from ..model.group import Group, GroupJoinRequest, GroupMember, GroupMessage, GroupMessageRead
from ..model.message import MsgExtraSystem, MsgTypeSystem
from .constants import (
    GroupDissolved,
    GroupNormal,
    GroupTypeConsumerOnly,
    GroupTypeMixed,
    MemberActive,
    MemberKicked,
    MemberLeft,
    RoleAdmin,
    RoleMember,
    RoleOwner,
    UserTypeBusiness,
    UserTypeConsumer,
)
from .params import (
    ConversationVO,
    CreateParam,
    GroupSearchVO,
    GroupVO,
    HandleJoinRequestParam,
    InviteParam,
    JoinRequestVO,
    KickParam,
    MemberVO,
    MessageVO,
    SendMessageParam,
    SetNicknameParam,
    SetRoleParam,
    TransferOwnerParam,
    UpdateParam,
)
from .repository import GroupRepository


def _fmt_dt(dt: Optional[datetime]) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


class GroupService:
    def __init__(self, repository: GroupRepository, user_repository: IMUserRepository):
        self.repository = repository
        self.user_repository = user_repository
        self.db = repository.db

    def create(self, owner_id: str, owner_type: str, param: CreateParam) -> GroupVO:
        if not param.name:
            raise BusinessException("群名称不能为空", 400)
        if len(param.name) > 100:
            raise BusinessException("群名称不能超过100个字符", 400)
        if not owner_id:
            raise BusinessException("用户未登录", 401)

        group_type = GroupTypeConsumerOnly if owner_type == UserTypeConsumer else GroupTypeMixed
        now = datetime.now()
        group = Group(
            id=generate_id(),
            name=param.name,
            avatar=param.avatar,
            owner_id=owner_id,
            owner_type=owner_type,
            group_type=group_type,
            max_members=200,
            status=GroupNormal,
            created_at=now,
            updated_at=now,
        )
        self.repository.add(group)
        self.repository.flush()

        self.repository.add(
            GroupMember(
                id=generate_id(),
                group_id=group.id,
                user_id=owner_id,
                user_type=owner_type,
                role=RoleOwner,
                joined_at=now,
                status=MemberActive,
            )
        )

        if param.member_ids:
            self._validate_member_type(group_type, param.member_type)
            existing = self.repository.count_existing_active_members(group.id, param.member_ids, param.member_type)
            if existing > 0:
                raise BusinessException("部分成员已在群中", 400)

            current_count = self.repository.count_active_members(group.id)
            if current_count + len(param.member_ids) > group.max_members:
                raise BusinessException(f"群成员数量不能超过{group.max_members}人", 400)

            members_to_add: list[GroupMember] = []
            messages_to_add: list[GroupMessage] = []
            for user_id in param.member_ids:
                if user_id == owner_id:
                    continue
                members_to_add.append(
                    GroupMember(
                        id=generate_id(),
                        group_id=group.id,
                        user_id=user_id,
                        user_type=param.member_type,
                        role=RoleMember,
                        joined_at=now,
                        status=MemberActive,
                    )
                )
                messages_to_add.append(
                    GroupMessage(
                        id=generate_id(),
                        group_id=group.id,
                        sender_id=owner_id,
                        sender_type=owner_type,
                        content="欢迎加入群聊",
                        extra=json.dumps(
                            MsgExtraSystem(
                                action="join",
                                user_id=user_id,
                                user_type=param.member_type,
                            ).model_dump(),
                            ensure_ascii=False,
                        ),
                        msg_type=MsgTypeSystem,
                        created_at=now,
                    )
                )
            self.repository.add_members(members_to_add)
            self.repository.add_messages(messages_to_add)

        self.repository.commit()
        return GroupVO(
            id=group.id,
            name=group.name,
            avatar=group.avatar or "",
            owner_id=group.owner_id,
            owner_type=group.owner_type,
            group_type=group.group_type,
            member_count=self.repository.count_active_members(group.id),
        )

    def update(self, operator_id: str, operator_type: str, param: UpdateParam) -> None:
        if not param.group_id:
            raise BusinessException("参数错误", 400)

        member = self._check_owner_or_admin(param.group_id, operator_id, operator_type)
        updates = {}
        if param.name is not None:
            if len(param.name) > 100:
                raise BusinessException("群名称不能超过100个字符", 400)
            updates["name"] = param.name
        if param.avatar is not None:
            updates["avatar"] = param.avatar
        if param.notice is not None and member.role == RoleOwner:
            updates["notice"] = param.notice
        if updates:
            self.repository.update_group(param.group_id, updates)
            self.repository.commit()

    def dissolve(self, operator_id: str, group_id: str) -> None:
        if not group_id or not operator_id:
            raise BusinessException("参数错误", 400)
        group = self.repository.find_group(group_id)
        if not group:
            raise BusinessException("群不存在", 400)
        if group.owner_id != operator_id:
            raise BusinessException("仅群主可解散群", 403)
        self.repository.dissolve_group(group_id, datetime.now(), MemberLeft, GroupDissolved)
        self.repository.commit()

    def detail(self, group_id: str) -> Optional[GroupVO]:
        if not group_id:
            return None
        group = self.repository.find_group(group_id)
        if not group:
            return None
        return GroupVO(
            id=group.id,
            name=group.name,
            avatar=group.avatar or "",
            owner_id=group.owner_id,
            owner_type=group.owner_type,
            group_type=group.group_type,
            notice=group.notice or "",
            member_count=self.repository.count_active_members(group_id),
        )

    def my_groups(self, user_id: str, user_type: str) -> list[GroupVO]:
        if not user_id:
            return []
        group_ids = self.repository.list_member_group_ids(user_id, user_type)
        groups = self.repository.list_groups_by_ids(group_ids)
        count_map = self.repository.active_member_counts(group_ids)
        return [
            GroupVO(
                id=group.id,
                name=group.name,
                avatar=group.avatar or "",
                owner_id=group.owner_id,
                owner_type=group.owner_type,
                group_type=group.group_type,
                notice=group.notice or "",
                member_count=count_map.get(group.id, 0),
            )
            for group in groups
        ]

    def invite(self, operator_id: str, operator_type: str, param: InviteParam) -> None:
        if not param.user_ids:
            return
        self._check_owner_or_admin(param.group_id, operator_id, operator_type)
        group = self.repository.find_group(param.group_id)
        if not group:
            raise BusinessException("群不存在", 400)
        self._validate_member_type(group.group_type, param.user_type)
        existing = self.repository.count_existing_active_members(param.group_id, param.user_ids, param.user_type)
        if existing > 0:
            raise BusinessException("部分成员已在群中", 400)
        current_count = self.repository.count_active_members(param.group_id)
        if current_count + len(param.user_ids) > group.max_members:
            raise BusinessException(f"群成员数量不能超过{group.max_members}人", 400)

        now = datetime.now()
        self.repository.add_members(
            [
                GroupMember(
                    id=generate_id(),
                    group_id=param.group_id,
                    user_id=user_id,
                    user_type=param.user_type,
                    role=RoleMember,
                    joined_at=now,
                    status=MemberActive,
                )
                for user_id in param.user_ids
            ]
        )
        self.repository.commit()

    def join_group(self, user_id: str, user_type: str, group_id: str) -> None:
        group = self.repository.find_group(group_id)
        if not group:
            raise BusinessException("群不存在", 400)

        if group.is_public:
            self.repository.add_member(
                GroupMember(
                    id=generate_id(),
                    group_id=group_id,
                    user_id=user_id,
                    user_type=user_type,
                    role=RoleMember,
                    joined_at=datetime.now(),
                    status=MemberActive,
                )
            )
        else:
            existing_req = self.repository.count_pending_join_request(group_id, user_id, user_type)
            if existing_req > 0:
                raise BusinessException("已发送过加群请求", 400)
            self.repository.create_join_request(
                GroupJoinRequest(
                    id=generate_id(),
                    group_id=group_id,
                    user_id=user_id,
                    user_type=user_type,
                    status="pending",
                    created_at=datetime.now(),
                )
            )
        self.repository.commit()

    def pending_join_requests(self, group_id: str) -> list[JoinRequestVO]:
        return [
            JoinRequestVO(
                id=request.id,
                group_id=request.group_id,
                user_id=request.user_id,
                user_type=request.user_type,
                remark=getattr(request, "remark", "") or "",
                created_at=_fmt_dt(request.created_at),
            )
            for request in self.repository.list_pending_join_requests(group_id)
        ]

    def handle_join_request(self, operator_id: str, operator_type: str, param: HandleJoinRequestParam) -> None:
        request = self.repository.find_join_request(param.request_id)
        if not request or request.status != "pending":
            raise BusinessException("请求不存在或已处理", 400)
        self._check_owner_or_admin(request.group_id, operator_id, operator_type)

        request.status = param.action
        request.updated_at = datetime.now()
        if param.action == "approved":
            self.repository.add_member(
                GroupMember(
                    id=generate_id(),
                    group_id=request.group_id,
                    user_id=request.user_id,
                    user_type=request.user_type,
                    role=RoleMember,
                    joined_at=datetime.now(),
                    status=MemberActive,
                )
            )
        self.repository.commit()

    def leave_group(self, user_id: str, user_type: str, group_id: str) -> None:
        self.repository.update_member_status(group_id, user_id, user_type, MemberLeft)
        self.repository.commit()

    def kick(self, operator_id: str, operator_type: str, param: KickParam) -> None:
        self._check_owner_or_admin(param.group_id, operator_id, operator_type)
        self.repository.update_member_status(param.group_id, param.user_id, param.user_type, MemberKicked)
        self.repository.commit()

    def set_role(self, operator_id: str, param: SetRoleParam) -> None:
        group = self.repository.find_group(param.group_id)
        if not group or group.owner_id != operator_id:
            raise BusinessException("仅群主可设置角色", 403)

        now = datetime.now()
        if param.role == RoleOwner:
            self.repository.update_member_role(param.group_id, operator_id, group.owner_type, RoleAdmin)
            self.repository.update_member_role(param.group_id, param.user_id, param.user_type, RoleOwner)
            self.repository.update_group(
                param.group_id,
                {"owner_id": param.user_id, "owner_type": param.user_type, "updated_at": now},
            )
        else:
            self.repository.update_member_role(param.group_id, param.user_id, param.user_type, param.role)
        self.repository.commit()

    def transfer_owner(self, operator_id: str, param: TransferOwnerParam) -> None:
        group = self.repository.find_group(param.group_id)
        if not group or group.owner_id != operator_id:
            raise BusinessException("仅群主可转让群", 403)
        new_owner = self.repository.find_active_member(param.group_id, param.new_owner_id, param.new_owner_type)
        if not new_owner:
            raise BusinessException("新群主不在群中", 400)

        now = datetime.now()
        self.repository.update_group(
            param.group_id,
            {"owner_id": param.new_owner_id, "owner_type": param.new_owner_type, "updated_at": now},
        )
        self.repository.update_member_role(param.group_id, operator_id, group.owner_type, RoleAdmin)
        self.repository.update_member_role(param.group_id, param.new_owner_id, param.new_owner_type, RoleOwner)
        self.repository.commit()

    def set_member_nickname(self, operator_id: str, operator_type: str, param: SetNicknameParam) -> None:
        self._check_owner_or_admin(param.group_id, operator_id, operator_type)
        self.repository.update_member_nickname(param.group_id, param.user_id, param.user_type, param.nickname)
        self.repository.commit()

    def members(self, group_id: str) -> list[MemberVO]:
        if not group_id:
            return []

        records = self.repository.list_members(group_id)
        business_ids = [member.user_id for member in records if member.user_type == UserTypeBusiness]
        consumer_ids = [member.user_id for member in records if member.user_type == UserTypeConsumer]
        name_map: dict[str, str] = {}

        if business_ids:
            for user in self.user_repository.list_sys_users(business_ids):
                name_map[f"BUSINESS:{user.id}"] = user.nickname or user.username or user.id
        if consumer_ids:
            for user in self.user_repository.list_client_users(consumer_ids):
                name_map[f"CONSUMER:{user.id}"] = user.nickname or user.username or user.id

        now = datetime.now()
        return [
            MemberVO(
                user_id=member.user_id,
                user_type=member.user_type,
                role=member.role,
                nickname=member.nickname or name_map.get(f"{member.user_type}:{member.user_id}", member.user_id),
                joined_at=_fmt_dt(member.joined_at),
                is_muted=member.muted_until is not None and member.muted_until > now,
            )
            for member in records
        ]

    def messages(self, group_id: str, cursor: str = "", size: int = 20) -> tuple[list[MessageVO], bool]:
        return self._page_messages(group_id, "", cursor, size)

    def search_messages(
        self, group_id: str, keyword: str, cursor: str = "", size: int = 20
    ) -> tuple[list[MessageVO], bool]:
        return self._page_messages(group_id, keyword, cursor, size)

    def send_message(self, sender_id: str, sender_type: str, param: SendMessageParam) -> MessageVO:
        msg_type = param.msg_type or "TEXT"
        now = datetime.now()
        message = GroupMessage(
            id=generate_id(),
            group_id=param.group_id,
            sender_id=sender_id,
            sender_type=sender_type,
            content=param.content,
            extra=param.extra,
            msg_type=msg_type,
            reply_to=param.reply_to or None,
            created_at=now,
        )
        self.repository.add(message)
        self.repository.commit()

        payload = {
            "message_id": message.id,
            "group_id": param.group_id,
            "sender_id": sender_id,
            "sender_type": sender_type,
            "content": param.content,
            "msg_type": msg_type,
            "extra": param.extra,
            "created_at": _fmt_dt(now),
        }
        ws_msg = WSMessage(type="group_message", payload=payload)
        cross_hub = get_global_cross_hub()
        for member in self.repository.list_other_active_members(param.group_id, sender_id, sender_type):
            if member.user_type == UserTypeConsumer:
                if cross_hub:
                    schedule_ws_task(cross_hub.send_to_consumer(member.user_id, ws_msg))
            else:
                if cross_hub:
                    schedule_ws_task(cross_hub.send_to_user(member.user_id, ws_msg))
        return MessageVO.model_validate(message)

    def recall_message(self, group_id: str, message_id: str, user_id: str, user_type: str) -> None:
        message = self.repository.find_group_message(group_id, message_id)
        if not message:
            raise BusinessException("消息不存在", 400)
        if message.sender_id != user_id or message.sender_type != user_type:
            raise BusinessException("只能撤回自己的消息", 403)
        if message.created_at and (datetime.now() - message.created_at) > timedelta(minutes=5):
            raise BusinessException("超过5分钟，无法撤回", 400)

        message.content = "消息已被撤回"
        message.msg_type = MsgTypeSystem
        self.repository.commit()

    def mark_read(self, group_id: str, user_id: str, user_type: str, message_id: str = "") -> None:
        now = datetime.now()
        existing = self.repository.find_group_read(group_id, user_id, user_type)
        if existing:
            existing.read_at = now
        else:
            self.repository.add(
                GroupMessageRead(
                    id=generate_id(),
                    group_id=group_id,
                    user_id=user_id,
                    user_type=user_type,
                    read_at=now,
                )
            )
        self.repository.commit()

    def mark_conversation_read(self, group_id: str, user_id: str, user_type: str) -> None:
        self.mark_read(group_id, user_id, user_type)

    def mute_member(
        self,
        operator_id: str,
        operator_type: str,
        param: KickParam,
        duration: timedelta = timedelta(minutes=60),
    ) -> None:
        self._check_owner_or_admin(param.group_id, operator_id, operator_type)
        self.repository.update_member_muted_until(
            param.group_id,
            param.user_id,
            param.user_type,
            datetime.now() + duration,
        )
        self.repository.commit()

    def unmute_member(self, operator_id: str, operator_type: str, param: KickParam) -> None:
        self._check_owner_or_admin(param.group_id, operator_id, operator_type)
        self.repository.update_member_muted_until(param.group_id, param.user_id, param.user_type, None)
        self.repository.commit()

    def search_groups(self, keyword: str, limit: int = 20) -> list[GroupSearchVO]:
        if not keyword:
            return []
        if limit > 50:
            limit = 50
        groups = self.repository.search_groups(keyword, limit)
        count_map = self.repository.active_member_counts([group.id for group in groups])
        return [
            GroupSearchVO(
                id=group.id,
                name=group.name,
                avatar=group.avatar or "",
                member_count=count_map.get(group.id, 0),
            )
            for group in groups
        ]

    def my_group_conversations(self, user_id: str, user_type: str) -> list[ConversationVO]:
        if not user_id:
            return []
        group_ids = self.repository.list_member_group_ids(user_id, user_type)
        groups = self.repository.list_groups_by_ids(group_ids)
        count_map = self.repository.active_member_counts(group_ids)
        last_map = self.repository.list_group_last_messages(group_ids)
        unread_map = self.repository.unread_group_counts(group_ids, user_id, user_type)

        results = []
        for group in groups:
            vo = ConversationVO(
                conversation_id=f"group:{group.id}",
                conversation_type="group",
                group_id=group.id,
                group_name=group.name,
                group_avatar=group.avatar or "",
                member_count=count_map.get(group.id, 0),
                unread_count=unread_map.get(group.id, 0),
            )
            last_message = last_map.get(group.id)
            if last_message:
                vo.last_content = last_message.content or ""
                vo.last_time = _fmt_dt(last_message.created_at)
            results.append(vo)
        return results

    def _check_owner_or_admin(self, group_id: str, operator_id: str, operator_type: str) -> GroupMember:
        member = self.repository.find_active_member(group_id, operator_id, operator_type)
        if not member or member.role not in (RoleOwner, RoleAdmin):
            raise BusinessException("无权限", 403)
        return member

    def _validate_member_type(self, group_type: str, member_type: str) -> None:
        if group_type == GroupTypeConsumerOnly and member_type != UserTypeConsumer:
            raise BusinessException("C端群只能邀请C端成员", 400)

    def _page_messages(
        self, group_id: str, keyword: str, cursor: str = "", size: int = 20
    ) -> tuple[list[MessageVO], bool]:
        if size < 1:
            size = 20
        if size > 100:
            size = 100

        cursor_dt = None
        if cursor:
            try:
                cursor_dt = datetime.strptime(cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                cursor_dt = None
        records = self.repository.page_group_messages(group_id, keyword, cursor_dt, size)
        has_more = len(records) > size
        if has_more:
            records = records[:size]
        return [MessageVO.model_validate(record) for record in records], has_more


def get_group_service(db: Session = Depends(get_db)) -> GroupService:
    return GroupService(GroupRepository(db), IMUserRepository(db))


__all__ = ["GroupService", "get_group_service"]
