from __future__ import annotations

import pytest

from plugins.plugin_sys.role.params import GrantPermissionParam, PermissionItem, RefreshRoleSessionACLParam, RolePageParam
from plugins.plugin_sys.role.service import RoleService
from plugins.plugin_sys.user.params import (
    BatchRefreshSessionACLParam,
    GrantRoleParam,
    GrantUserPermissionParam,
    RefreshSessionACLParam,
    UpdateStatusParam,
    UserPageParam,
)
from plugins.plugin_sys.user.service import UserService


class _UserRepo:
    def __init__(self) -> None:
        self.find_page_param = None
        self.db = self

    def find_page_by_filters(self, param):
        self.find_page_param = param
        return {"records": [], "total": 0}

    def execute(self, *args, **kwargs):
        return self

    def all(self):
        return []

    def scalars(self):
        return self

    def commit(self):
        return None

    def rollback(self):
        return None

    def add(self, entity):
        return None


class _RoleRepo:
    def __init__(self) -> None:
        self.find_page_param = None
        self.grant_permissions_calls = []
        self.db = self

    def find_page(self, param):
        self.find_page_param = param
        return {"records": [], "total": 0}

    def grant_permissions(self, role_id, permissions, created_by=None):
        self.grant_permissions_calls.append((role_id, permissions, created_by))

    def find_resources_with_extra_by_ids(self, resource_ids):
        return []

    def add_missing_permissions(self, role_id, permissions):
        return None

    def execute(self, *args, **kwargs):
        return self

    def scalars(self):
        return self

    def all(self):
        return []

    def add(self, entity):
        return None

    def commit(self):
        return None


@pytest.mark.asyncio
async def test_user_page_caps_page_size() -> None:
    service = UserService.__new__(UserService)
    service.repository = _UserRepo()
    service.db = service.repository.db

    service.page(UserPageParam(current=0, size=500))

    assert service.repository.find_page_param.current == 1
    assert service.repository.find_page_param.size == 100


@pytest.mark.asyncio
async def test_role_page_caps_page_size() -> None:
    service = RoleService.__new__(RoleService)
    service.repository = _RoleRepo()
    service.db = service.repository.db

    service.page(RolePageParam(current=0, size=500))

    assert service.repository.find_page_param.current == 1
    assert service.repository.find_page_param.size == 100


@pytest.mark.asyncio
async def test_user_refresh_batch_deduplicates(monkeypatch) -> None:
    service = UserService.__new__(UserService)
    service.repository = _UserRepo()
    service.db = service.repository.db
    called = []

    async def _refresh(user_id: str):
        called.append(user_id)

    monkeypatch.setattr("plugins.plugin_sys.user.service.Business.refresh_user_sessions_acl", _refresh)

    await service.batch_refresh_session_acl(BatchRefreshSessionACLParam(user_ids=["u1", "u1", "", "u2"]))

    assert called == ["u1", "u2"]


@pytest.mark.asyncio
async def test_user_update_status_kickout_non_active(monkeypatch) -> None:
    service = UserService.__new__(UserService)
    service.repository = _UserRepo()
    service.db = service.repository.db
    kicked = []

    async def _kickout(user_id: str):
        kicked.append(user_id)

    monkeypatch.setattr("plugins.plugin_sys.user.service.Business.sessions", lambda: type("S", (), {"kickout_user": staticmethod(_kickout)})())

    await service.update_status(UpdateStatusParam(ids=["u1", "u2"], status="DISABLED"))

    assert kicked == ["u1", "u2"]


@pytest.mark.asyncio
async def test_role_grant_permissions_refreshes_acl(monkeypatch) -> None:
    service = RoleService.__new__(RoleService)
    service.repository = _RoleRepo()
    service.db = service.repository.db
    refreshed = []

    async def _refresh(param: RefreshRoleSessionACLParam):
        refreshed.append(param.role_id)

    monkeypatch.setattr(service, "refresh_session_acl", _refresh)

    await service.grant_permissions(
        "r1",
        [PermissionItem(permission_code="sys:user:view")],
        actor=None,
    )

    assert service.repository.grant_permissions_calls
    assert refreshed == ["r1"]
