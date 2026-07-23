"""端到端测试：通过 POST /api/v1/portal/oj/submit 提交代码。

使用 in-memory SQLite + mock 认证 + mock MQ 捕获 JudgeRequest。
打印完整 API 请求体、响应体、JudgeRequest 构建结果。

用法：
    pytest tests/api/test_submit_api.py -v -s
"""

import json
from typing import Any
from unittest.mock import AsyncMock

import pytest
from httpx import AsyncClient

from app.core.config.enums import AccountType
from app.core.security.session import SessionPayload
from app.modules.oj.enums import OjJudgeMode, OjTestCaseType

# ── 测试数据 ID ──────────────────────────────────────

LANG_CPP17_ID = "lang-cpp17-test"
PROBLEM_STANDARD_ID = "p-std-test-001"
PROBLEM_SPJ_ID = "p-spj-test-001"
PROBLEM_INTERACTIVE_ID = "p-int-test-001"


def _print_separator(title: str):
    print(f"\n{'=' * 70}")
    print(f"  {title}")
    print(f"{'=' * 70}")


# ── 种子数据 ────────────────────────────────────────

async def seed_data(db_session) -> dict[str, Any]:
    from app.modules.oj.judge.language.model import OjLanguage
    from app.modules.oj.problem.judge_config.model import OjProblemJudgeConfig
    from app.modules.oj.problem.problem.model import OjProblem
    from app.modules.oj.problem.dataset.model import OjDataset
    from app.modules.oj.problem.test_case.model import OjTestCase

    lang = OjLanguage(
        id=LANG_CPP17_ID, key="cpp17", name="C++17",
        extension=".cpp",
        compile_command="/usr/bin/g++ -std=c++17 -O2 -o {exe} {source}",
        run_command="{exe}",
        status="ENABLED",
    )
    db_session.add(lang)

    # STANDARD 题目 — 3 个测试用例，2 个内联 + 1 个文件引用
    problem_std = OjProblem(
        id=PROBLEM_STANDARD_ID, code="v-test-std", title="标准判题测试",
        judge_mode=OjJudgeMode.STANDARD.value,
        time_limit_ms=2000, memory_limit_kb=262144, points=100.0, partial=True,
        allow_languages=[LANG_CPP17_ID],
        status="ENABLED", visibility="PUBLIC",
    )
    db_session.add(problem_std)

    ds_std = OjDataset(id="ds-std-test-001", problem_id=PROBLEM_STANDARD_ID,
                       is_active=True, name="标准测试集", version="v1")
    db_session.add(ds_std)
    db_session.add(OjTestCase(
        id="tc-std-001", dataset_id=ds_std.id, case_no=1,
        case_type=OjTestCaseType.NORMAL.value,
        input_inline="hello\n", output_inline="hello\n",
        points=33.33, sort=1,
    ))
    db_session.add(OjTestCase(
        id="tc-std-002", dataset_id=ds_std.id, case_no=2,
        case_type=OjTestCaseType.NORMAL.value,
        input_inline="world\n", output_inline="world\n",
        points=33.33, sort=2, batch_no=1, batch_dependencies=[],
    ))
    db_session.add(OjTestCase(
        id="tc-std-003", dataset_id=ds_std.id, case_no=3,
        case_type=OjTestCaseType.NORMAL.value,
        input_file="testdata/hello.txt", output_file="testdata/hello_out.txt",
        points=33.34, sort=3, batch_no=1, batch_dependencies=[],
    ))

    # SPJ 题目
    problem_spj = OjProblem(
        id=PROBLEM_SPJ_ID, code="v-test-spj", title="SPJ判题测试",
        judge_mode=OjJudgeMode.SPECIAL_JUDGE.value,
        time_limit_ms=2000, memory_limit_kb=262144, points=100.0,
        allow_languages=[LANG_CPP17_ID],
        status="ENABLED", visibility="PUBLIC",
    )
    db_session.add(problem_spj)
    db_session.add(OjProblemJudgeConfig(
        problem_id=PROBLEM_SPJ_ID,
        spj_language_id=LANG_CPP17_ID,
        spj_source=(
            '#include <iostream>\n#include <fstream>\n#include <string>\n'
            'int main(int argc, char* argv[]) {\n'
            '  if (argc < 3) return 3;\n'
            '  std::ifstream u(argv[2]);\n'
            '  if (!u) return 3;\n'
            '  std::string t;\n'
            '  while (u >> t) { if (t == "ACCEPT") { return 0; } }\n'
            '  return 1;\n'
            '}\n'
        ),
    ))
    ds_spj = OjDataset(id="ds-spj-test-001", problem_id=PROBLEM_SPJ_ID,
                       is_active=True, name="SPJ测试集", version="v1")
    db_session.add(ds_spj)
    db_session.add(OjTestCase(
        id="tc-spj-001", dataset_id=ds_spj.id, case_no=1,
        case_type=OjTestCaseType.NORMAL.value,
        input_inline="", points=100.0, sort=1,
    ))

    # INTERACTIVE 题目
    problem_int = OjProblem(
        id=PROBLEM_INTERACTIVE_ID, code="v-test-int", title="交互判题测试",
        judge_mode=OjJudgeMode.INTERACTIVE.value,
        time_limit_ms=2000, memory_limit_kb=262144, points=100.0,
        allow_languages=[LANG_CPP17_ID],
        status="ENABLED", visibility="PUBLIC",
    )
    db_session.add(problem_int)
    db_session.add(OjProblemJudgeConfig(
        problem_id=PROBLEM_INTERACTIVE_ID,
        interactor_language_id=LANG_CPP17_ID,
        interactor_source=(
            '#include <iostream>\n#include <string>\n'
            'int main() {\n'
            '  std::cout << "Alice" << std::endl;\n'
            '  std::string r;\n'
            '  if (!std::getline(std::cin, r)) return 1;\n'
            '  if (r != "Hello, Alice!") return 1;\n'
            '  return 0;\n'
            '}\n'
        ),
    ))
    ds_int = OjDataset(id="ds-int-test-001", problem_id=PROBLEM_INTERACTIVE_ID,
                       is_active=True, name="交互测试集", version="v1")
    db_session.add(ds_int)
    db_session.add(OjTestCase(
        id="tc-int-001", dataset_id=ds_int.id, case_no=1,
        case_type=OjTestCaseType.NORMAL.value,
        input_inline="", points=100.0, sort=1,
    ))

    await db_session.commit()
    return {"lang": lang, "problem_std": problem_std, "problem_spj": problem_spj, "problem_int": problem_int}


