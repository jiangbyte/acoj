"""Migration registration for plugin_sys.log."""

from core.db import register_model
from .models import SysLog

register_model(SysLog)
