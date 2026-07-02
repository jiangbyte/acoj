import json
import logging
import re
from dataclasses import dataclass
from typing import Any

from fastapi import FastAPI
from fastapi.routing import APIRoute

from app.platform.cache.keys import (
    permission_resource_cache_key,
    permission_resource_method_cache_key,
)
from app.platform.cache.redis import get_redis

logger = logging.getLogger(__name__)

PERMISSION_KEY_PATTERN = re.compile(r"^[a-z0-9*]+(?::[a-z0-9*]+)+$")
API_VERSION_PREFIX_PATTERN = re.compile(r"^/api/v[0-9]+(?=/|$)")
PERMISSION_META_ATTR = "__permission_meta__"
ACCOUNT_TYPE_META_ATTR = "__account_type_meta__"

RESOURCE_NAME_FALLBACK = "未定义接口名称"


@dataclass(slots=True)
class PermissionResource:
    permission_key: str
    name: str
    route_path: str
    method: str

    @property
    def resource_text(self) -> str:
        return f"{self.permission_key}[{self.name}]"


def _iter_dependant_calls(dependant: Any) -> list[Any]:
    calls: list[Any] = []
    for dependency in getattr(dependant, "dependencies", []):
        call = getattr(dependency, "call", None)
        if call is not None:
            calls.append(call)
        calls.extend(_iter_dependant_calls(dependency))
    return calls


def _normalize_methods(route: APIRoute) -> list[str]:
    return sorted(
        method for method in (route.methods or set()) if method not in {"HEAD", "OPTIONS"}
    )


def normalize_route_path(path: str) -> str:
    normalized = path.strip() or "/"
    if normalized == "/api":
        return "/"
    normalized = API_VERSION_PREFIX_PATTERN.sub("", normalized, count=1) or "/"
    return normalized


def normalize_permission_route_path(route: APIRoute) -> str:
    normalized = normalize_route_path(route.path)
    route_tags = [str(tag).strip("/") for tag in getattr(route, "tags", []) if str(tag).strip("/")]
    for tag in route_tags:
        prefix = f"/{tag}"
        if normalized == prefix:
            return "/"
        if normalized.startswith(prefix + "/"):
            return normalized.removeprefix(prefix)
    return normalized


def _resolve_route_name(route: APIRoute) -> str:
    if route.summary:
        return route.summary
    endpoint_name = getattr(route.endpoint, "__name__", "")
    return endpoint_name or RESOURCE_NAME_FALLBACK


def _extract_permission_key(route: APIRoute) -> str | None:
    permission_keys: list[str] = []
    for call in _iter_dependant_calls(route.dependant):
        permission_meta = getattr(call, PERMISSION_META_ATTR, None)
        if not permission_meta:
            continue
        permission_key = str(permission_meta.get("permission_key", "")).strip()
        if not permission_key or not PERMISSION_KEY_PATTERN.fullmatch(permission_key):
            logger.warning(
                "Skip invalid permission key while scanning routes",
                extra={"permission_key": permission_key},
            )
            continue
        permission_keys.append(permission_key)
    if not permission_keys:
        return None
    return sorted(permission_keys)[0]


def scan_permission_registry(app: FastAPI) -> list[PermissionResource]:
    resources: list[PermissionResource] = []
    seen_permission_keys: set[str] = set()
    for route in app.routes:
        if not isinstance(route, APIRoute):
            continue
        permission_key = _extract_permission_key(route)
        if not permission_key:
            continue
        methods = _normalize_methods(route)
        if not methods:
            continue
        route_path = normalize_permission_route_path(route)
        if permission_key in seen_permission_keys:
            continue
        seen_permission_keys.add(permission_key)
        resources.append(
            PermissionResource(
                permission_key=permission_key,
                name=_resolve_route_name(route),
                route_path=route_path,
                method=methods[0],
            )
        )
    return sorted(resources, key=lambda item: item.permission_key)


async def sync_permission_registry(app: FastAPI) -> list[PermissionResource]:
    resources = scan_permission_registry(app)
    redis = get_redis()
    if not redis:
        raise RuntimeError("Redis is required to sync permission registry")

    resource_key = permission_resource_cache_key()
    method_key = permission_resource_method_cache_key()
    resource_values = [resource.resource_text for resource in resources]
    method_map = {resource.resource_text: resource.method for resource in resources}
    await redis.set(resource_key, json.dumps(resource_values, ensure_ascii=True))
    await redis.set(method_key, json.dumps(method_map, ensure_ascii=True))
    return resources


async def list_permission_resources() -> list[str]:
    redis = get_redis()
    if not redis:
        raise RuntimeError("Redis is required to read permission registry")
    raw = await redis.get(permission_resource_cache_key())
    if not raw:
        raise RuntimeError("Permission registry is not synced in Redis")
    raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
    return [str(item) for item in json.loads(raw_text)]


async def list_registered_permission_keys() -> set[str]:
    resources = await list_permission_resources()
    permission_keys: set[str] = set()
    for resource in resources:
        index = resource.find("[")
        permission_keys.add(resource[:index] if index > -1 else resource)
    return permission_keys


async def ensure_registered_permission_key(permission_key: str) -> None:
    registered_permission_keys = await list_registered_permission_keys()
    if permission_key not in registered_permission_keys:
        from app.core.exceptions.business import BusinessError

        raise BusinessError(f"Permission is not registered in Redis: {permission_key}")
