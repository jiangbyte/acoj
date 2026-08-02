"""ICPC/ACM/AtCoder: solve count + penalty minutes."""

from __future__ import annotations

from datetime import datetime
from typing import Any

from app.modules.biz.contest.formats.base import BaseContestFormat, SubmissionScoreInput
from app.modules.biz.contest.lifecycle import ensure_aware

_PENALTY_RESULTS = {"WA", "TLE", "MLE", "RE", "OLE", "SE"}


class ICPCContestFormat(BaseContestFormat):
    name = "icpc"
    config_defaults = {"penalty_minutes": 20}

    def update_participation(
        self,
        *,
        real_start: datetime,
        submissions: list[SubmissionScoreInput],
        problems: list[dict[str, Any]],
    ) -> tuple[float, int, float, dict[str, Any]]:
        start = ensure_aware(real_start)
        penalty_min = int(self.config.get("penalty_minutes") or 20)
        full_by_cp = {p["id"]: float(p["points"]) for p in problems}

        # per problem: wrong before AC, AC time
        wrong: dict[str, int] = {p["id"]: 0 for p in problems}
        ac_time: dict[str, int] = {}
        format_data: dict[str, Any] = {}

        ordered = sorted(submissions, key=lambda s: ensure_aware(s.created_at))
        for sub in ordered:
            if sub.is_pretest:
                continue
            cp = sub.contest_problem_id
            if cp in ac_time:
                continue
            full = full_by_cp.get(cp, 0.0)
            secs = max(0, int((ensure_aware(sub.created_at) - start).total_seconds()))
            solved = full > 0 and abs(sub.points - full) < 1e-9
            if solved:
                ac_time[cp] = secs
            elif (sub.result or "").upper() in _PENALTY_RESULTS or sub.points < full:
                if (sub.result or "").upper() != "CE":
                    wrong[cp] = wrong.get(cp, 0) + 1

        solves = 0
        cumtime = 0
        last_ac = 0
        for p in problems:
            cp = p["id"]
            if cp in ac_time:
                solves += 1
                t = ac_time[cp]
                pen = wrong.get(cp, 0) * penalty_min * 60
                total = t + pen
                cumtime += total
                last_ac = max(last_ac, t)
                format_data[cp] = {
                    "solved": True,
                    "time": t,
                    "penalty": wrong.get(cp, 0),
                    "display": total // 60,
                }
            else:
                format_data[cp] = {
                    "solved": False,
                    "penalty": wrong.get(cp, 0),
                    "display": None,
                }

        return float(solves), cumtime, float(last_ac), format_data


class ACMContestFormat(ICPCContestFormat):
    name = "acm"


class AtCoderContestFormat(ICPCContestFormat):
    name = "atcoder"
    config_defaults = {"penalty_minutes": 5}
