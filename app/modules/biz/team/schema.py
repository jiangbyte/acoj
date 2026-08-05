"""Team DTOs."""

from datetime import datetime
from typing import Any

from pydantic import Field

from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.biz.team.enums import TeamMemberRole, TeamScope, TeamStatus, TeamVisibility


class OjTeamCreateIndependentRequest(ApiSchema):
    name: str = Field(min_length=1, max_length=200)
    description: str | None = None
    max_members: int = Field(default=50, ge=2, le=500)


class OjTeamCreateCourseRequest(ApiSchema):
    course_id: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=200)
    description: str | None = None
    visibility: TeamVisibility = TeamVisibility.PRIVATE
    max_members: int = Field(default=50, ge=2, le=500)
    member_account_ids: list[str] = Field(default_factory=list)


class OjTeamUpdateRequest(ApiSchema):
    id: str = Field(min_length=1, max_length=64)
    name: str | None = None
    description: str | None = None
    max_members: int | None = None
    status: TeamStatus | None = None
    visibility: TeamVisibility | None = None


class OjTeamAdminPageQuery(ApiSchema):
    pagination: PageQuery
    scope: TeamScope | None = None
    course_id: str | None = None
    name: str | None = None
    status: TeamStatus | None = None
    visibility: TeamVisibility | None = None


class OjTeamPortalPageQuery(ApiSchema):
    pagination: PageQuery
    keyword: str | None = None


class OjTeamPublicSchema(ApiSchema):
    """公开独立小组浏览，不暴露邀请码 / IM。"""

    id: str
    name: str
    description: str | None = None
    status: str
    visibility: str
    max_members: int
    member_count: int
    created_at: datetime
    is_member: bool = False


class OjTeamSchema(ApiSchema):
    id: str
    scope: str
    course_id: str | None = None
    class_id: str | None = None
    name: str
    description: str | None = None
    owner_id: str
    invite_code: str | None = None
    im_group_id: str | None = None
    status: str
    visibility: str
    max_members: int
    member_count: int
    extra: dict[str, Any]
    created_at: datetime
    updated_at: datetime
    conversation_id: str | None = None
    is_member: bool = False


class OjTeamMemberSchema(ApiSchema):
    id: str
    team_id: str
    account_id: str
    role: str
    joined_at: datetime


class OjTeamMemberAddRequest(ApiSchema):
    team_id: str = Field(min_length=1, max_length=64)
    account_ids: list[str] = Field(min_length=1)


class OjTeamMemberRemoveRequest(ApiSchema):
    team_id: str = Field(min_length=1, max_length=64)
    account_id: str = Field(min_length=1, max_length=64)


class OjTeamJoinRequest(ApiSchema):
    invite_code: str = Field(min_length=8, max_length=8)


class OjTeamOwnerUpdateRequest(ApiSchema):
    """Portal 组长更新小组；课内不可改 visibility。"""

    id: str = Field(min_length=1, max_length=64)
    name: str | None = Field(default=None, min_length=1, max_length=200)
    description: str | None = None
    max_members: int | None = Field(default=None, ge=2, le=500)
    visibility: TeamVisibility | None = None


class OjTeamInviteRefreshRequest(ApiSchema):
    team_id: str = Field(min_length=1, max_length=64)


class OjTeamUserSearchItem(ApiSchema):
    account_id: str
    username: str | None = None
    nickname: str | None = None
    avatar: str | None = None
