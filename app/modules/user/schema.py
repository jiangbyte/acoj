from app.core.config.enums import LoginScope, UserType
from app.core.schema.base import ApiSchema
from app.modules.user.admin.schema import AdminProfileResponse
from app.modules.user.portal.schema import PortalProfileResponse


class AdminMeResponse(ApiSchema):
    """管理端当前登录账户信息响应模型。"""

    account_id: str
    account_type: UserType
    login_scope: LoginScope
    profile: AdminProfileResponse


class PortalMeResponse(ApiSchema):
    """门户端当前登录账户信息响应模型。"""

    account_id: str
    account_type: UserType
    login_scope: LoginScope
    profile: PortalProfileResponse
