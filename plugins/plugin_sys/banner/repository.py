from typing import Optional, List, Dict, Any
from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, delete as sa_delete, update as sa_update
from .models import SysBanner
from .params import BannerPageParam


class BannerRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def find_by_id(self, id: str) -> Optional[SysBanner]:
        return (await self.db.execute(select(SysBanner).where(SysBanner.id == id))).scalar_one_or_none()

    async def find_page(self, param: BannerPageParam) -> Dict[str, Any]:
        current = max(1, param.current)
        size = max(1, param.size)
        if size > 100:
            size = 100
        offset = (current - 1) * size
        stmt = select(SysBanner).order_by(SysBanner.created_at.desc())
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = (await self.db.execute(count_stmt)).scalar() or 0
        records = list((await self.db.execute(stmt.offset(offset).limit(size))).scalars().all())
        return {"records": records, "total": total}

    async def insert(self, entity: SysBanner, user_id: Optional[str] = None) -> SysBanner:
        from sdk.utils.snowflake_utils import generate_id
        now = datetime.now()
        if not entity.id:
            entity.id = generate_id()
        if entity.created_at is None:
            entity.created_at = now
        entity.updated_at = now
        if user_id is not None and entity.created_by is None:
            entity.created_by = user_id
        self.db.add(entity)
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def update(self, entity: SysBanner, user_id: Optional[str] = None) -> SysBanner:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def update_by_id(self, banner_id: str, updates: dict) -> None:
        await self.db.execute(sa_update(SysBanner).where(SysBanner.id == banner_id).values(**updates))
        await self.db.commit()

    async def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysBanner).where(SysBanner.id.in_(ids))
        affected = (await self.db.execute(stmt)).rowcount
        await self.db.commit()
        return affected

    async def list_all_ordered(self) -> List[SysBanner]:
        return list((await self.db.execute(
            select(SysBanner).order_by(SysBanner.sort_code.asc())
        )).scalars().all())
