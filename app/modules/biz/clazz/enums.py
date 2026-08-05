from enum import StrEnum


class ClassStatus(StrEnum):
    ENABLED = "ENABLED"
    DISABLED = "DISABLED"


class ClassVisibility(StrEnum):
    PUBLIC = "PUBLIC"
    PRIVATE = "PRIVATE"


class ClassMemberRole(StrEnum):
    STUDENT = "STUDENT"
    ASSISTANT = "ASSISTANT"
