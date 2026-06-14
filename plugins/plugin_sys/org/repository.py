from typing import List, Dict, Any, Optional
from datetime import datetime
from sqlalchemy import select, func, or_, delete as sa_delete
from sqlalchemy.ext.asyncio import AsyncSession
from .models import SysOrg
from .params import OrgPageParam


class OrgRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    # ---- base CRUD ----

    async def find_by_id(self, id: str) -> Optional[SysOrg]:
        return (await self.db.execute(select(SysOrg).where(SysOrg.id == id))).scalar_one_or_none()

    async def find_by_ids(self, ids: List[str]) -> List[SysOrg]:
        result = await self.db.execute(
            select(SysOrg).where(SysOrg.id.in_(ids))
        )
        return list(result.scalars().all())

    async def find_all(self) -> List[SysOrg]:
        return list((await self.db.execute(select(SysOrg))).scalars().all())

    async def insert(self, entity: SysOrg, user_id: Optional[str] = None) -> SysOrg:
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

    async def update(self, entity: SysOrg, user_id: Optional[str] = None) -> SysOrg:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysOrg).where(SysOrg.id.in_(ids))
        affected = (await self.db.execute(stmt)).rowcount
        await self.db.commit()
        return affected

    # ---- custom ----

    async def find_page_by_filters(self, param: OrgPageParam) -> Dict[str, Any]:
        filters = []
        if param.parent_id is not None:
            if param.parent_id in ("", "0"):
                filters.append(SysOrg.parent_id.is_(None))
            else:
                filters.append(or_(SysOrg.parent_id == param.parent_id, SysOrg.id == param.parent_id))
        if param.keyword:
            filters.append(SysOrg.name.like(f"%{param.keyword}%"))

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count()).select_from(SysOrg).where(*filters)
        total = (await self.db.execute(count_stmt)).scalar() or 0

        stmt = select(SysOrg).where(*filters).order_by(SysOrg.sort_code.asc()).offset(offset).limit(size)
        result = await self.db.execute(stmt)
        records = list(result.scalars().all())

        return {"records": records, "total": total}

    async def find_all_ordered(self) -> List[SysOrg]:
        result = await self.db.execute(
            select(SysOrg).order_by(SysOrg.sort_code.asc())
        )
        return list(result.scalars().all())
