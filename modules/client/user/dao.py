from typing import Optional
from sqlalchemy import select
from sqlalchemy.orm import Session
from .models import ClientUser
from core.db.base_dao import BaseDAO


class ClientUserDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, ClientUser)

    def find_by_account(self, account: str) -> Optional[ClientUser]:
        return (
            self.db.execute(
                select(ClientUser).where(
                    ClientUser.account == account,
                    ClientUser.is_deleted == self._soft_delete_not_deleted,
                )
            )
            .scalar_one_or_none()
        )
