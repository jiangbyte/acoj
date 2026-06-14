from __future__ import annotations

from fastapi import Request
from micosauth import (
    MicosRealmSetting,
    MicosRedisSetting,
    MicosService,
    MicosSetting,
    build_runtime,
)
from micosauth.app import MicosRuntime
from micosauth.config import EmptyMicosAccessProvider, MicosAccessProvider
from micosauth.utils.auth import MicosAuthUtil
from micosauth.utils.session import MicosSessionUtil

from sdk.config.settings import settings

BUSINESS_REALM_ID = "BUSINESS"
CONSUMER_REALM_ID = "CONSUMER"

_service: MicosService | None = None
_runtime: MicosRuntime | None = None


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
    service = MicosService(MicosSetting(redis=redis_setting))
    service.register_realm(
        MicosRealmSetting(
            realm_id=BUSINESS_REALM_ID,
            token_name=settings.token.token_name,
            token_ttl_seconds=settings.token.expire_seconds,
            keep_old_token_on_same_device_login=True,
            access_provider=EmptyMicosAccessProvider(),
        )
    )
    service.register_realm(
        MicosRealmSetting(
            realm_id=CONSUMER_REALM_ID,
            token_name=settings.token.token_name,
            token_ttl_seconds=settings.token.expire_seconds,
            keep_old_token_on_same_device_login=True,
            access_provider=EmptyMicosAccessProvider(),
        )
    )
    return service


def init_micosauth(service: MicosService | None = None) -> MicosService:
    global _service, _runtime
    service = service or build_micos_service()
    _service = service
    _runtime = build_runtime(service)
    return service


def get_micos_runtime() -> MicosRuntime:
    global _service, _runtime
    if _runtime is None:
        _service = _service or build_micos_service()
        _runtime = build_runtime(_service)
    return _runtime


def get_micos_service() -> MicosService:
    return get_micos_runtime().service


def get_auth_util() -> MicosAuthUtil:
    return get_micos_runtime().auth


def get_micos_session_util() -> MicosSessionUtil:
    return get_micos_runtime().session


def register_access_provider(realm_id: str, provider: MicosAccessProvider) -> None:
    get_micos_service().register_access_provider(realm_id, provider)


def invalidate_acl_cache(realm_id: str, login_id: str | None = None) -> None:
    get_micos_service().invalidate_acl_cache(realm_id, login_id)


async def get_current_login_id(request: Request, realm_id: str | None = None) -> str:
    if realm_id and getattr(request.state, "micos_realm_id", "") == realm_id:
        return str(getattr(request.state, "micos_login_id", "") or "")
    if not realm_id:
        return str(getattr(request.state, "micos_login_id", "") or "")
    token = str(getattr(request.state, "micos_token", "") or "")
    if not token:
        return ""
    result = await get_auth_util().inspect_token(token, realm_id)
    if not result.valid:
        return ""
    return result.login_id