# ── Auth 和 MQ mock ─────────────────────────────────

captured_requests: list[dict] = []


async def mock_publish(**kwargs):
    payload = kwargs.get("payload")
    if payload:
        captured_requests.append(payload)
        print(f"\n  [JudgeRequest captured ({kwargs.get('topic', '?')})]")
        print(json.dumps(payload, indent=2, ensure_ascii=False)[:3000])
    return None


# ── 固定件 ──────────────────────────────────────────

@pytest.fixture
async def seeded_db(db_session):
    data = await seed_data(db_session)
    return data


@pytest.fixture
async def api_client(client: AsyncClient, monkeypatch):
    from app.modules.oj.submission.submission import service_portal as portal_module
    from app.deps.auth import get_current_session, get_current_account, require_account_type

    captured_requests.clear()
    monkeypatch.setattr(portal_module.event_producer, "publish", mock_publish)

    fake_session = SessionPayload(
        token="test-token-xxx",
        account_id="test-user-001",
        account_type=AccountType.PORTAL,
        permission_keys=[],
    )

    async def _fake_session():
        return fake_session

    async def _fake_account():
        return fake_session

    app = client._transport.app
    app.dependency_overrides[get_current_session] = _fake_session
    app.dependency_overrides[get_current_account] = _fake_account

    # Seed data through the app's own DB session (same in-memory SQLite)
    from app.deps.db import get_db_session
    db_override = app.dependency_overrides.get(get_db_session)
    if db_override:
        async for session in db_override():
            await seed_data(session)
            break

    yield client
    app.dependency_overrides.clear()


# ── 测试函数 ─────────────────────────────────────────

