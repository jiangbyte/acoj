import json

from fastapi import FastAPI
from sqlalchemy import select

from app.core.security.permission_registry import (
    ACCOUNT_TYPE_META_ATTR,
    PERMISSION_META_ATTR,
    scan_permission_registry,
    sync_permission_registry,
)
from app.core.exceptions.business import BusinessError
from app.deps.auth import require_account_type, require_permission
from app.factory import create_app
from app.modules.iam.enums import ResourceType
from app.modules.iam.resource.model import SysResource
from app.modules.iam.resource.schema import ResourceCreateRequest, ResourcePermissionBindRequest
from app.modules.iam.resource.service import ResourceService
from app.platform.cache.keys import (
    permission_resource_cache_key,
    permission_resource_method_cache_key,
)


class FakeRedis:
    def __init__(self) -> None:
        self.values: dict[str, str] = {}

    async def set(self, key: str, value: str) -> None:
        self.values[key] = value

    async def get(self, key: str):
        return self.values.get(key)


async def test_permission_dependency_carries_scan_metadata():
    permission_dependency = require_permission("iam:account:list")
    account_type_dependency = require_account_type(*[])

    assert getattr(permission_dependency, PERMISSION_META_ATTR) == {
        "permission_key": "iam:account:list"
    }
    assert getattr(account_type_dependency, ACCOUNT_TYPE_META_ATTR) == {"account_types": []}


def test_scan_permission_registry_collects_api_resources():
    app: FastAPI = create_app()

    items = scan_permission_registry(app)

    assert any(item.permission_key == "file:file:upload" for item in items)
    file_page = next(item for item in items if item.permission_key == "file:file:page")
    assert file_page.route_path == "/file/page"
    assert file_page.method == "GET"
    assert file_page.resource_text == "file:file:page[page]"


async def test_sync_permission_registry_writes_cache_structure(monkeypatch):
    fake_redis = FakeRedis()
    app: FastAPI = create_app()

    monkeypatch.setattr("app.core.security.permission_registry.get_redis", lambda: fake_redis)

    items = await sync_permission_registry(app)
    assert items
    resource_values = json.loads(fake_redis.values[permission_resource_cache_key()])
    method_map = json.loads(fake_redis.values[permission_resource_method_cache_key()])

    assert "file:file:page[page]" in resource_values
    assert method_map["file:file:page[page]"] == "GET"


async def test_bind_resource_permission_requires_registered_permission_key(db_session, monkeypatch):
    fake_redis = FakeRedis()
    fake_redis.values[permission_resource_cache_key()] = json.dumps([])
    monkeypatch.setattr("app.core.security.permission_registry.get_redis", lambda: fake_redis)

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
