from contextvars import ContextVar
from typing import Optional
from fastapi import Request

from .base_auth_tool import BaseAuthTool


TOKEN_PREFIX = "hei:auth:BUSINESS:token:"
SESSION_PREFIX = "hei:auth:BUSINESS:session:"
DISABLE_KEY_PREFIX = "hei:auth:BUSINESS:disable:"


class BusinessAuthTool(BaseAuthTool):
    REALM_ID = "BUSINESS"
    TOKEN_PREFIX = TOKEN_PREFIX
    SESSION_PREFIX = SESSION_PREFIX
    DISABLE_KEY_PREFIX = DISABLE_KEY_PREFIX
    _request_var: ContextVar[Optional[Request]] = ContextVar('business_auth_request', default=None)
