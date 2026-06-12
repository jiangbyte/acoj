from .params import UsernameLoginParam, UsernameLoginResult, UsernameRegisterParam, UsernameRegisterResult
from .logic import init_auth, do_login, do_register
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = [
    "UsernameLoginParam", "UsernameLoginResult", "UsernameRegisterParam", "UsernameRegisterResult",
    "init_auth", "do_login", "do_register",
    "router"
]
