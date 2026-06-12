from __future__ import annotations

from typing import Optional

from fastapi import Depends, Request
from pydantic import BaseModel

from sdk.auth import HeiAuthTool, HeiClientAuthTool


class ActorContext(BaseModel):
    user_id: str = ""
    login_type: str = ""
    username: str = ""


async def get_current_actor(request: Request) -> ActorContext:
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request) or ""
    return ActorContext(user_id=user_id, login_type="BUSINESS")


async def get_current_client_actor(request: Request) -> ActorContext:
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request) or ""
    return ActorContext(user_id=user_id, login_type="CONSUMER")
