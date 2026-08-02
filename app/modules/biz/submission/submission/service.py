from __future__ import annotations

import asyncio
from typing import Any

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import BusinessError
from app.core.response.pagination import PageData, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema
from app.modules.biz.problem.judge_bridge import build_admin_trial_payload
from app.modules.biz.problem.problem.schema import OjProblemTrialJudgeResult
from app.modules.biz.submission.enums import SubmissionKind, SubmissionStatus
from app.modules.biz.submission.events import publish_submission_event
from app.modules.biz.submission.submission.model import OjSubmission, OjSubmissionCase, OjSubmissionSource
from app.modules.biz.submission.submission.repository import OjSubmissionRepository
from app.modules.biz.submission.submission.schema import (
    OjContestSubmissionSchema,
    OjSubmissionAdminPageQuery,
    OjSubmissionCaseSchema,
    OjSubmissionDetailSchema,
    OjSubmissionListSchema,
    OjSubmissionRejudgeRequest,
    OjSubmissionRejudgeResult,
)
from app.modules.iam.account.repository import AccountRepository
from app.modules.user.utils.profile import get_profiles_batch
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id
from app.platform.storage.url import resolve_file_url
from app.platform.tasks.celery_app import celery_app

REJUDGE_BATCH_LIMIT = 20
_TERMINAL = {SubmissionStatus.COMPLETED.value, SubmissionStatus.FAILED.value}


