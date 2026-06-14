from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Callable, Iterable

from fastapi import HTTPException, Request, status

from sdk.auth.auth.business_auth_tool import BusinessAuthTool
from sdk.auth.auth.consumer_auth_tool import ConsumerAuthTool
from sdk.auth.matcher import match, match_permission, match_permissions_and, match_permissions_or
from sdk.auth.enums import CheckMode
from sdk.auth.provider import EMPTY_PERMISSION_PROVIDER, PermissionProviderProtocol

BusinessID = "BUSINESS"
ConsumerID = "CONSUMER"
PathMatcher = Callable[[str], bool]


_realm_registry: dict[str, "Realm"] = {}


def _join_values(values: list[str]) -> str:
    return ",".join(str(item) for item in values)


@dataclass(slots=True)
class ScopeInfo:
    group_scope: str = ""
    org_scope: str = ""
    custom_group_ids: list[str] = field(default_factory=list)
    custom_org_ids: list[str] = field(default_factory=list)


@dataclass(slots=True)
class ACLSnapshot:
    permissions: list[str] = field(default_factory=list)
    roles: list[str] = field(default_factory=list)
    scope_map: dict[str, ScopeInfo] = field(default_factory=dict)


@dataclass(slots=True)
class SessionClaims:
    user_id: str
    realm_id: str
    created_at: str = ""
    extra: dict[str, Any] = field(default_factory=dict)
    acl: ACLSnapshot = field(default_factory=ACLSnapshot)


def _to_scope_info(scope: dict[str, Any] | None) -> ScopeInfo:
    data = scope or {}
    return ScopeInfo(
        group_scope=str(data.get("group_scope") or ""),
        org_scope=str(data.get("org_scope") or ""),
        custom_group_ids=[str(item) for item in (data.get("custom_group_ids") or [])],
        custom_org_ids=[str(item) for item in (data.get("custom_org_ids") or [])],
    )


def _to_acl_snapshot(acl: dict[str, Any] | None) -> ACLSnapshot:
    data = acl or {}
    return ACLSnapshot(
        permissions=list(data.get("permissions") or []),
        roles=list(data.get("roles") or []),
        scope_map={
            str(code): _to_scope_info(scope)
            for code, scope in dict(data.get("scope_map") or {}).items()
        },
    )


def _to_session_claims(claims: dict[str, Any] | None, fallback_realm_id: str) -> SessionClaims | None:
    if not claims:
        return None
    return SessionClaims(
        user_id=str(claims.get("user_id") or ""),
        realm_id=str(claims.get("realm_id") or fallback_realm_id),
        created_at=str(claims.get("created_at") or ""),
        extra=dict(claims.get("extra") or {}),
        acl=_to_acl_snapshot(claims.get("acl") or {}),
    )


