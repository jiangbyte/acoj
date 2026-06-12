"""plugin_im compatibility exports."""

import logging
from sdk.kernel.registry import get_registered_models

logger = logging.getLogger(__name__)

from plugins.plugin_im.model.message import Message, Conversation, ConversationUnread
from plugins.plugin_im.model.im_file import ImFile
from plugins.plugin_im.model.broadcast import Broadcast, BroadcastRead
from plugins.plugin_im.model.friend import FriendRequest, Friendship, FriendBlock
from plugins.plugin_im.model.group import Group, GroupMember, GroupJoinRequest, GroupMessage, GroupMessageRead

logger.info(
    "[plugin_im.models] Loaded %d registered models",
    len(get_registered_models()),
)
