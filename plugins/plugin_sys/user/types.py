"""
User type definitions — mirrors hei-gin's plugins/plugin-sys/user/types.go.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class PermissionItem:
    """A permission entry with scope info."""
    permission_code: str = ""
    scope: str = "ALL"
    custom_scope_group_ids: Optional[str] = None
    custom_scope_org_ids: Optional[str] = None


@dataclass
class UpdateStatusParam:
    """Batch update user status."""
    ids: list[str] = field(default_factory=list)
    status: str = "ACTIVE"


@dataclass
class BatchImportUser:
    """Single user in a batch import."""
    username: Optional[str] = None
    nickname: Optional[str] = None
    phone: Optional[str] = None
    email: Optional[str] = None
    gender: Optional[str] = None
    password: Optional[str] = None


@dataclass
class BatchImportParam:
    """Batch import users."""
    users: list[BatchImportUser] = field(default_factory=list)
