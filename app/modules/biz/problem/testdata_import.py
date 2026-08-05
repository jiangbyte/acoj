"""Import ACOJ testdata zip into oj_problem_test_case rows.

Zip is only an admin bulk packaging format. After import:
- each .in/.out pair becomes ONE row in oj_problem_test_case
- in/out are stored as independent storage objects (file keys)
- MQ / trial-judge sends those rows via build_worker_test_cases — never the zip itself

DB writes are batched in one transaction to avoid N+1 flush/commit per case.
"""

from __future__ import annotations

import asyncio
import io
import json
import re
import zipfile
from collections.abc import Iterable

from pydantic import Field
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StorageProvider
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.schema.base import ApiSchema
from app.modules.biz.problem.data.model import OjProblemData
from app.modules.biz.problem.enums import TestCaseDataMode, TestCaseType
from app.modules.biz.problem.judge_bridge import read_storage_bytes, sha256_hex
from app.modules.biz.problem.problem.model import OjProblem
from app.modules.biz.problem.test_case.model import OjProblemTestCase
from app.modules.sys.file.model import SysFile
from app.platform.db.transaction import transactional
from app.platform.id_generator.snowflake import generate_snowflake_id
from app.platform.storage.manager import get_storage, resolve_storage_config

_IGNORE_NAME_RE = re.compile(r"(^|/)\.|__MACOSX|Thumbs\.db$|Desktop\.ini$", re.I)
_NAT_SPLIT = re.compile(r"(\d+)")


def _natural_key(value: str) -> list:
    return [int(part) if part.isdigit() else part.lower() for part in _NAT_SPLIT.split(value) if part]


class ImportZipRequest(ApiSchema):
    problem_id: str = Field(min_length=1, max_length=64)
    zip_file_key: str = Field(min_length=1, max_length=512)
    replace: bool = True


class ImportZipResult(ApiSchema):
    """Import expands zip into test-case table rows (source of truth for MQ)."""

    imported: int
    case_nos: list[int]
    case_ids: list[str]
    zip_file_key: str | None = None


def _strip_junk(names: Iterable[str]) -> list[str]:
    cleaned: list[str] = []
    for name in names:
        normalized = name.replace("\\", "/").lstrip("/")
        if not normalized or normalized.endswith("/"):
            continue
        if _IGNORE_NAME_RE.search(normalized):
            continue
        cleaned.append(normalized)
    return cleaned


def _strip_single_root(names: list[str]) -> list[str]:
    if not names:
        return names
    tops = {n.split("/", 1)[0] for n in names if "/" in n}
    files_at_root = [n for n in names if "/" not in n]
    if files_at_root or len(tops) != 1:
        return names
    root = next(iter(tops))
    prefix = f"{root}/"
    return [n[len(prefix) :] for n in names if n.startswith(prefix)]


def _pair_cases(names: list[str]) -> tuple[list[tuple[str, str, str]], list[str]]:
    """Return ([(stem, in_path, out_path), ...], errors)."""
    by_stem: dict[str, dict[str, str]] = {}
    extras: list[str] = []
    for name in names:
        lower = name.lower()
        if lower.endswith(".in"):
            stem = name[: -len(".in")]
            by_stem.setdefault(stem, {})["in"] = name
        elif lower.endswith(".out"):
            stem = name[: -len(".out")]
            by_stem.setdefault(stem, {})["out"] = name
        elif lower.endswith(".ans"):
            stem = name[: -len(".ans")]
            by_stem.setdefault(stem, {})["out"] = name
        elif lower.endswith("config.json") and name.rsplit("/", 1)[-1].lower() == "config.json":
            continue
        else:
            extras.append(name)
    errors: list[str] = []
    if extras:
        errors.append(f"unsupported files: {', '.join(extras[:10])}")
    pairs: list[tuple[str, str, str]] = []
    for stem in sorted(by_stem.keys(), key=_natural_key):
        entry = by_stem[stem]
        if "in" not in entry or "out" not in entry:
            errors.append(f"unpaired stem: {stem}")
            continue
        pairs.append((stem, entry["in"], entry["out"]))
    return pairs, errors


def _load_config_points(zf: zipfile.ZipFile, names: list[str]) -> dict[str, dict]:
    config_name = next((n for n in names if n.rsplit("/", 1)[-1].lower() == "config.json"), None)
    if not config_name:
        return {}
    raw = json.loads(zf.read(config_name).decode("utf-8"))
    cases = raw.get("cases") if isinstance(raw, dict) else None
    if not isinstance(cases, list):
        return {}
    result: dict[str, dict] = {}
    for item in cases:
        if not isinstance(item, dict):
            continue
        stem = str(item.get("stem") or "").strip()
        if stem:
            result[stem] = item
    return result


