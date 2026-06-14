from typing import Optional, Dict, Any, List
from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, delete as sa_delete, update as sa_update
from .models import SysPosition
from .params import PositionPageParam
from ..user.models import SysUser


class PositionRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    # ---- base CRUD ----

    async def find_by_id(self, id: str) -> Optional[SysPosition]:
        return (await self.db.execute(select(SysPosition).where(SysPosition.id == id))).scalar_one_or_none()

    async def find_by_ids(self, ids: List[str]) -> List[SysPosition]:
        return list((await self.db.execute(
            select(SysPosition).where(SysPosition.id.in_(ids))
        )).scalars().all())

    async def find_all(self) -> List[SysPosition]:
        return list((await self.db.execute(select(SysPosition))).scalars().all())

    async def find_all_ordered(self) -> List[SysPosition]:
        return list((await self.db.execute(select(SysPosition).order_by(SysPosition.sort_code.asc()))).scalars().all())

    async def insert(self, entity: SysPosition, user_id: Optional[str] = None) -> SysPosition:
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

    async def update(self, entity: SysPosition, user_id: Optional[str] = None) -> SysPosition:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysPosition).where(SysPosition.id.in_(ids))
        affected = (await self.db.execute(stmt)).rowcount
        await self.db.commit()
        return affected

    # ---- custom ----

    async def find_page_by_filters(self, param: PositionPageParam) -> Dict[str, Any]:
        filters = []
        if param.keyword:
            filters.append(SysPosition.name.like(f"%{param.keyword}%"))
        if param.category:
            filters.append(SysPosition.category == param.category)
        if param.org_id:
            filters.append(SysPosition.org_id == param.org_id)

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count()).select_from(SysPosition).where(*filters)
        total = (await self.db.execute(count_stmt)).scalar() or 0

        stmt = select(SysPosition).where(*filters).order_by(SysPosition.sort_code.asc()).offset(offset).limit(size)
        records = list((await self.db.execute(stmt)).scalars().all())

        return {"records": records, "total": total}

    async def update_by_id(self, position_id: str, updates: dict) -> None:
        await self.db.execute(sa_update(SysPosition).where(SysPosition.id == position_id).values(**updates))
        await self.db.commit()

    async def clear_user_positions(self, ids: List[str]) -> None:
        if not ids:
            return
        await self.db.execute(sa_update(SysUser).where(SysUser.position_id.in_(ids)).values(position_id=None))
        await self.db.commit()
