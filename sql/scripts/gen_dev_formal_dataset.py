#!/usr/bin/env python3
"""从线上 shared-dev 导出 JSON 生成正式业务数据 patch。

用法:
  python3 sql/scripts/gen_dev_formal_dataset.py \
    --input ~/Downloads/acoj-dev-export-20261121 \
    --output sql/patches/20261121_dev_formal_dataset.sql
"""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any


TABLE_ORDER = [
    "sys_category",
    "sys_tag",
    "data_problem",
    "data_test_case",
    "data_problem_tag",
    "data_set",
    "data_set_problem",
    "data_progress",
    "data_contest",
    "data_contest_problem",
    "data_contest_participant",
    "data_contest_auth",
    "sys_user",
    "sys_user_role",
    "data_submit",
    "data_judge_case",
    "data_solved",
    "data_library",
    "task_reports",
    "task_similarity",
]

# 字段名中需要按 ID 映射替换的引用列
ID_COLUMNS = {
    "id",
    "user_id",
    "problem_id",
    "category_id",
    "category",
    "set_id",
    "contest_id",
    "module_id",
    "submit_id",
    "submit_user",
    "origin_user",
    "origin_id",
    "tag_id",
    "create_user",
    "update_user",
}

PRESERVE_USER_IDS = {"0", "1", "2"}
PRESERVE_CATEGORY_IDS = {"0"}

SEED_ID_RE = re.compile(
    r"^(?:"
    r"junit_.*"
    r"|9(?:00|10|20)\d+"
    r"|cat9\d+"
    r"|cp\d+[A-Z]"
    r"|cpp\d+"
    r"|9000001r?"
    r"|tc9\d+"
    r"|pt9\d+"
    r"|sp9\d+"
    r"|setp9\d+"
    r")$"
)

DISPLAY_ID_RE = re.compile(r"^P(\d+)$", re.IGNORECASE)


class Snowflake:
    """稳定可复跑的雪花 ID：固定纪元 + 顺序序号。"""

    def __init__(self, worker_id: int = 7, datacenter_id: int = 3, start_seq: int = 1):
        self.worker_id = worker_id & 0x1F
        self.datacenter_id = datacenter_id & 0x1F
        self.seq = start_seq
        # 2024-01-01 UTC 附近固定起点，保证输出 19 位左右
        self.base_ts = 1704067200000

    def next_id(self) -> str:
        ts = self.base_ts + self.seq
        value = ((ts - 1288834974657) << 22) | (self.datacenter_id << 17) | (self.worker_id << 12) | (self.seq & 0xFFF)
        self.seq += 1
        return str(value)


def looks_seed_id(value: str) -> bool:
    if not value or value in PRESERVE_USER_IDS or value in PRESERVE_CATEGORY_IDS:
        return False
    if SEED_ID_RE.match(value):
        return True
    # 短可读 ID / 非数字雪花
    if value.isdigit() and len(value) <= 10:
        return True
    if not value.isdigit() and len(value) < 16:
        return True
    return False


def load_export(input_dir: Path) -> dict[str, list[dict[str, Any]]]:
    data: dict[str, list[dict[str, Any]]] = {}
    for table in TABLE_ORDER:
        path = input_dir / f"{table}.json"
        if not path.exists():
            data[table] = []
            continue
        payload = json.loads(path.read_text(encoding="utf-8"))
        data[table] = payload.get("rows") or []
    return data


def collect_ids(tables: dict[str, list[dict[str, Any]]]) -> set[str]:
    ids: set[str] = set()
    for rows in tables.values():
        for row in rows:
            for key, value in row.items():
                if key in ID_COLUMNS and isinstance(value, str) and value:
                    ids.add(value)
    return ids


def build_id_map(all_ids: set[str], snow: Snowflake) -> dict[str, str]:
    mapping: dict[str, str] = {}
    for old in sorted(all_ids):
        if old in PRESERVE_USER_IDS or old in PRESERVE_CATEGORY_IDS:
            mapping[old] = old
            continue
        if looks_seed_id(old):
            mapping[old] = snow.next_id()
        else:
            mapping[old] = old
    return mapping


