"""Migration registration for plugin_sys.config."""

from core.db import register_model
from .models import SysConfig

register_model(SysConfig)
