"""Smoke tests for submission performance routes (no live DB required).

Uses mocked repo rows to exercise service unavailable paths and direct route
handler wiring — proves practice AC, contest AC (portal vs admin), and non-AC
return HTTP-success payloads with correct ``available`` without 500.
"""

from __future__ import annotations

from datetime import UTC, datetime
from unittest.mock import AsyncMock, MagicMock

import pytest

from app.core.schema.base import IdQuery
from app.modules.biz.submission.enums import SubmissionKind, SubmissionResult, SubmissionStatus
from app.modules.biz.submission.performance.schema import SimilarSubmissionQuery
from app.modules.biz.submission.performance.service import SubmissionPerformanceService
from app.modules.biz.submission.portal.router import submission_performance, submission_similar
from app.modules.biz.submission.submission.router import performance as admin_performance


def _submission(
    *,
    sub_id: str,
    kind: SubmissionKind,
    result: SubmissionResult,
    contest_id: str | None = None,
) -> MagicMock:
    row = MagicMock()
    row.id = sub_id
    row.user_id = "user-1"
    row.problem_id = "problem-1"
    row.language_key = "cpp"
    row.kind = kind.value
    row.status = SubmissionStatus.COMPLETED.value
    row.result = result.value
    row.time_ms = 100
    row.memory_kb = 1024
    row.contest_id = contest_id
    row.created_at = datetime(2026, 1, 1, tzinfo=UTC)
    return row


def _service_with(*submissions: MagicMock) -> SubmissionPerformanceService:
    by_id = {s.id: s for s in submissions}
    db = MagicMock()
    svc = SubmissionPerformanceService(db)
    svc.repo = AsyncMock()
    svc.repo.get_by_id = AsyncMock(side_effect=lambda sid: by_id.get(sid))
    svc._load_pool_rows = AsyncMock(return_value=list(submissions))
    return svc


@pytest.mark.asyncio
async def test_service_practice_ac_available() -> None:
    sub = _submission(sub_id="practice-ac", kind=SubmissionKind.OFFICIAL, result=SubmissionResult.AC)
    svc = _service_with(sub)
    out = await svc.get_performance(IdQuery(id="practice-ac"), viewer=None, for_admin=False)
    assert out.available is True
    assert out.scope == "practice"
    assert out.insufficient_sample is True


@pytest.mark.asyncio
async def test_service_portal_contest_ac_unavailable() -> None:
    sub = _submission(
        sub_id="contest-ac",
        kind=SubmissionKind.CONTEST,
        result=SubmissionResult.AC,
        contest_id="contest-1",
    )
    svc = _service_with(sub)
    out = await svc.get_performance(IdQuery(id="contest-ac"), viewer=None, for_admin=False)
    assert out.available is False
    assert "竞赛" in (out.reason or "")


@pytest.mark.asyncio
async def test_service_admin_contest_ac_available() -> None:
    sub = _submission(
        sub_id="contest-ac",
        kind=SubmissionKind.CONTEST,
        result=SubmissionResult.AC,
        contest_id="contest-1",
    )
    svc = _service_with(sub)
    out = await svc.get_performance(IdQuery(id="contest-ac"), viewer=None, for_admin=True)
    assert out.available is True
    assert out.scope == "contest"
    assert out.contest_id == "contest-1"


@pytest.mark.asyncio
async def test_service_non_ac_unavailable() -> None:
    sub = _submission(sub_id="non-ac", kind=SubmissionKind.OFFICIAL, result=SubmissionResult.WA)
    svc = _service_with(sub)
    perf = await svc.get_performance(IdQuery(id="non-ac"), viewer=None, for_admin=False)
    similar = await svc.list_similar(
        SimilarSubmissionQuery(id="non-ac", size=5), viewer=None, for_admin=False
    )
    assert perf.available is False
    assert "AC" in (perf.reason or "")
    assert similar.available is False
    assert similar.items == []


@pytest.mark.asyncio
async def test_portal_route_handlers_return_success_wrapper(monkeypatch: pytest.MonkeyPatch) -> None:
    practice = _submission(sub_id="practice-ac", kind=SubmissionKind.OFFICIAL, result=SubmissionResult.AC)
    contest = _submission(
        sub_id="contest-ac",
        kind=SubmissionKind.CONTEST,
        result=SubmissionResult.AC,
        contest_id="contest-1",
    )
    non_ac = _submission(sub_id="non-ac", kind=SubmissionKind.OFFICIAL, result=SubmissionResult.WA)
    svc = _service_with(practice, contest, non_ac)
    monkeypatch.setattr(
        "app.modules.biz.submission.portal.router.SubmissionPerformanceService",
        lambda _db: svc,
    )
    db = MagicMock()

    practice_resp = await submission_performance(IdQuery(id="practice-ac"), db, None)
    contest_resp = await submission_performance(IdQuery(id="contest-ac"), db, None)
    non_ac_resp = await submission_performance(IdQuery(id="non-ac"), db, None)
    similar_resp = await submission_similar(SimilarSubmissionQuery(id="non-ac", size=5), db, None)

    assert practice_resp.code == 200 and practice_resp.data.available is True
    assert contest_resp.code == 200 and contest_resp.data.available is False
    assert non_ac_resp.code == 200 and non_ac_resp.data.available is False
    assert similar_resp.code == 200 and similar_resp.data.available is False


@pytest.mark.asyncio
async def test_admin_route_handler_contest_ac(monkeypatch: pytest.MonkeyPatch) -> None:
    contest = _submission(
        sub_id="contest-ac",
        kind=SubmissionKind.CONTEST,
        result=SubmissionResult.AC,
        contest_id="contest-1",
    )
    svc = _service_with(contest)
    monkeypatch.setattr(
        "app.modules.biz.submission.submission.router.SubmissionPerformanceService",
        lambda _db: svc,
    )
    resp = await admin_performance(IdQuery(id="contest-ac"), MagicMock())
    assert resp.code == 200
    assert resp.data.available is True
    assert resp.data.scope == "contest"
