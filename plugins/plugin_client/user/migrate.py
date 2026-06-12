"""Migration registration for plugin_client.user."""

from core.db import register_model
from .models import ClientUser

register_model(ClientUser)
