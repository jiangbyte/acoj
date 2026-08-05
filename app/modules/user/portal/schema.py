from datetime import datetime
from typing import Literal

from pydantic import Field

from app.core.schema.base import ApiSchema
from app.core.security.transport import PasswordKeyMixin


class PortalProfileResponse(ApiSchema):
    """门户账户扩展资料响应模型。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None
    phone_login_enabled: bool = False
    email_login_enabled: bool = False
    created_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])
    updated_at: datetime | None = Field(default=None, examples=["2026-06-17T12:00:00Z"])


class PortalPublicProfileResponse(ApiSchema):
    """门户公开主页资料响应模型。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None


class PortalPublicSpaceQuery(ApiSchema):
    """门户公开主页查询。"""

    account_id: str = Field(min_length=1, max_length=64)


class PortalRatingRankItem(ApiSchema):
    """门户 Rating 排行项。"""

    rank: int
    account_id: str
    nickname: str | None = None
    avatar: str | None = None
    rating: int
    contests: int = 0
    delta: int = 0


class PortalSolvedRankItem(ApiSchema):
    """门户练习通关排行项。"""

    rank: int
    account_id: str
    nickname: str | None = None
    avatar: str | None = None
    solved: int


class PortalRankMeResponse(ApiSchema):
    """当前用户在指定榜单上的名次摘要。"""

    board: str
    rank: int | None = None
    score: int = 0
    nickname: str | None = None
    avatar: str | None = None
    contests: int = 0
    delta: int = 0


class PortalRankSummaryResponse(ApiSchema):
    """榜单汇总指标。"""

    board: str
    total_users: int = 0
    top_score: int = 0
    avg_score: int = 0
    max_delta: int = 0


class PortalRankBoardQuery(ApiSchema):
    board: Literal["solved", "rating"] = "solved"


class PortalProfileUpsertPayload(ApiSchema):
    """门户账户资料写入载荷。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    phone: str | None = None
    email: str | None = None


class PortalUserCenterProfileUpdateRequest(ApiSchema):
    """当前门户用户个人资料更新请求。"""

    name: str | None = Field(default=None, max_length=64)
    nickname: str | None = Field(default=None, max_length=64)
    avatar: str | None = None
    signature: str | None = None


class PortalUserCenterPasswordUpdateRequest(PasswordKeyMixin):
    """当前门户用户修改密码请求。"""

    old_password: str = Field(min_length=1, max_length=512)
    new_password: str = Field(min_length=1, max_length=512)


class PortalUserCenterPhoneUpdateRequest(PasswordKeyMixin):
    """当前门户用户手机号绑定更新请求。"""

    password: str = Field(min_length=1, max_length=512)
    phone: str | None = Field(default=None, max_length=32)
    phone_login_enabled: bool = False


class PortalUserCenterEmailUpdateRequest(PasswordKeyMixin):
    """当前门户用户邮箱绑定更新请求。"""

    password: str = Field(min_length=1, max_length=512)
    email: str | None = Field(default=None, max_length=128)
    email_login_enabled: bool = False


class PortalUserCenterAvatarUpdateResponse(ApiSchema):
    """当前门户用户头像更新响应。"""

    avatar: str
    file_id: str
    object_name: str
    url: str
