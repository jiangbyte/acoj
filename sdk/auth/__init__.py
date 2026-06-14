from .realm import (
    ACLSnapshot,
    Business,
    BusinessID,
    Consumer,
    ConsumerID,
    Realm,
    ScopeInfo,
    SessionClaims,
    Sessions,
    all_realms,
    current_realm,
    infer_realm,
    infer_realm_id_from_path,
    resolve_realm,
    realm_from_id,
    register_realm,
)
from .matcher import match, match_permission, match_permissions_and, match_permissions_or


async def get_current_login_id(request):
    realm = current_realm(request)
    if not realm:
        return ""
    return await realm.get_login_id(request) or ""


async def get_current_realm_id(request):
    realm = current_realm(request)
    return realm.id if realm else ""


async def get_current_auth_context(request):
    realm = current_realm(request)
    if not realm:
        return "", ""
    return (await realm.get_login_id(request) or "", realm.id)

__all__ = [
    "Realm",
    "Business",
    "Consumer",
    "BusinessID",
    "ConsumerID",
    "Sessions",
    "register_realm",
    "all_realms",
    "current_realm",
    "get_current_login_id",
    "get_current_realm_id",
    "realm_from_id",
    "resolve_realm",
    "infer_realm",
    "infer_realm_id_from_path",
    "ScopeInfo",
    "ACLSnapshot",
    "SessionClaims",
    "match",
    "match_permission",
    "match_permissions_and",
    "match_permissions_or",
]
