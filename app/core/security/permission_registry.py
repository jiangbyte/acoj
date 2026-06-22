import hashlib
import json
import logging
import re
from collections import defaultdict
from dataclasses import asdict, dataclass, field
from typing import Any

from fastapi import FastAPI
from fastapi.routing import APIRoute

from app.platform.cache.keys import (
    permission_registry_module_resources_key,
    permission_registry_modules_key,
    permission_registry_permissions_key,
    permission_registry_resource_permissions_key,
    permission_registry_resources_key,
)
from app.platform.cache.redis import get_redis

logger = logging.getLogger(__name__)
PERMISSION_KEY_PATTERN = re.compile(r"^[a-z0-9*]+(?::[a-z0-9*]+)+$")
PERMISSION_META_ATTR = "__permission_meta__"
SCOPE_META_ATTR = "__scope_meta__"
RESOURCE_CATEGORY = "resource"
MODULE_CATEGORY = "module"
PERMISSION_CATEGORY = "permission"


@dataclass(slots=True)
class PermissionRouteRef:
    path: str
    methods: list[str]
    login_scopes: list[str]


@dataclass(slots=True)
class PermissionRegistryItem:
    permission_key: str
    module: str
    source: str
    routes: list[PermissionRouteRef] = field(default_factory=list)
    methods: list[str] = field(default_factory=list)
    login_scopes: list[str] = field(default_factory=list)


@dataclass(slots=True)
class PermissionModuleItem:
    module: str
    name: str
    category: str = MODULE_CATEGORY


@dataclass(slots=True)
class PermissionResourceItem:
    resource_code: str
    module: str
    path: str
    methods: list[str]
    login_scopes: list[str]
    source: str
    category: str = RESOURCE_CATEGORY


@dataclass(slots=True)
class PermissionPointItem:
    permission_key: str
    module: str
    resource_code: str
    category: str = PERMISSION_CATEGORY


@dataclass(slots=True)
class PermissionRegistryConflict:
    permission_key: str
    existing_module: str
    existing_resource_code: str
    new_module: str
    new_resource_code: str


@dataclass(slots=True)
class PermissionRegistrySnapshot:
    modules: list[PermissionModuleItem] = field(default_factory=list)
    resources: list[PermissionResourceItem] = field(default_factory=list)
    permissions: list[PermissionPointItem] = field(default_factory=list)
    conflicts: list[PermissionRegistryConflict] = field(default_factory=list)


@dataclass(slots=True)
class ScanArtifacts:
    snapshot: PermissionRegistrySnapshot
    permission_routes: dict[str, dict[str, PermissionRouteRef]]
    permission_methods: dict[str, set[str]]
    permission_scopes: dict[str, set[str]]
    resource_map: dict[str, PermissionResourceItem]


def _iter_dependant_calls(dependant: Any) -> list[Any]:
    calls: list[Any] = []
    for dependency in getattr(dependant, "dependencies", []):
        call = getattr(dependency, "call", None)
        if call is not None:
            calls.append(call)
        calls.extend(_iter_dependant_calls(dependency))
    return calls


def _detect_module(route: APIRoute) -> str:
    module_path = getattr(route.endpoint, "__module__", "")
    if module_path.startswith("app.modules."):
        parts = module_path.split(".")
        if len(parts) >= 3:
            return parts[2]
    return module_path or "unknown"


def _normalize_methods(route: APIRoute) -> list[str]:
    return sorted(method for method in (route.methods or set()) if method not in {"HEAD", "OPTIONS"})


def _build_resource_code(module_name: str, path: str, methods: list[str]) -> str:
    method_token = ",".join(methods)
    digest = hashlib.sha1(f"{module_name}|{path}|{method_token}".encode("utf-8")).hexdigest()[:12]
    return f"{module_name}:{digest}"


