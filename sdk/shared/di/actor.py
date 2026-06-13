from __future__ import annotations

from fastapi import Request
from pydantic import BaseModel

from sdk.auth import Business, Consumer


class ActorContext(BaseModel):
    user_id: str = ""
    realm_id: str = ""
    username: str = ""


async def get_current_actor(request: Request) -> ActorContext:
    user_id = await Business.get_login_id(request) or ""
    return ActorContext(user_id=user_id, realm_id="BUSINESS")


async def get_current_client_actor(request: Request) -> ActorContext:
    user_id = await Consumer.get_login_id(request) or ""
    return ActorContext(user_id=user_id, realm_id="CONSUMER")