class Realm:
    def __init__(
        self,
        realm_id: str,
        tool_cls: type,
        *,
        path_matchers: Iterable[PathMatcher] = (),
    ):
        self.id = realm_id
        self.tool = tool_cls
        self._path_matchers = tuple(path_matchers)

    def init(self, expire: int, token_name: str) -> None:
        self.tool.init(expire=expire, token_name=token_name)

    def set_permission_provider(self, provider: PermissionProviderProtocol | None) -> None:
        self.tool.set_permission_provider(provider)

    def add_path_matcher(self, matcher: PathMatcher) -> None:
        self._path_matchers = (*self._path_matchers, matcher)

    def matches_path(self, path: str) -> bool:
        return any(matcher(path) for matcher in self._path_matchers)

    async def login(self, request: Request | None, login_id: str | int, extra: dict[str, Any] | None = None) -> str:
        return await self.tool.login(login_id, request, extra or {})

    async def logout(self, request: Request | None = None, login_id: str | int | None = None) -> None:
        await self.tool.logout(login_id=login_id, request=request)

    async def is_login(self, request: Request | None) -> bool:
        return await self.tool.is_login(request)

    async def check_login(self, request: Request | None) -> None:
        await self.tool.check_login(request)

    async def get_login_id(self, request: Request | None) -> str | None:
        return await self.tool.get_login_id_default_null(request)

    async def get_login_id_default_null(self, request: Request | None) -> str | None:
        return await self.get_login_id(request)

    async def get_login_id_by_token(self, token: str) -> str | None:
        return await self.tool.get_login_id_by_token(token)

    async def get_token_value(self, request: Request | None) -> str | None:
        return await self.tool.get_token_value(request)

    def get_token_name(self) -> str:
        return self.tool.get_token_name()

    async def get_extra(self, key: str, request: Request | None) -> Any:
        claims = await self.claims(request)
        if not claims:
            return None
        return claims.extra.get(key)

    async def get_session(self, request: Request | None) -> dict[str, Any]:
        claims = await self.claims(request)
        if not claims:
            return {}
        return {
            "user_id": claims.user_id,
            "realm_id": claims.realm_id,
            "created_at": claims.created_at,
            "extra": dict(claims.extra),
            "acl": {
                "permissions": list(claims.acl.permissions),
                "roles": list(claims.acl.roles),
                "scope_map": {
                    code: {
                        "group_scope": scope.group_scope,
                        "org_scope": scope.org_scope,
                        "custom_group_ids": list(scope.custom_group_ids),
                        "custom_org_ids": list(scope.custom_org_ids),
                    }
                    for code, scope in claims.acl.scope_map.items()
                },
            },
        }

    async def kickout(self, login_id: str) -> None:
        await self.tool.kickout(login_id)

    async def kickout_with_context(self, login_id: str) -> None:
        await self.kickout(login_id)

    async def kickout_token(self, login_id: str, token: str) -> None:
        await self.tool.kickout_token(login_id, token)

    async def kickout_token_with_context(self, login_id: str, token: str) -> None:
        await self.kickout_token(login_id, token)

    async def renew_timeout(self, request: Request | None, timeout: int | None = None) -> None:
        await self.tool.renew_timeout(timeout=timeout, request=request)

    async def get_token_timeout(self, request: Request | None) -> int:
        return await self.tool.get_token_timeout(request)

    async def get_session_timeout(self, request: Request | None) -> int:
        return await self.tool.get_session_timeout(request)

    async def disable(self, login_id: str, time_seconds: int) -> None:
        await self.tool.disable(login_id, time_seconds)

    async def is_disable(self, login_id: str) -> bool:
        return await self.tool.is_disable(login_id)

    async def check_disable(self, login_id: str) -> None:
        await self.tool.check_disable(login_id)

    async def get_disable_time(self, login_id: str) -> int:
        return await self.tool.get_disable_time(login_id)

    async def untie_disable(self, login_id: str) -> None:
        await self.tool.untie_disable(login_id)

    async def permission_list(self, request: Request | None) -> list[str]:
        claims = await self.claims(request)
        return claims.acl.permissions if claims else []

    async def role_list(self, request: Request | None) -> list[str]:
        claims = await self.claims(request)
        return claims.acl.roles if claims else []

    async def has_permission(self, request: Request | None, permission: str) -> bool:
        owned = await self.permission_list(request)
        return match_permission(permission, owned)

    async def has_permission_and(self, request: Request | None, permissions: list[str]) -> bool:
        owned = await self.permission_list(request)
        return match_permissions_and(permissions, owned)

    async def has_permission_or(self, request: Request | None, permissions: list[str]) -> bool:
        owned = await self.permission_list(request)
        return match_permissions_or(permissions, owned)

    async def has_role(self, request: Request | None, role: str) -> bool:
        owned = await self.role_list(request)
        return role in owned

    async def has_role_and(self, request: Request | None, roles: list[str]) -> bool:
        owned = await self.role_list(request)
        return all(role in owned for role in roles)

    async def has_role_or(self, request: Request | None, roles: list[str]) -> bool:
        owned = await self.role_list(request)
        return any(role in owned for role in roles)

    async def scope_for(self, request: Request | None, permission: str) -> tuple[ScopeInfo, bool]:
        if not permission:
            return ScopeInfo(), False
        claims = await self.claims(request)
        if not claims:
            return ScopeInfo(), False
        scope = claims.acl.scope_map.get(permission)
        return (scope or ScopeInfo()), scope is not None

    async def claims(self, request: Request | None) -> SessionClaims | None:
        claims, ok = await self.tool.get_claims(request)
        if not ok or not claims:
            return None
        return _to_session_claims(claims, self.id)

    async def get_claims(self, request: Request | None) -> tuple[SessionClaims | None, bool]:
        claims, ok = await self.tool.get_claims(request)
        return _to_session_claims(claims, self.id), ok

    async def refresh_user_sessions_acl(self, user_id: str) -> None:
        await self.tool.refresh_user_sessions_acl(user_id)

    async def refresh_acl(self, user_id: str) -> ACLSnapshot:
        acl = await self.tool._load_acl(user_id)
        return _to_acl_snapshot(acl)

    async def check_permission(self, request: Request | None, permission: str) -> None:
        await ensure_permission(self, permission, request)

    async def check_role(self, request: Request | None, role: str) -> None:
        await ensure_role(self, role, request)

    def sessions(self) -> RealmSessionService:
        return RealmSessionService(self)


