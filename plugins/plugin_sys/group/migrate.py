"""Migration registration for plugin_sys.group."""

from core.db import register_model
from .models import SysGroup

register_model(SysGroup)
