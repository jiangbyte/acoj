"""Migration registration for plugin_sys.position."""

from core.db import register_model
from .models import SysPosition

register_model(SysPosition)
