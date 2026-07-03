from pydantic import Field

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse
from app.core.schema.base import ApiSchema


class LoginRequest(ApiSchema):
    account: str = Field(min_length=3, max_length=128)
    password: str = Field(min_length=6, max_length=128)


class LoginPayload(ApiSchema):
    """登录服务载荷，包含目标账户类型。"""

    account: str
    password: str
    account_type: AccountType
    client_ip: str | None = None
    user_agent: str | None = None
    device_label: str | None = None


class LoginResponse(ApiSchema):
    token: str
    account_id: str
    account_type: AccountType


class RegisterRequest(ApiSchema):
    account: str = Field(min_length=3, max_length=64)
    password: str = Field(min_length=6, max_length=128)
    name: str = Field(min_length=1, max_length=64)
    nickname: str | None = Field(default=None, max_length=64)
    phone: str | None = Field(default=None, max_length=32)
    email: str | None = Field(default=None, max_length=128)


class RegisterResponse(ApiSchema):
    account_id: str
    account: str
    account_type: AccountType


class LogoutResponse(ApiSchema):
    success: bool = True


class CancelAccountRequest(ApiSchema):
    cancel_reason: str | None = Field(default=None, max_length=500)


class CancelAccountResponse(ApiSchema):
    success: bool = True


LoginApiResponse = ApiResponse[LoginResponse]
RegisterApiResponse = ApiResponse[RegisterResponse]
LogoutApiResponse = ApiResponse[LogoutResponse]
CancelAccountApiResponse = ApiResponse[CancelAccountResponse]
