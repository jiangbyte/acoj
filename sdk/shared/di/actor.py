from __future__ import annotations

from fastapi import Request
from pydantic import BaseModel

from sdk.auth import get_current_auth_context


class ActorContext(BaseModel):
    user_id: str = ""
    realm_id: str = ""
    username: str = ""


async def get_current_actor(request: Request) -> ActorContext:
    user_id, realm_id = await get_current_auth_context(request)
    if not realm_id:
        return ActorContext()
    username = str(getattr(request.state, "loginUser", "") or "")
    return ActorContext(user_id=user_id, realm_id=realm_id, username=username)


async def get_current_client_actor(request: Request) -> ActorContext:
    return await get_current_actor(request)
