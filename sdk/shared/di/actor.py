from __future__ import annotations

from fastapi import Request
from pydantic import BaseModel


class ActorContext(BaseModel):
    user_id: str = ""
    realm_id: str = ""


async def get_current_actor(request: Request) -> ActorContext:
    user_id = str(getattr(request.state, "micos_login_id", "") or "")
    realm_id = str(getattr(request.state, "micos_realm_id", "") or "")
    if not realm_id:
        return ActorContext()
    return ActorContext(user_id=user_id, realm_id=realm_id)


async def get_current_client_actor(request: Request) -> ActorContext:
    return await get_current_actor(request)
