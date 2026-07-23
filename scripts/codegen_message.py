"""
Generate scaffolding code for message module tables using the built-in codegen system.

Usage: python scripts/codegen_message.py
"""

import asyncio
import json
import os
from pathlib import Path

from sqlalchemy import text

from app.core.config.settings import settings
from app.modules.sys.codegen.model import SysCodegenField, SysCodegenPlan
from app.modules.sys.codegen.templates import render_files
from app.platform.db.session import create_async_engine
from app.platform.id_generator.snowflake import generate_snowflake_id

BASE_DIR = Path("app/modules/message")

# Define codegen plans for each table
PLANS = [
    {
        "table": "msg_terminal",
        "entity": "MsgTerminal",
        "business": "终端管理",
        "module_path": "message/terminal",
    },
    {
        "table": "msg_notification",
        "entity": "MsgNotification",
        "business": "通知管理",
        "module_path": "message/notification",
    },
    {
        "table": "msg_announcement",
        "entity": "MsgAnnouncement",
        "business": "公告管理",
        "module_path": "message/announcement",
    },
    {
        "table": "msg_group",
        "entity": "MsgGroup",
        "business": "群组管理",
        "module_path": "message/group",
    },
    {
        "table": "msg_friend",
        "entity": "MsgFriend",
        "business": "好友管理",
        "module_path": "message/friend",
    },
    {
        "table": "msg_conversation",
        "entity": "MsgConversation",
        "business": "会话管理",
        "module_path": "message/conversation",
    },
]

# Map DB column type to python/ts types
TYPE_MAP = {
    "character varying": ("str", "string"),
    "character": ("str", "string"),
    "text": ("str", "string"),
    "integer": ("int", "number"),
    "bigint": ("int", "number"),
    "smallint": ("int", "number"),
    "boolean": ("bool", "boolean"),
    "double precision": ("float", "number"),
    "numeric": ("float", "number"),
    "json": ("dict[str, Any]", "Record<string, any>"),
    "jsonb": ("dict[str, Any]", "Record<string, any>"),
    "timestamp with time zone": ("datetime", "string"),
    "timestamp without time zone": ("datetime", "string"),
    "date": ("datetime", "string"),
    "uuid": ("str", "string"),
}


async def reflect_columns(engine, table_name: str) -> list[dict]:
    async with engine.connect() as conn:
        result = await conn.execute(
            text("""
                SELECT
                    c.column_name,
                    COALESCE(pgd.description, '') as column_comment,
                    c.data_type,
                    c.is_nullable,
                    c.character_maximum_length,
                    c.ordinal_position,
                    CASE WHEN pk.column_name IS NOT NULL THEN true ELSE false END as is_primary_key
                FROM information_schema.columns c
                LEFT JOIN pg_catalog.pg_description pgd
                    ON pgd.objsubid = c.ordinal_position
                    AND pgd.objoid = (SELECT oid FROM pg_class WHERE relname = :table_name)
                LEFT JOIN (
                    SELECT ku.column_name
                    FROM information_schema.table_constraints tc
                    JOIN information_schema.key_column_usage ku
                        ON tc.constraint_name = ku.constraint_name
                    WHERE tc.constraint_type = 'PRIMARY KEY'
                        AND tc.table_name = :table_name2
                ) pk ON c.column_name = pk.column_name
                WHERE c.table_name = :table_name3
                ORDER BY c.ordinal_position
            """),
            {"table_name": table_name, "table_name2": table_name, "table_name3": table_name},
        )
        columns = []
        for row in result:
            data_type = (row[2] or "").lower()
            py_type, ts_type = TYPE_MAP.get(data_type, ("str", "string"))
            is_pk = bool(row[6])
            is_nullable = row[3] == "YES"
            columns.append({
                "column_name": row[0],
                "column_comment": row[1] or "",
                "db_type": data_type,
                "python_type": py_type,
                "typescript_type": ts_type,
                "is_primary_key": is_pk,
                "is_nullable": is_nullable,
                "max_length": row[4],
                "sort": row[5],
            })
        return columns


