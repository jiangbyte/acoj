from sqlalchemy.orm import Session
from .models import SysBanner
from core.db.base_dao import BaseDAO


class BannerDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysBanner)
