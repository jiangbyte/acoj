"""Build JudgePayload for acoj-worker from oj_problem_test_case rows.

Source of truth for MQ test_cases[]:
  EACH ROW in oj_problem_test_case → one TestCaseData entry.

Zip packages are never sent to the worker. Zip import only materializes rows
(and per-case storage keys / inline text). trial-judge and future submission
bridges must call build_worker_test_cases().
"""

from __future__ import annotations

import hashlib
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions.business import BusinessError, NotFoundError
from app.modules.biz.problem.data.model import OjProblemData
from app.modules.biz.problem.enums import JudgeMode, TestCaseDataMode
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.test_case.model import OjProblemTestCase
from app.modules.biz.problem.sandbox_languages import ensure_sandbox_language_key
from app.platform.id_generator.snowflake import generate_snowflake_id
from app.platform.storage.manager import get_storage

# Admin trial languages — keys aligned with acoj-sandbox LanguageId.
TRIAL_LANGUAGES: dict[str, dict[str, Any]] = {
    "c99": {
        "key": "c99",
        "name": "C99",
        "extension": ".c",
        "compile_command": "gcc -O2 -std=c99 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "c11": {
        "key": "c11",
        "name": "C11",
        "extension": ".c",
        "compile_command": "gcc -O2 -std=c11 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "c17": {
        "key": "c17",
        "name": "C17",
        "extension": ".c",
        "compile_command": "gcc -O2 -std=c17 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "cpp11": {
        "key": "cpp11",
        "name": "C++11",
        "extension": ".cpp",
        "compile_command": "g++ -O2 -std=c++11 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "cpp14": {
        "key": "cpp14",
        "name": "C++14",
        "extension": ".cpp",
        "compile_command": "g++ -O2 -std=c++14 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "cpp17": {
        "key": "cpp17",
        "name": "C++17",
        "extension": ".cpp",
        "compile_command": "g++ -O2 -std=c++17 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "cpp20": {
        "key": "cpp20",
        "name": "C++20",
        "extension": ".cpp",
        "compile_command": "g++ -O2 -std=c++20 {src} -o {exe}",
        "run_command": "{exe}",
    },
    "python3": {
        "key": "python3",
        "name": "Python 3",
        "extension": ".py",
        "compile_command": None,
        "run_command": "python3 {src}",
    },
    "java17": {
        "key": "java17",
        "name": "Java 17",
        "extension": ".java",
        "compile_command": "javac {src}",
        "run_command": "java -Xss256m Main",
    },
    "go": {
        "key": "go",
        "name": "Go",
        "extension": ".go",
        "compile_command": "go build -o {exe} {src}",
        "run_command": "{exe}",
    },
    "nodejs": {
        "key": "nodejs",
        "name": "Node.js",
        "extension": ".js",
        "compile_command": None,
        "run_command": "node {src}",
    },
    "rust": {
        "key": "rust",
        "name": "Rust",
        "extension": ".rs",
        "compile_command": "rustc -O -o {exe} {src}",
        "run_command": "{exe}",
    },
}


def read_storage_bytes(object_name: str) -> bytes:
    """Read object bytes from the configured storage provider."""
    storage = get_storage()
    if hasattr(storage, "get_path"):
        path = storage.get_path(object_name)
        if not path.exists():
            raise NotFoundError(f"Storage object not found: {object_name}")
        return path.read_bytes()
    client = getattr(storage, "client", None)
    bucket = getattr(storage, "bucket", None)
    if client is None or bucket is None:
        raise BusinessError("Current storage does not support reading objects")
    response = client.get_object(Bucket=bucket, Key=object_name)
    return response["Body"].read()


def sha256_hex(content: bytes) -> str:
    return hashlib.sha256(content).hexdigest()


