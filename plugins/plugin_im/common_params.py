from __future__ import annotations

from typing import Generic, TypeVar

from pydantic import BaseModel, Field


T = TypeVar("T")


class CursorResult(BaseModel, Generic[T]):
    records: list[T] = Field(default_factory=list)
    has_more: bool = False


class ConversationCreateResult(BaseModel):
    conversation_id: str = ""
    display_name: str = ""


class SendMessageResult(BaseModel):
    conversation_id: str = ""
