"""Migration registration for plugin_sys.file."""

from core.db import register_model
from .models import SysFile

register_model(SysFile)
