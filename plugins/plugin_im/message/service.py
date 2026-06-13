"""Message service."""

from __future__ import annotations

import asyncio
import hashlib
import json
import os
from datetime import datetime
from typing import Optional

from fastapi import Depends, UploadFile
from sqlalchemy.orm import Session

from sdk.auth.enums import RealmID as LTE
from sdk.infra.db import get_db
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import page_data
from plugins.plugin_im import ws as im_ws
from plugins.plugin_im.ws import Message as WSMessage

from ..group import GroupService, get_group_service
from ..model.im_file import ImFile
from ..model.message import Conversation, Message, MsgTypeSystem, generate_conversation_id
from .im_file_repository import ImFileRepository
from .params import (
    ConversationMessageVO,
    ConversationVO,
    ForwardParam,
    GetOrCreateConversationParam,
    MessagePageParam,
    MessageSendParam,
    MessageToMessageVO,
    MessageVO,
    RecallParam,
    SearchParam,
    UploadFileResult,
)
from .repository import MessageRepository
from .user_repository import IMUserRepository


ALLOWED_EXTENSIONS = {
    ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".ico",
    ".bmp", ".tiff",
    ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".pdf",
    ".txt", ".csv", ".md",
    ".zip", ".rar", ".7z", ".tar", ".gz",
    ".mp3", ".wav", ".ogg",
    ".mp4", ".avi", ".mkv", ".mov", ".webm",
    ".json", ".xml", ".yaml", ".yml",
}
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".ico", ".bmp", ".tiff"}


def _normalize_receiver_ids(ids: list[str]) -> list[str]:
    result: list[str] = []
    seen: set[str] = set()
    for raw in ids:
        value = str(raw).strip()
        if not value or value in seen:
            continue
        seen.add(value)
        result.append(value)
    return result


def _fmt_dt(dt: Optional[datetime]) -> str:
    if dt is None:
        return ""
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def _is_image_ext(ext: str) -> bool:
    return ext.lower() in IMAGE_EXTENSIONS


def _format_file_size(bytes_size: int) -> tuple[int, str]:
    if bytes_size < 1024:
        return 0, f"{bytes_size} B"
    kb = bytes_size // 1024
    if kb < 1024:
        return kb, f"{kb} KB"
    mb = kb / 1024
    return kb, f"{mb:.1f} MB"


