from pydantic import Field

from app.core.config.enums import LoginScope, UserType
from app.core.response.schema import ApiResponse
from app.core.schema.base import ApiSchema


class LoginRequest(ApiSchema):
    account: str = Field(min_length=3, max_length=64)
    password: str = Field(min_length=6, max_length=128)


class LoginPayload(ApiSchema):
    """登录服务载荷，包含登录域。"""

    account: str
    password: str
    login_scope: LoginScope


class LoginResponse(ApiSchema):
    token: str
    account_id: str
    account_type: UserType
    login_scope: LoginScope


class LogoutResponse(ApiSchema):
    success: bool = True


LoginApiResponse = ApiResponse[LoginResponse]
LogoutApiResponse = ApiResponse[LogoutResponse]