def _build_registry_snapshot(app: FastAPI) -> ScanArtifacts:
    module_items: dict[str, PermissionModuleItem] = {}
    resource_items: dict[str, PermissionResourceItem] = {}
    permission_items: dict[str, PermissionPointItem] = {}
    permission_routes: dict[str, dict[str, PermissionRouteRef]] = defaultdict(dict)
    permission_methods: dict[str, set[str]] = defaultdict(set)
    permission_scopes: dict[str, set[str]] = defaultdict(set)
    conflicts: list[PermissionRegistryConflict] = []

    for route in app.routes:
        if not isinstance(route, APIRoute):
            continue

        route_methods = _normalize_methods(route)
        route_scopes: set[str] = set()
        route_permissions: set[str] = set()

        for call in _iter_dependant_calls(route.dependant):
            permission_meta = getattr(call, PERMISSION_META_ATTR, None)
            if permission_meta:
                permission_key = str(permission_meta.get("permission_key", "")).strip()
                if not permission_key or not PERMISSION_KEY_PATTERN.fullmatch(permission_key):
                    logger.warning(
                        "Skip invalid permission key while scanning routes",
                        extra={"permission_key": permission_key},
                    )
                    continue
                route_permissions.add(permission_key)

            scope_meta = getattr(call, SCOPE_META_ATTR, None)
            if scope_meta:
                route_scopes.update(str(scope) for scope in scope_meta.get("login_scopes", []))

        if not route_permissions:
            continue

        module_name = _detect_module(route)
        source = getattr(route.endpoint, "__module__", "unknown")
        resource_code = _build_resource_code(module_name, route.path, route_methods)

        module_items.setdefault(module_name, PermissionModuleItem(module=module_name, name=module_name))
        resource_items[resource_code] = PermissionResourceItem(
            resource_code=resource_code,
            module=module_name,
            path=route.path,
            methods=route_methods,
            login_scopes=sorted(route_scopes),
            source=source,
        )

        for permission_key in sorted(route_permissions):
            existing = permission_items.get(permission_key)
            if existing and (
                existing.module != module_name or existing.resource_code != resource_code
            ):
                conflicts.append(
                    PermissionRegistryConflict(
                        permission_key=permission_key,
                        existing_module=existing.module,
                        existing_resource_code=existing.resource_code,
                        new_module=module_name,
                        new_resource_code=resource_code,
                    )
                )
                logger.warning(
                    "Permission key mapped to multiple resources",
                    extra={
                        "permission_key": permission_key,
                        "existing_module": existing.module,
                        "existing_resource_code": existing.resource_code,
                        "new_module": module_name,
                        "new_resource_code": resource_code,
                    },
                )
                continue

            permission_items.setdefault(
                permission_key,
                PermissionPointItem(
                    permission_key=permission_key,
                    module=module_name,
                    resource_code=resource_code,
                ),
            )
            permission_routes[permission_key][route.path] = PermissionRouteRef(
                path=route.path,
                methods=route_methods,
                login_scopes=sorted(route_scopes),
            )
            permission_methods[permission_key].update(route_methods)
            permission_scopes[permission_key].update(route_scopes)

    return ScanArtifacts(
        snapshot=PermissionRegistrySnapshot(
            modules=sorted(module_items.values(), key=lambda item: item.module),
            resources=sorted(resource_items.values(), key=lambda item: item.resource_code),
            permissions=sorted(permission_items.values(), key=lambda item: item.permission_key),
            conflicts=conflicts,
        ),
        permission_routes=permission_routes,
        permission_methods=permission_methods,
        permission_scopes=permission_scopes,
        resource_map=resource_items,
    )


def scan_permission_registry(app: FastAPI) -> list[PermissionRegistryItem]:
    artifacts = _build_registry_snapshot(app)
    snapshot = artifacts.snapshot

    items: list[PermissionRegistryItem] = []
    for permission in snapshot.permissions:
        resource = artifacts.resource_map[permission.resource_code]
        items.append(
            PermissionRegistryItem(
                permission_key=permission.permission_key,
                module=permission.module,
                source=resource.source,
                routes=sorted(
                    artifacts.permission_routes[permission.permission_key].values(),
                    key=lambda route_ref: route_ref.path,
                ),
                methods=sorted(artifacts.permission_methods[permission.permission_key]),
                login_scopes=sorted(artifacts.permission_scopes[permission.permission_key]),
            )
        )
    return items


def _serialize_dataclass(item: object) -> str:
    return json.dumps(asdict(item), ensure_ascii=True, sort_keys=True)


def _deserialize_resource(raw: str | bytes) -> PermissionResourceItem:
    raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
    return PermissionResourceItem(**json.loads(raw_text))


def _deserialize_permission(raw: str | bytes) -> PermissionPointItem:
    raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
    return PermissionPointItem(**json.loads(raw_text))


