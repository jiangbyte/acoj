#!/usr/bin/env python3
"""Seed portal-visible problems + demo contests (idempotent by contest key / problem code)."""

from __future__ import annotations

import asyncio
import sys
from datetime import UTC, date, datetime, timedelta
from pathlib import Path
from zoneinfo import ZoneInfo

from sqlalchemy import delete, select

from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.problem.model import OjContestProblem
from app.modules.biz.problem.enums import ProblemDifficulty, ProblemStatus
from app.modules.biz.problem.language.model import OjProblemLanguage
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.stats import refresh_problem_ac_stats
from app.modules.biz.study.enums import LearningPlanCategory, ProblemListKind, ProblemListVisibility
from app.modules.biz.study.model import (
    OjDailyProblem,
    OjLearningPlan,
    OjLearningPlanItem,
    OjLearningPlanSection,
    OjProblemList,
    OjProblemListItem,
)
from app.platform.db.session import get_session_factory, init_engine
from app.platform.id_generator.snowflake import generate_snowflake_id

_SEED_DIR = Path(__file__).resolve().parent
if str(_SEED_DIR) not in sys.path:
    sys.path.insert(0, str(_SEED_DIR))
from seed_oj_dict import upsert_oj_dicts  # noqa: E402

SHANGHAI = ZoneInfo("Asia/Shanghai")

# code -> (display name, markdown description)
PORTAL_PROBLEMS: dict[str, tuple[str, str]] = {
    "1": (
        "A + B Problem",
        """## 题目描述

给定两个整数 $a$ 和 $b$，输出它们的和。

## 输入格式

一行两个整数 $a, b$（$|a|,|b| \\le 10^9$）。

## 输出格式

一个整数，表示 $a+b$。

## 样例输入

```
1 2
```

## 样例输出

```
3
```
""",
    ),
    "L10": (
        "多语言 A+B",
        """## 题目描述

经典 A+B，开放多种编程语言。

输入两个整数，输出它们的和。
""",
    ),
    "T10": (
        "回显（ACM）",
        """## 题目描述

读入一行字符串并原样输出（echo）。

用于练习 ACM/ICPC 赛制下的提交与判题。
""",
    ),
    "T11": (
        "回显（部分分）",
        """## 题目描述

读入一行并输出。部分测试点可得分，适合 OI 赛制练习。
""",
    ),
    "T12": (
        "回显（子任务）",
        """## 题目描述

读入一行并输出。含 batch 子任务依赖，适合 IOI 赛制练习。
""",
    ),
    "T40": (
        "空白字符处理",
        """## 题目描述

输出时注意行首行尾空白的处理。读入一行并正确输出。
""",
    ),
    "T57": (
        "多行回显",
        """## 题目描述

多行输入输出练习。按题面要求原样输出各行内容。
""",
    ),
}

DEFAULT_LANGS = ("cpp17", "python3", "c11", "java17", "go")

# Demo difficulty map for portal showcase problems (Easy|Medium|Hard).
PROBLEM_DIFFICULTY_BY_CODE: dict[str, str] = {
    "1": ProblemDifficulty.EASY.value,
    "L10": ProblemDifficulty.EASY.value,
    "T10": ProblemDifficulty.MEDIUM.value,
    "T11": ProblemDifficulty.HARD.value,
    "T12": ProblemDifficulty.MEDIUM.value,
    "T40": ProblemDifficulty.EASY.value,
    "T57": ProblemDifficulty.MEDIUM.value,
}


async def ensure_languages(db, problem_id: str) -> None:
    existing = set(
        (
            await db.execute(
                select(OjProblemLanguage.language_key).where(OjProblemLanguage.problem_id == problem_id)
            )
        )
        .scalars()
        .all()
    )
    for key in DEFAULT_LANGS:
        if key in existing:
            row = (
                await db.execute(
                    select(OjProblemLanguage).where(
                        OjProblemLanguage.problem_id == problem_id,
                        OjProblemLanguage.language_key == key,
                    )
                )
            ).scalar_one()
            row.status = "ENABLED"
        else:
            db.add(
                OjProblemLanguage(
                    id=generate_snowflake_id(),
                    problem_id=problem_id,
                    language_key=key,
                    status="ENABLED",
                )
            )


