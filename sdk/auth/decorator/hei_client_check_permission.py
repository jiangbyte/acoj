from sdk.enums import CheckModeEnum, LoginTypeEnum
from .hei_check_permission import HeiCheckPermission


def HeiClientCheckPermission(permission, mode: str = CheckModeEnum.AND):
    return HeiCheckPermission(permission, mode, login_type=LoginTypeEnum.CONSUMER)


def hei_client_check_permission(permission, mode: str = CheckModeEnum.AND):
    return HeiClientCheckPermission(permission, mode)
