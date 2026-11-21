#!/usr/bin/env python3
"""补齐 shared-dev 业务闭环：用户 ↔ 题库/题集/竞赛 ↔ 提交/判题用例/solved/样本/进度。

直接写库，并可选落 SQL 备份。
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime, timedelta
from typing import Any

import pymysql


CODES = {
    "ACOJ-1001": "a,b=map(int,input().split())\nprint(a+b)\n",
    "ACOJ-1002": "import math\na,b=map(int,input().split())\nprint(math.gcd(a,b))\n",
    "ACOJ-1003": "print(input()[::-1])\n",
    "ACOJ-1004": (
        "n=int(input())\n"
        "if n<2:\n print('No')\n"
        "else:\n"
        " ok=True\n"
        " for i in range(2,int(n**0.5)+1):\n"
        "  if n%i==0:\n"
        "   ok=False;break\n"
        " print('Yes' if ok else 'No')\n"
    ),
    "ACOJ-1005": "n=int(input());print(sum(map(int,input().split())))\n",
    "ACOJ-1006": (
        "n,t=map(int,input().split())\n"
        "a=list(map(int,input().split()))\n"
        "l,r=0,n-1\n"
        "ans=-1\n"
        "while l<=r:\n"
        " m=(l+r)//2\n"
        " if a[m]==t: ans=m;break\n"
        " if a[m]<t: l=m+1\n"
        " else: r=m-1\n"
        "print(ans)\n"
    ),
    "ACOJ-1007": (
        "n=int(input())\n"
        "if n<=2:\n"
        " print(1)\n"
        "else:\n"
        " a=b=1\n"
        " for _ in range(3,n+1):\n"
        "  a,b=b,a+b\n"
        " print(b)\n"
    ),
    "ACOJ-1008": (
        "n=int(input())\n"
        "a=list(map(int,input().split()))\n"
        "best=cur=a[0]\n"
        "for x in a[1:]:\n"
        " cur=max(x,cur+x)\n"
        " best=max(best,cur)\n"
        "print(best)\n"
    ),
}

# 轻微变体，便于查重样例仍可成对
CODE_VARIANTS = {
    "ACOJ-1001": [
        "a,b=map(int,input().split())\nprint(a+b)\n",
        "# A+B\nx,y = map(int, input().split())\nprint(x + y)\n",
        "a, b = map(int, input().split())\nprint(a+b)\n",
    ],
}


class Snowflake:
    def __init__(self, start_seq: int = 5000):
        self.seq = start_seq
        self.base_ts = 1704067200000
        self.worker_id = 11
        self.datacenter_id = 5

    def next_id(self) -> str:
        ts = self.base_ts + self.seq
        value = (
            ((ts - 1288834974657) << 22)
            | (self.datacenter_id << 17)
            | (self.worker_id << 12)
            | (self.seq & 0xFFF)
        )
        self.seq += 1
        return str(value)


def connect():
    return pymysql.connect(
        host="47.110.3.35",
        port=3306,
        user="root",
        password="infra123!",
        database="astro_code",
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=False,
    )


def fetch_all(cur, sql: str, args=None):
    cur.execute(sql, args)
    return cur.fetchall()


def ensure_participants(cur, snow: Snowflake, contests: list[dict], users: list[dict], now: str):
    existing = {
        (r["contest_id"], r["user_id"])
        for r in fetch_all(cur, "SELECT contest_id, user_id FROM data_contest_participant")
    }
    inserted = 0
    for contest in contests:
        for user in users:
            key = (contest["id"], user["id"])
            if key in existing:
                continue
            cur.execute(
                """
                INSERT INTO data_contest_participant
                (id, contest_id, user_id, team_id, team_name, is_team_leader, register_time,
                 status, deleted, create_time, create_user, update_time, update_user)
                VALUES (%s,%s,%s,NULL,NULL,0,%s,'REGISTERED',0,%s,%s,%s,%s)
                """,
                (
                    snow.next_id(),
                    contest["id"],
                    user["id"],
                    now,
                    now,
                    user["id"],
                    now,
                    user["id"],
                ),
            )
            inserted += 1
            existing.add(key)
    return inserted


def upsert_library(cur, snow: Snowflake, row: dict[str, Any]):
    cur.execute(
        """
        SELECT id FROM data_library
        WHERE user_id=%s AND module_type=%s AND module_id=%s AND problem_id=%s AND language=%s
        LIMIT 1
        """,
        (row["user_id"], row["module_type"], row["module_id"], row["problem_id"], row["language"]),
    )
    found = cur.fetchone()
    if found:
        cur.execute(
            """
            UPDATE data_library
            SET submit_id=%s, submit_time=%s, code=%s, code_length=%s, code_token=NULL,
                code_token_name=NULL, code_token_texts=NULL, access_count=0, deleted=0,
                update_time=%s, update_user=%s
            WHERE id=%s
            """,
            (
                row["submit_id"],
                row["submit_time"],
                row["code"],
                row["code_length"],
                row["now"],
                row["user_id"],
                found["id"],
            ),
        )
        return found["id"], False
    lib_id = snow.next_id()
    cur.execute(
        """
        INSERT INTO data_library
        (id, user_id, module_type, module_id, problem_id, submit_id, submit_time, language,
         code, code_token, code_token_name, code_token_texts, code_length, access_count,
         deleted, create_time, create_user, update_time, update_user)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,NULL,NULL,NULL,%s,0,0,%s,%s,%s,%s)
        """,
        (
            lib_id,
            row["user_id"],
            row["module_type"],
            row["module_id"],
            row["problem_id"],
            row["submit_id"],
            row["submit_time"],
            row["language"],
            row["code"],
            row["code_length"],
            row["now"],
            row["user_id"],
            row["now"],
            row["user_id"],
        ),
    )
    return lib_id, True


def upsert_solved(cur, snow: Snowflake, row: dict[str, Any]):
    cur.execute(
        """
        SELECT id FROM data_solved
        WHERE user_id=%s AND module_type=%s AND module_id=%s AND problem_id=%s
        LIMIT 1
        """,
        (row["user_id"], row["module_type"], row["module_id"], row["problem_id"]),
    )
    found = cur.fetchone()
    if found:
        cur.execute(
            """
            UPDATE data_solved
            SET submit_id=%s, solved=1, first_solved_time=COALESCE(first_solved_time,%s),
                first_submit_time=COALESCE(first_submit_time,%s), deleted=0,
                update_time=%s, update_user=%s
            WHERE id=%s
            """,
            (row["submit_id"], row["now"], row["now"], row["now"], row["user_id"], found["id"]),
        )
        return False
    cur.execute(
        """
        INSERT INTO data_solved
        (id, module_type, module_id, user_id, problem_id, submit_id, solved, deleted,
         first_solved_time, first_submit_time, create_time, create_user, update_time, update_user)
        VALUES (%s,%s,%s,%s,%s,%s,1,0,%s,%s,%s,%s,%s,%s)
        """,
        (
            snow.next_id(),
            row["module_type"],
            row["module_id"],
            row["user_id"],
            row["problem_id"],
            row["submit_id"],
            row["now"],
            row["now"],
            row["now"],
            row["user_id"],
            row["now"],
            row["user_id"],
        ),
    )
    return True


def upsert_progress(cur, snow: Snowflake, row: dict[str, Any]):
    if row["module_type"] != "SET":
        return False
    cur.execute(
        """
        SELECT id FROM data_progress
        WHERE user_id=%s AND module_type=%s AND module_id=%s AND problem_id=%s
        LIMIT 1
        """,
        (row["user_id"], row["module_type"], row["module_id"], row["problem_id"]),
    )
    found = cur.fetchone()
    if found:
        cur.execute(
            """
            UPDATE data_progress
            SET status='ACCEPTED', completed=1, completed_at=%s, deleted=0,
                update_time=%s, update_user=%s
            WHERE id=%s
            """,
            (row["now"], row["now"], row["user_id"], found["id"]),
        )
        return False
    cur.execute(
        """
        INSERT INTO data_progress
        (id, user_id, module_type, module_id, problem_id, status, extra_json, completed,
         completed_at, deleted, create_time, create_user, update_time, update_user)
        VALUES (%s,%s,%s,%s,%s,'ACCEPTED',NULL,1,%s,0,%s,%s,%s,%s)
        """,
        (
            snow.next_id(),
            row["user_id"],
            row["module_type"],
            row["module_id"],
            row["problem_id"],
            row["now"],
            row["now"],
            row["user_id"],
            row["now"],
            row["user_id"],
        ),
    )
    return True


def insert_submit_bundle(
    cur,
    snow: Snowflake,
    *,
    user_id: str,
    module_type: str,
    module_id: str,
    problem: dict,
    code: str,
    language: str,
    now: str,
    create_judge_cases: bool,
):
    submit_id = snow.next_id()
    code_len = len(code)
    cur.execute(
        """
        INSERT INTO data_submit
        (id, user_id, module_type, module_id, problem_id, language, code, code_length,
         submit_type, max_time, max_memory, message, test_case, status, is_finish,
         similarity, task_id, report_id, similarity_category, deleted,
         create_time, create_user, update_time, update_user, judge_task_id)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,1,1.00,1024.00,'',NULL,'ACCEPTED',1,
                0.00,NULL,NULL,NULL,0,%s,%s,%s,%s,%s)
        """,
        (
            submit_id,
            user_id,
            module_type,
            module_id,
            problem["id"],
            language,
            code,
            code_len,
            now,
            user_id,
            now,
            user_id,
            f"task-{submit_id}",
        ),
    )

    if create_judge_cases:
        cases = fetch_all(
            cur,
            "SELECT case_sign, input_data, expected_output, is_sample, score FROM data_test_case WHERE problem_id=%s ORDER BY is_sample DESC, case_sign",
            (problem["id"],),
        )
        for case in cases:
            cur.execute(
                """
                INSERT INTO data_judge_case
                (id, submit_id, case_sign, input_data, output_data, expected_output,
                 input_file_path, input_file_size, output_file_path, output_file_size,
                 max_time, max_memory, is_sample, score, status, message, exit_code,
                 deleted, create_time, create_user, update_time, update_user)
                VALUES (%s,%s,%s,%s,%s,%s,'',0,'',0,1.00,256.00,%s,%s,'ACCEPTED','',0,0,%s,'0',%s,'0')
                """,
                (
                    snow.next_id(),
                    submit_id,
                    case["case_sign"],
                    case["input_data"],
                    case["expected_output"],
                    case["expected_output"],
                    case["is_sample"],
                    case["score"] if case.get("score") is not None else 0,
                    now,
                    now,
                ),
            )

    ctx = {
        "user_id": user_id,
        "module_type": module_type,
        "module_id": module_id,
        "problem_id": problem["id"],
        "submit_id": submit_id,
        "submit_time": now,
        "language": language,
        "code": code,
        "code_length": code_len,
        "now": now,
    }
    upsert_library(cur, snow, ctx)
    upsert_solved(cur, snow, ctx)
    upsert_progress(cur, snow, ctx)
    return submit_id


def backfill_missing_libraries(cur, snow: Snowflake, now: str):
    rows = fetch_all(
        cur,
        """
        SELECT s.id, s.user_id, s.module_type, s.module_id, s.problem_id, s.language, s.code, s.code_length, s.create_time
        FROM data_submit s
        LEFT JOIN data_library l ON l.submit_id = s.id
        WHERE s.status='ACCEPTED' AND l.id IS NULL
        """,
    )
    n = 0
    for s in rows:
        upsert_library(
            cur,
            snow,
            {
                "user_id": s["user_id"],
                "module_type": s["module_type"],
                "module_id": s["module_id"],
                "problem_id": s["problem_id"],
                "submit_id": s["id"],
                "submit_time": s["create_time"].strftime("%Y-%m-%d %H:%M:%S")
                if hasattr(s["create_time"], "strftime")
                else str(s["create_time"]),
                "language": s["language"],
                "code": s["code"],
                "code_length": s["code_length"] or len(s["code"] or ""),
                "now": now,
            },
        )
        upsert_solved(
            cur,
            snow,
            {
                "user_id": s["user_id"],
                "module_type": s["module_type"],
                "module_id": s["module_id"],
                "problem_id": s["problem_id"],
                "submit_id": s["id"],
                "now": now,
            },
        )
        n += 1
    return n


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="write to shared-dev")
    args = parser.parse_args()
    if not args.apply:
        raise SystemExit("pass --apply to write shared-dev")

    snow = Snowflake()
    now_dt = datetime.now()
    now = now_dt.strftime("%Y-%m-%d %H:%M:%S")
    conn = connect()
    stats = {
        "participants": 0,
        "submits": 0,
        "backfill_libs": 0,
    }
    try:
        with conn.cursor() as cur:
            users = fetch_all(
                cur,
                "SELECT id, username FROM sys_user WHERE id NOT IN ('0','1','2') AND deleted=0 ORDER BY username",
            )
            contests = fetch_all(cur, "SELECT id, title FROM data_contest WHERE deleted=0 ORDER BY title")
            problems = {
                r["display_id"]: r
                for r in fetch_all(
                    cur,
                    "SELECT id, display_id, title FROM data_problem WHERE deleted=0",
                )
            }
            sets = fetch_all(cur, "SELECT id, title FROM data_set WHERE deleted=0 ORDER BY title")
            set_problems = fetch_all(
                cur,
                """
                SELECT sp.set_id, sp.problem_id, p.display_id, s.title set_title
                FROM data_set_problem sp
                JOIN data_problem p ON p.id=sp.problem_id
                JOIN data_set s ON s.id=sp.set_id
                ORDER BY s.title, sp.sort
                """,
            )
            contest_problems = fetch_all(
                cur,
                """
                SELECT cp.contest_id, cp.problem_id, cp.problem_code, p.display_id, c.title contest_title
                FROM data_contest_problem cp
                JOIN data_problem p ON p.id=cp.problem_id
                JOIN data_contest c ON c.id=cp.contest_id
                ORDER BY c.title, cp.sort
                """,
            )

            stats["participants"] = ensure_participants(cur, snow, contests, users, now)
            stats["backfill_libs"] = backfill_missing_libraries(cur, snow, now)

            # 每用户：题库 AC（覆盖不同题目）、题集 AC、竞赛 AC
            # 题目轮转，保证 8 题都有业务活动
            problem_ids = [problems[k] for k in sorted(problems.keys())]
            set_by_title = {s["title"]: s for s in sets}
            contest_by_title = {c["title"]: c for c in contests}

            # 预设分配，形成闭环覆盖
            plan = [
                # username, problem display, set title + problem, contest title + problem
                ("tester", ["ACOJ-1001", "ACOJ-1002"], ("Intro Training Camp", "ACOJ-1003"), ("ACOJ Open Contest · Running", "ACOJ-1005")),
                ("ew123", ["ACOJ-1003", "ACOJ-1004"], ("Intro Training Camp", "ACOJ-1004"), ("ACOJ Open Contest · Running", "ACOJ-1006")),
                ("user142530", ["ACOJ-1005", "ACOJ-1006"], ("Algorithm Intermediate", "ACOJ-1007"), ("Autumn Challenge · Finished", "ACOJ-1008")),
                ("user153857", ["ACOJ-1007", "ACOJ-1008"], ("Timed Weekly Drill · Basics", "ACOJ-1001"), ("Spring Warm-up · Registering", "ACOJ-1002")),
            ]

            user_map = {u["username"]: u for u in users}
            for idx, (uname, pids, set_pair, contest_pair) in enumerate(plan):
                user = user_map.get(uname)
                if not user:
                    continue
                t = (now_dt + timedelta(minutes=idx)).strftime("%Y-%m-%d %H:%M:%S")
                for j, display_id in enumerate(pids):
                    problem = problems[display_id]
                    variants = CODE_VARIANTS.get(display_id) or [CODES[display_id]]
                    code = variants[j % len(variants)]
                    insert_submit_bundle(
                        cur,
                        snow,
                        user_id=user["id"],
                        module_type="PROBLEM",
                        module_id=problem["id"],
                        problem=problem,
                        code=code,
                        language="python",
                        now=t,
                        create_judge_cases=True,
                    )
                    stats["submits"] += 1

                set_title, set_display = set_pair
                set_row = set_by_title[set_title]
                set_problem = problems[set_display]
                insert_submit_bundle(
                    cur,
                    snow,
                    user_id=user["id"],
                    module_type="SET",
                    module_id=set_row["id"],
                    problem=set_problem,
                    code=CODES[set_display],
                    language="python",
                    now=t,
                    create_judge_cases=True,
                )
                stats["submits"] += 1

                contest_title, contest_display = contest_pair
                contest_row = contest_by_title[contest_title]
                contest_problem = problems[contest_display]
                insert_submit_bundle(
                    cur,
                    snow,
                    user_id=user["id"],
                    module_type="CONTEST",
                    module_id=contest_row["id"],
                    problem=contest_problem,
                    code=CODES[contest_display],
                    language="python",
                    now=t,
                    create_judge_cases=True,
                )
                stats["submits"] += 1

            # SuperAdmin 也补一条 SET/CONTEST，避免只有 PROBLEM
            admin_id = "1"
            intro = set_by_title["Intro Training Camp"]
            running = contest_by_title["ACOJ Open Contest · Running"]
            insert_submit_bundle(
                cur,
                snow,
                user_id=admin_id,
                module_type="SET",
                module_id=intro["id"],
                problem=problems["ACOJ-1001"],
                code=CODES["ACOJ-1001"],
                language="python",
                now=now,
                create_judge_cases=False,
            )
            stats["submits"] += 1
            insert_submit_bundle(
                cur,
                snow,
                user_id=admin_id,
                module_type="CONTEST",
                module_id=running["id"],
                problem=problems["ACOJ-1003"],
                code=CODES["ACOJ-1003"],
                language="python",
                now=now,
                create_judge_cases=False,
            )
            stats["submits"] += 1

        conn.commit()
        print(json.dumps({"ok": True, **stats}, ensure_ascii=False))
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
