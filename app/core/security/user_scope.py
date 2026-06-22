from collections.abc import Iterable

from app.core.config.enums import LoginScope
from app.core.exceptions.business import AuthorizationError


def assert_scope_allowed(actual_scope: str, expected_scopes: Iterable[LoginScope]) -> None:
    """校验当前登录域是否在允许访问的枚举范围内。"""
    if actual_scope not in {scope.value for scope in expected_scopes}:
        raise AuthorizationError(f"Login scope {actual_scope!r} is not allowed")
