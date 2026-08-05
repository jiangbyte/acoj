from enum import StrEnum


class TeamScope(StrEnum):
    INDEPENDENT = "INDEPENDENT"
    COURSE = "COURSE"


class TeamStatus(StrEnum):
    ENABLED = "ENABLED"
    DISABLED = "DISABLED"
    DISSOLVED = "DISSOLVED"


class TeamVisibility(StrEnum):
    PUBLIC = "PUBLIC"
    PRIVATE = "PRIVATE"


class TeamMemberRole(StrEnum):
    OWNER = "OWNER"
    ADMIN = "ADMIN"
    MEMBER = "MEMBER"