async def seed_problems(db) -> dict[str, str]:
    """Return code -> id for portal problems."""
    now = datetime.now(UTC)
    id_by_code: dict[str, str] = {}
    for code, (name, description) in PORTAL_PROBLEMS.items():
        entity = (await db.execute(select(OjProblem).where(OjProblem.code == code))).scalar_one_or_none()
        if entity is None:
            print("skip missing problem", code)
            continue
        entity.name = name
        entity.description = description
        entity.summary = name
        entity.status = ProblemStatus.PUBLISHED.value
        entity.is_public = True
        entity.difficulty = PROBLEM_DIFFICULTY_BY_CODE.get(code, ProblemDifficulty.MEDIUM.value)
        if entity.published_at is None:
            entity.published_at = now
        await ensure_languages(db, entity.id)
        await refresh_problem_ac_stats(db, entity.id)
        id_by_code[code] = entity.id
        print(
            "problem",
            code,
            "->",
            name,
            entity.id,
            "diff=",
            entity.difficulty,
            f"ac={entity.ac_rate}%",
            f"solvers={entity.user_count}",
        )
    await db.flush()
    return id_by_code


async def upsert_contest(
    db,
    *,
    key: str,
    name: str,
    summary: str,
    description: str,
    start: datetime,
    end: datetime,
    format_name: str,
    is_rated: bool,
    freeze_seconds: int | None,
    problems: list[tuple[str, str, float, bool]],
) -> str:
    """problems: list of (label, problem_id, points, partial)."""
    existing = (await db.execute(select(OjContest).where(OjContest.key == key))).scalar_one_or_none()
    if existing is not None:
        await db.execute(delete(OjContestProblem).where(OjContestProblem.contest_id == existing.id))
        await db.delete(existing)
        await db.flush()
        print("replaced contest", key)

    contest = OjContest(
        id=generate_snowflake_id(),
        key=key,
        name=name,
        summary=summary,
        description=description,
        start_time=start,
        end_time=end,
        time_limit_seconds=None,
        freeze_seconds=freeze_seconds,
        is_visible=True,
        is_private=False,
        access_code=None,
        is_rated=is_rated,
        rating_floor=None,
        rating_ceiling=None,
        rate_all=False,
        scoreboard_visibility="VISIBLE",
        format_name=format_name,
        format_config={"penalty_minutes": 20} if format_name in {"icpc", "acm", "atcoder"} else {},
        points_precision=0 if format_name in {"icpc", "acm", "atcoder"} else 2,
        hide_problem_tags=False,
        hide_problem_authors=False,
        run_pretests_only=False,
        use_clarifications=True,
        tester_see_scoreboard=True,
        tester_see_submissions=True,
        locked_after=None,
        register_start=start - timedelta(days=14),
        register_end=end,
        registration_mode="AUTO",
        list_visibility="PUBLIC",
        user_count=0,
        extra={},
    )
    db.add(contest)
    await db.flush()
    for sort, (label, pid, points, partial) in enumerate(problems, start=1):
        db.add(
            OjContestProblem(
                id=generate_snowflake_id(),
                contest_id=contest.id,
                problem_id=pid,
                label=label,
                points=points,
                partial=partial,
                is_pretested=False,
                sort=sort,
                max_submissions=None,
                output_prefix_override=None,
            )
        )
    await db.flush()
    print("contest", key, contest.id, "problems", len(problems))
    return contest.id


