"""Migration registration for plugin_sys.org."""

from core.db import register_model
from .models import SysOrg

register_model(SysOrg)
