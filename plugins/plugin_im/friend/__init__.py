from .service import FriendService, get_friend_service
from .api.v1.api import sys_router as router, client_router

__all__ = [
    "FriendService", "get_friend_service",
    "router", "client_router",
]