def map_value(value: Any, id_map: dict[str, str], column: str) -> Any:
    if value is None:
        return None
    if column in ID_COLUMNS and isinstance(value, str) and value in id_map:
        return id_map[value]
    return value


def clean_text(value: Any, column: str) -> Any:
    if not isinstance(value, str):
        return value
    text = value
    if column == "source":
        text = text.replace("ACOJ Seed", "ACOJ")
    if column in {"description", "title"}:
        text = re.sub(r"\s*\(demo data[^)]*\)", "", text, flags=re.IGNORECASE)
        text = re.sub(r"\bdemo\b", "", text, flags=re.IGNORECASE)
        text = re.sub(r"\bSeed\b", "", text)
        text = re.sub(r"\s{2,}", " ", text).strip(" .")
        if column == "description" and text and not text.endswith("."):
            # keep original punctuation if stripped awkwardly
            pass
    if column == "display_id":
        m = DISPLAY_ID_RE.match(text)
        if m:
            text = f"ACOJ-{m.group(1)}"
    return text


def transform_user(row: dict[str, Any], id_map: dict[str, str]) -> dict[str, Any] | None:
    user_id = row.get("id")
    if user_id in PRESERVE_USER_IDS:
        return None  # 不覆盖系统账号
    out = dict(row)
    for key in list(out.keys()):
        out[key] = map_value(out[key], id_map, key)
    # 1. 先按原始用户名改正式账号，再 scrub 其它文案
    raw_username = str(row.get("username") or "")
    suffix = str(out["id"])[-6:]
    if raw_username == "tester2":
        out["username"] = "tester"
        out["nickname"] = "Tester"
        email = out.get("email")
        if isinstance(email, str) and "tester2" in email:
            out["email"] = "tester@acoj.dev"
    elif raw_username.startswith("junit_") or raw_username in {"authchk01", "user"}:
        out["username"] = f"user{suffix}"
        out["nickname"] = f"User{suffix}"
        email = out.get("email")
        if isinstance(email, str) and ("junit" in email or "test.local" in email):
            out["email"] = f"user{suffix}@acoj.dev"
    # 2. 兜底：中文昵称 / User_数字 → 英文 User{数字或后缀}
    nickname = str(out.get("nickname") or "")
    cn_nick = re.match(r"^用户[_-]?(\d+)$", nickname)
    us_nick = re.match(r"^User_(\d+)$", nickname)
    if cn_nick:
        out["nickname"] = f"User{cn_nick.group(1)}"
        if out.get("username") in {"user", "", None}:
            out["username"] = f"user{cn_nick.group(1)}"
    elif us_nick:
        out["nickname"] = f"User{us_nick.group(1)}"
    elif re.search(r"[\u4e00-\u9fff]", nickname):
        out["nickname"] = f"User{suffix}"
    for key in list(out.keys()):
        if key in {"username", "nickname", "email"}:
            continue
        out[key] = clean_text(out[key], key)
    return out


def transform_row(table: str, row: dict[str, Any], id_map: dict[str, str]) -> dict[str, Any] | None:
    if table == "sys_user":
        return transform_user(row, id_map)
    if table == "sys_category" and row.get("id") in PRESERVE_CATEGORY_IDS:
        return None
    if table == "sys_user_role":
        user_id = row.get("user_id")
        # 丢弃指向不存在用户或系统账号的脏角色行（系统角色保留原库）
        if user_id in PRESERVE_USER_IDS:
            return None
        mapped_user = id_map.get(user_id, user_id)
        # 稍后用有效用户集合过滤
        out = dict(row)
        for key in list(out.keys()):
            out[key] = map_value(out[key], id_map, key)
            out[key] = clean_text(out[key], key)
        out["_mapped_user_id"] = mapped_user
        return out

    out = dict(row)
    for key in list(out.keys()):
        out[key] = map_value(out[key], id_map, key)
        out[key] = clean_text(out[key], key)
    return out


def sql_literal(value: Any) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)) and not isinstance(value, bool):
        return str(value)
    text = str(value)
    text = text.replace("\\", "\\\\").replace("'", "''")
    return f"'{text}'"