def _default_widget(column_name: str, python_type: str) -> str:
    if column_name == "status":
        return "dict"
    if python_type in {"int", "float"}:
        return "number"
    if python_type == "bool":
        return "switch"
    if any(kw in column_name for kw in ("content", "description", "remark")):
        return "textarea"
    return "input"


def _default_query_op(column_name: str, python_type: str) -> str | None:
    if column_name == "status" or python_type in {"int", "bool"}:
        return "EQ"
    if column_name in {"name", "title", "code", "category", "type"}:
        return "LIKE"
    return None


AUDIT_COLUMNS = {"created_at", "created_by", "updated_at", "updated_by"}


def build_field(column: dict) -> SysCodegenField:
    name = column["column_name"]
    is_pk = column["is_primary_key"]
    is_audit = name in AUDIT_COLUMNS
    is_nullable = column["is_nullable"]
    python_type = column["python_type"]
    return SysCodegenField(
        table_role="MAIN",
        column_name=name,
        column_comment=column.get("column_comment"),
        db_type=column["db_type"],
        python_type=python_type,
        typescript_type=column["typescript_type"],
        form_widget=_default_widget(name, python_type),
        dict_code="COMMON_STATUS" if name == "status" else None,
        query_operator=_default_query_op(name, python_type),
        show_in_table=not is_audit,
        show_in_form=not is_pk and not is_audit,
        show_in_detail=True,
        show_in_query=name in {"name", "title", "code", "status", "category", "type", "keyword"},
        is_primary_key=is_pk,
        is_required=not is_nullable and not is_pk and not is_audit,
        is_unique=False,
        is_nullable=is_nullable,
        max_length=column.get("max_length"),
        sort=column.get("sort", 99),
    )


async def gen_table(engine, cfg: dict):
    table = cfg["table"]
    print(f"  Generating {cfg['entity']} from table '{table}'...")

    columns = await reflect_columns(engine, table)
    fields = [build_field(c) for c in columns]

    table_name = cfg["table"]
    entity_name = cfg["entity"]
    module_path = cfg["module_path"]
    business_name = cfg["business"]

    plan = SysCodegenPlan(
        id=generate_snowflake_id(),
        name=f"{business_name} (codegen)",
        gen_type="TABLE",
        status="ENABLED",
        author="codegen_script",
        main_table=table_name,
        main_pk="id",
        main_entity_name=entity_name,
        main_module_path=module_path,
        main_business_name=business_name,
        api_prefix=f"/message/{table_name.replace('msg_', '').replace('_', '-')}s",
        permission_prefix=f"message:{table_name.replace('msg_', '').replace('_', '-')}",
        menu_name=business_name,
        menu_path=f"/message/{table_name.replace('msg_', '')}",
        component_path=f"message/{table_name.replace('msg_', '')}/index.vue",
    )

    files = render_files(plan, fields, [])

    # Only write backend files (model, schema, repository, service, router, module)
    backend_files = [f for f in files if f.path.endswith(".py")]
    for f in backend_files:
        filepath = Path(f.path)
        filepath.parent.mkdir(parents=True, exist_ok=True)
        filepath.write_text(f.content, encoding="utf-8")
        print(f"    Wrote {filepath}")

    print(f"  Done ({len(backend_files)} backend files)")


async def main():
    engine = create_async_engine(settings.db.url)
    print(f"Connected to DB, generating {len(PLANS)} modules...\n")

    for cfg in PLANS:
        await gen_table(engine, cfg)

    await engine.dispose()
    print("\nAll done! Generated scaffolding for:")
    for cfg in PLANS:
        print(f"  - app/modules/message/{cfg['module_path']}/")


if __name__ == "__main__":
    asyncio.run(main())
