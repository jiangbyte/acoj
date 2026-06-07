"""
UserProvider — resolves user names by ID.

Mirrors hei-gin's ``plugins/plugin-sys/provider/user_provider.go``.
"""

from __future__ import annotations

from core.db import SessionLocal
from plugins.plugin_sys.user.models import SysUser


class UserProvider:
    """Resolves a user's display name (nickname) by their ID."""

    def get_user_name_by_id(self, user_id: str) -> str:
        """Return the user's nickname, falling back to their ID if not found."""
        db = SessionLocal()
        try:
            user = db.query(SysUser).filter(SysUser.id == user_id).first()
            if user and user.nickname:
                return user.nickname
            return user_id
        finally:
            db.close()
