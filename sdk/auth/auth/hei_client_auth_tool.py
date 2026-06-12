from contextvars import ContextVar
from typing import Optional
from fastapi import Request

from sdk.enums import LoginTypeEnum
from sdk.constants import TOKEN_PREFIX_CONSUMER, SESSION_PREFIX_CONSUMER, DISABLE_KEY_CONSUMER
from .base_auth_tool import BaseAuthTool


class HeiClientAuthTool(BaseAuthTool):
    TYPE = LoginTypeEnum.CONSUMER
    TOKEN_PREFIX = TOKEN_PREFIX_CONSUMER
    SESSION_PREFIX = SESSION_PREFIX_CONSUMER
    DISABLE_KEY = DISABLE_KEY_CONSUMER
    _request_var: ContextVar[Optional[Request]] = ContextVar('hei_client_auth_request', default=None)
