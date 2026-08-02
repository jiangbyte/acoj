"""IOI: max per-batch points across submissions, then sum batches."""

from __future__ import annotations

from datetime import datetime
from typing import Any

from app.modules.biz.contest.formats.base import BaseContestFormat, SubmissionScoreInput
from app.modules.biz.contest.formats.oi import OIContestFormat
from app.modules.biz.contest.lifecycle import ensure_aware


class IOIContestFormat(BaseContestFormat):
    name = "ioi"
    config_defaults = {"cumtime": False, "use_batch": True}

    def update_participation(
        self,
        *,
        real_start: datetime,
        submissions: list[SubmissionScoreInput],
        problems: list[dict[str, Any]],
    ) -> tuple[float, int, float, dict[str, Any]]:
        if not self.config.get("use_batch", True):
            return OIContestFormat(self.contest, self.config).update_participation(
                real_start=real_start, submissions=submissions, problems=problems
            )

        start = ensure_aware(real_start)
        # best[cp][batch] = points
        best: dict[str, dict[str, float]] = {}
        last_time: dict[str, int] = {}
        format_data: dict[str, Any] = {}

        for sub in submissions:
            if sub.is_pretest:
                continue
            cp = sub.contest_problem_id
            batches = sub.batch_points or {"0": sub.points}
            slot = best.setdefault(cp, {})
            improved = False
            for b, pts in batches.items():
                if pts > slot.get(b, -1.0):
                    slot[b] = pts
                    improved = True
            if improved:
                last_time[cp] = max(0, int((ensure_aware(sub.created_at) - start).total_seconds()))

        score = 0.0
        cumtime = 0
        for p in problems:
            cp = p["id"]
            batches = best.get(cp, {})
            pts = sum(batches.values()) if batches else 0.0
            # Cap at contest problem points
            pts = min(pts, float(p["points"]))
            score += pts
            cell: dict[str, Any] = {"points": pts, "batches": batches}
            if pts > 0 and cp in last_time:
                cell["time"] = last_time[cp]
                if self.config.get("cumtime"):
                    cumtime += last_time[cp]
            format_data[cp] = cell

        tie = float(cumtime) if self.config.get("cumtime") else 0.0
        return score, cumtime if self.config.get("cumtime") else 0, tie, format_data
