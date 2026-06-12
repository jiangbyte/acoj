from contextvars import ContextVar
from typing import Optional
from fastapi import Request

from sdk.enums import LoginTypeEnum
from sdk.constants import TOKEN_PREFIX_BUSINESS, SESSION_PREFIX_BUSINESS, DISABLE_KEY_BUSINESS
from .base_auth_tool import BaseAuthTool


class HeiAuthTool(BaseAuthTool):
    TYPE = LoginTypeEnum.BUSINESS
    TOKEN_PREFIX = TOKEN_PREFIX_BUSINESS
    SESSION_PREFIX = SESSION_PREFIX_BUSINESS
    DISABLE_KEY = DISABLE_KEY_BUSINESS
    _request_var: ContextVar[Optional[Request]] = ContextVar('hei_auth_request', default=None)
