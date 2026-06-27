import json

from fastapi import FastAPI
from sqlalchemy import select

from app.core.config.enums import ResourceType
from app.core.exceptions.business import BusinessError
from app.core.security.permission_registry import (
    PERMISSION_META_ATTR,
    SCOPE_META_ATTR,
    get_permission_definition,
    scan_permission_registry,
    sync_permission_registry,
)
from app.deps.auth import require_permission, require_scope
from app.factory import create_app
from app.modules.iam.resource.model import SysResource
from app.modules.iam.resource.schema import ResourceCreateRequest, ResourcePermissionBindRequest
from app.modules.iam.resource.service import ResourceService
from app.platform.cache.keys import (
    permission_registry_module_resources_key,
    permission_registry_modules_key,
    permission_registry_permissions_key,
    permission_registry_resource_permissions_key,
    permission_registry_resources_key,
)


class FakeRedis:
    def __init__(self) -> None:
        self.values: dict[str, str] = {}
        self.hashes: dict[str, dict[str, str]] = {}
        self.sets: dict[str, set[str]] = {}

    async def keys(self, pattern: str) -> list[str]:
        if pattern != "permission:registry:*":
            return []
        matched = set()
        for key in self.values:
            if key.startswith("permission:registry:"):
                matched.add(key)
        for key in self.hashes:
            if key.startswith("permission:registry:"):
                matched.add(key)
        for key in self.sets:
            if key.startswith("permission:registry:"):
                matched.add(key)
        return sorted(matched)

    def pipeline(self):
        return FakePipeline(self)

    async def hget(self, key: str, field: str):
        return self.hashes.get(key, {}).get(field)

    async def hgetall(self, key: str):
        return dict(self.hashes.get(key, {}))

    async def delete(self, *keys: str) -> None:
        for key in keys:
            self.values.pop(key, None)
            self.hashes.pop(key, None)
            self.sets.pop(key, None)


class FakePipeline:
    def __init__(self, redis: FakeRedis) -> None:
        self.redis = redis
        self.operations: list[tuple[str, tuple[object, ...]]] = []

    def delete(self, *keys: str):
        self.operations.append(("delete", keys))
        return self

    def hset(self, key: str, field: str, value: str):
        self.operations.append(("hset", (key, field, value)))
        return self

    def sadd(self, key: str, *values: str):
        self.operations.append(("sadd", (key, *values)))
        return self

    async def execute(self):
        for operation, payload in self.operations:
            if operation == "delete":
                for key in payload:
                    assert isinstance(key, str)
                    self.redis.values.pop(key, None)
                    self.redis.hashes.pop(key, None)
                    self.redis.sets.pop(key, None)
            elif operation == "hset":
                key, field, value = payload
                assert isinstance(key, str)
                assert isinstance(field, str)
                assert isinstance(value, str)
                self.redis.hashes.setdefault(key, {})[field] = value
            elif operation == "sadd":
                key, *values = payload
                assert isinstance(key, str)
                str_values = [str(value) for value in values]
                self.redis.sets.setdefault(key, set()).update(str_values)


async def test_permission_dependency_carries_scan_metadata():
    permission_dependency = require_permission("iam:account:list")
    scope_dependency = require_scope(*[])

    assert getattr(permission_dependency, PERMISSION_META_ATTR) == {
        "permission_key": "iam:account:list"
    }
    assert getattr(scope_dependency, SCOPE_META_ATTR) == {"login_scopes": []}


def test_scan_permission_registry_collects_routes():
    app: FastAPI = create_app()

    items = scan_permission_registry(app)

    assert any(item.permission_key == "iam:account:page" for item in items)
    file_list = next(item for item in items if item.permission_key == "file:list")
    assert file_list.module == "file"
    assert "/api/v1/admin/list" in [route_ref.path for route_ref in file_list.routes]
    assert "admin" in file_list.login_scopes


async def test_sync_and_resolve_permission_registry(monkeypatch):
    fake_redis = FakeRedis()
    app: FastAPI = create_app()

    monkeypatch.setattr("app.core.security.permission_registry.get_redis", lambda: fake_redis)

    items = await sync_permission_registry(app)
    assert items
    assert permission_registry_modules_key() in fake_redis.hashes
    assert permission_registry_resources_key() in fake_redis.hashes
    assert permission_registry_permissions_key() in fake_redis.hashes

    definition = await get_permission_definition("iam:permission:list")
    assert definition is not None
    assert definition.permission_key == "iam:permission:list"
    assert any(
        route_ref.path == "/api/v1/admin/permissions/registry"
        for route_ref in definition.routes
    )

    raw_permission = fake_redis.hashes[permission_registry_permissions_key()]["iam:permission:list"]
    resource_code = json.loads(raw_permission)["resource_code"]
    assert resource_code in fake_redis.sets[permission_registry_module_resources_key("iam")]
    assert "iam:permission:list" in fake_redis.sets[
        permission_registry_resource_permissions_key(resource_code)
    ]


async def test_bind_resource_permission_requires_registered_permission(db_session, monkeypatch):
    async def fake_get_permission_definition(permission_key: str):
        return None

    monkeypatch.setattr(
        "app.modules.iam.permission.service.get_permission_definition",
        fake_get_permission_definition,
    )

    service = ResourceService(db_session)
    await service.create(
        ResourceCreateRequest(
            code="iam:resource:test",
            name="test",
            resource_type=ResourceType.BUTTON,
        )
    )
    resource_id = (
        await db_session.execute(
            select(SysResource.id).where(SysResource.code == "iam:resource:test")
        )
    ).scalar_one()
    await db_session.rollback()

    try:
        await service.bind_resource_permission(
            ResourcePermissionBindRequest(
                resource_id=resource_id,
                permission_key="missing:permission",
            )
        )
    except BusinessError as exc:
        assert str(exc) == "Permission is not registered in Redis: missing:permission"
    else:
        raise AssertionError("Expected registered permission validation to fail")
