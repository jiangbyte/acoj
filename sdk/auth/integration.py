from __future__ import annotations

from typing import Any

from fastapi import Request
from micosauth import (
    MicosAuthUtil,
    MicosRealmSetting,
    MicosRedisSetting,
    MicosService,
    MicosSessionUtil,
    MicosSetting,
)
from micosauth.adapters.fastapi.context import get_micos_service as get_service_from_request
from micosauth.provider import EmptyMicosAccessProvider, MicosAccessProvider

from sdk.config.settings import settings

BUSINESS_REALM_ID = "BUSINESS"
CONSUMER_REALM_ID = "CONSUMER"

_service: MicosService | None = None
_auth_util: MicosAuthUtil | None = None
_session_util: MicosSessionUtil | None = None


def build_micos_service() -> MicosService:
    redis_setting = MicosRedisSetting(
        host=settings.redis.host,
        port=settings.redis.port,
        password=settings.redis.password or None,
        db=settings.redis.database,
        max_connections=settings.redis.max_connections,
        socket_connect_timeout=settings.redis.socket_connect_timeout,
        socket_timeout=settings.redis.socket_timeout,
        retry_on_timeout=settings.redis.retry_on_timeout,
        health_check_interval=settings.redis.health_check_interval,
    )
    micos_setting = MicosSetting(redis=redis_setting)
    service = MicosService(micos_setting)
    service.register_realm(
        MicosRealmSetting(
            realm_id=BUSINESS_REALM_ID,
            token_name=settings.token.token_name,
            token_ttl_seconds=settings.token.expire_seconds,
            access_provider=EmptyMicosAccessProvider(),
        )
    )
    service.register_realm(
        MicosRealmSetting(
            realm_id=CONSUMER_REALM_ID,
            token_name=settings.token.token_name,
            token_ttl_seconds=settings.token.expire_seconds,
            access_provider=EmptyMicosAccessProvider(),
        )
    )
    return service


def init_micosauth(service: MicosService | None = None) -> MicosService:
    global _service, _auth_util, _session_util
    if service is None:
        service = build_micos_service()
    _service = service
    _auth_util = MicosAuthUtil(service)
    _session_util = MicosSessionUtil(service)
    return service


def get_micos_service() -> MicosService:
    global _service
    if _service is None:
        _service = build_micos_service()
    return _service


def get_auth_util() -> MicosAuthUtil:
    global _auth_util
    if _auth_util is None:
        _auth_util = MicosAuthUtil(get_micos_service())
    return _auth_util


def get_micos_session_util() -> MicosSessionUtil:
    global _session_util
    if _session_util is None:
        _session_util = MicosSessionUtil(get_micos_service())
    return _session_util


def register_access_provider(realm_id: str, provider: MicosAccessProvider) -> None:
    get_micos_service().register_access_provider(realm_id, provider)


async def get_current_login_id(request: Request, realm_id: str | None = None) -> str:
    resolved_realm_id = await get_current_realm_id(request, realm_id)
    if not resolved_realm_id:
        return ""
    login_id = str(getattr(request.state, "micos_login_id", "") or "")
    state_realm_id = str(getattr(request.state, "micos_realm_id", "") or "")
    if login_id and state_realm_id == resolved_realm_id:
        return login_id
    token = await _extract_token(request, resolved_realm_id)
    if not token:
        return ""
    result = await get_auth_util().inspect_token(token, resolved_realm_id)
    if not result.valid:
        return ""
    _apply_result_to_request(request, token, result)
    return result.login_id


async def get_current_realm_id(request: Request, realm_id: str | None = None) -> str:
    if realm_id:
        return realm_id
    value = str(getattr(request.state, "micos_realm_id", "") or "")
    return value


async def get_current_auth_context(request: Request, realm_id: str | None = None) -> tuple[str, str]:
    resolved_realm_id = await get_current_realm_id(request, realm_id)
    if not resolved_realm_id:
        return "", ""
    login_id = await get_current_login_id(request, resolved_realm_id)
    return login_id, resolved_realm_id


async def _extract_token(request: Request, realm_id: str) -> str:
    try:
        service = get_service_from_request(request)
    except Exception:
        service = get_micos_service()
    realm = service.get_realm(realm_id)
    token = request.headers.get(realm.token_name) or request.cookies.get(realm.token_name)
    return str(token or "")


def _apply_result_to_request(request: Request, token: str, result) -> None:
    request.state.micos_token = token
    request.state.micos_realm_id = result.realm_id
    request.state.micos_login_id = result.login_id
    request.state.micos_claims = result.claims
    request.state.micos_session = result.session
    request.state.micos_acl = result.acl
