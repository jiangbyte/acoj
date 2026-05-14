from typing import Dict, Any, List
from datetime import datetime
from sqlalchemy import func, select
from sqlalchemy.orm import Session, load_only
from .models import SysLog
from .params import LogPageParam
from core.db.base_dao import BaseDAO
from core.db.query_wrapper import QueryWrapper

# Columns excluded from page queries for performance (loaded only in detail)
_PAGE_EXCLUDED = {"param_json", "result_json", "exe_message", "sign_data"}


class LogDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysLog)

    def find_page(self, param: LogPageParam, query_builder=None) -> Dict[str, Any]:
        """Paginated log query — skips LONGTEXT fields (param_json, result_json,
        exe_message, sign_data) for performance.  Full content is loaded
        on-demand in ``detail()``."""
        filters = []

        if param.keyword:
            filters.append(SysLog.name.ilike(f"%{param.keyword}%"))
        if param.category:
            filters.append(SysLog.category == param.category)
        if param.exe_status:
            filters.append(SysLog.exe_status == param.exe_status)

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        # Count
        count_stmt = select(func.count(SysLog.id)).where(*filters)
        total = self.db.execute(count_stmt).scalar() or 0

        # Data — include only non-LONGTEXT columns
        cols_to_load = [c for c in SysLog.__table__.columns if c.name not in _PAGE_EXCLUDED]
        load_attrs = [getattr(SysLog, c.name) for c in cols_to_load]
        query_stmt = (
            select(SysLog)
            .where(*filters)
            .options(load_only(*load_attrs))
            .order_by(SysLog.op_time.desc())
            .offset(offset)
            .limit(size)
        )
        records = list(self.db.execute(query_stmt).scalars().all())

        return {"records": records, "total": total}

    def count_by_category_since(self, category: str, since: datetime) -> int:
        """Count log entries of a given category since a specific time."""
        stmt = select(func.count(SysLog.id)).where(
            SysLog.category == category,
            SysLog.op_time >= since,
        )
        return self.db.execute(stmt).scalar() or 0

    def count_group_by_category_since(self, categories: List[str], since: datetime) -> Dict[str, int]:
        """Count log entries grouped by category since a specific time."""
        from sqlalchemy import func as f
        stmt = (
            select(SysLog.category, func.count(SysLog.id))
            .where(
                SysLog.category.in_(categories),
                SysLog.op_time >= since,
            )
            .group_by(SysLog.category)
        )
        return dict(self.db.execute(stmt).all())

    def count_total_by_category(self, categories: List[str]) -> Dict[str, int]:
        """Total count of log entries grouped by category."""
        from sqlalchemy import func as f
        stmt = (
            select(SysLog.category, func.count(SysLog.id))
            .where(SysLog.category.in_(categories))
            .group_by(SysLog.category)
        )
        return dict(self.db.execute(stmt).all())

    def daily_counts_since(self, categories: List[str], since: datetime) -> List[dict]:
        """Daily count of log entries for each category since a date, ordered by day."""
        from sqlalchemy import func as f, cast, Date
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
        return [
            {"day": row[0], "category": row[1], "count": row[2]}
            for row in self.db.execute(stmt).all()
        ]
