from typing import List
from sqlalchemy.orm import Session
from .models import SysNotice
from core.db.base_dao import BaseDAO


class NoticeDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysNotice)
