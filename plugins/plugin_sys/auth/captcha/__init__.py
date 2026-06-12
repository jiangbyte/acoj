from sdk.captcha import CaptchaResult, b_captcha, c_captcha
from .api import v1_router as router

from sdk.kernel.registry import register_router
register_router(router)

__all__ = ["CaptchaResult", "b_captcha", "c_captcha", "router"]
