from core.enums import CheckModeEnum, LoginTypeEnum
from .hei_check_role import HeiCheckRole


def HeiClientCheckRole(role, mode: str = CheckModeEnum.AND):
    return HeiCheckRole(role, mode, login_type=LoginTypeEnum.CLIENT)


def hei_client_check_role(role, mode: str = CheckModeEnum.AND):
    return HeiClientCheckRole(role, mode)