class OjSubmissionService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = OjSubmissionRepository(db)

    async def page_admin(self, query: OjSubmissionAdminPageQuery) -> PageData[OjSubmissionListSchema]:
        items, total = await self.repo.page_admin(query)
        schemas = await self._to_list_schemas(items)
        return build_page(query.pagination, total, schemas)

    async def detail(self, query: IdQuery) -> OjSubmissionDetailSchema:
        entity = await self.repo.get_detail_with_relations(query.id)
        return await self._to_detail_schema(entity)

    async def delete(self, payload: IdsRequest) -> None:
        async with transactional(self.db):
            await self.repo.delete_many(payload.ids)

    async def create_trial_and_judge(
        self,
        *,
        problem_id: str,
        user_id: str,
        language_key: str,
        source: str,
        case_ids: list[str] | None,
        wait_timeout_sec: int,
        wait: bool = False,
    ) -> OjProblemTrialJudgeResult:
        return await self._create_and_judge(
            problem_id=problem_id,
            user_id=user_id,
            language_key=language_key,
            source=source,
            kind=SubmissionKind.TRIAL,
            case_ids=case_ids,
            wait_timeout_sec=wait_timeout_sec,
            wait=wait,
        )

    async def create_official_and_judge(
        self,
        *,
        problem_id: str,
        user_id: str,
        language_key: str,
        source: str,
        wait_timeout_sec: int = 60,
        wait: bool = False,
    ) -> OjProblemTrialJudgeResult:
        return await self._create_and_judge(
            problem_id=problem_id,
            user_id=user_id,
            language_key=language_key,
            source=source,
            kind=SubmissionKind.OFFICIAL,
            case_ids=None,
            wait_timeout_sec=wait_timeout_sec,
            wait=wait,
        )

    async def _create_and_judge(
        self,
        *,
        problem_id: str,
        user_id: str,
        language_key: str,
        source: str,
        kind: SubmissionKind,
        case_ids: list[str] | None,
        wait_timeout_sec: int,
        wait: bool = False,
    ) -> OjProblemTrialJudgeResult:
        submission_id = generate_snowflake_id()
        judge_payload = await build_admin_trial_payload(
            self.db,
            problem_id=problem_id,
            language_key=language_key,
            source=source,
            case_ids=case_ids,
            submission_id=submission_id,
        )

        async with transactional(self.db):
            self.db.add(
                OjSubmission(
                    id=submission_id,
                    user_id=user_id,
                    problem_id=problem_id,
                    language_key=language_key,
                    kind=kind.value,
                    status=SubmissionStatus.JUDGING.value,
                    score=0,
                    time_ms=0,
                    memory_kb=0,
                    case_points=0,
                    case_total=0,
                )
            )
            self.db.add(
                OjSubmissionSource(
                    id=generate_snowflake_id(),
                    submission_id=submission_id,
                    source=source,
                )
            )
            await self.db.flush()

        self.enqueue_judge(submission_id, judge_payload)

        if wait:
            snap = await self._wait_until_terminal(submission_id, wait_timeout_sec)
            return OjProblemTrialJudgeResult.model_validate(snap)

        return OjProblemTrialJudgeResult.model_validate(await self.snapshot_for_events(submission_id))

    async def rejudge(self, payload: OjSubmissionRejudgeRequest) -> OjSubmissionRejudgeResult:
        ids = list(dict.fromkeys(payload.ids))
        if len(ids) > REJUDGE_BATCH_LIMIT:
            raise BusinessError(f"一次最多重判 {REJUDGE_BATCH_LIMIT} 条")

        result = OjSubmissionRejudgeResult()
        for submission_id in ids:
            try:
                detail = await self.repo.get_detail_with_relations(submission_id)
                if detail.locked_at is not None:
                    raise BusinessError(f"{submission_id} 已锁定，无法重判")
                if detail.source_row is None or not detail.source_row.source:
                    raise BusinessError(f"{submission_id} 缺少源码")

                judge_payload = await build_admin_trial_payload(
                    self.db,
                    problem_id=detail.problem_id,
                    language_key=detail.language_key,
                    source=detail.source_row.source,
                    case_ids=None,
                    submission_id=detail.id,
                )

                async with transactional(self.db):
                    entity = await self.repo.get_required(submission_id)
                    entity.status = SubmissionStatus.JUDGING.value
                    entity.result = None
                    entity.error = None
                    entity.score = 0
                    entity.time_ms = 0
                    entity.memory_kb = 0
                    entity.compile_output = None
                    await self.repo.replace_cases(submission_id, [])
                    await self.db.flush()

                self.enqueue_judge(submission_id, judge_payload)
                result.queued += 1
            except Exception as exc:  # noqa: BLE001
                result.failed += 1
                result.errors.append(f"{submission_id}: {exc}")
                async with transactional(self.db):
                    entity = await self.repo.get_by_id(submission_id)
                    if entity is not None:
                        entity.status = SubmissionStatus.FAILED.value
                        entity.error = str(exc)
                        await self.db.flush()
                await publish_submission_event(
                    submission_id, await self.snapshot_for_events(submission_id)
                )

        return result

    def enqueue_judge(self, submission_id: str, judge_payload: dict[str, Any]) -> str:
        """Enqueue judge.execute; link/link_error apply on acoj_api (no slot held while judging)."""
        from app.modules.biz.submission.submission.tasks import (
            apply_failure_signature,
            apply_success_signature,
        )

        async_result = celery_app.send_task(
            "judge.execute",
            args=[judge_payload],
            queue="judge",
            # Result travels in the link message; skip Redis result-backend write (large cases).
            ignore_result=True,
            link=apply_success_signature(submission_id),
            link_error=apply_failure_signature(submission_id),
        )
        return str(async_result.id)

    async def apply_judge_result(self, submission_id: str, raw: dict[str, Any]) -> None:
        entity = await self.repo.get_required(submission_id)
        status = str(raw.get("status") or SubmissionStatus.COMPLETED.value)
        if status not in {s.value for s in SubmissionStatus}:
            status = SubmissionStatus.COMPLETED.value
        entity.status = status
        if raw.get("error") and not raw.get("result"):
            entity.status = SubmissionStatus.FAILED.value
        elif status not in _TERMINAL:
            entity.status = SubmissionStatus.COMPLETED.value
        entity.result = raw.get("result")
        entity.score = float(raw.get("score") or 0)
        entity.time_ms = int(raw.get("time_ms") or 0)
        entity.memory_kb = int(raw.get("memory_kb") or 0)
        entity.compile_output = raw.get("compile_output")
        entity.error = raw.get("error")

        case_rows: list[OjSubmissionCase] = []
        case_points = 0.0
        cases = raw.get("cases") or []
        for item in cases:
            if not isinstance(item, dict):
                continue
            points = float(item.get("points") or item.get("score") or 0)
            case_points += points
            case_rows.append(
                OjSubmissionCase(
                    id=generate_snowflake_id(),
                    submission_id=submission_id,
                    case_no=int(item.get("case_no") or 0),
                    result=item.get("result") or item.get("status"),
                    score=points,
                    time_ms=int(item.get("time_ms") or item.get("time") or 0),
                    memory_kb=int(item.get("memory_kb") or item.get("memory") or 0),
                    stdout_preview=item.get("stdout_preview"),
                    stderr_preview=item.get("stderr_preview"),
                    feedback=item.get("feedback"),
                )
            )
        entity.case_points = case_points
        case_total = float(raw.get("case_total") or raw.get("max_score") or 0)
        if not case_total:
            for item in cases:
                if not isinstance(item, dict):
                    continue
                case_total += float(item.get("max_points") or item.get("total") or 0)
        entity.case_total = case_total if case_total else 0
        await self.repo.replace_cases(submission_id, case_rows)
        await self.db.flush()

        # Contest hot path: update contest_submission.points + participation scoreboard fields.
        from app.modules.biz.contest.scoring import apply_contest_submission_result

        await apply_contest_submission_result(self.db, submission_id)

    async def snapshot_for_events(self, submission_id: str) -> dict[str, Any]:
        detail = await self.detail(IdQuery(id=submission_id))
        return {
            "submission_id": detail.id,
            "status": detail.status,
            "result": detail.result,
            "score": detail.score,
            "time_ms": detail.time_ms,
            "memory_kb": detail.memory_kb,
            "compile_output": detail.compile_output,
            "compile_error": bool(detail.result == "CE" or (detail.error and detail.status == SubmissionStatus.FAILED.value)),
            "cases": [
                {
                    "case_no": c.case_no,
                    "result": c.result,
                    "points": c.score,
                    "score": c.score,
                    "time_ms": c.time_ms,
                    "memory_kb": c.memory_kb,
                    "stdout_preview": c.stdout_preview or "",
                    "stderr_preview": c.stderr_preview or "",
                }
                for c in detail.cases
            ],
            "error": detail.error,
            "wall_time_ms": 0,
        }

    async def _wait_until_terminal(self, submission_id: str, wait_timeout_sec: int) -> dict[str, Any]:
        """Optional wait by polling DB (link callback writes result; event loop stays free)."""
        deadline = asyncio.get_running_loop().time() + wait_timeout_sec
        while asyncio.get_running_loop().time() < deadline:
            snap = await self.snapshot_for_events(submission_id)
            if snap.get("status") in _TERMINAL:
                return snap
            await asyncio.sleep(0.4)
        raise BusinessError("Judge timed out waiting for worker")

    async def _to_detail_schema(self, entity: OjSubmission) -> OjSubmissionDetailSchema:
        base = (await self._to_list_schemas([entity]))[0]
        source = entity.source_row.source if entity.source_row is not None else None
        cases = [to_schema(OjSubmissionCaseSchema, case) for case in (entity.cases or [])]
        contest_sub = (
            to_schema(OjContestSubmissionSchema, entity.contest_submission)
            if entity.contest_submission is not None
            else None
        )
        return OjSubmissionDetailSchema(
            **base.model_dump(),
            compile_output=entity.compile_output,
            error=entity.error,
            source=source,
            cases=cases,
            contest_submission=contest_sub,
        )

    async def _to_list_schemas(self, items: list[OjSubmission]) -> list[OjSubmissionListSchema]:
        if not items:
            return []
        problem_map = await self.repo.map_problem_labels([item.problem_id for item in items])
        contest_map = await self.repo.map_contest_labels(
            [item.contest_id for item in items if item.contest_id]
        )
        user_ids = [item.user_id for item in items]
        profile_map = await self._batch_user_profiles(user_ids)
        schemas: list[OjSubmissionListSchema] = []
        for item in items:
            p_code, p_name = problem_map.get(item.problem_id, (None, None))
            c_key, c_name = (
                contest_map.get(item.contest_id, (None, None)) if item.contest_id else (None, None)
            )
            profile = profile_map.get(item.user_id) or {}
            schemas.append(
                OjSubmissionListSchema(
                    id=item.id,
                    user_id=item.user_id,
                    user_nickname=profile.get("nickname"),
                    user_avatar=profile.get("avatar"),
                    user_account_type=profile.get("account_type"),
                    problem_id=item.problem_id,
                    problem_code=p_code,
                    problem_name=p_name,
                    language_key=item.language_key,
                    kind=item.kind,
                    status=item.status,
                    result=item.result,
                    score=item.score,
                    time_ms=item.time_ms,
                    memory_kb=item.memory_kb,
                    contest_id=item.contest_id,
                    contest_key=c_key,
                    contest_name=c_name,
                    case_points=item.case_points,
                    case_total=item.case_total,
                    locked_at=item.locked_at,
                    created_at=item.created_at,
                    updated_at=item.updated_at,
                )
            )
        return schemas

    async def _batch_user_profiles(self, user_ids: list[str]) -> dict[str, dict[str, str | None]]:
        """Resolve avatar/nickname by SysAccount.account_type (ADMIN / PORTAL)."""
        unique_ids = list(dict.fromkeys(uid for uid in user_ids if uid))
        if not unique_ids:
            return {}
        accounts = await AccountRepository(self.db).list_accounts_by_ids(unique_ids)
        type_by_id = {account.id: account.account_type for account in accounts}

        groups: dict[str, list[str]] = {}
        for account_id, account_type in type_by_id.items():
            if account_type:
                groups.setdefault(account_type, []).append(account_id)

        result: dict[str, dict[str, str | None]] = {
            account_id: {
                "account_type": account_type,
                "nickname": None,
                "avatar": None,
            }
            for account_id, account_type in type_by_id.items()
        }
        for account_type_str, account_ids in groups.items():
            try:
                account_type = AccountType(account_type_str)
            except ValueError:
                continue
            profiles = await get_profiles_batch(self.db, account_type, account_ids)
            for account_id, profile in profiles.items():
                result[account_id] = {
                    "account_type": account_type_str,
                    "nickname": getattr(profile, "nickname", None) or getattr(profile, "name", None),
                    "avatar": resolve_file_url(getattr(profile, "avatar", None)),
                }
        return result
