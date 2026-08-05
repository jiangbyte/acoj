from enum import StrEnum


class CourseStatus(StrEnum):
    DRAFT = "DRAFT"
    PUBLISHED = "PUBLISHED"
    ARCHIVED = "ARCHIVED"


class CourseVisibility(StrEnum):
    PUBLIC = "PUBLIC"
    PRIVATE = "PRIVATE"


class CourseAccessScope(StrEnum):
    """公开课：全员可学；私有课：绑定班级。"""

    OPEN = "OPEN"
    CLASS = "CLASS"


class CourseBindingMode(StrEnum):
    """合班：一门课挂多班；分班：每班一门独立课实例。仅私有课使用。"""

    SHARED = "SHARED"
    PER_CLASS = "PER_CLASS"


class AnnouncementStatus(StrEnum):
    DRAFT = "DRAFT"
    PUBLISHED = "PUBLISHED"
    REVOKED = "REVOKED"


class TaskMode(StrEnum):
    REALTIME = "REALTIME"
    ASYNC = "ASYNC"


class TaskStatus(StrEnum):
    DRAFT = "DRAFT"
    PUBLISHED = "PUBLISHED"
    CLOSED = "CLOSED"


class ProgressStatus(StrEnum):
    NOT_STARTED = "NOT_STARTED"
    IN_PROGRESS = "IN_PROGRESS"
    DONE = "DONE"
