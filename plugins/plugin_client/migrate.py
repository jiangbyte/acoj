"""Centralized migration registration for plugin_client."""

from core.db import register_model

from plugins.plugin_client.user.models import ClientUser


def register_all_models() -> None:
    register_model(ClientUser)
