from __future__ import annotations

from plugins.plugin_client.assembly import register as register_client
from plugins.plugin_im.assembly import register as register_im
from plugins.plugin_sys.assembly import register as register_sys


def register_plugins() -> None:
    register_sys()
    register_client()
    register_im()
