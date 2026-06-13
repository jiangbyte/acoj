from __future__ import annotations

import pytest

from plugins.plugin_im.message.params import MessageSendParam
from plugins.plugin_im.message.service import MessageService, _normalize_receiver_ids
from sdk.web.exception import BusinessException


class _Repo:
    def __init__(self) -> None:
        self.db = self
        self.records = None
        self.commit_called = False

    def create_messages(self, records):
        self.records = records

    def upsert_conversations(self, conversations):
        return None

    def commit(self):
        self.commit_called = True

    def find_owned_by_id(self, message_id, user_id, user_type):
        return None


class _UserRepo:
    def find_sys_user(self, user_id):
        return None

    def find_client_user(self, user_id):
        return None


class _FileRepo:
    def insert(self, record):
        return None


@pytest.mark.asyncio
async def test_normalize_receiver_ids_matches_gin() -> None:
    assert _normalize_receiver_ids([" u1 ", "", "u2", "u1", "  "]) == ["u1", "u2"]


@pytest.mark.asyncio
async def test_send_message_rejects_empty_receivers() -> None:
    service = MessageService(_Repo(), _UserRepo(), _FileRepo())

    with pytest.raises(BusinessException, match="接收人不能为空"):
        await service.send_message(
            MessageSendParam(content="hello", receiver_ids=["", "  "]),
            "sender",
            "BUSINESS",
        )


@pytest.mark.asyncio
async def test_send_message_deduplicates_receivers() -> None:
    service = MessageService(_Repo(), _UserRepo(), _FileRepo())

    conversation_ids = await service.send_message(
        MessageSendParam(content="hello", receiver_ids=["u1", " u1 ", "u2"]),
        "sender",
        "BUSINESS",
    )

    assert len(conversation_ids) == 2
    assert len(service.repository.records) == 2
    assert [record.receiver_id for record in service.repository.records] == ["u1", "u2"]


@pytest.mark.asyncio
async def test_send_message_rejects_too_many_receivers() -> None:
    service = MessageService(_Repo(), _UserRepo(), _FileRepo())

    with pytest.raises(BusinessException, match="单次最多发送给200个接收人"):
        await service.send_message(
            MessageSendParam(content="hello", receiver_ids=[f"u{i}" for i in range(201)]),
            "sender",
            "BUSINESS",
        )


@pytest.mark.asyncio
async def test_send_message_rejects_too_long_content() -> None:
    service = MessageService(_Repo(), _UserRepo(), _FileRepo())

    with pytest.raises(BusinessException, match="消息内容不能超过5000个字符"):
        await service.send_message(
            MessageSendParam(content="x" * 5001, receiver_ids=["u1"]),
            "sender",
            "BUSINESS",
        )
