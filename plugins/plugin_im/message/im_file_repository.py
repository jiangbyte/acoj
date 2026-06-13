from __future__ import annotations

from typing import Optional

from sqlalchemy import select
from sqlalchemy.orm import Session

from plugins.plugin_im.model.im_file import ImFile


class ImFileRepository:
    def __init__(self, db: Session):
        self.db = db

    def insert(self, entity: ImFile) -> None:
        self.db.add(entity)
        self.db.commit()

    def find_by_key(self, bucket: str, file_key: str) -> Optional[ImFile]:
        stmt = select(ImFile).where(ImFile.bucket == bucket, ImFile.file_key == file_key)
        return self.db.execute(stmt).scalar_one_or_none()