class RealmSessionService:
    def __init__(self, realm: Realm):
        self.realm = realm

    async def page(self, current: int, size: int, keyword: str | None = None) -> tuple[list[dict[str, Any]], int]:
        return await self.realm.tool.list_session_infos(current=current, size=size, keyword=keyword)

    async def page_by_user_ids(self, user_ids: list[str], current: int, size: int) -> tuple[list[dict[str, Any]], int]:
        return await self.realm.tool.list_session_infos_by_user_ids(user_ids, current=current, size=size)

    async def tokens(self, user_id: str) -> list[dict[str, Any]]:
        return await self.realm.tool.get_session_tokens(user_id)

    async def stats(self) -> dict[str, int]:
        return await self.realm.tool.get_session_stats()

    async def trend(self, days: list[str]) -> dict[str, int]:
        return await self.realm.tool.get_session_daily_counts(days)

    async def kickout_user(self, user_id: str) -> None:
        await self.realm.kickout(user_id)

    async def kickout_token(self, user_id: str, token: str) -> None:
        await self.realm.kickout_token(user_id, token)


class AggregateSessionService:
    def __init__(self, realms: list[Realm]):
        self.realms = realms

    async def stats_by_realm(self) -> dict[str, dict[str, int]]:
        result: dict[str, dict[str, int]] = {}
        for realm in self.realms:
            result[realm.id] = await realm.sessions().stats()
        return result

    async def stats(self) -> dict[str, int]:
        result = {"total_count": 0, "one_hour_newly_added": 0, "max_token_count": 0}
        for stats in (await self.stats_by_realm()).values():
            result["total_count"] += stats.get("total_count", 0)
            result["one_hour_newly_added"] += stats.get("one_hour_newly_added", 0)
            result["max_token_count"] = max(result["max_token_count"], stats.get("max_token_count", 0))
        return result

    async def trend_by_realm(self, days: list[str]) -> dict[str, dict[str, int]]:
        result: dict[str, dict[str, int]] = {}
        for realm in self.realms:
            result[realm.id] = await realm.sessions().trend(days)
        return result


def Sessions(*realms: Realm) -> AggregateSessionService:
    return AggregateSessionService([realm for realm in realms if realm is not None])


def register_realm(realm: Realm) -> Realm:
    if realm.id in _realm_registry:
        raise ValueError(f"duplicate realm registration: {realm.id}")
    realm.set_permission_provider(EMPTY_PERMISSION_PROVIDER)
    _realm_registry[realm.id] = realm
    return realm


def all_realms() -> list[Realm]:
    return list(_realm_registry.values())


def realm_from_id(realm_id: str) -> Realm:
    realm = _realm_registry.get(str(realm_id or ""))
    if realm is None:
        raise ValueError(f"unknown realm: {realm_id}")
    return realm


def infer_realm(path: str) -> Realm | None:
    for realm in all_realms():
        if realm.matches_path(path):
            return realm
    return None


def current_realm(request: Request | None) -> Realm | None:
    if request is None:
        return None
    state_realm = getattr(request.state, "login_realm", None)
    if state_realm is not None:
        return state_realm
    return infer_realm(request.url.path)


def resolve_realm(realm_id: str | None, request: Request | None, *, guard_name: str) -> Realm:
    if realm_id is not None:
        return realm_from_id(realm_id)
    realm = current_realm(request)
    if not realm:
        raise ValueError(f"cannot infer realm for {guard_name}")
    return realm


def infer_realm_id_from_path(path: str) -> str | None:
    realm = infer_realm(path)
    return realm.id if realm else None


def is_public_path(path: str, public_paths: list[str]) -> bool:
    for item in public_paths:
        if item == "/":
            if path == "/":
                return True
            continue
        if item.endswith("/"):
            if path.startswith(item):
                return True
            continue
        if path == item or path.startswith(item + "/"):
            return True
    return False


async def check_permissions(
    realm: Realm,
    required: list[str],
    request: Request | None,
    mode: str = CheckMode.AND,
) -> bool:
    owned = await realm.permission_list(request)
    if mode == CheckMode.OR:
        return any(any(match(candidate, req) for candidate in owned) for req in required)
    return all(any(match(candidate, req) for candidate in owned) for req in required)


async def check_roles(
    realm: Realm,
    required: list[str],
    request: Request | None,
    mode: str = CheckMode.AND,
) -> bool:
    owned = await realm.role_list(request)
    if mode == CheckMode.OR:
        return any(role in owned for role in required)
    return all(role in owned for role in required)


async def ensure_permission(realm: Realm, permission: str, request: Request | None) -> None:
    if not await check_permissions(realm, [permission], request):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少权限: {_join_values([permission])}")


async def ensure_role(realm: Realm, role: str, request: Request | None) -> None:
    if not await check_roles(realm, [role], request):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=f"缺少角色: {_join_values([role])}")

Business = register_realm(
    Realm(
        BusinessID,
        BusinessAuthTool,
        path_matchers=(lambda path: path.startswith("/api/v") and "/c/" not in path,),
    )
)
Consumer = register_realm(
    Realm(
        ConsumerID,
        ConsumerAuthTool,
        path_matchers=(lambda path: path.startswith("/api/v") and "/c/" in path,),
    )
)
