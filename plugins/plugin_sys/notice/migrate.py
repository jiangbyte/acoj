"""Migration registration for plugin_sys.notice."""

from core.db import register_model
from .models import SysNotice

register_model(SysNotice)