class MessageService:
    def __init__(
        self,
        repository: MessageRepository,
        user_repository: IMUserRepository,
        file_repository: ImFileRepository,
    ):
        self.repository = repository
        self.user_repository = user_repository
        self.file_repository = file_repository
        self.db = repository.db

    @classmethod
    def from_db(cls, db: Session) -> "MessageService":
        return cls(MessageRepository(db), IMUserRepository(db), ImFileRepository(db))

    async def send_message(self, param: MessageSendParam, sender_id: str, sender_type: str) -> list[str]:
        if im_ws.GlobalCrossHub and not await im_ws.GlobalCrossHub.allow_message(sender_id, sender_type):
            raise BusinessException("发送消息过于频繁，请稍后重试", 429)

        msg_type = param.msg_type or "TEXT"
        receiver_type = param.receiver_type or "BUSINESS"
        receiver_ids = _normalize_receiver_ids(param.receiver_ids)
        if not receiver_ids:
            raise BusinessException("接收人不能为空", 400)
        if len(receiver_ids) > 200:
            raise BusinessException("单次最多发送给200个接收人", 400)
        if len(param.content) > 5000:
            raise BusinessException("消息内容不能超过5000个字符", 400)
        now = datetime.now()

        records = []
        for receiver_id in receiver_ids:
            conversation_id = generate_conversation_id(
                sender_id, LTE(sender_type), receiver_id, LTE(receiver_type)
            )
            records.append(
                Message(
                    id=generate_id(),
                    conversation_id=conversation_id,
                    content=param.content,
                    extra=param.extra,
                    msg_type=msg_type,
                    sender_id=sender_id,
                    sender_type=sender_type,
                    receiver_id=receiver_id,
                    receiver_type=receiver_type,
                    status="unread",
                    created_at=now,
                    updated_at=now,
                )
            )

        self.repository.create_messages(records)
        self.repository.upsert_conversations(
            [
                Conversation(
                    id=record.conversation_id,
                    from_id=record.sender_id,
                    from_type=record.sender_type,
                    to_id=record.receiver_id,
                    to_type=record.receiver_type,
                    last_msg=record.content,
                    last_time=now,
                    created_at=now,
                    updated_at=now,
                )
                for record in records
            ]
        )
        self.repository.commit()

        for index, receiver_id in enumerate(receiver_ids):
            record = records[index]
            ws_payload = {
                "message_id": record.id,
                "conversation_id": record.conversation_id,
                "content": param.content,
                "msg_type": msg_type,
                "extra": param.extra or "",
                "sender_id": sender_id,
                "sender_type": sender_type,
                "title": "",
                "created_at": _fmt_dt(now),
            }
            ws_msg = WSMessage(type="new_message", payload=ws_payload)
            if receiver_type == "CONSUMER":
                if im_ws.GlobalCrossHub:
                    asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_consumer(receiver_id, ws_msg, record.id))
            else:
                if im_ws.GlobalCrossHub:
                    asyncio.ensure_future(im_ws.GlobalCrossHub.send_to_user(receiver_id, ws_msg, record.id))

        return [record.conversation_id for record in records]

    def page_messages(self, user_id: str, user_type: str, param: MessagePageParam) -> dict:
        param.current = max(1, param.current)
        param.size = max(1, min(param.size, 100))
        records, total = self.repository.page_messages(user_id, user_type, param)
        return page_data([MessageToMessageVO(record) for record in records], total, param.current, param.size)

    def unread_count(self, user_id: str, user_type: str) -> int:
        return self.repository.count_unread(user_id, user_type)

    def detail_message(self, message_id: str, user_id: str, user_type: str) -> Optional[MessageVO]:
        entity = self.repository.find_owned_by_id(message_id, user_id, user_type)
        return MessageToMessageVO(entity) if entity else None

    def mark_read(self, message_id: str, user_id: str, user_type: str) -> None:
        self.repository.mark_read(message_id, user_id, user_type)

    def mark_conversation_read(self, receiver_id: str, receiver_type: str, conversation_id: str) -> None:
        self.repository.mark_conversation_read(receiver_id, receiver_type, conversation_id)

    def mark_all_read(self, receiver_id: str, receiver_type: str) -> None:
        self.repository.mark_all_read(receiver_id, receiver_type)

    def remove_messages(self, user_id: str, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.soft_delete_messages(user_id, ids)

    def recall_message(self, user_id: str, user_type: str, param: RecallParam) -> None:
        message = self.repository.find_by_id(param.message_id)
        if not message:
            raise BusinessException("消息不存在", 400)
        if message.sender_id != user_id or message.sender_type != user_type:
            raise BusinessException("只能撤回自己的消息", 403)
        if message.created_at and (datetime.now() - message.created_at).total_seconds() > 300:
            raise BusinessException("超过5分钟，无法撤回", 400)

        message.content = "消息已被撤回"
        message.msg_type = MsgTypeSystem
        message.updated_at = datetime.now()
        self.db.commit()

    async def forward_message(self, user_id: str, user_type: str, param: ForwardParam) -> None:
        original = self.repository.find_owned_by_id(param.message_id, user_id, user_type)
        if not original:
            raise BusinessException("消息不存在", 400)

        send_param = MessageSendParam(
            content=original.content,
            msg_type=original.msg_type,
            extra=original.extra,
            receiver_ids=param.target_ids,
            receiver_type=param.target_type,
        )
        await self.send_message(send_param, user_id, user_type)

    def search_messages(self, user_id: str, user_type: str, param: SearchParam) -> tuple[list[MessageVO], bool]:
        if param.size < 1:
            param.size = 20
        if param.size > 100:
            param.size = 100

        cursor_dt = None
        if param.cursor:
            try:
                cursor_dt = datetime.strptime(param.cursor, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                cursor_dt = None
        records = self.repository.search_messages(user_id, user_type, param.keyword, cursor_dt, param.size + 1)
        has_more = len(records) > param.size
        if has_more:
            records = records[:param.size]
        return [MessageToMessageVO(record) for record in records], has_more

    def conversations(
        self,
        current_user_id: str,
        user_type: str,
        cursor: str = "",
        size: int = 20,
        group_service: Optional[GroupService] = None,
    ) -> tuple[list[ConversationVO], bool]:
        if size < 1:
            size = 20
        if size > 100:
            size = 100

        result_map = self._build_single_conversations(current_user_id, user_type)
        if group_service is not None:
            for group_vo in group_service.my_group_conversations(current_user_id, user_type):
                result_map[f"group:{group_vo.group_id}"] = ConversationVO(
                    conversation_id=f"group:{group_vo.group_id}",
                    conversation_type="group",
                    group_id=group_vo.group_id,
                    group_name=group_vo.group_name,
                    group_avatar=group_vo.group_avatar,
                    member_count=group_vo.member_count,
                    last_content=group_vo.last_content,
                    last_time=group_vo.last_time,
                    unread_count=group_vo.unread_count,
                )

        result = sorted(result_map.values(), key=lambda item: item.last_time, reverse=True)
        if cursor:
            result = [item for item in result if item.last_time > cursor]
        has_more = len(result) > size
        if has_more:
            result = result[:size]
        return result, has_more

    def conversation_messages(
        self, current_user_id: str, conversation_id: str, cursor: str = "", size: int = 20
    ) -> tuple[list[ConversationMessageVO], bool]:
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
        records = self.repository.list_conversation_messages(current_user_id, user_type, conversation_id, cursor_dt, size + 1)
        has_more = len(records) > size
        if has_more:
            records = records[:size]

        return [
            ConversationMessageVO(
                id=record.id,
                sender_id=record.sender_id or "",
                sender_type=record.sender_type or "",
                content=record.content or "",
                msg_type=record.msg_type,
                extra=record.extra or "",
                status=record.status,
                file_url=self.resolve_file_url(record.content or "", record.extra or "")
                if record.msg_type in ("IMAGE", "FILE")
                else "",
                created_at=_fmt_dt(record.created_at),
            )
            for record in records
        ], has_more

    def get_or_create_conversation(
        self, user_id: str, user_type: str, param: GetOrCreateConversationParam
    ) -> tuple[str, str]:
        if not param.user_id or not param.user_type:
            raise BusinessException("参数错误", 400)

        conversation_id = generate_conversation_id(
            user_id, LTE(user_type), param.user_id, LTE(param.user_type)
        )
        display_name = param.user_id
        if param.user_type == "BUSINESS":
            user = self.user_repository.find_sys_user(param.user_id)
            if user:
                display_name = user.nickname or user.username or param.user_id
        else:
            user = self.user_repository.find_client_user(param.user_id)
            if user:
                display_name = user.nickname or user.username or param.user_id
        return conversation_id, display_name

    async def upload_file(
        self,
        file: UploadFile,
        sender_id: str,
        sender_type: str,
        engine_type: str = "LOCAL",
        bucket: str = "DEFAULT",
        conversation_id: str = "",
        msg_type: str = "",
    ) -> UploadFileResult:
        if not file.filename:
            raise BusinessException("文件名为空", 400)

        ext = os.path.splitext(file.filename)[1].lower()
        if ext not in ALLOWED_EXTENSIONS:
            raise BusinessException(f"不支持的文件类型: {ext}", 400)

        effective_msg_type = msg_type or "FILE"
        if _is_image_ext(ext):
            effective_msg_type = "IMAGE"

        content = await file.read()
        file_size = len(content)
        checksum = hashlib.sha256(content).hexdigest()

        file_key = generate_id() + ext
        storage_path = f"uploads/im/{file_key}"
        os.makedirs(os.path.dirname(storage_path), exist_ok=True)
        with open(storage_path, "wb") as fp:
            fp.write(content)

        file_size_kb, size_info = _format_file_size(file_size)
        thumbnail = file_key if _is_image_ext(ext) else ""
        record = ImFile(
            id=generate_id(),
            engine=engine_type,
            bucket=bucket,
            file_key=file_key,
            name=file.filename,
            suffix=ext,
            size_kb=file_size_kb,
            size_info=size_info,
            storage_path=storage_path,
            download_path="",
            thumbnail=thumbnail,
            checksum=checksum,
            checksum_algo="sha256",
            conversation_id=conversation_id,
            sender_id=sender_id,
            sender_type=sender_type,
            msg_type=effective_msg_type,
            created_at=datetime.now(),
        )
        self.file_repository.insert(record)

        return UploadFileResult(
            url=f"/{storage_path}",
            file_key=file_key,
            bucket=bucket,
            engine=engine_type,
            original_name=file.filename,
            file_size=file_size,
            file_type=ext,
        )

    def resolve_file_url(self, content: str, extra: str) -> str:
        if content.startswith("http"):
            return content
        if not content:
            return ""

        engine = "LOCAL"
        bucket = "DEFAULT"
        if extra:
            try:
                meta = json.loads(extra)
                if "engine" in meta:
                    engine = meta["engine"]
                if "bucket" in meta:
                    bucket = meta["bucket"]
            except (json.JSONDecodeError, TypeError):
                pass

        try:
            from sdk.infra.storage.factory import get_url

            return get_url(engine, bucket, content)
        except (ImportError, Exception):
            return f"/uploads/im/{content}"

    def _build_single_conversations(self, current_user_id: str, user_type: str) -> dict[str, ConversationVO]:
        rows = self.repository.list_latest_message_rows(current_user_id, user_type)
        if not rows:
            return {}

        conversation_ids = [row.conversation_id for row in rows]
        unread_map = self.repository.unread_counts_by_conversation(conversation_ids, current_user_id, user_type)
        business_ids: list[str] = []
        consumer_ids: list[str] = []
        result_map: dict[str, ConversationVO] = {}

        for row in rows:
            if row.sender_id == current_user_id and row.sender_type == user_type:
                other_id, other_type = row.receiver_id, row.receiver_type
            else:
                other_id, other_type = row.sender_id, row.sender_type

            if other_type == "BUSINESS":
                business_ids.append(other_id)
            else:
                consumer_ids.append(other_id)

            result_map[row.conversation_id] = ConversationVO(
                conversation_id=row.conversation_id,
                conversation_type="single",
                other_user_id=other_id,
                other_user_type=other_type,
                last_content=row.content or "",
                last_time=_fmt_dt(row.created_at),
                unread_count=unread_map.get(row.conversation_id, 0),
            )

        nickname_map: dict[str, str] = {}
        avatar_map: dict[str, str] = {}
        if business_ids:
            for user in self.user_repository.list_sys_users(business_ids):
                key = f"BUSINESS:{user.id}"
                nickname_map[key] = user.nickname or user.username or ""
                avatar_map[key] = user.avatar or ""
        if consumer_ids:
            for user in self.user_repository.list_client_users(consumer_ids):
                key = f"CONSUMER:{user.id}"
                nickname_map[key] = user.nickname or user.username or ""
                avatar_map[key] = user.avatar or ""

        for vo in result_map.values():
            key = f"{vo.other_user_type}:{vo.other_user_id}"
            vo.other_nickname = nickname_map.get(key, "")
            vo.other_avatar = avatar_map.get(key, "")
        return result_map


def get_message_service(db: Session = Depends(get_db)) -> MessageService:
    return MessageService.from_db(db)


__all__ = ["MessageService", "get_message_service"]
