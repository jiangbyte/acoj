"""Log Repository — mirrors hei-gin plugin-sys/log/service.go queries."""

from typing import Dict, Any, List, Optional
from datetime import datetime
from sqlalchemy import func, select, delete as sa_delete
from sqlalchemy.orm import load_only
from sqlalchemy.ext.asyncio import AsyncSession
from .models import SysLog
from .params import LogPageParam

_PAGE_EXCLUDED = {"param_json", "result_json", "exe_message", "sign_data"}


class LogRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    # ---- base CRUD ----

    async def find_by_id(self, id: str) -> Optional[SysLog]:
        return (await self.db.execute(select(SysLog).where(SysLog.id == id))).scalar_one_or_none()

    async def find_by_ids(self, ids: List[str]) -> List[SysLog]:
        return list((await self.db.execute(
            select(SysLog).where(SysLog.id.in_(ids))
        )).scalars().all())

    async def insert(self, entity: SysLog, user_id: Optional[str] = None) -> SysLog:
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

    async def update(self, entity: SysLog, user_id: Optional[str] = None) -> SysLog:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        await self.db.commit()
        await self.db.refresh(entity)
        return entity

    async def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysLog).where(SysLog.id.in_(ids))
        affected = (await self.db.execute(stmt)).rowcount
        await self.db.commit()
        return affected

    # ---- custom ----

    async def find_page(self, param: LogPageParam, query_builder=None) -> Dict[str, Any]:
        """Mirrors Go Page: keyword → name/op_user/op_ip, order by created_at DESC."""
        filters = []

        if param.category:
            filters.append(SysLog.category == param.category)
        if param.keyword:
            kw = f"%{param.keyword}%"
            filters.append(
                SysLog.name.ilike(kw) |
                SysLog.op_user.ilike(kw) |
                SysLog.op_ip.ilike(kw)
            )

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count(SysLog.id)).where(*filters)
        total = (await self.db.execute(count_stmt)).scalar() or 0

        cols_to_load = [c for c in SysLog.__table__.columns if c.name not in _PAGE_EXCLUDED]
        load_attrs = [getattr(SysLog, c.name) for c in cols_to_load]
        query_stmt = (
            select(SysLog)
            .where(*filters)
            .options(load_only(*load_attrs))
            .order_by(SysLog.created_at.desc())
            .offset(offset)
            .limit(size)
        )
        records = list((await self.db.execute(query_stmt)).scalars().all())

        return {"records": records, "total": total}

    async def count_by_category_since(self, category: str, since: datetime) -> int:
        stmt = select(func.count(SysLog.id)).where(
            SysLog.category == category,
            SysLog.op_time >= since,
        )
        return (await self.db.execute(stmt)).scalar() or 0

    async def count_group_by_category_since(self, categories: List[str], since: datetime) -> Dict[str, int]:
        stmt = (
            select(SysLog.category, func.count(SysLog.id))
            .where(
                SysLog.category.in_(categories),
                SysLog.op_time >= since,
            )
            .group_by(SysLog.category)
        )
        return dict((await self.db.execute(stmt)).all())

    async def count_total_by_category(self, categories: List[str]) -> Dict[str, int]:
        stmt = (
            select(SysLog.category, func.count(SysLog.id))
            .where(SysLog.category.in_(categories))
            .group_by(SysLog.category)
        )
        return dict((await self.db.execute(stmt)).all())

    async def daily_counts_since(self, categories: List[str], since: datetime) -> List[dict]:
        from sqlalchemy import cast, Date
        stmt = (
            select(
                cast(SysLog.op_time, Date).label("day"),
                SysLog.category,
                func.count(SysLog.id).label("cnt"),
            )
            .where(
                SysLog.category.in_(categories),
                SysLog.op_time >= since,
            )
            .group_by("day", SysLog.category)
            .order_by("day")
        )
        result = await self.db.execute(stmt)
        return [
            {"day": row[0], "category": row[1], "count": row[2]}
            for row in result.all()
        ]
