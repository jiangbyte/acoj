"""Permission registry scan and Redis cache sync."""

import json
import logging
from datetime import timedelta

from sdk.auth.consts import PermissionCacheKey
from sdk.config.settings import settings
from sdk.kernel.registry import get_registered_perm_entries

logger = logging.getLogger(__name__)


def permission_cache_ttl_seconds() -> int:
    raw = settings.raw.get("permission_cache_ttl", 300)
    try:
        ttl = int(raw)
    except (TypeError, ValueError):
        ttl = 300
    return max(ttl, 0)


def _build_module_tree(permissions: dict[str, dict[str, str]]) -> dict[str, dict[str, dict[str, str]]]:
    tree: dict = {}
    for code, entry in permissions.items():
        module = entry.get("module") or ""
        name = entry.get("name") or ""
        if module not in tree:
            tree[module] = {}
        tree[module][code] = {
            "code": code,
            "module": module,
            "name": name,
        }
    return tree


async def sync_to_redis(permissions: dict[str, dict[str, str]]) -> None:
    from sdk.infra.db.redis import get_client

    redis_client = get_client()
    if not redis_client:
        logger.warning("Redis not available, skipping permission cache")
        return

    tree = _build_module_tree(permissions)
    ttl = permission_cache_ttl_seconds()
    payload = json.dumps(tree, ensure_ascii=False)
    if ttl == 0:
        await redis_client.set(PermissionCacheKey, payload)
    else:
        await redis_client.set(PermissionCacheKey, payload, ex=timedelta(seconds=ttl))
    total = sum(len(v) for v in tree.values())
    logger.info(f"Cached {total} permissions in Redis across {len(tree)} modules")


async def get_modules_from_redis() -> list:
    from sdk.infra.db.redis import get_client

    redis_client = get_client()
    if not redis_client:
        return []
    data = await redis_client.get(PermissionCacheKey)
    if not data:
        return []
    tree = json.loads(data)
    return sorted(tree.keys())


async def get_permissions_by_module_from_redis(module: str) -> list:
    from sdk.infra.db.redis import get_client

    redis_client = get_client()
    if not redis_client:
        return []
    data = await redis_client.get(PermissionCacheKey)
    if not data:
        return []
    tree = json.loads(data)
    module_perms = tree.get(module, {})
    return list(module_perms.values())


async def run_permission_scan(app=None):
    permissions = get_registered_perm_entries()
    if not permissions:
        logger.info("No permissions registered, nothing to cache")
        return permissions
    await sync_to_redis(permissions)
    return permissions
