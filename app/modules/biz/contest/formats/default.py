"""DEFAULT: sum of best contest points per problem."""

from __future__ import annotations

from datetime import datetime
from typing import Any

from app.modules.biz.contest.formats.base import BaseContestFormat, SubmissionScoreInput
from app.modules.biz.contest.lifecycle import ensure_aware


class DefaultContestFormat(BaseContestFormat):
    name = "default"

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
            prev = best.get(cp, 0.0)
            if sub.points >= prev:
                best[cp] = sub.points
                secs = max(0, int((ensure_aware(sub.created_at) - start).total_seconds()))
                last_time[cp] = secs

        score = 0.0
        cumtime = 0
        for p in problems:
            cp = p["id"]
            pts = float(best.get(cp, 0.0))
            score += pts
            cell = {"points": pts}
            if pts > 0 and cp in last_time:
                cumtime += last_time[cp]
                cell["time"] = last_time[cp]
            format_data[cp] = cell

        return score, cumtime, float(cumtime), format_data
