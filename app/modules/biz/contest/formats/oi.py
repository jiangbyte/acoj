"""OI: sum of best scores per problem (partial allowed)."""

from __future__ import annotations

from datetime import datetime
from typing import Any

from app.modules.biz.contest.formats.base import BaseContestFormat, SubmissionScoreInput
from app.modules.biz.contest.lifecycle import ensure_aware


class OIContestFormat(BaseContestFormat):
    name = "oi"
    config_defaults = {"cumtime": False}

    def update_participation(
        self,
        *,
        real_start: datetime,
        submissions: list[SubmissionScoreInput],
        problems: list[dict[str, Any]],
    ) -> tuple[float, int, float, dict[str, Any]]:
        start = ensure_aware(real_start)
        best: dict[str, float] = {}
        last_time: dict[str, int] = {}
        format_data: dict[str, Any] = {}

        for sub in submissions:
            if sub.is_pretest:
                continue
            cp = sub.contest_problem_id
            prev = best.get(cp, -1.0)
            if sub.points > prev:
                best[cp] = sub.points
                last_time[cp] = max(0, int((ensure_aware(sub.created_at) - start).total_seconds()))

        score = 0.0
        cumtime = 0
        for p in problems:
            cp = p["id"]
            pts = float(best.get(cp, 0.0))
            score += pts
            cell: dict[str, Any] = {"points": pts}
            if pts > 0 and cp in last_time:
                cell["time"] = last_time[cp]
                if self.config.get("cumtime"):
                    cumtime += last_time[cp]
            format_data[cp] = cell

        tie = float(cumtime) if self.config.get("cumtime") else 0.0
        return score, cumtime if self.config.get("cumtime") else 0, tie, format_data
