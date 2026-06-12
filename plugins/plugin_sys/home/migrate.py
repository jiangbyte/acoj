"""Migration registration for plugin_sys.home."""

from core.db import register_model
from .models import SysQuickAction

register_model(SysQuickAction)
