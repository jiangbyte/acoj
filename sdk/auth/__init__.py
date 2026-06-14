from __future__ import annotations

from .integration import (
    BUSINESS_REALM_ID,
    CONSUMER_REALM_ID,
    build_micos_service,
    get_auth_util,
    get_current_auth_context,
    get_current_login_id,
    get_current_realm_id,
    get_micos_service,
    get_micos_session_util,
    init_micosauth,
    register_access_provider,
)

BusinessID = BUSINESS_REALM_ID
ConsumerID = CONSUMER_REALM_ID

__all__ = [
    "BusinessID",
    "ConsumerID",
    "build_micos_service",
    "get_auth_util",
    "get_current_auth_context",
    "get_current_login_id",
    "get_current_realm_id",
    "get_micos_service",
    "get_micos_session_util",
    "init_micosauth",
    "register_access_provider",
]
