"""Class DTOs."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.clazz.enums import ClassMemberRole, ClassStatus, ClassVisibility


class OjClassCreateRequest(ApiSchema):
    code: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=200)
    summary: str | None = None
    visibility: ClassVisibility = ClassVisibility.PRIVATE
    extra: dict[str, Any] = Field(default_factory=dict)


class OjClassUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    code: str | None = None
    name: str | None = None
    summary: str | None = None
    status: ClassStatus | None = None
    visibility: ClassVisibility | None = None
    extra: dict[str, Any] | None = None


class OjClassAdminPageQuery(ApiSchema):
    pagination: PageQuery
    code: str | None = None
    name: str | None = None
    status: ClassStatus | None = None
    visibility: ClassVisibility | None = None


class OjClassPortalPageQuery(ApiSchema):
    pagination: PageQuery
    keyword: str | None = None


class OjClassPublicSchema(ApiSchema):
    """公开浏览用，不暴露邀请码 / IM。"""

    id: str
    code: str
    name: str
    summary: str | None = None
    status: str
    visibility: str
    member_count: int
    created_at: datetime
    joined: bool = False


class OjClassSchema(ApiSchema):
    id: str
    code: str
    name: str
    summary: str | None = None
    invite_code: str | None = None
    status: str
    visibility: str
    im_group_id: str | None = None
    member_count: int
    extra: dict[str, Any]
    created_at: datetime
    created_by: str | None = None
    updated_at: datetime
    updated_by: str | None = None
    conversation_id: str | None = None
    joined: bool = False


class OjClassMemberSchema(ApiSchema):
    id: str
    class_id: str
    account_id: str
    role: str
    joined_at: datetime
    left_at: datetime | None = None


class OjClassMemberAddRequest(ApiSchema):
    class_id: str = Field(min_length=1, max_length=64)
    account_ids: list[str] = Field(min_length=1)
    role: ClassMemberRole = ClassMemberRole.STUDENT


class OjClassMemberRemoveRequest(ApiSchema):
    class_id: str = Field(min_length=1, max_length=64)
    account_id: str = Field(min_length=1, max_length=64)


class OjClassJoinRequest(ApiSchema):
    invite_code: str = Field(min_length=8, max_length=8)


class OjClassRefreshInviteRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
