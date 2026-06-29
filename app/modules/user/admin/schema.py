from datetime import datetime

from pydantic import Field

from app.core.schema.base import ApiSchema


class AdminProfileResponse(ApiSchema):
    """管理端账户扩展资料响应模型。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    title: str | None = None
    employee_no: str | None = None
    remark: str | None = None
    created_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])
    updated_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])


class AdminProfileUpsertPayload(ApiSchema):
    """管理端账户资料写入载荷。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    title: str | None = None
    employee_no: str | None = None
    remark: str | None = None
