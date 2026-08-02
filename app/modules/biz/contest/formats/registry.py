"""Contest format registry."""

from __future__ import annotations

from typing import Any

from app.core.exceptions.business import BusinessError
from app.modules.biz.contest.enums import ContestFormat
from app.modules.biz.contest.formats.base import BaseContestFormat
from app.modules.biz.contest.formats.default import DefaultContestFormat
from app.modules.biz.contest.formats.icpc import ACMContestFormat, AtCoderContestFormat, ICPCContestFormat
from app.modules.biz.contest.formats.ioi import IOIContestFormat
from app.modules.biz.contest.formats.oi import OIContestFormat

_FORMATS: dict[str, type[BaseContestFormat]] = {
    ContestFormat.DEFAULT.value: DefaultContestFormat,
    ContestFormat.ACM.value: ACMContestFormat,
    ContestFormat.ICPC.value: ICPCContestFormat,
    ContestFormat.ATCODER.value: AtCoderContestFormat,
    ContestFormat.OI.value: OIContestFormat,
    ContestFormat.IOI.value: IOIContestFormat,
}


def get_format(contest, format_name: str | None = None, config: dict[str, Any] | None = None) -> BaseContestFormat:
    name = (format_name or getattr(contest, "format_name", None) or ContestFormat.DEFAULT.value).lower()
    # legacy aliases
    if name in {"atocoder", "ATOCCODER"}:
        name = ContestFormat.ATCODER.value
    if name in {"ecole", "ecoo"}:
        name = ContestFormat.DEFAULT.value
    cls = _FORMATS.get(name)
    if cls is None:
        raise BusinessError(f"不支持的赛制: {name}")
    cfg = config if config is not None else (getattr(contest, "format_config", None) or {})
    return cls(contest, cfg)


def list_formats() -> list[dict[str, Any]]:
    return [
        {"value": ContestFormat.DEFAULT.value, "label": "Default（最高分求和）"},
        {"value": ContestFormat.ACM.value, "label": "ACM（过题+罚时）"},
        {"value": ContestFormat.ICPC.value, "label": "ICPC（过题+罚时20）"},
        {"value": ContestFormat.ATCODER.value, "label": "AtCoder（过题+罚时5）"},
        {"value": ContestFormat.OI.value, "label": "OI（部分分）"},
        {"value": ContestFormat.IOI.value, "label": "IOI（Subtask/Batch）"},
    ]