async def sync_permission_registry(app: FastAPI) -> list[PermissionRegistryItem]:
    artifacts = _build_registry_snapshot(app)
    snapshot = artifacts.snapshot
    items: list[PermissionRegistryItem] = []
    for permission in snapshot.permissions:
        resource = artifacts.resource_map[permission.resource_code]
        items.append(
            PermissionRegistryItem(
                permission_key=permission.permission_key,
                module=permission.module,
                source=resource.source,
                routes=sorted(
                    artifacts.permission_routes[permission.permission_key].values(),
                    key=lambda route_ref: route_ref.path,
                ),
                methods=sorted(artifacts.permission_methods[permission.permission_key]),
                login_scopes=sorted(artifacts.permission_scopes[permission.permission_key]),
            )
        )
    redis = get_redis()
    if not redis:
        logger.info("Skip permission registry sync because Redis is unavailable")
        return items

    modules_key = permission_registry_modules_key()
    resources_key = permission_registry_resources_key()
    permissions_key = permission_registry_permissions_key()
    existing_keys = await redis.keys("permission:registry:*")

    pipe = redis.pipeline()
    if existing_keys:
        pipe.delete(*existing_keys)
    for module_item in snapshot.modules:
        pipe.hset(modules_key, module_item.module, _serialize_dataclass(module_item))
    for resource_item in snapshot.resources:
        pipe.hset(resources_key, resource_item.resource_code, _serialize_dataclass(resource_item))
        pipe.sadd(permission_registry_module_resources_key(resource_item.module), resource_item.resource_code)
    for permission_item in snapshot.permissions:
        pipe.hset(permissions_key, permission_item.permission_key, _serialize_dataclass(permission_item))
        pipe.sadd(
            permission_registry_resource_permissions_key(permission_item.resource_code),
            permission_item.permission_key,
        )
    await pipe.execute()
    return items


async def get_permission_definition(permission_key: str) -> PermissionRegistryItem | None:
    redis = get_redis()
    if not redis:
        logger.info(
            "Permission registry lookup skipped because Redis is unavailable",
            extra={"permission_key": permission_key},
        )
        return None

    raw_permission = await redis.hget(permission_registry_permissions_key(), permission_key)
    if not raw_permission:
        return None
    permission = _deserialize_permission(raw_permission)
    raw_resource = await redis.hget(permission_registry_resources_key(), permission.resource_code)
    if not raw_resource:
        logger.warning(
            "Permission registry resource missing",
            extra={"permission_key": permission_key, "resource_code": permission.resource_code},
        )
        return None
    resource = _deserialize_resource(raw_resource)
    return PermissionRegistryItem(
        permission_key=permission.permission_key,
        module=permission.module,
        source=resource.source,
        routes=[
            PermissionRouteRef(
                path=resource.path,
                methods=list(resource.methods),
                login_scopes=list(resource.login_scopes),
            )
        ],
        methods=list(resource.methods),
        login_scopes=list(resource.login_scopes),
    )


async def list_permission_definitions() -> list[PermissionRegistryItem]:
    redis = get_redis()
    if not redis:
        logger.info("Permission registry listing skipped because Redis is unavailable")
        return []

    raw_permissions = await redis.hgetall(permission_registry_permissions_key())
    if not raw_permissions:
        return []

    raw_resources = await redis.hgetall(permission_registry_resources_key())
    resources = {
        (key.decode("utf-8") if isinstance(key, bytes) else key): _deserialize_resource(value)
        for key, value in raw_resources.items()
    }

    items: list[PermissionRegistryItem] = []
    for raw_permission in raw_permissions.values():
        permission = _deserialize_permission(raw_permission)
        resource = resources.get(permission.resource_code)
        if not resource:
            logger.warning(
                "Permission registry resource missing during list",
                extra={"permission_key": permission.permission_key, "resource_code": permission.resource_code},
            )
            continue
        items.append(
            PermissionRegistryItem(
                permission_key=permission.permission_key,
                module=permission.module,
                source=resource.source,
                routes=[
                    PermissionRouteRef(
                        path=resource.path,
                        methods=list(resource.methods),
                        login_scopes=list(resource.login_scopes),
                    )
                ],
                methods=list(resource.methods),
                login_scopes=list(resource.login_scopes),
            )
        )
    return sorted(items, key=lambda item: item.permission_key)
