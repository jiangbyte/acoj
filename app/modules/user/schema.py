from app.core.config.enums import AccountType
from app.core.schema.base import ApiSchema
from app.modules.user.admin.schema import AdminProfileResponse
from app.modules.user.portal.schema import PortalProfileResponse


class AdminMeResponse(ApiSchema):
    """管理端当前登录账户信息响应模型。"""

    account_id: str
    account: str
    account_type: AccountType
    name: str | None = None
    nickname: str | None = None
    avatar: str | None = None
    role_ids: list[str]
    dept_ids: list[str]
    group_ids: list[str]
    permission_keys: list[str]
    button_codes: list[str]
    profile: AdminProfileResponse


class PortalMeResponse(ApiSchema):
    """门户端当前登录账户信息响应模型。"""

    account_id: str
    account: str
    account_type: AccountType
    profile: PortalProfileResponse
