from core.captcha import CaptchaResult, c_captcha
from .api import v1_router as router

from core.plugin.registry import register_router
register_router(router)

__all__ = ["CaptchaResult", "c_captcha", "router"]
