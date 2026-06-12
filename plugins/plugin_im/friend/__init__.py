from .models import FriendRequest, Friendship, FriendBlock
from .params import (
    SendRequestParam, HandleRequestParam, FriendVO, FriendRequestVO,
    BlockVO, RemarkParam, BlockParam, SearchResult,
)
from .repository import FriendRepository
from .service import FriendService, get_friend_service
from .api.v1.api import sys_router as router, client_router

from sdk.kernel.registry import register_router

register_router(router)
register_router(client_router)

__all__ = [
    "FriendRequest", "Friendship", "FriendBlock",
    "SendRequestParam", "HandleRequestParam", "FriendVO", "FriendRequestVO",
    "BlockVO", "RemarkParam", "BlockParam", "SearchResult",
    "FriendRepository",
    "FriendService", "get_friend_service",
    "router", "client_router",
]
