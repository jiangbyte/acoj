from sqlalchemy.orm import Session
from .models import SysModule, SysResource
from core.db.base_dao import BaseDAO


class ModuleDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysModule)


class ResourceDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysResource)
