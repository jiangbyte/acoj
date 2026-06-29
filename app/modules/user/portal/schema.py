from datetime import datetime

from pydantic import Field

from app.core.schema.base import ApiSchema


class PortalProfileResponse(ApiSchema):
    """门户账户扩展资料响应模型。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    bio: str | None = None
    level: str | None = None
    created_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])
    updated_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])


class PortalProfileUpsertPayload(ApiSchema):
    """门户账户资料写入载荷。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    bio: str | None = None
    level: str | None = None