def emit_insert(table: str, rows: list[dict[str, Any]]) -> list[str]:
    if not rows:
        return [f"-- {table}: 0 rows"]
    # drop helper keys
    cleaned = []
    for row in rows:
        cleaned.append({k: v for k, v in row.items() if not k.startswith("_")})
    columns = list(cleaned[0].keys())
    lines = [f"-- {table}: {len(cleaned)} rows"]
    for row in cleaned:
        values = ", ".join(sql_literal(row.get(col)) for col in columns)
        col_sql = ", ".join(f"`{c}`" for c in columns)
        lines.append(f"INSERT INTO `{table}` ({col_sql}) VALUES ({values});")
    return lines


def build_delete_section() -> list[str]:
    return [
        "-- Dev formal dataset patch",
        "-- Generated from live shared-dev export. Remaps seed IDs and cleans demo wording.",
        "SET NAMES utf8mb4;",
        "SET FOREIGN_KEY_CHECKS = 0;",
        "",
        "-- 1. Clear business tables. Never delete system users 0/1/2.",
        "DELETE FROM `task_similarity`;",
        "DELETE FROM `task_reports`;",
        "DELETE FROM `data_library`;",
        "DELETE FROM `data_judge_case`;",
        "DELETE FROM `data_solved`;",
        "DELETE FROM `data_submit`;",
        "DELETE FROM `data_contest_auth`;",
        "DELETE FROM `data_contest_participant`;",
        "DELETE FROM `data_contest_problem`;",
        "DELETE FROM `data_contest`;",
        "DELETE FROM `data_progress`;",
        "DELETE FROM `data_set_problem`;",
        "DELETE FROM `data_set`;",
        "DELETE FROM `data_problem_tag`;",
        "DELETE FROM `data_test_case`;",
        "DELETE FROM `data_problem`;",
        "DELETE FROM `sys_user_role` WHERE `user_id` NOT IN ('0', '1', '2');",
        "DELETE FROM `sys_user` WHERE `id` NOT IN ('0', '1', '2');",
        "DELETE FROM `sys_tag`;",
        "DELETE FROM `sys_category` WHERE `id` <> '0';",
        "",
        "-- 2. 写入正规化数据",
    ]


def generate(input_dir: Path, output_path: Path, mapping_path: Path) -> None:
    tables = load_export(input_dir)
    snow = Snowflake()
    id_map = build_id_map(collect_ids(tables), snow)

    transformed: dict[str, list[dict[str, Any]]] = {}
    valid_users: set[str] = set(PRESERVE_USER_IDS)

    # users first to know valid set
    user_rows: list[dict[str, Any]] = []
    for row in tables.get("sys_user", []):
        out = transform_row("sys_user", row, id_map)
        if out is None:
            continue
        user_rows.append(out)
        valid_users.add(str(out["id"]))
    transformed["sys_user"] = user_rows

    for table in TABLE_ORDER:
        if table == "sys_user":
            continue
        rows_out: list[dict[str, Any]] = []
        for row in tables.get(table, []):
            out = transform_row(table, row, id_map)
            if out is None:
                continue
            if table == "sys_user_role":
                mapped_user = str(out.get("_mapped_user_id") or out.get("user_id"))
                if mapped_user not in valid_users or mapped_user in PRESERVE_USER_IDS:
                    continue
            rows_out.append(out)
        transformed[table] = rows_out

    lines = build_delete_section()
    for table in TABLE_ORDER:
        lines.extend(emit_insert(table, transformed.get(table, [])))
        lines.append("")
    lines.append("SET FOREIGN_KEY_CHECKS = 1;")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    mapping_path.write_text(json.dumps(id_map, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(f"wrote {output_path} ({output_path.stat().st_size} bytes)")
    print(f"wrote {mapping_path} ({len(id_map)} id mappings)")
    for table in TABLE_ORDER:
        print(f"  {table}: {len(transformed.get(table, []))} rows")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument(
        "--mapping",
        type=Path,
        default=None,
        help="optional id mapping json path",
    )
    args = parser.parse_args()
    mapping = args.mapping or args.output.with_suffix(".idmap.json")
    generate(args.input, args.output, mapping)


if __name__ == "__main__":
    main()
