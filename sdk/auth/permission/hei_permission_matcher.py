import fnmatch
import re
from typing import List


class HeiPermissionMatcher:
    """
    权限匹配工具类
    支持多种分隔符和通配符匹配:
    - 冒号分隔: user:add, user:**
    - 点分隔: user.add, user.**
    - 斜杠分隔: user/add, user/**
    
    通配符规则:
    - * 匹配单个层级
    - ** 匹配任意多层级
    """

    @classmethod
    def match(cls, pattern: str, permission: str) -> bool:
        """
        判断权限是否匹配模式
        
        Args:
            pattern: 权限模式，如 "user:*", "user.**", "/user/**"
            permission: 实际权限，如 "user:add", "user.profile.update"
        
        Returns:
            是否匹配
        """
        if not pattern or not permission:
            return False
        
        if pattern == permission:
            return True
        
        if pattern == "*" or pattern == "**":
            return True
        
        separator = cls._detect_separator(pattern)
        
        if "**" in pattern:
            return cls._match_double_wildcard(pattern, permission, separator)
        elif "*" in pattern:
            return cls._match_single_wildcard(pattern, permission, separator)
        
        return pattern == permission

    @classmethod
    def match_any(cls, patterns: List[str], permission: str) -> bool:
        """
        判断权限是否匹配任意一个模式
        
        Args:
            patterns: 权限模式列表
            permission: 实际权限
        
        Returns:
            是否匹配任意一个
        """
        return any(cls.match(p, permission) for p in patterns)

    @classmethod
    def match_all(cls, patterns: List[str], permission: str) -> bool:
        """
        判断权限是否匹配所有模式
        
        Args:
            patterns: 权限模式列表
            permission: 实际权限
        
        Returns:
            是否匹配所有
        """
        return all(cls.match(p, permission) for p in patterns)

    @classmethod
    def has_permission(cls, required: str, permissions: List[str]) -> bool:
        """
        判断权限列表中是否有匹配所需权限的
        
        Args:
            required: 需要的权限
            permissions: 拥有的权限列表
        
        Returns:
            是否有匹配的权限
        """
        return any(cls.match(p, required) for p in permissions)

    @classmethod
    def has_permission_and(cls, required_list: List[str], permissions: List[str]) -> bool:
        """
        判断权限列表中是否有匹配所有所需权限的
        
        Args:
            required_list: 需要的权限列表
            permissions: 拥有的权限列表
        
        Returns:
            是否都有匹配的权限
        """
        return all(cls.has_permission(r, permissions) for r in required_list)

    @classmethod
    def has_permission_or(cls, required_list: List[str], permissions: List[str]) -> bool:
        """
        判断权限列表中是否有匹配任意一个所需权限的
        
        Args:
            required_list: 需要的权限列表
            permissions: 拥有的权限列表
        
        Returns:
            是否有任意一个匹配的权限
        """
        return any(cls.has_permission(r, permissions) for r in required_list)

    @classmethod
    def _detect_separator(cls, pattern: str) -> str:
        """
        检测分隔符类型
        
        Args:
            pattern: 权限模式
        
        Returns:
            分隔符字符
        """
        if "/" in pattern:
            return "/"
        elif ":" in pattern:
            return ":"
        elif "." in pattern:
            return "."
        return ":"

    @classmethod
    def _match_single_wildcard(cls, pattern: str, permission: str, separator: str) -> bool:
        """
        匹配单层通配符 *
        
        Args:
            pattern: 权限模式
            permission: 实际权限
            separator: 分隔符
        
        Returns:
            是否匹配
        """
        pattern_parts = pattern.split(separator)
        permission_parts = permission.split(separator)
        
        if len(pattern_parts) != len(permission_parts):
            return False
        
        for p_part, perm_part in zip(pattern_parts, permission_parts):
            if p_part == "*":
                continue
            if p_part != perm_part:
                return False
        
        return True

    @classmethod
    def _match_double_wildcard(cls, pattern: str, permission: str, separator: str) -> bool:
        """
        匹配多层通配符 **
        
        Args:
            pattern: 权限模式
            permission: 实际权限
            separator: 分隔符
        
        Returns:
            是否匹配
        """
        pattern_parts = pattern.split(separator)
        permission_parts = permission.split(separator)
        
        return cls._match_parts_with_double_wildcard(pattern_parts, permission_parts)

    @classmethod
    def _match_parts_with_double_wildcard(cls, pattern_parts: List[str], permission_parts: List[str]) -> bool:
        """
        递归匹配带有 ** 的部分
        
        Args:
            pattern_parts: 模式部分列表
            permission_parts: 权限部分列表
        
        Returns:
            是否匹配
        """
        if not pattern_parts:
            return not permission_parts
        
        if not permission_parts:
            return all(p == "**" for p in pattern_parts)
        
        if pattern_parts[0] == "**":
            if len(pattern_parts) == 1:
                return True
            for i in range(len(permission_parts) + 1):
                if cls._match_parts_with_double_wildcard(pattern_parts[1:], permission_parts[i:]):
                    return True
            return False
        
        if pattern_parts[0] == "*":
            return cls._match_parts_with_double_wildcard(pattern_parts[1:], permission_parts[1:])
        
        if pattern_parts[0] == permission_parts[0]:
            return cls._match_parts_with_double_wildcard(pattern_parts[1:], permission_parts[1:])
        
        return False