async def upsert_official_list(
    db,
    *,
    code: str,
    title: str,
    summary: str,
    problem_ids: list[str],
    sort: int = 0,
) -> str:
    existing = (await db.execute(select(OjProblemList).where(OjProblemList.code == code))).scalar_one_or_none()
    if existing is not None:
        await db.execute(delete(OjProblemListItem).where(OjProblemListItem.list_id == existing.id))
        await db.delete(existing)
        await db.flush()
        print("replaced problem list", code)

    entity = OjProblemList(
        id=generate_snowflake_id(),
        kind=ProblemListKind.OFFICIAL.value,
        owner_id=None,
        code=code,
        title=title,
        summary=summary,
        cover_url=None,
        visibility=ProblemListVisibility.PUBLIC.value,
        is_system=False,
        status="ENABLED",
        sort=sort,
        extra={},
    )
    db.add(entity)
    await db.flush()
    for i, pid in enumerate(problem_ids):
        db.add(OjProblemListItem(id=generate_snowflake_id(), list_id=entity.id, problem_id=pid, sort=i))
    await db.flush()
    print("problem list", code, entity.id, "items", len(problem_ids))
    return entity.id


async def upsert_learning_plan(
    db,
    *,
    code: str,
    title: str,
    subtitle: str,
    overview: str,
    category: str,
    sections: list[tuple[str, list[str]]],
    sort: int = 0,
) -> str:
    existing = (await db.execute(select(OjLearningPlan).where(OjLearningPlan.code == code))).scalar_one_or_none()
    if existing is not None:
        section_ids = list(
            (
                await db.execute(
                    select(OjLearningPlanSection.id).where(OjLearningPlanSection.plan_id == existing.id)
                )
            )
            .scalars()
            .all()
        )
        if section_ids:
            await db.execute(delete(OjLearningPlanItem).where(OjLearningPlanItem.section_id.in_(section_ids)))
        await db.execute(delete(OjLearningPlanSection).where(OjLearningPlanSection.plan_id == existing.id))
        await db.delete(existing)
        await db.flush()
        print("replaced learning plan", code)

    plan = OjLearningPlan(
        id=generate_snowflake_id(),
        code=code,
        title=title,
        subtitle=subtitle,
        overview=overview,
        cover_url=None,
        category=category,
        status="ENABLED",
        sort=sort,
        extra={},
    )
    db.add(plan)
    await db.flush()
    total = 0
    for si, (sec_title, pids) in enumerate(sections):
        section = OjLearningPlanSection(
            id=generate_snowflake_id(),
            plan_id=plan.id,
            title=sec_title,
            sort=si,
        )
        db.add(section)
        await db.flush()
        for pi, pid in enumerate(pids):
            db.add(
                OjLearningPlanItem(
                    id=generate_snowflake_id(),
                    section_id=section.id,
                    problem_id=pid,
                    sort=pi,
                )
            )
            total += 1
    await db.flush()
    print("learning plan", code, plan.id, "problems", total)
    return plan.id


async def seed_daily_problems(db, problem_ids: list[str], days: int = 30) -> int:
    if not problem_ids:
        print("skip daily: no problems")
        return 0
    today = datetime.now(SHANGHAI).date()
    start = today - timedelta(days=days - 1)
    existing = list(
        (
            await db.execute(
                select(OjDailyProblem).where(OjDailyProblem.day_date >= start, OjDailyProblem.day_date <= today)
            )
        )
        .scalars()
        .all()
    )
    by_day = {row.day_date: row for row in existing}
    count = 0
    for offset in range(days):
        day: date = start + timedelta(days=offset)
        pid = problem_ids[offset % len(problem_ids)]
        row = by_day.get(day)
        if row is None:
            db.add(OjDailyProblem(id=generate_snowflake_id(), day_date=day, problem_id=pid))
            count += 1
        else:
            row.problem_id = pid
            count += 1
    await db.flush()
    print("daily problems upserted", count, f"from {start} to {today}")
    return count


