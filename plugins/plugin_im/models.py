"""
plugin_im — Consolidated ORM model registration.

Follows the same pattern as plugins/plugin_sys/models.py.
"""

import logging
from core.plugin.registry import register_model, get_registered_models

logger = logging.getLogger(__name__)

# Import all model classes
from plugins.plugin_im.model.message import Message, Conversation, ConversationUnread
from plugins.plugin_im.model.im_file import ImFile
from plugins.plugin_im.model.broadcast import Broadcast, BroadcastRead
from plugins.plugin_im.model.friend import FriendRequest, Friendship, FriendBlock
from plugins.plugin_im.model.group import Group, GroupMember, GroupJoinRequest, GroupMessage, GroupMessageRead

# Register all models
register_model(Message)
register_model(Conversation)
register_model(ConversationUnread)
register_model(ImFile)
register_model(Broadcast)
register_model(BroadcastRead)
register_model(FriendRequest)
register_model(Friendship)
register_model(FriendBlock)
register_model(Group)
register_model(GroupMember)
register_model(GroupJoinRequest)
register_model(GroupMessage)
register_model(GroupMessageRead)

logger.info(
    "[plugin_im.models] Registered %d IM models via HeiBase",
    len(get_registered_models()),
)
