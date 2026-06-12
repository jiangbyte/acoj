from __future__ import annotations

from sqlalchemy.orm import Session

from plugins.plugin_im.model.im_file import ImFile


class ImFileRepository:
    def __init__(self, db: Session):
        self.db = db

    def insert(self, entity: ImFile) -> None:
        self.db.add(entity)
        self.db.commit()

