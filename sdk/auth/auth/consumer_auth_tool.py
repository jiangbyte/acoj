from contextvars import ContextVar
from typing import Optional
from fastapi import Request

from sdk.auth.enums import RealmID
from .base_auth_tool import BaseAuthTool


TOKEN_PREFIX = "hei:auth:CONSUMER:token:"
SESSION_PREFIX = "hei:auth:CONSUMER:session:"
DISABLE_KEY_PREFIX = "hei:auth:CONSUMER:disable:"


class ConsumerAuthTool(BaseAuthTool):
    REALM_ID = RealmID.CONSUMER
    TOKEN_PREFIX = TOKEN_PREFIX
    SESSION_PREFIX = SESSION_PREFIX
    DISABLE_KEY_PREFIX = DISABLE_KEY_PREFIX
    _request_var: ContextVar[Optional[Request]] = ContextVar('consumer_auth_request', default=None)
