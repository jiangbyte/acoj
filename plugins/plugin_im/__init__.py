"""
plugin_im — Instant Messaging Plugin.

Includes:
- WebSocket real-time messaging (ws/)
- Friend management (friend/)
- Group chat (group/)
- Single-chat messages (message/)
- Broadcast announcements (broadcast/)
"""

# Import plugin class first (auto-registers via __init_subclass__)
from plugins.plugin_im.plugin import IMPlugin

# Import sub-modules to trigger module-level register_router() calls
from plugins.plugin_im import broadcast
from plugins.plugin_im import friend
from plugins.plugin_im import group
from plugins.plugin_im import message
from plugins.plugin_im import model

__all__ = ["IMPlugin"]
