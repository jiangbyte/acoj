"""Contest format plugin base."""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from datetime import datetime
from typing import Any


@dataclass
class ProblemScoreRow:
    contest_problem_id: str
    problem_id: str
    label: str
    points: float  # contest problem full points
    partial: bool
    best_points: float = 0.0
    attempts: int = 0
    solves: int = 0
    first_solve_seconds: int | None = None
    last_score_seconds: int | None = None
    is_solved: bool = False
    batch_best: dict[str, float] = field(default_factory=dict)
    cell: dict[str, Any] = field(default_factory=dict)


@dataclass
class SubmissionScoreInput:
    contest_problem_id: str
    problem_id: str
    points: float
    result: str | None
    created_at: datetime
    is_pretest: bool
    case_scores: list[tuple[int, float, str | None]]  # case_no, score, result
    batch_points: dict[str, float] = field(default_factory=dict)


class BaseContestFormat(ABC):
    name: str = "base"
    config_defaults: dict[str, Any] = {}

    def __init__(self, contest, config: dict[str, Any] | None):
        self.contest = contest
        self.config = {**self.config_defaults, **(config or {})}

    @abstractmethod
    def update_participation(
        self,
        *,
        real_start: datetime,
        submissions: list[SubmissionScoreInput],
        problems: list[dict[str, Any]],
    ) -> tuple[float, int, float, dict[str, Any]]:
        """Return score, cumtime(seconds), tiebreaker, format_data."""

    def sort_key(self, row: dict[str, Any]) -> tuple:
        return (
            1 if row.get("is_disqualified") else 0,
            -float(row.get("score") or 0),
            int(row.get("cumtime") or 0),
            float(row.get("tiebreaker") or 0),
        )
