from enum import Enum


class PermissionPathEnum(str, Enum):
    """权限来源路径（值越小优先级越高）"""
    DIRECT = "P0"        # User → Direct Permission
    USER_ROLE = "P1"     # User → Role → Permission
    GROUP_ROLE = "P2"    # User → Group → Role → Permission
    ORG_ROLE = "P3"      # User → Org → Role → Permission
