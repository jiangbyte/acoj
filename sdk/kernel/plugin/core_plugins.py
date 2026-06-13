"""
Core service plugins — mirrors hei-gin's ``sdk/auth/module.go``,
``sdk/captcha/module.go``, ``sdk/utils/module.go``, ``sdk/scheduler/module.go``.

Each core service is registered explicitly by the kernel assembly layer,
mirroring hei-gin's ``module.Register()`` flow without relying on Python
import side effects.
"""

from __future__ import annotations

import logging
from typing import TYPE_CHECKING

from sdk.kernel.plugin import HeiPlugin, PluginInfo
from sdk.config.settings import settings

if TYPE_CHECKING:
    from fastapi import FastAPI

logger = logging.getLogger(__name__)

# Module-level reference set by lifespan so plugins can access the app
_current_app: FastAPI | None = None


def set_current_app(app: FastAPI) -> None:
    """Store the current FastAPI app for plugin access."""
    global _current_app
    _current_app = app


def get_current_app() -> FastAPI | None:
    """Return the current FastAPI app instance."""
    return _current_app


# ═════════════════════════════════════════════════════════════════════
# AuthPlugin  —  mirrors hei-gin sdk/auth/module.go
# ═════════════════════════════════════════════════════════════════════

class AuthPlugin(HeiPlugin):
    """Initialises auth tools and runs permission scan on start.

    Mirrors hei-gin's ``sdk/auth/authModule``.
    """

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="auth",
            version="1.0.0",
            description="Authentication & permission system",
        )

    def on_init(self):
        from sdk.auth import Business, Consumer

        Business.init(
            expire=settings.token.expire_seconds,
            token_name=settings.token.token_name,
        )
        Consumer.init(
            expire=settings.token.expire_seconds,
            token_name=settings.token.token_name,
        )
        logger.info("[AuthPlugin] AuthTool initialised")

    async def on_start(self):
        """Run permission auto-discovery after all routes are mounted."""
        app = get_current_app()
        if app is None:
            logger.warning("[AuthPlugin] No app reference — skipping permission scan")
            return
        try:
            from sdk.auth.permission_scan import run_permission_scan
            await run_permission_scan(app)
        except Exception as e:
            logger.warning("[AuthPlugin] Permission scan skipped: %s", e)


# ═════════════════════════════════════════════════════════════════════
# UtilsPlugin  —  mirrors hei-gin sdk/utils/module.go
# ═════════════════════════════════════════════════════════════════════

class UtilsPlugin(HeiPlugin):
    """Initialises SM2 cryptography.

    Mirrors hei-gin's ``sdk/utils/utilsModule``.
    """

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="utils",
            version="1.0.0",
            description="SM2 cryptography initialisation",
        )

    def on_init(self):
        from sdk.utils import init as sm2_init
        sm2_init(settings.sm2.private_key, settings.sm2.public_key)
        logger.info("[UtilsPlugin] SM2 initialised")


# ═════════════════════════════════════════════════════════════════════
# CaptchaPlugin  —  mirrors hei-gin sdk/captcha/module.go
# ═════════════════════════════════════════════════════════════════════

class CaptchaPlugin(HeiPlugin):
    """Initialises captcha services (needs Redis).

    Mirrors hei-gin's ``sdk/captcha/captchaModule``.
    """

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="captcha",
            version="1.0.0",
            description="Captcha service initialisation",
        )

    async def on_start(self):
        """Captcha needs Redis — initialise after Redis is ready."""
        from sdk.infra.db.redis import get_client
        from sdk.captcha import b_captcha, c_captcha

        redis = get_client()
        b_captcha.init(redis)
        c_captcha.init(redis)
        logger.info("[CaptchaPlugin] Captcha initialised")


# ═════════════════════════════════════════════════════════════════════
# SchedulerPlugin  —  mirrors hei-gin sdk/scheduler/module.go
# ═════════════════════════════════════════════════════════════════════

class SchedulerPlugin(HeiPlugin):
    """Starts/stops the background task scheduler.

    Mirrors hei-gin's ``sdk/scheduler/schedulerModule``.
    """

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="scheduler",
            version="1.0.0",
            description="Cron-based background task scheduler",
        )

    async def on_start(self):
        from sdk.infra.scheduler import start as scheduler_start
        scheduler_start()
        logger.info("[SchedulerPlugin] Scheduler started")

    async def on_stop(self):
        from sdk.infra.scheduler import stop as scheduler_stop
        scheduler_stop()
        logger.info("[SchedulerPlugin] Scheduler stopped")


CORE_PLUGIN_CLASSES: tuple[type[HeiPlugin], ...] = (
    AuthPlugin,
    UtilsPlugin,
    CaptchaPlugin,
    SchedulerPlugin,
)