def test_case_to_worker_dict(case: OjProblemTestCase, *, require_output: bool = True) -> dict[str, Any]:
    """Map one oj_problem_test_case row → one worker TestCaseData dict."""
    base: dict[str, Any] = {
        "case_no": case.case_no,
        "points": float(case.points or 0),
        "time_limit_ms": case.time_limit_ms,
        "memory_limit_kb": case.memory_limit_kb,
        "batch_no": case.batch_no,
        "batch_depends": list(case.batch_depends or []),
        "input_inline": "",
        "output_inline": None,
        "input_file": None,
        "output_file": None,
        "input_sha256": "",
        "output_sha256": "",
    }
    mode = (case.data_mode or TestCaseDataMode.FILE).lower()
    if mode == TestCaseDataMode.INLINE:
        base["input_inline"] = case.input_inline or ""
        if require_output:
            if case.output_inline is None:
                raise BusinessError(f"Test case {case.case_no} missing inline output")
            base["output_inline"] = case.output_inline
        else:
            base["output_inline"] = case.output_inline if case.output_inline is not None else ""
        return base
    if not case.input_file:
        raise BusinessError(f"Test case {case.case_no} missing input storage key")
    if require_output and not case.output_file:
        raise BusinessError(f"Test case {case.case_no} missing output storage key")
    base["input_file"] = case.input_file
    base["output_file"] = case.output_file
    base["input_sha256"] = case.input_sha256 or ""
    base["output_sha256"] = case.output_sha256 or ""
    return base


async def build_worker_test_cases(
    db: AsyncSession,
    problem_id: str,
    *,
    case_ids: list[str] | None = None,
    require_output: bool = True,
) -> list[dict[str, Any]]:
    """Load oj_problem_test_case rows as worker test_cases[] (one dict per row)."""
    stmt = (
        select(OjProblemTestCase)
        .where(OjProblemTestCase.problem_id == problem_id)
        .order_by(OjProblemTestCase.sort.asc(), OjProblemTestCase.case_no.asc())
    )
    if case_ids:
        stmt = stmt.where(OjProblemTestCase.id.in_(case_ids))
    rows = list((await db.execute(stmt)).scalars().all())
    if not rows:
        raise BusinessError("No test cases available for this problem")
    return [test_case_to_worker_dict(row, require_output=require_output) for row in rows]


async def load_text_by_key(object_name: str | None) -> str | None:
    if not object_name:
        return None
    return read_storage_bytes(object_name).decode("utf-8", errors="replace")


async def build_admin_trial_payload(
    db: AsyncSession,
    *,
    problem_id: str,
    language_key: str,
    source: str,
    case_ids: list[str] | None = None,
) -> dict[str, Any]:
    """Build JudgePayload for admin trial.

    test_cases comes only from oj_problem_test_case rows (file or inline),
    never from oj_problem_data.zip_object_name.
    """
    problem = await db.get(OjProblem, problem_id)
    if problem is None:
        raise NotFoundError("OjProblem not found")
    language_key = ensure_sandbox_language_key(language_key)
    language = TRIAL_LANGUAGES.get(language_key)
    if language is None:
        raise BusinessError(
            f"Trial judge 暂未配置该语言的编译/运行命令: {language_key}（language_key 已与 sandbox 对齐，但试测白名单未覆盖）"
        )
    if not source.strip():
        raise BusinessError("source is required")

    data_stmt = select(OjProblemData).where(OjProblemData.problem_id == problem_id)
    data = (await db.execute(data_stmt)).scalar_one_or_none()
    judge_mode = (data.judge_mode if data and data.judge_mode else JudgeMode.STANDARD)
    if judge_mode not in {
        JudgeMode.STANDARD,
        JudgeMode.SPECIAL_JUDGE,
        JudgeMode.INTERACTIVE,
    }:
        judge_mode = JudgeMode.STANDARD

    test_cases = await build_worker_test_cases(
        db,
        problem_id,
        case_ids=case_ids,
        require_output=judge_mode != JudgeMode.INTERACTIVE,
    )
    payload: dict[str, Any] = {
        "submission_id": f"trial-{problem_id}-{generate_snowflake_id()}",
        "judge_mode": judge_mode,
        "problem": {
            "code": problem.code,
            "time_limit_ms": problem.time_limit_ms,
            "memory_limit_kb": problem.memory_limit_kb,
            "points": float(problem.points),
            "partial": bool(problem.partial),
        },
        "language": language,
        "source": source,
        "test_cases": test_cases,
    }

    if data and judge_mode == JudgeMode.SPECIAL_JUDGE:
        spj_source = (data.spj_source or "").strip()
        if not spj_source:
            raise BusinessError("SPECIAL_JUDGE mode requires spj_source")
        payload["spj"] = {
            "language": TRIAL_LANGUAGES["cpp17"],
            "source": spj_source,
        }
    elif data and judge_mode == JudgeMode.INTERACTIVE:
        interactor_source = (data.interactor_source or "").strip()
        if not interactor_source:
            raise BusinessError("INTERACTIVE mode requires interactor_source")
        payload["interactor"] = {
            "language": TRIAL_LANGUAGES["cpp17"],
            "source": interactor_source,
        }
    return payload
