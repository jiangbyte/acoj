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
from sdk.infra.db import get_redis

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
    """初始化和关闭 micosauth。"""

    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="auth",
            version="1.0.0",
            description="Authentication & permission system",
            dependencies=["utils"],
            settings_prefix="plugins.auth",
        )

    def on_init(self):
        from sdk.auth import init_micosauth

        init_micosauth()
        logger.info("[AuthPlugin] micosauth initialised")

    async def on_start(self):
        """启动 micosauth。"""
        app = get_current_app()
        if app is None:
            logger.warning("[AuthPlugin] No app reference")
        from sdk.auth import get_micos_service

        await get_micos_service().init()

    async def on_stop(self):
        from sdk.auth import get_micos_service

        await get_micos_service().close()
        logger.info("[AuthPlugin] micosauth stopped")


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
            settings_prefix="plugins.utils",
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
            dependencies=["auth"],
            settings_prefix="plugins.captcha",
        )

    async def on_start(self):
        """Captcha needs Redis — initialise after Redis is ready."""
        from sdk.captcha import b_captcha, c_captcha

        redis = get_redis()
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
            settings_prefix="plugins.scheduler",
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
