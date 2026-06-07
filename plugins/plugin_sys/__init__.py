"""
plugin_sys — System management plugin.

Self-registration entry point (对标 hei-gin blank imports ``imports.go``).

Each sub-module import triggers:
  1. ``HeiPlugin.__init_subclass__`` — plugin class registration
  2. ``HeiBase.__init_subclass__`` — model registration
  3. ``register_router(router)`` — route registration at module level
"""

import logging
from core.plugin.registry import register_router
from core.app.health import router as health_router

logger = logging.getLogger(__name__)

# ── Register health route ───────────────────────────────────────────
register_router(health_router)

# ── 1. Plugin class ─────────────────────────────────────────────────
from . import plugin

# ── 2. Models (auto-registers via HeiBase) ──────────────────────────
from . import models

# ── 3. Sub-modules (each auto-registers its router) ─────────────────
from . import user, role, permission, resource
from . import dict, config, banner, log
from . import notice, org, position, group, file
from . import home, session, analyze
from .auth import captcha, sm2, username

logger.info("plugin_sys loaded successfully")
