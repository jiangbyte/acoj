"""Offline OJ codegen: render HEI codegen templates into biz/problem/* and biz/contest/* submodules.

Usage (from acoj/):
  python scripts/codegen/gen_oj_modules.py
"""

from __future__ import annotations

import re
import sys
from pathlib import Path
from types import SimpleNamespace

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT))

from app.modules.sys.codegen.templates import render_files  # noqa: E402
from app.platform.id_generator.snowflake import generate_snowflake_id  # noqa: E402

AUTHOR = "Charlie"


def field(
    name: str,
    *,
    comment: str,
    db_type: str = "varchar",
    python_type: str = "str",
    ts_type: str = "string",
    max_length: int | None = None,
    nullable: bool = True,
    pk: bool = False,
    required: bool | None = None,
    widget: str | None = None,
    query: str | None = None,
    show_query: bool | None = None,
    show_table: bool = True,
    show_form: bool | None = None,
    sort: int = 99,
) -> SimpleNamespace:
    is_audit = name in {"created_at", "created_by", "updated_at", "updated_by"}
    if required is None:
        required = (not nullable) and (not pk) and (not is_audit)
    if widget is None:
        if python_type in {"int", "float"}:
            widget = "number"
        elif python_type == "bool":
            widget = "switch"
        elif python_type == "datetime":
            widget = "datetime"
        elif any(k in name for k in ("content", "description", "summary", "feedback", "reason", "script")):
            widget = "textarea"
        elif python_type in {"dict", "list"} or "json" in db_type.lower():
            widget = "textarea"
        else:
            widget = "input"
    if show_form is None:
        show_form = (not pk) and (not is_audit)
    if show_query is None:
        show_query = name in {"code", "name", "key", "title", "status", "role"} or name.endswith("_id")
    if query is None and show_query:
        if python_type in {"int", "bool"} or name.endswith("_id") or name in {"role", "status", "key", "code"}:
            query = "EQ" if (name.endswith("_id") or name in {"role", "status"} or python_type in {"int", "bool"}) else "LIKE"
            if name in {"code", "name", "key", "title"}:
                query = "LIKE"
    return SimpleNamespace(
        id=generate_snowflake_id(),
        plan_id="local",
        table_role="MAIN",
        column_name=name,
        column_comment=comment,
        db_type=db_type,
        python_type=python_type,
        typescript_type=ts_type,
        form_widget=widget,
        dict_code=None,
        query_operator=query,
        show_in_table=show_table and not is_audit,
        show_in_form=show_form,
        show_in_detail=True,
        show_in_query=bool(show_query) and not pk and not is_audit,
        is_primary_key=pk,
        is_required=required,
        is_unique=False,
        is_nullable=nullable,
        max_length=max_length,
        sort=sort,
        created_at=None,
        created_by=None,
        updated_at=None,
        updated_by=None,
    )


def audit_fields(start: int = 900) -> list[SimpleNamespace]:
    return [
        field("created_at", comment="创建时间", db_type="timestamptz", python_type="datetime", ts_type="string", nullable=False, sort=start),
        field("created_by", comment="创建人", db_type="varchar", python_type="str", max_length=64, nullable=True, sort=start + 1),
        field("updated_at", comment="更新时间", db_type="timestamptz", python_type="datetime", ts_type="string", nullable=False, sort=start + 2),
        field("updated_by", comment="更新人", db_type="varchar", python_type="str", max_length=64, nullable=True, sort=start + 3),
    ]


def pk_field() -> SimpleNamespace:
    return field("id", comment="主键", db_type="varchar", python_type="str", max_length=64, nullable=False, pk=True, sort=1)


def plan_ns(**kwargs) -> SimpleNamespace:
    defaults = dict(
        id=generate_snowflake_id(),
        description=None,
        main_pk="id",
        resource_module_id=None,
        parent_resource_id=None,
        icon="icon-park-outline:code",
        sort=99,
        tree_parent_field=None,
        tree_label_field=None,
        sub_table=None,
        sub_pk=None,
        sub_foreign_key=None,
        sub_entity_name=None,
        sub_business_name=None,
        gen_type="TABLE",
        author=AUTHOR,
    )
    defaults.update(kwargs)
    return SimpleNamespace(**defaults)


# ---------------------------------------------------------------------------
# Module definitions
# ---------------------------------------------------------------------------

