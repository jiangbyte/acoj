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


class PortalPublicProfileResponse(ApiSchema):
    """门户公开主页资料响应模型。"""

    account_id: str
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    signature: str | None = None
    bio: str | None = None
    level: str | None = None


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


class PortalUserCenterProfileUpdateRequest(ApiSchema):
    """当前门户用户个人资料更新请求。"""

    name: str = Field(min_length=1, max_length=64)
    nickname: str | None = Field(default=None, max_length=64)
    avatar: str | None = None
    signature: str | None = None
    bio: str | None = Field(default=None, max_length=255)


class PortalUserCenterPasswordUpdateRequest(ApiSchema):
    """当前门户用户修改密码请求。"""

    old_password: str = Field(min_length=1, max_length=128)
    new_password: str = Field(min_length=6, max_length=128)


class PortalUserCenterPhoneUpdateRequest(ApiSchema):
    """当前门户用户手机号绑定更新请求。"""

    password: str = Field(min_length=1, max_length=128)
    phone: str | None = Field(default=None, max_length=32)


class PortalUserCenterEmailUpdateRequest(ApiSchema):
    """当前门户用户邮箱绑定更新请求。"""

    password: str = Field(min_length=1, max_length=128)
    email: str | None = Field(default=None, max_length=128)


class PortalUserCenterAvatarUpdateResponse(ApiSchema):
    """当前门户用户头像更新响应。"""

    avatar: str
    file_id: str
    object_name: str
    url: str
