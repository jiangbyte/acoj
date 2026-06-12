"""
plugin_im — Instant messaging plugin.

Each import triggers:
  1. plugin registration
  2. model registration preparation
  3. module router registration
"""

import logging

logger = logging.getLogger(__name__)

# 1. Plugin class
from . import plugin

# 2. Models
from . import model

# 3. Feature modules
from . import broadcast, friend, group, message

logger.info("plugin_im loaded successfully")
