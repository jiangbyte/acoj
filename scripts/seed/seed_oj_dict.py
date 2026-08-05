#!/usr/bin/env python3
"""Idempotent seed for OJ-related sys_dict entries (submission + problem difficulty)."""

from __future__ import annotations

import asyncio
from datetime import UTC, datetime

from sqlalchemy import select

from app.modules.sys.dict.model import SysDict
from app.platform.db.session import get_session_factory, init_engine

# Fixed IDs keep portal/admin dict trees stable across environments.
OJ_DICT_ROWS: list[dict[str, object]] = [
    # SUBMISSION_RESULT
    {
        "id": "100155",
        "code": "SUBMISSION_RESULT",
        "label": "提交判题结果",
        "value": "SUBMISSION_RESULT",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100156", "code": "SUBMISSION_RESULT_AC", "label": "AC", "value": "AC", "color": "#00b8a3", "parent_id": "100155", "sort": 1},
    {"id": "100157", "code": "SUBMISSION_RESULT_WA", "label": "WA", "value": "WA", "color": "#ff375f", "parent_id": "100155", "sort": 2},
    {"id": "100158", "code": "SUBMISSION_RESULT_TLE", "label": "TLE", "value": "TLE", "color": "#ffa116", "parent_id": "100155", "sort": 3},
    {"id": "100159", "code": "SUBMISSION_RESULT_MLE", "label": "MLE", "value": "MLE", "color": "#ffa116", "parent_id": "100155", "sort": 4},
    {"id": "100160", "code": "SUBMISSION_RESULT_RE", "label": "RE", "value": "RE", "color": "#ff4d4f", "parent_id": "100155", "sort": 5},
    {"id": "100161", "code": "SUBMISSION_RESULT_CE", "label": "CE", "value": "CE", "color": "#8c8c8c", "parent_id": "100155", "sort": 6},
    {"id": "100162", "code": "SUBMISSION_RESULT_OLE", "label": "OLE", "value": "OLE", "color": "#ffa116", "parent_id": "100155", "sort": 7},
    {"id": "100163", "code": "SUBMISSION_RESULT_SE", "label": "SE", "value": "SE", "color": "#d03050", "parent_id": "100155", "sort": 8},
    {"id": "100164", "code": "SUBMISSION_RESULT_IE", "label": "IE", "value": "IE", "color": "#d03050", "parent_id": "100155", "sort": 9},
    # SUBMISSION_STATUS
    {
        "id": "100165",
        "code": "SUBMISSION_STATUS",
        "label": "提交判题状态",
        "value": "SUBMISSION_STATUS",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100166", "code": "SUBMISSION_STATUS_QUEUED", "label": "QUEUED", "value": "QUEUED", "color": "#2080f0", "parent_id": "100165", "sort": 1},
    {"id": "100167", "code": "SUBMISSION_STATUS_JUDGING", "label": "JUDGING", "value": "JUDGING", "color": "#2080f0", "parent_id": "100165", "sort": 2},
    {"id": "100168", "code": "SUBMISSION_STATUS_COMPLETED", "label": "COMPLETED", "value": "COMPLETED", "color": "#18a058", "parent_id": "100165", "sort": 3},
    {"id": "100169", "code": "SUBMISSION_STATUS_FAILED", "label": "FAILED", "value": "FAILED", "color": "#d03050", "parent_id": "100165", "sort": 4},
    # SUBMISSION_KIND
    {
        "id": "100170",
        "code": "SUBMISSION_KIND",
        "label": "提交类型",
        "value": "SUBMISSION_KIND",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100171", "code": "SUBMISSION_KIND_OFFICIAL", "label": "OFFICIAL", "value": "OFFICIAL", "color": "#18a058", "parent_id": "100170", "sort": 1},
    {"id": "100172", "code": "SUBMISSION_KIND_TRIAL", "label": "TRIAL", "value": "TRIAL", "color": "#2080f0", "parent_id": "100170", "sort": 2},
    {"id": "100173", "code": "SUBMISSION_KIND_CONTEST", "label": "CONTEST", "value": "CONTEST", "color": "#722ed1", "parent_id": "100170", "sort": 3},
    # PROBLEM_DIFFICULTY
    {
        "id": "100174",
        "code": "PROBLEM_DIFFICULTY",
        "label": "题目难度",
        "value": "PROBLEM_DIFFICULTY",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100175", "code": "PROBLEM_DIFFICULTY_EASY", "label": "简单", "value": "Easy", "color": "#00b8a3", "parent_id": "100174", "sort": 1},
    {"id": "100176", "code": "PROBLEM_DIFFICULTY_MEDIUM", "label": "中等", "value": "Medium", "color": "#ffa116", "parent_id": "100174", "sort": 2},
    {"id": "100177", "code": "PROBLEM_DIFFICULTY_HARD", "label": "困难", "value": "Hard", "color": "#ff375f", "parent_id": "100174", "sort": 3},
    # PROBLEM_LIST_KIND
    {
        "id": "100180",
        "code": "PROBLEM_LIST_KIND",
        "label": "题单类型",
        "value": "PROBLEM_LIST_KIND",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100181", "code": "PROBLEM_LIST_KIND_PERSONAL", "label": "个人", "value": "PERSONAL", "color": "#18a058", "parent_id": "100180", "sort": 1},
    {"id": "100182", "code": "PROBLEM_LIST_KIND_OFFICIAL", "label": "官方", "value": "OFFICIAL", "color": "#722ed1", "parent_id": "100180", "sort": 2},
    # PROBLEM_LIST_VISIBILITY
    {
        "id": "100183",
        "code": "PROBLEM_LIST_VISIBILITY",
        "label": "题单可见性",
        "value": "PROBLEM_LIST_VISIBILITY",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100184", "code": "PROBLEM_LIST_VISIBILITY_PRIVATE", "label": "私有", "value": "PRIVATE", "color": "#8c8c8c", "parent_id": "100183", "sort": 1},
    {"id": "100185", "code": "PROBLEM_LIST_VISIBILITY_PUBLIC", "label": "公开", "value": "PUBLIC", "color": "#18a058", "parent_id": "100183", "sort": 2},
    # LEARNING_PLAN_CATEGORY
    {
        "id": "100186",
        "code": "LEARNING_PLAN_CATEGORY",
        "label": "学习计划分类",
        "value": "LEARNING_PLAN_CATEGORY",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100187", "code": "LEARNING_PLAN_CATEGORY_FEATURED", "label": "精选", "value": "FEATURED", "color": "#18a058", "parent_id": "100186", "sort": 1},
    {"id": "100188", "code": "LEARNING_PLAN_CATEGORY_INTERVIEW", "label": "面试准备", "value": "INTERVIEW", "color": "#722ed1", "parent_id": "100186", "sort": 2},
    # CONTEST_FORMAT
    {
        "id": "100190",
        "code": "CONTEST_FORMAT",
        "label": "竞赛赛制",
        "value": "CONTEST_FORMAT",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100191", "code": "CONTEST_FORMAT_DEFAULT", "label": "Default", "value": "default", "color": "#8c8c8c", "parent_id": "100190", "sort": 1},
    {"id": "100192", "code": "CONTEST_FORMAT_ACM", "label": "ACM", "value": "acm", "color": "#1677FF", "parent_id": "100190", "sort": 2},
    {"id": "100193", "code": "CONTEST_FORMAT_ICPC", "label": "ICPC", "value": "icpc", "color": "#1677FF", "parent_id": "100190", "sort": 3},
    {"id": "100194", "code": "CONTEST_FORMAT_ATCODER", "label": "AtCoder", "value": "atcoder", "color": "#722ed1", "parent_id": "100190", "sort": 4},
    {"id": "100195", "code": "CONTEST_FORMAT_OI", "label": "OI", "value": "oi", "color": "#18a058", "parent_id": "100190", "sort": 5},
    {"id": "100196", "code": "CONTEST_FORMAT_IOI", "label": "IOI", "value": "ioi", "color": "#18a058", "parent_id": "100190", "sort": 6},
    # CONTEST_LIFECYCLE_STATUS
    {
        "id": "100197",
        "code": "CONTEST_LIFECYCLE_STATUS",
        "label": "竞赛状态",
        "value": "CONTEST_LIFECYCLE_STATUS",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100198", "code": "CONTEST_LIFECYCLE_STATUS_SCHEDULED", "label": "未开始", "value": "SCHEDULED", "color": "#1677FF", "parent_id": "100197", "sort": 1},
    {"id": "100199", "code": "CONTEST_LIFECYCLE_STATUS_RUNNING", "label": "进行中", "value": "RUNNING", "color": "#18a058", "parent_id": "100197", "sort": 2},
    {"id": "100200", "code": "CONTEST_LIFECYCLE_STATUS_ENDED", "label": "已结束", "value": "ENDED", "color": "#8c8c8c", "parent_id": "100197", "sort": 3},
    {"id": "100201", "code": "CONTEST_LIFECYCLE_STATUS_LOCKED", "label": "已锁定", "value": "LOCKED", "color": "#fa8c16", "parent_id": "100197", "sort": 4},
    # CONTEST_TYPE（门户筛选：计分 / 不计分 / 私有）
    {
        "id": "100202",
        "code": "CONTEST_TYPE",
        "label": "竞赛类型",
        "value": "CONTEST_TYPE",
        "color": "#2080f0",
        "parent_id": None,
        "sort": 0,
    },
    {"id": "100203", "code": "CONTEST_TYPE_RATED", "label": "计分", "value": "RATED", "color": "#faad14", "parent_id": "100202", "sort": 1},
    {"id": "100204", "code": "CONTEST_TYPE_UNRATED", "label": "不计分", "value": "UNRATED", "color": "#18a058", "parent_id": "100202", "sort": 2},
    {"id": "100205", "code": "CONTEST_TYPE_PRIVATE", "label": "私有", "value": "PRIVATE", "color": "#8c8c8c", "parent_id": "100202", "sort": 3},
]


async def upsert_oj_dicts(db) -> int:
    """Insert or refresh OJ dict rows. Returns number of rows touched."""
    now = datetime.now(UTC)
    touched = 0
    for row in OJ_DICT_ROWS:
        entity_id = str(row["id"])
        entity = (
            await db.execute(select(SysDict).where(SysDict.id == entity_id))
        ).scalar_one_or_none()
        if entity is None:
            by_code = (
                await db.execute(select(SysDict).where(SysDict.code == str(row["code"])))
            ).scalar_one_or_none()
            entity = by_code
        if entity is None:
            entity = SysDict(id=entity_id)
            db.add(entity)
        entity.code = str(row["code"])
        entity.label = str(row["label"])
        entity.value = str(row["value"])
        entity.color = str(row["color"]) if row.get("color") else None
        entity.category = "SYS"
        entity.parent_id = str(row["parent_id"]) if row.get("parent_id") else None
        entity.status = "ENABLED"
        entity.sort = int(row["sort"])
        if entity.created_at is None:
            entity.created_at = now
        entity.updated_at = now
        touched += 1
    await db.flush()
    return touched


async def main() -> None:
    init_engine()
    async with get_session_factory()() as db:
        count = await upsert_oj_dicts(db)
        await db.commit()
        print(f"oj dict seed done: {count} rows")


if __name__ == "__main__":
    asyncio.run(main())