async def test_submit_standard(api_client: AsyncClient):
    """STANDARD: 提交 → 200, JudgeRequest 含 language + test_cases + 内联数据"""
    _print_separator("TEST: STANDARD 提交")
    source = '#include <iostream>\nint main() { std::cout << "hello" << std::endl; return 0; }'
    payload = {"problem_id": PROBLEM_STANDARD_ID, "language_id": LANG_CPP17_ID, "source": source}
    print(f"\n[API 请求] POST /api/v1/portal/oj/submit")
    print(json.dumps(payload, indent=2, ensure_ascii=False))

    response = await api_client.post("/api/v1/portal/oj/submit", json=payload)
    data = response.json()
    print(f"\n[API 响应] HTTP {response.status_code}")
    print(json.dumps(data, indent=2, ensure_ascii=False))

    assert response.status_code == 200, str(data)
    assert data["code"] == 200
    assert data["data"]["submission_id"]
    assert data["data"]["status"] == "QUEUED"

    assert len(captured_requests) == 1
    req = captured_requests[0]
    assert req["judge_mode"] == "STANDARD"
    assert "language" in req
    assert req["language"]["key"] == "cpp17"
    assert len(req["test_cases"]) == 3
    assert req["test_cases"][0]["input_inline"] == "hello\n"
    assert req["test_cases"][2]["input_file"] == "testdata/hello.txt"
    print("  [PASS] STANDARD 提交接口: JudgeRequest 构建正确")


async def test_submit_spj(api_client: AsyncClient):
    """SPJ: 提交 → 200, JudgeRequest 含 spj + language(用户)"""
    _print_separator("TEST: SPJ 提交")
    source = '#include <iostream>\nint main() { std::cout << "ACCEPT" << std::endl; return 0; }'
    payload = {"problem_id": PROBLEM_SPJ_ID, "language_id": LANG_CPP17_ID, "source": source}
    print(f"\n[API 请求] POST /api/v1/portal/oj/submit")
    print(json.dumps(payload, indent=2, ensure_ascii=False))

    response = await api_client.post("/api/v1/portal/oj/submit", json=payload)
    data = response.json()
    print(f"\n[API 响应] HTTP {response.status_code}")
    print(json.dumps(data, indent=2, ensure_ascii=False))
    assert response.status_code == 200, str(data)

    req = captured_requests[0]
    assert req["judge_mode"] == "SPECIAL_JUDGE"
    assert "spj" in req
    assert req["spj"]["language"]["key"] == "cpp17"
    assert "source" in req["spj"]
    assert "language" in req, "SPJ 模式必须含用户 language"
    print("  [PASS] SPJ 提交接口: JudgeRequest 含 spj.source + 用户 language")


async def test_submit_interactive(api_client: AsyncClient):
    """INTERACTIVE: 提交 → 200, JudgeRequest 含 interactor + language(用户)"""
    _print_separator("TEST: INTERACTIVE 提交")
    source = '#include <iostream>\n#include <string>\nint main() { std::string name; std::getline(std::cin, name); std::cout << "Hello, " << name << "!" << std::endl; return 0; }'
    payload = {"problem_id": PROBLEM_INTERACTIVE_ID, "language_id": LANG_CPP17_ID, "source": source}
    print(f"\n[API 请求] POST /api/v1/portal/oj/submit")
    print(json.dumps(payload, indent=2, ensure_ascii=False))

    response = await api_client.post("/api/v1/portal/oj/submit", json=payload)
    data = response.json()
    print(f"\n[API 响应] HTTP {response.status_code}")
    print(json.dumps(data, indent=2, ensure_ascii=False))
    assert response.status_code == 200, str(data)

    req = captured_requests[0]
    assert req["judge_mode"] == "INTERACTIVE"
    assert "interactor" in req
    assert req["interactor"]["language"]["key"] == "cpp17"
    assert req["interactor"]["source"]
    assert req["interactor"]["time_limit_ms"] == 4000
    assert "language" in req, "INTERACTIVE 模式必须含用户 language (用户程序需要编译)"
    print("  [PASS] INTERACTIVE 提交接口: JudgeRequest 含 interactor + 用户 language")


async def test_submit_problem_not_found(api_client: AsyncClient):
    """题目不存在 → 404"""
    _print_separator("TEST: 题目不存在 → 404")
    payload = {"problem_id": "nonexistent", "language_id": LANG_CPP17_ID, "source": "int main(){}"}
    response = await api_client.post("/api/v1/portal/oj/submit", json=payload)
    assert response.status_code == 404, str(response.json())
    print(f"\n[API 响应] HTTP {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    print("  [PASS] 404 正确")


async def test_submit_unauthorized(client: AsyncClient):
    """无 token → 401"""
    _print_separator("TEST: 无 token → 401")
    payload = {"problem_id": PROBLEM_STANDARD_ID, "language_id": LANG_CPP17_ID, "source": "int main(){}"}
    response = await client.post("/api/v1/portal/oj/submit", json=payload)
    assert response.status_code == 401, str(response.json())
    print(f"\n[API 响应] HTTP {response.status_code}")
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    print("  [PASS] 401 正确")
