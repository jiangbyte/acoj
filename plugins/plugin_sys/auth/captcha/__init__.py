from core.captcha import CaptchaResult, b_captcha, c_captcha
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["CaptchaResult", "b_captcha", "c_captcha", "router"]