async def seed_study_content(db, id_by_code: dict[str, str]) -> None:
    codes = [c for c in ("1", "L10", "T10", "T11", "T12", "T40", "T57") if c in id_by_code]
    pids = [id_by_code[c] for c in codes]
    if len(pids) < 3:
        print("skip study seed: need at least 3 portal problems")
        return

    await upsert_official_list(
        db,
        code="HOT-100",
        title="热门 100 题精选",
        summary="门户演示官方题单：覆盖入门到中等难度的课堂练习。",
        problem_ids=pids[:5],
        sort=1,
    )
    await upsert_official_list(
        db,
        code="EASY-START",
        title="新手入门题单",
        summary="适合刚开始刷题的同学，建立基本输入输出与模拟能力。",
        problem_ids=pids[:3],
        sort=2,
    )

    await upsert_learning_plan(
        db,
        code="FEATURED-INTRO",
        title="算法入门路径",
        subtitle="从语法与模拟到基础算法",
        overview="本路径帮助你建立稳定的练习节奏。按章节循序渐进，完成每日练习并在题单中巩固。",
        category=LearningPlanCategory.FEATURED.value,
        sections=[
            ("第 1 章 · 入门模拟", pids[:2]),
            ("第 2 章 · 进阶练习", pids[2:5] or pids[:2]),
        ],
        sort=1,
    )
    await upsert_learning_plan(
        db,
        code="INTERVIEW-CORE",
        title="期末复习专题",
        subtitle="覆盖常见笔试与综合作业题型",
        overview="面向阶段性复习的短路径：挑重点题快速过一遍，关注通过率与解题思路。",
        category=LearningPlanCategory.INTERVIEW.value,
        sections=[
            ("基础题", pids[:3]),
            ("综合题", pids[3:] or pids[:2]),
        ],
        sort=2,
    )

    await seed_daily_problems(db, pids, days=30)


async def main() -> None:
    init_engine()
    async with get_session_factory()() as db:
        dict_count = await upsert_oj_dicts(db)
        print("oj dict rows:", dict_count)
        id_by_code = await seed_problems(db)
        now = datetime.now(UTC)

        def pid(*codes: str) -> list[str]:
            return [id_by_code[c] for c in codes if c in id_by_code]

        # Running weekly ICPC-style
        await upsert_contest(
            db,
            key="P-WEEKLY",
            name="ACOJ 周赛 #1",
            summary="面向门户的周赛，进行中可报名提交",
            description="""# ACOJ 周赛 #1

欢迎参加门户演示周赛。

- 赛制：ICPC
- 题目：A/B/C 三题
- 可使用答疑

祝你 AC！
""",
            start=now - timedelta(hours=1),
            end=now + timedelta(days=2),
            format_name="icpc",
            is_rated=True,
            freeze_seconds=3600,
            problems=[
                ("A", id_by_code["1"], 100, False),
                ("B", id_by_code["T10"], 100, False),
                ("C", id_by_code["L10"], 100, False),
            ]
            if all(c in id_by_code for c in ("1", "T10", "L10"))
            else [],
        )

        # Upcoming
        await upsert_contest(
            db,
            key="P-UPCOMING",
            name="新手练习赛",
            summary="即将开始的新手向练习赛",
            description="""# 新手练习赛

适合刚入门的同学。开赛前可报名，开赛后查看题面并提交。
""",
            start=now + timedelta(days=1),
            end=now + timedelta(days=1, hours=3),
            format_name="icpc",
            is_rated=False,
            freeze_seconds=None,
            problems=[
                ("A", id_by_code["1"], 100, False),
                ("B", id_by_code["T40"], 100, False),
            ]
            if all(c in id_by_code for c in ("1", "T40"))
            else [],
        )

        # Ended archive
        await upsert_contest(
            db,
            key="P-ARCHIVE",
            name="往期公开赛（已结束）",
            summary="已结束的公开赛，可查看榜单与题面",
            description="""# 往期公开赛

比赛已结束，欢迎复盘题目与榜单。
""",
            start=now - timedelta(days=7),
            end=now - timedelta(days=6),
            format_name="oi",
            is_rated=False,
            freeze_seconds=None,
            problems=[
                ("A", id_by_code["T11"], 100, True),
                ("B", id_by_code["T12"], 100, True),
                ("C", id_by_code["T57"], 100, False),
            ]
            if all(c in id_by_code for c in ("T11", "T12", "T57"))
            else [],
        )

        await seed_study_content(db, id_by_code)

        await db.commit()
        print("done. portal problems:", len(id_by_code))


if __name__ == "__main__":
    asyncio.run(main())