MODULES: list[dict] = [
    # ---- problem ----
    {
        "plan": plan_ns(
            name="biz.problem.type",
            main_table="oj_problem_type",
            main_entity_name="OjProblemType",
            main_module_path="biz/problem/type",
            main_business_name="题目类型",
            api_prefix="/biz/problem/type",
            permission_prefix="biz:problem:type",
            menu_name="题目类型",
            menu_path="/biz/problem/type",
            component_path="/biz/problem/type/index.vue",
            sort=10,
        ),
        "fields": [
            pk_field(),
            field("code", comment="类型编码", max_length=32, nullable=False, sort=10),
            field("name", comment="类型名称", max_length=100, nullable=False, sort=20),
            field("sort", comment="排序", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=30),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (UniqueConstraint("code", name="uq_oj_problem_type_code"),)
''',
        "companion_model": '''

class OjProblemTypeRel(Base, TimestampMixin):
    """题目 ↔ 类型 关联。"""

    __tablename__ = "oj_problem_type_rel"
    __table_args__ = (
        UniqueConstraint("problem_id", "type_id", name="uq_oj_problem_type_rel"),
        Index("ix_oj_problem_type_rel_type", "type_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键")
    problem_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="题目ID")
    type_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="类型ID")
''',
        "depends_on": (),
        "order": 200,
    },
    {
        "plan": plan_ns(
            name="biz.problem.group",
            main_table="oj_problem_group",
            main_entity_name="OjProblemGroup",
            main_module_path="biz/problem/group",
            main_business_name="题目分组",
            api_prefix="/biz/problem/group",
            permission_prefix="biz:problem:group",
            menu_name="题目分组",
            menu_path="/biz/problem/group",
            component_path="/biz/problem/group/index.vue",
            sort=20,
        ),
        "fields": [
            pk_field(),
            field("code", comment="分组编码", max_length=32, nullable=False, sort=10),
            field("name", comment="分组名称", max_length=100, nullable=False, sort=20),
            field("sort", comment="排序", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=30),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (UniqueConstraint("code", name="uq_oj_problem_group_code"),)
''',
        "depends_on": (),
        "order": 201,
    },
    {
        "plan": plan_ns(
            name="biz.problem.problem",
            main_table="oj_problem",
            main_entity_name="OjProblem",
            main_module_path="biz/problem/problem",
            main_business_name="题目",
            api_prefix="/biz/problem/problem",
            permission_prefix="biz:problem:problem",
            menu_name="题目",
            menu_path="/biz/problem/problem",
            component_path="/biz/problem/problem/index.vue",
            sort=30,
        ),
        "fields": [
            pk_field(),
            field("code", comment="题目编码", max_length=32, nullable=False, sort=10),
            field("name", comment="题目标题", max_length=200, nullable=False, sort=20),
            field("description", comment="题面正文", db_type="text", python_type="str", nullable=False, sort=30),
            field("summary", comment="摘要", db_type="text", python_type="str", nullable=True, sort=40),
            field("group_id", comment="分组ID", max_length=64, nullable=True, sort=50, query="EQ"),
            field("time_limit_ms", comment="时间限制（毫秒）", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=60),
            field("memory_limit_kb", comment="内存限制（KB）", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=70),
            field("points", comment="题目分值", db_type="float", python_type="float", ts_type="number", nullable=False, sort=80),
            field("partial", comment="是否允许部分分", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=90),
            field("short_circuit", comment="遇错是否短路判题", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=100),
            field("is_public", comment="是否公开可见", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=110, query="EQ"),
            field("is_manually_managed", comment="测试数据是否人工托管", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=120),
            field("published_at", comment="发布时间", db_type="timestamptz", python_type="datetime", nullable=True, sort=130),
            field("submission_source_visibility", comment="提交源码可见性", max_length=32, nullable=False, sort=140),
            field("is_full_markup", comment="是否允许完整 Markup", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=150),
            field("og_image", comment="OpenGraph 图片", max_length=255, nullable=True, sort=160),
            field("user_count", comment="通过人数", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=170, show_form=False),
            field("ac_rate", comment="通过率", db_type="float", python_type="float", ts_type="number", nullable=False, sort=180, show_form=False),
            field("extra", comment="扩展信息", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=False, sort=190),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("code", name="uq_oj_problem_code"),
        Index("ix_oj_problem_is_public", "is_public"),
        Index("ix_oj_problem_group_id", "group_id"),
        Index("ix_oj_problem_published_at", "published_at"),
    )
''',
        "depends_on": ("biz.problem.group",),
        "order": 210,
    },
    {
        "plan": plan_ns(
            name="biz.problem.staff",
            main_table="oj_problem_staff",
            main_entity_name="OjProblemStaff",
            main_module_path="biz/problem/staff",
            main_business_name="题目人员",
            api_prefix="/biz/problem/staff",
            permission_prefix="biz:problem:staff",
            menu_name="题目人员",
            menu_path="/biz/problem/staff",
            component_path="/biz/problem/staff/index.vue",
            sort=50,
        ),
        "fields": [
            pk_field(),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("account_id", comment="账户ID", max_length=64, nullable=False, sort=20, query="EQ"),
            field("role", comment="角色", max_length=32, nullable=False, sort=30, query="EQ"),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("problem_id", "account_id", "role", name="uq_oj_problem_staff_role"),
        Index("ix_oj_problem_staff_account", "account_id", "role"),
    )
''',
        "depends_on": ("biz.problem.problem",),
        "order": 221,
    },
    {
        "plan": plan_ns(
            name="biz.problem.language",
            main_table="oj_problem_language",
            main_entity_name="OjProblemLanguage",
            main_module_path="biz/problem/language",
            main_business_name="题目语言",
            api_prefix="/biz/problem/language",
            permission_prefix="biz:problem:language",
            menu_name="题目语言",
            menu_path="/biz/problem/language",
            component_path="/biz/problem/language/index.vue",
            sort=60,
        ),
        "fields": [
            pk_field(),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("language_key", comment="语言标识", max_length=32, nullable=False, sort=20, query="EQ"),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("problem_id", "language_key", name="uq_oj_problem_language"),
        Index("ix_oj_problem_language_key", "language_key"),
    )
''',
        "depends_on": ("biz.problem.problem",),
        "order": 222,
    },
    {
        "plan": plan_ns(
            name="biz.problem.language_limit",
            main_table="oj_problem_language_limit",
            main_entity_name="OjProblemLanguageLimit",
            main_module_path="biz/problem/language_limit",
            main_business_name="题目语言限制",
            api_prefix="/biz/problem/language-limit",
            permission_prefix="biz:problem:languagelimit",
            menu_name="题目语言限制",
            menu_path="/biz/problem/language-limit",
            component_path="/biz/problem/language-limit/index.vue",
            sort=70,
        ),
        "fields": [
            pk_field(),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("language_key", comment="语言标识", max_length=32, nullable=False, sort=20, query="EQ"),
            field("time_limit_ms", comment="时间限制（毫秒）", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=30),
            field("memory_limit_kb", comment="内存限制（KB）", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=40),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("problem_id", "language_key", name="uq_oj_problem_language_limit"),
    )
''',
        "depends_on": ("biz.problem.problem",),
        "order": 223,
    },
    {
        "plan": plan_ns(
            name="biz.problem.data",
            main_table="oj_problem_data",
            main_entity_name="OjProblemData",
            main_module_path="biz/problem/data",
            main_business_name="题目数据",
            api_prefix="/biz/problem/data",
            permission_prefix="biz:problem:data",
            menu_name="题目数据",
            menu_path="/biz/problem/data",
            component_path="/biz/problem/data/index.vue",
            sort=80,
        ),
        "fields": [
            pk_field(),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("zip_file_id", comment="测试数据 zip 文件ID", max_length=64, nullable=True, sort=20),
            field("generator_file_id", comment="数据生成器文件ID", max_length=64, nullable=True, sort=30),
            field("checker", comment="Checker 类型", max_length=32, nullable=False, sort=40),
            field("checker_args", comment="Checker 参数", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=False, sort=50),
            field("spj_source", comment="SPJ 源码（C++17）", db_type="text", python_type="str", ts_type="string", nullable=True, sort=60),
            field("interactor_source", comment="交互器源码（C++17）", db_type="text", python_type="str", ts_type="string", nullable=True, sort=70),
            field("output_prefix", comment="输出前缀比对长度", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=80),
            field("output_limit", comment="输出长度上限", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=90),
            field("enable_unicode", comment="是否启用 Unicode", db_type="boolean", python_type="bool", ts_type="boolean", nullable=True, sort=100),
            field("disable_big_math", comment="是否禁用大整数", db_type="boolean", python_type="bool", ts_type="boolean", nullable=True, sort=110),
            field("feedback", comment="数据校验反馈", db_type="text", python_type="str", nullable=True, sort=120),
            field("extra", comment="扩展信息", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=False, sort=130),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (UniqueConstraint("problem_id", name="uq_oj_problem_data_problem"),)
''',
        "depends_on": ("biz.problem.problem",),
        "order": 224,
    },
    {
        "plan": plan_ns(
            name="biz.problem.test_case",
            main_table="oj_problem_test_case",
            main_entity_name="OjProblemTestCase",
            main_module_path="biz/problem/test_case",
            main_business_name="测试点",
            api_prefix="/biz/problem/test-case",
            permission_prefix="biz:problem:testcase",
            menu_name="测试点",
            menu_path="/biz/problem/test-case",
            component_path="/biz/problem/test-case/index.vue",
            sort=90,
        ),
        "fields": [
            pk_field(),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("case_no", comment="测试点编号", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=20, query="EQ"),
            field("sort", comment="顺序", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=30),
            field("case_type", comment="类型", max_length=32, nullable=False, sort=40, query="EQ"),
            field("input_file", comment="输入文件名", max_length=255, nullable=True, sort=50),
            field("output_file", comment="输出文件名", max_length=255, nullable=True, sort=60),
            field("input_file_id", comment="输入文件ID", max_length=64, nullable=True, sort=70),
            field("output_file_id", comment="输出文件ID", max_length=64, nullable=True, sort=80),
            field("input_sha256", comment="输入 SHA256", max_length=64, nullable=True, sort=90),
            field("output_sha256", comment="输出 SHA256", max_length=64, nullable=True, sort=100),
            field("points", comment="分值", db_type="float", python_type="float", ts_type="number", nullable=True, sort=110),
            field("is_pretest", comment="是否 pretest", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=120),
            field("time_limit_ms", comment="覆盖时间限制", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=130),
            field("memory_limit_kb", comment="覆盖内存限制", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=140),
            field("batch_no", comment="batch 编号", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=150),
            field("batch_depends", comment="依赖 batch 列表", db_type="json", python_type="list", ts_type="number[]", nullable=False, sort=160),
            field("checker", comment="覆盖 Checker", max_length=32, nullable=True, sort=170),
            field("checker_args", comment="覆盖 Checker 参数", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=True, sort=180),
            field("generator_args", comment="生成器参数", db_type="text", python_type="str", nullable=True, sort=190),
            field("output_prefix", comment="覆盖输出前缀", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=200),
            field("output_limit", comment="覆盖输出上限", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=210),
            field("extra", comment="扩展信息", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=False, sort=220),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("problem_id", "case_no", name="uq_oj_problem_test_case_no"),
        Index("ix_oj_problem_test_case_problem_order", "problem_id", "sort"),
    )
''',
        "depends_on": ("biz.problem.problem",),
        "order": 225,
    },
    {
        "plan": plan_ns(
            name="biz.problem.solution",
            main_table="oj_problem_solution",
            main_entity_name="OjProblemSolution",
            main_module_path="biz/problem/solution",
            main_business_name="题解",
            api_prefix="/biz/problem/solution",
            permission_prefix="biz:problem:solution",
            menu_name="题解",
            menu_path="/biz/problem/solution",
            component_path="/biz/problem/solution/index.vue",
            sort=100,
        ),
        "fields": [
            pk_field(),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("content", comment="题解正文", db_type="text", python_type="str", nullable=False, sort=20),
            field("is_public", comment="是否公开", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=30, query="EQ"),
            field("publish_at", comment="公开时间", db_type="timestamptz", python_type="datetime", nullable=True, sort=40),
            field("author_account_ids", comment="作者账户ID列表", db_type="json", python_type="list", ts_type="string[]", nullable=False, sort=50),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (UniqueConstraint("problem_id", name="uq_oj_problem_solution_problem"),)
''',
        "depends_on": ("biz.problem.problem",),
        "order": 226,
    },
    # ---- contest ----
    {
        "plan": plan_ns(
            name="biz.contest.tag",
            main_table="oj_contest_tag",
            main_entity_name="OjContestTag",
            main_module_path="biz/contest/tag",
            main_business_name="竞赛标签",
            api_prefix="/biz/contest/tag",
            permission_prefix="biz:contest:tag",
            menu_name="竞赛标签",
            menu_path="/biz/contest/tag",
            component_path="/biz/contest/tag/index.vue",
            sort=10,
        ),
        "fields": [
            pk_field(),
            field("code", comment="标签编码", max_length=32, nullable=False, sort=10),
            field("name", comment="标签名称", max_length=100, nullable=False, sort=20),
            field("color", comment="颜色", max_length=7, nullable=False, sort=30),
            field("description", comment="描述", db_type="text", python_type="str", nullable=True, sort=40),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (UniqueConstraint("code", name="uq_oj_contest_tag_code"),)
''',
        "companion_model": '''

class OjContestTagRel(Base, TimestampMixin):
    """竞赛 ↔ 标签 关联。"""

    __tablename__ = "oj_contest_tag_rel"
    __table_args__ = (
        UniqueConstraint("contest_id", "tag_id", name="uq_oj_contest_tag_rel"),
        Index("ix_oj_contest_tag_rel_tag", "tag_id"),
    )

    id: Mapped[str] = mapped_column(String(64), primary_key=True, nullable=False, default=generate_snowflake_id, comment="主键")
    contest_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="竞赛ID")
    tag_id: Mapped[str] = mapped_column(String(64), nullable=False, comment="标签ID")
''',
        "depends_on": (),
        "order": 300,
    },
    {
        "plan": plan_ns(
            name="biz.contest.contest",
            main_table="oj_contest",
            main_entity_name="OjContest",
            main_module_path="biz/contest/contest",
            main_business_name="竞赛",
            api_prefix="/biz/contest/contest",
            permission_prefix="biz:contest:contest",
            menu_name="竞赛",
            menu_path="/biz/contest/contest",
            component_path="/biz/contest/contest/index.vue",
            sort=20,
        ),
        "fields": [
            pk_field(),
            field("key", comment="竞赛标识", max_length=32, nullable=False, sort=10),
            field("name", comment="竞赛名称", max_length=200, nullable=False, sort=20),
            field("description", comment="竞赛说明", db_type="text", python_type="str", nullable=False, sort=30),
            field("summary", comment="摘要", db_type="text", python_type="str", nullable=True, sort=40),
            field("start_time", comment="开始时间", db_type="timestamptz", python_type="datetime", nullable=False, sort=50),
            field("end_time", comment="结束时间", db_type="timestamptz", python_type="datetime", nullable=False, sort=60),
            field("time_limit_seconds", comment="个人参赛时长（秒）", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=70),
            field("is_visible", comment="是否公开可见", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=80, query="EQ"),
            field("is_private", comment="是否仅限指定选手", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=90),
            field("access_code", comment="参赛准入码", max_length=255, nullable=True, sort=100),
            field("is_rated", comment="是否计入 Rating", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=110),
            field("rating_floor", comment="Rating 下限", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=120),
            field("rating_ceiling", comment="Rating 上限", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=130),
            field("rate_all", comment="无提交也计 Rating", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=140),
            field("scoreboard_visibility", comment="榜单可见性", max_length=32, nullable=False, sort=150),
            field("format_name", comment="赛制", max_length=32, nullable=False, sort=160),
            field("format_config", comment="赛制配置", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=True, sort=170),
            field("points_precision", comment="分数小数精度", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=180),
            field("hide_problem_tags", comment="赛中隐藏题目标签", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=190),
            field("hide_problem_authors", comment="赛中隐藏命题人", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=200),
            field("run_pretests_only", comment="赛中仅跑 pretest", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=210),
            field("use_clarifications", comment="使用答疑", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=220),
            field("tester_see_scoreboard", comment="测试员可见榜单", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=230),
            field("tester_see_submissions", comment="测试员可见提交", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=240),
            field("show_short_display", comment="展示简短设置摘要", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=250),
            field("problem_label_script", comment="题目标签脚本", db_type="text", python_type="str", nullable=True, sort=260),
            field("locked_after", comment="重判锁定时间", db_type="timestamptz", python_type="datetime", nullable=True, sort=270),
            field("og_image", comment="OpenGraph 图片", max_length=255, nullable=True, sort=280),
            field("logo_override_image", comment="Logo 覆盖图", max_length=255, nullable=True, sort=290),
            field("user_count", comment="正式参赛人数", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=300, show_form=False),
            field("extra", comment="扩展信息", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=False, sort=310),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("key", name="uq_oj_contest_key"),
        Index("ix_oj_contest_is_visible", "is_visible"),
        Index("ix_oj_contest_start_time", "start_time"),
        Index("ix_oj_contest_end_time", "end_time"),
    )
''',
        "depends_on": (),
        "order": 310,
    },
    {
        "plan": plan_ns(
            name="biz.contest.staff",
            main_table="oj_contest_staff",
            main_entity_name="OjContestStaff",
            main_module_path="biz/contest/staff",
            main_business_name="竞赛人员",
            api_prefix="/biz/contest/staff",
            permission_prefix="biz:contest:staff",
            menu_name="竞赛人员",
            menu_path="/biz/contest/staff",
            component_path="/biz/contest/staff/index.vue",
            sort=40,
        ),
        "fields": [
            pk_field(),
            field("contest_id", comment="竞赛ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("account_id", comment="账户ID", max_length=64, nullable=False, sort=20, query="EQ"),
            field("role", comment="角色", max_length=32, nullable=False, sort=30, query="EQ"),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("contest_id", "account_id", "role", name="uq_oj_contest_staff_role"),
        Index("ix_oj_contest_staff_account", "account_id", "role"),
    )
''',
        "depends_on": ("biz.contest.contest",),
        "order": 321,
    },
    {
        "plan": plan_ns(
            name="biz.contest.private_contestant",
            main_table="oj_contest_private_contestant",
            main_entity_name="OjContestPrivateContestant",
            main_module_path="biz/contest/private_contestant",
            main_business_name="私有竞赛选手",
            api_prefix="/biz/contest/private-contestant",
            permission_prefix="biz:contest:privatecontestant",
            menu_name="私有竞赛选手",
            menu_path="/biz/contest/private-contestant",
            component_path="/biz/contest/private-contestant/index.vue",
            sort=50,
        ),
        "fields": [
            pk_field(),
            field("contest_id", comment="竞赛ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("account_id", comment="账户ID", max_length=64, nullable=False, sort=20, query="EQ"),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("contest_id", "account_id", name="uq_oj_contest_private_contestant"),
        Index("ix_oj_contest_private_contestant_account", "account_id"),
    )
''',
        "depends_on": ("biz.contest.contest",),
        "order": 322,
    },
    {
        "plan": plan_ns(
            name="biz.contest.banned_user",
            main_table="oj_contest_banned_user",
            main_entity_name="OjContestBannedUser",
            main_module_path="biz/contest/banned_user",
            main_business_name="竞赛禁赛用户",
            api_prefix="/biz/contest/banned-user",
            permission_prefix="biz:contest:banneduser",
            menu_name="竞赛禁赛用户",
            menu_path="/biz/contest/banned-user",
            component_path="/biz/contest/banned-user/index.vue",
            sort=60,
        ),
        "fields": [
            pk_field(),
            field("contest_id", comment="竞赛ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("account_id", comment="账户ID", max_length=64, nullable=False, sort=20, query="EQ"),
            field("reason", comment="禁赛原因", db_type="text", python_type="str", nullable=True, sort=30),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("contest_id", "account_id", name="uq_oj_contest_banned_user"),
    )
''',
        "depends_on": ("biz.contest.contest",),
        "order": 323,
    },
    {
        "plan": plan_ns(
            name="biz.contest.problem",
            main_table="oj_contest_problem",
            main_entity_name="OjContestProblem",
            main_module_path="biz/contest/problem",
            main_business_name="竞赛题目",
            api_prefix="/biz/contest/problem",
            permission_prefix="biz:contest:problem",
            menu_name="竞赛题目",
            menu_path="/biz/contest/problem",
            component_path="/biz/contest/problem/index.vue",
            sort=70,
        ),
        "fields": [
            pk_field(),
            field("contest_id", comment="竞赛ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("problem_id", comment="题目ID", max_length=64, nullable=False, sort=20, query="EQ"),
            field("points", comment="竞赛内分值", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=30),
            field("partial", comment="是否允许部分分", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=40),
            field("is_pretested", comment="是否仅 pretest 计分", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=50),
            field("sort", comment="题目顺序", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=60),
            field("label", comment="展示标签", max_length=16, nullable=True, sort=70),
            field("max_submissions", comment="最大提交次数", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=80),
            field("output_prefix_override", comment="输出前缀覆盖", db_type="integer", python_type="int", ts_type="number", nullable=True, sort=90),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("contest_id", "problem_id", name="uq_oj_contest_problem"),
        UniqueConstraint("contest_id", "sort", name="uq_oj_contest_problem_sort"),
        Index("ix_oj_contest_problem_problem", "problem_id"),
    )
''',
        "depends_on": ("biz.contest.contest", "biz.problem.problem"),
        "order": 330,
    },
    {
        "plan": plan_ns(
            name="biz.contest.participation",
            main_table="oj_contest_participation",
            main_entity_name="OjContestParticipation",
            main_module_path="biz/contest/participation",
            main_business_name="竞赛参赛",
            api_prefix="/biz/contest/participation",
            permission_prefix="biz:contest:participation",
            menu_name="竞赛参赛",
            menu_path="/biz/contest/participation",
            component_path="/biz/contest/participation/index.vue",
            sort=80,
        ),
        "fields": [
            pk_field(),
            field("contest_id", comment="竞赛ID", max_length=64, nullable=False, sort=10, query="EQ"),
            field("account_id", comment="账户ID", max_length=64, nullable=False, sort=20, query="EQ"),
            field("real_start", comment="实际开始参赛时间", db_type="timestamptz", python_type="datetime", nullable=False, sort=30),
            field("score", comment="总分", db_type="float", python_type="float", ts_type="number", nullable=False, sort=40),
            field("cumtime", comment="累计时间/罚时（秒）", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=50),
            field("tiebreaker", comment="平分决胜值", db_type="float", python_type="float", ts_type="number", nullable=False, sort=60),
            field("is_disqualified", comment="是否取消资格", db_type="boolean", python_type="bool", ts_type="boolean", nullable=False, sort=70),
            field("virtual", comment="虚拟状态", db_type="integer", python_type="int", ts_type="number", nullable=False, sort=80, query="EQ"),
            field("format_data", comment="赛制私有数据", db_type="json", python_type="dict", ts_type="Record<string, any>", nullable=True, sort=90),
            *audit_fields(),
        ],
        "model_extra": '''    __table_args__ = (
        UniqueConstraint("contest_id", "account_id", "virtual", name="uq_oj_contest_participation"),
        Index("ix_oj_contest_participation_scoreboard", "contest_id", "virtual", "score"),
        Index("ix_oj_contest_participation_account", "account_id"),
    )
''',
        "depends_on": ("biz.contest.contest",),
        "order": 340,
    },
]


def patch_model(content: str, model_extra: str) -> str:
    """Inject UniqueConstraint/Index imports and __table_args__."""
    if "UniqueConstraint" not in content and ("UniqueConstraint" in model_extra or "Index" in model_extra):
        content = content.replace(
            "from sqlalchemy import JSON, Boolean, DateTime, Float, Integer, Numeric, String, Text",
            "from sqlalchemy import JSON, Boolean, DateTime, Float, Index, Integer, Numeric, String, Text, UniqueConstraint",
        )
    # Fix list JSON defaults (codegen only injects default=dict for dict[str, Any])
    content = re.sub(
        r"(Mapped\[list\] = mapped_column\(JSON, nullable=False)(, comment=)",
        r"\1, default=list\2",
        content,
    )
    content = re.sub(
        r"(class \w+\(Base, TimestampMixin\):\n    __tablename__ = \"[^\"]+\"\n)",
        rf"\1{model_extra}",
        content,
        count=1,
    )
    return content


def patch_module(content: str, depends_on: tuple[str, ...], order: int) -> str:
    deps = ", ".join(f'"{d}"' for d in depends_on)
    # Insert depends_on / order before closing paren of ModuleSpec
    if "depends_on=" not in content:
        content = content.replace(
            "    models=(",
            f"    order={order},\n    depends_on=({deps},),\n    models=(",
        )
    return content


def patch_list_schema(content: str) -> str:
    """Fix list fields default_factory in schema."""
    content = content.replace(
        "batch_depends: list = Field(default_factory=dict)",
        "batch_depends: list = Field(default_factory=list)",
    )
    content = content.replace(
        "author_account_ids: list = Field(default_factory=dict)",
        "author_account_ids: list = Field(default_factory=list)",
    )
    # codegen may emit bare list without default for required non-nullable
    return content


def write_domain_enums() -> None:
    problem_enums = ROOT / "app/modules/biz/problem/enums.py"
    contest_enums = ROOT / "app/modules/biz/contest/enums.py"
    problem_enums.parent.mkdir(parents=True, exist_ok=True)
    contest_enums.parent.mkdir(parents=True, exist_ok=True)
    (ROOT / "app/modules/biz/problem/__init__.py").write_text('"""题目大模块。"""\n', encoding="utf-8")
    (ROOT / "app/modules/biz/contest/__init__.py").write_text('"""竞赛大模块。"""\n', encoding="utf-8")

    problem_enums.write_text(
        '''from enum import StrEnum


class ProblemStaffRole(StrEnum):
    """题目人员角色。"""

    AUTHOR = "AUTHOR"
    CURATOR = "CURATOR"
    TESTER = "TESTER"


class SubmissionSourceVisibility(StrEnum):
    """提交源码可见性。"""

    FOLLOW = "FOLLOW"
    ALWAYS = "ALWAYS"
    SOLVED = "SOLVED"
    ONLY_OWN = "ONLY_OWN"


class ProblemChecker(StrEnum):
    """标准 checker 类型。"""

    STANDARD = "standard"
    FLOATS = "floats"
    FLOATS_ABS = "floatsabs"
    FLOATS_REL = "floatsrel"
    RSTRIPPED = "rstripped"
    SORTED = "sorted"
    IDENTICAL = "identical"
    LINECOUNT = "linecount"
    CUSTOM = "custom"


class TestCaseType(StrEnum):
    """测试点类型。"""

    NORMAL = "NORMAL"
    BATCH_START = "BATCH_START"
    BATCH_END = "BATCH_END"
''',
        encoding="utf-8",
    )

    contest_enums.write_text(
        '''from enum import IntEnum, StrEnum


class ContestStaffRole(StrEnum):
    """竞赛人员角色。"""

    AUTHOR = "AUTHOR"
    CURATOR = "CURATOR"
    TESTER = "TESTER"
    SPECTATOR = "SPECTATOR"


class ScoreboardVisibility(StrEnum):
    """榜单可见性。"""

    VISIBLE = "VISIBLE"
    AFTER_CONTEST = "AFTER_CONTEST"
    AFTER_PARTICIPATION = "AFTER_PARTICIPATION"
    HIDDEN = "HIDDEN"


class ContestParticipationVirtual(IntEnum):
    """参赛虚拟状态。"""

    SPECTATE = -1
    LIVE = 0


class ContestFormat(StrEnum):
    """竞赛赛制。"""

    DEFAULT = "default"
    ICPC = "icpc"
    IOI = "ioi"
    ATOCCODER = "atcoder"
    ECOLE = "ecole"
''',
        encoding="utf-8",
    )


def main() -> None:
    write_domain_enums()
    api_exports: list[str] = []

    for mod in MODULES:
        plan = mod["plan"]
        files = render_files(plan, mod["fields"], [])
        for item in files:
            path = ROOT / item.path
            if item.path.endswith("index.ts.append"):
                api_exports.append(item.content.strip())
                continue
            # skip menu SQL into scripts root clutter — still write under scripts/codegen/oj/
            if item.path.startswith("scripts/") and item.path.endswith("_menu_permission.sql"):
                path = ROOT / "scripts/codegen/oj" / Path(item.path).name

            content = item.content
            if item.path.endswith("/model.py"):
                companion = mod.get("companion_model", "")
                # Only inject __table_args__ from model_extra; companion is appended after.
                import_hint = ""
                if "Index(" in companion:
                    import_hint += "Index"
                if "UniqueConstraint(" in companion:
                    import_hint += "UniqueConstraint"
                content = patch_model(content, mod.get("model_extra", "") + import_hint)
                if companion:
                    content = content.rstrip() + "\n" + companion.lstrip("\n")
                    if not content.endswith("\n"):
                        content += "\n"
            elif item.path.endswith("/module.py"):
                content = patch_module(content, mod.get("depends_on", ()), mod.get("order", 100))
            elif item.path.endswith("/schema.py"):
                content = patch_list_schema(content)

            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
            print(f"wrote {path.relative_to(ROOT)}")

    # Merge API exports into admin api/index.ts
    api_index = ROOT / "web/admin/src/api/index.ts"
    existing = api_index.read_text(encoding="utf-8")
    marker = "\n// --- oj modules (codegen) ---\n"
    block = marker + "\n".join(api_exports) + "\n"
    if marker in existing:
        before, _, rest = existing.partition(marker)
        # drop old oj block until EOF or next unrelated section — replace to end
        existing = before.rstrip() + "\n" + block
    else:
        existing = existing.rstrip() + "\n" + block
    api_index.write_text(existing, encoding="utf-8")
    print(f"updated {api_index.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
