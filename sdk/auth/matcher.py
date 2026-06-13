from __future__ import annotations


def match(pattern: str, permission: str) -> bool:
    if not pattern or not permission:
        return False
    if pattern == permission or pattern in {"*", "**"}:
        return True

    separator = _detect_separator(pattern)
    if "**" in pattern:
        return _match_double_wildcard(pattern, permission, separator)
    if "*" in pattern:
        return _match_single_wildcard(pattern, permission, separator)
    return pattern == permission


def match_permission(required: str, permissions: list[str]) -> bool:
    return any(match(item, required) for item in permissions)


def match_permissions_and(required: list[str], permissions: list[str]) -> bool:
    return all(match_permission(item, permissions) for item in required)


def match_permissions_or(required: list[str], permissions: list[str]) -> bool:
    return any(match_permission(item, permissions) for item in required)


def _detect_separator(pattern: str) -> str:
    if "/" in pattern:
        return "/"
    if ":" in pattern:
        return ":"
    if "." in pattern:
        return "."
    return ":"


def _match_single_wildcard(pattern: str, permission: str, separator: str) -> bool:
    pattern_parts = pattern.split(separator)
    permission_parts = permission.split(separator)
    if len(pattern_parts) != len(permission_parts):
        return False
    for left, right in zip(pattern_parts, permission_parts):
        if left == "*":
            continue
        if left != right:
            return False
    return True


def _match_double_wildcard(pattern: str, permission: str, separator: str) -> bool:
    return _match_parts_with_double_wildcard(pattern.split(separator), permission.split(separator))


def _match_parts_with_double_wildcard(pattern_parts: list[str], permission_parts: list[str]) -> bool:
    if not pattern_parts:
        return not permission_parts
    if not permission_parts:
        return all(item == "**" for item in pattern_parts)
    if pattern_parts[0] == "**":
        if len(pattern_parts) == 1:
            return True
        for index in range(len(permission_parts) + 1):
            if _match_parts_with_double_wildcard(pattern_parts[1:], permission_parts[index:]):
                return True
        return False
    if pattern_parts[0] == "*":
        return _match_parts_with_double_wildcard(pattern_parts[1:], permission_parts[1:])
    if pattern_parts[0] == permission_parts[0]:
        return _match_parts_with_double_wildcard(pattern_parts[1:], permission_parts[1:])
    return False
