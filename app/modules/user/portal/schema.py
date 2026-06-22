from datetime import datetime

from pydantic import Field

from app.core.schema.base import ApiSchema


class PortalProfileResponse(ApiSchema):
    """门户账户扩展资料响应模型。"""

    account_id: str
    nickname: str | None = None
    avatar_url: str | None = None
    bio: str | None = None
    level: str | None = None
    created_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])
    updated_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])
