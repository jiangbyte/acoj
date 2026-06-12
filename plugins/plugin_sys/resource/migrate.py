"""Migration registration for plugin_sys.resource."""

from core.db import register_model
from .models import SysModule, SysResource

register_model(SysModule)
register_model(SysResource)
