"""Migration registration for plugin_sys.dict."""

from core.db import register_model
from .models import SysDict

register_model(SysDict)
