"""Enums for problem list / learning plan / daily."""

from enum import StrEnum


class ProblemListKind(StrEnum):
    PERSONAL = "PERSONAL"
    OFFICIAL = "OFFICIAL"


class ProblemListVisibility(StrEnum):
    PRIVATE = "PRIVATE"
    PUBLIC = "PUBLIC"


class LearningPlanCategory(StrEnum):
    FEATURED = "FEATURED"
    INTERVIEW = "INTERVIEW"
