"""Migration registration for plugin_sys.banner."""

from core.db import register_model
from .models import SysBanner

register_model(SysBanner)
