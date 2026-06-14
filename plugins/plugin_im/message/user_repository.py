from __future__ import annotations

from sqlalchemy import select
from sqlalchemy import or_
from sqlalchemy.orm import Session

from plugins.plugin_client.user.models import ClientUser
from plugins.plugin_sys.user.models import SysUser


class IMUserRepository:
    def __init__(self, db: Session):
        self.db = db

    def list_sys_users(self, user_ids: list[str]) -> list[SysUser]:
        if not user_ids:
            return []
        stmt = select(SysUser).where(SysUser.id.in_(user_ids))
        return list(self.db.execute(stmt).scalars().all())

    def list_client_users(self, user_ids: list[str]) -> list[ClientUser]:
        if not user_ids:
            return []
        stmt = select(ClientUser).where(ClientUser.id.in_(user_ids))
        return list(self.db.execute(stmt).scalars().all())

    def get_sys_user_by_id(self, user_id: str):
        stmt = select(SysUser).where(SysUser.id == user_id)
        return self.db.execute(stmt).scalar_one_or_none()

    def get_client_user_by_id(self, user_id: str):
        stmt = select(ClientUser).where(ClientUser.id == user_id)
        return self.db.execute(stmt).scalar_one_or_none()

    def search_sys_users(self, keyword: str, limit: int) -> list[SysUser]:
        like = f"%{keyword}%"
        stmt = select(SysUser).where(
            or_(SysUser.username.like(like), SysUser.nickname.like(like))
        ).limit(limit)
        return list(self.db.execute(stmt).scalars().all())

    def search_client_users(self, keyword: str, limit: int) -> list[ClientUser]:
        like = f"%{keyword}%"
        stmt = select(ClientUser).where(
            or_(ClientUser.username.like(like), ClientUser.nickname.like(like))
        ).limit(limit)
        return list(self.db.execute(stmt).scalars().all())
