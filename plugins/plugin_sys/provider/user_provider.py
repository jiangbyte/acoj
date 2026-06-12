"""User provider."""

from __future__ import annotations

from plugins.plugin_sys.user.repository import UserRepository


class UserProvider:
    def __init__(self, session_factory):
        self._session_factory = session_factory

    def get_user_name_by_id(self, user_id: str) -> str:
        db = self._session_factory()
        try:
            nickname = UserRepository(db).find_nickname_by_id(user_id)
            return nickname or user_id
        finally:
            db.close()