class TestdataImportService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def import_zip(self, payload: ImportZipRequest) -> ImportZipResult:
        problem_id = payload.problem_id
        problem = await self.db.get(OjProblem, problem_id)
        if problem is None:
            raise NotFoundError("OjProblem not found")

        try:
            zip_bytes = read_storage_bytes(payload.zip_file_key)
        except NotFoundError as exc:
            raise BusinessError(f"zip not found in storage: {payload.zip_file_key}") from exc

        try:
            zf = zipfile.ZipFile(io.BytesIO(zip_bytes))
        except zipfile.BadZipFile as exc:
            raise BusinessError("invalid zip file") from exc

        with zf:
            member_names = _strip_single_root(_strip_junk(zf.namelist()))
            pairs, errors = _pair_cases(member_names)
            if errors or not pairs:
                raise BusinessError("; ".join(errors) if errors else "no valid .in/.out pairs")
            config_points = _load_config_points(zf, member_names)
            default_points = float(problem.points) / len(pairs) if pairs else 0.0

            storage_config = resolve_storage_config()
            storage = get_storage(storage_config.id)
            bucket = (
                storage_config.bucket
                if storage_config.provider != StorageProvider.LOCAL
                else None
            )

            # Prepare all objects in memory; upload to storage then one DB transaction.
            prepared: list[dict] = []
            upload_jobs: list[tuple[str, bytes]] = []
            for index, (stem, in_path, out_path) in enumerate(pairs, start=1):
                in_bytes = zf.read(in_path)
                out_bytes = zf.read(out_path)
                in_key = f"oj/problem/{problem_id}/testdata/{stem}.in"
                out_key = f"oj/problem/{problem_id}/testdata/{stem}.out"
                in_file_id = generate_snowflake_id()
                out_file_id = generate_snowflake_id()
                case_id = generate_snowflake_id()
                cfg = config_points.get(stem, {})
                points = cfg.get("points")
                prepared.append(
                    {
                        "case_id": case_id,
                        "case_no": index,
                        "stem": stem,
                        "in_key": in_key,
                        "out_key": out_key,
                        "in_file_id": in_file_id,
                        "out_file_id": out_file_id,
                        "in_bytes": in_bytes,
                        "out_bytes": out_bytes,
                        "in_sha": sha256_hex(in_bytes),
                        "out_sha": sha256_hex(out_bytes),
                        "points": float(points) if points is not None else default_points,
                        "is_pretest": bool(cfg.get("is_pretest", False)),
                    }
                )
                upload_jobs.append((in_key, in_bytes))
                upload_jobs.append((out_key, out_bytes))

            # Storage uploads outside DB transaction; bounded concurrency avoids serial I/O wait.
            upload_sem = asyncio.Semaphore(8)

            async def _upload_one(object_name: str, content: bytes) -> tuple[str, str]:
                async with upload_sem:
                    url = await asyncio.to_thread(
                        storage.upload_bytes,
                        object_name,
                        content,
                        "application/octet-stream",
                    )
                    return object_name, url

            urls = dict(await asyncio.gather(*(_upload_one(name, body) for name, body in upload_jobs)))

            case_nos: list[int] = []
            case_ids: list[str] = []
            async with transactional(self.db):
                if payload.replace:
                    await self.db.execute(
                        delete(OjProblemTestCase).where(OjProblemTestCase.problem_id == problem_id)
                    )

                for item in prepared:
                    self.db.add(
                        SysFile(
                            id=item["in_file_id"],
                            object_name=item["in_key"],
                            original_name=f"{item['stem']}.in",
                            storage_config_id=storage_config.id,
                            storage_provider=storage_config.provider,
                            bucket=bucket,
                            content_type="application/octet-stream",
                            size=len(item["in_bytes"]),
                            url=urls[item["in_key"]],
                        )
                    )
                    self.db.add(
                        SysFile(
                            id=item["out_file_id"],
                            object_name=item["out_key"],
                            original_name=f"{item['stem']}.out",
                            storage_config_id=storage_config.id,
                            storage_provider=storage_config.provider,
                            bucket=bucket,
                            content_type="application/octet-stream",
                            size=len(item["out_bytes"]),
                            url=urls[item["out_key"]],
                        )
                    )
                    self.db.add(
                        OjProblemTestCase(
                            id=item["case_id"],
                            problem_id=problem_id,
                            case_no=item["case_no"],
                            sort=item["case_no"],
                            case_type=TestCaseType.NORMAL,
                            data_mode=TestCaseDataMode.FILE,
                            input_file=item["in_key"],
                            output_file=item["out_key"],
                            input_sha256=item["in_sha"],
                            output_sha256=item["out_sha"],
                            points=item["points"],
                            is_pretest=item["is_pretest"],
                            batch_depends=[],
                            extra={},
                        )
                    )
                    case_nos.append(item["case_no"])
                    case_ids.append(item["case_id"])

                # Archive zip key for admin re-import only; never used in JudgePayload.
                await self._upsert_data_zip_key(problem_id, payload.zip_file_key)

        return ImportZipResult(
            imported=len(case_ids),
            case_nos=case_nos,
            case_ids=case_ids,
            zip_file_key=payload.zip_file_key,
        )

    async def _upsert_data_zip_key(self, problem_id: str, zip_key: str) -> None:
        stmt = select(OjProblemData).where(OjProblemData.problem_id == problem_id)
        data = (await self.db.execute(stmt)).scalar_one_or_none()
        if data is None:
            self.db.add(
                OjProblemData(
                    id=generate_snowflake_id(),
                    problem_id=problem_id,
                    judge_mode="STANDARD",
                    zip_object_name=zip_key,
                    extra={},
                )
            )
        else:
            data.zip_object_name = zip_key
