"""
plugin_client — Consumer-facing APIs plugin.

Self-registration entry point.
"""

import logging

logger = logging.getLogger(__name__)

from . import plugin
from . import models
from . import session, user
from .auth import captcha, sm2, username

logger.info("plugin_client loaded")
