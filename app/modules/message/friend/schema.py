from datetime import datetime

from pydantic import Field

from app.core.config.enums import AccountType
from app.core.response.pagination import PageQuery
from app.core.schema.base import ApiSchema
from app.modules.message.enums import FriendRequestStatus, FriendStatus


class FriendApplyRequest(ApiSchema):
    friend_account_type: AccountType = Field(description="好友账户类型")
    friend_account_id: str = Field(min_length=1, max_length=64, description="好友账户ID")
    message: str | None = Field(default=None, max_length=500, description="申请附言")


class FriendHandleRequest(ApiSchema):
    request_id: str = Field(min_length=1, max_length=64, description="申请ID")
    accept: bool = Field(description="是否同意")


class FriendRemoveRequest(ApiSchema):
    friend_account_type: AccountType = Field(description="好友账户类型")
    friend_account_id: str = Field(min_length=1, max_length=64, description="好友账户ID")


class FriendSetRemarkRequest(ApiSchema):
    friend_account_type: AccountType = Field(description="好友账户类型")
    friend_account_id: str = Field(min_length=1, max_length=64, description="好友账户ID")
    remark: str | None = Field(default=None, max_length=64, description="备注名")


class FriendRequestSchema(ApiSchema):
    id: str
    applicant_type: AccountType | str
    applicant_id: str
    recipient_type: AccountType | str
    recipient_id: str
    message: str | None = None
    status: FriendRequestStatus | str
    applicant_name: str | None = None
    applicant_avatar: str | None = None
    created_at: datetime
    handled_at: datetime | None = None


class FriendSchema(ApiSchema):
    id: str
    account_type: AccountType | str
    account_id: str
    friend_account_type: AccountType | str
    friend_account_id: str
    remark: str | None = None
    status: FriendStatus | str
    friend_at: datetime
    friend_name: str | None = None
    friend_avatar: str | None = None
    friend_title: str | None = None
    friend_department: str | None = None


class FriendRequestCountResponse(ApiSchema):
    count: int = 0


class FriendSearchSchema(ApiSchema):
    account_type: AccountType | str
    account_id: str
    name: str | None = None
    avatar: str | None = None
    title: str | None = None
    department: str | None = None
    is_friend: bool = False
