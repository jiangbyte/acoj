from sdk.enums import LoginTypeEnum
from .hei_check_login import HeiCheckLogin


def HeiClientCheckLogin(func=None):
    return HeiCheckLogin(func, login_type=LoginTypeEnum.CONSUMER)


def hei_client_check_login(func=None):
    return HeiClientCheckLogin(func)
