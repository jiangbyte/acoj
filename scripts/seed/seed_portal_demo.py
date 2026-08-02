#!/usr/bin/env python3
"""Seed portal-visible problems + demo contests (idempotent by contest key / problem code)."""

from __future__ import annotations

import asyncio
from datetime import UTC, datetime, timedelta

from sqlalchemy import delete, select

from app.modules.biz.contest.contest.model import OjContest
from app.modules.biz.contest.problem.model import OjContestProblem
from app.modules.biz.problem.enums import ProblemStatus
from app.modules.biz.problem.language.model import OjProblemLanguage
from app.modules.biz.problem.problem.model import OjProblem
from app.platform.db.session import get_session_factory, init_engine
from app.platform.id_generator.snowflake import generate_snowflake_id

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
        if entity.published_at is None:
            entity.published_at = now
        await ensure_languages(db, entity.id)
        id_by_code[code] = entity.id
        print("problem", code, "->", name, entity.id)
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


async def main() -> None:
    init_engine()
    async with get_session_factory()() as db:
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

        await db.commit()
        print("done. portal problems:", len(id_by_code))


if __name__ == "__main__":
    asyncio.run(main())
