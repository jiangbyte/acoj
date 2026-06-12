"""Centralized migration registration for plugin_im."""

from sdk.kernel.registry import register_model
from plugins.plugin_im.model.message import Message, Conversation, ConversationUnread
from plugins.plugin_im.model.im_file import ImFile
from plugins.plugin_im.model.broadcast import Broadcast, BroadcastRead
from plugins.plugin_im.model.friend import FriendRequest, Friendship, FriendBlock
from plugins.plugin_im.model.group import Group, GroupMember, GroupJoinRequest, GroupMessage, GroupMessageRead


def register_all_models() -> None:
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


__all__ = ["register_all_models"]
