from __future__ import annotations

from typing import Optional

from sqlalchemy import func, or_, select, delete as sa_delete
from sqlalchemy.ext.asyncio import AsyncSession

from plugins.plugin_sys.file.models import SysFile
from plugins.plugin_sys.file.params import FilePageParam


class FileRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def insert(self, entity: SysFile) -> SysFile:
        self.db.add(entity)
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def page(self, param: FilePageParam) -> dict[str, object]:
        stmt = select(SysFile)
        if param.keyword:
            like = f"%{param.keyword}%"
            stmt = stmt.where(or_(SysFile.name.like(like), SysFile.file_key.like(like)))
        if param.engine:
            stmt = stmt.where(SysFile.engine == param.engine)
        if param.bucket:
            stmt = stmt.where(SysFile.bucket == param.bucket)
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = int((await self.db.execute(count_stmt)).scalar() or 0)
        stmt = stmt.order_by(SysFile.created_at.desc()).offset((param.current - 1) * param.size).limit(param.size)
        result = await self.db.execute(stmt)
        return {
            "records": list(result.scalars().all()),
            "total": total,
        }

    async def find_by_id(self, file_id: str) -> Optional[SysFile]:
        stmt = select(SysFile).where(SysFile.id == file_id)
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def find_by_key(self, bucket: str, file_key: str) -> Optional[SysFile]:
        stmt = select(SysFile).where(SysFile.bucket == bucket, SysFile.file_key == file_key)
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def find_by_ids(self, ids: list[str]) -> list[SysFile]:
        if not ids:
            return []
        stmt = select(SysFile).where(SysFile.id.in_(ids))
        return list((await self.db.execute(stmt)).scalars().all())

    async def delete_by_ids(self, ids: list[str]) -> None:
        if not ids:
            return
        await self.db.execute(sa_delete(SysFile).where(SysFile.id.in_(ids)))
        await self.db.commit()
