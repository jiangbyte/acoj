from enum import StrEnum


class MessageTargetScope(StrEnum):
    """消息目标范围。"""

    ALL = "ALL"
    SPECIFIC = "SPECIFIC"


class MessageContentType(StrEnum):
    """内容格式。"""

    TEXT = "TEXT"
    RICH = "RICH"


class NotificationStatus(StrEnum):
    """通知状态。"""

    DRAFT = "DRAFT"
    PUBLISHED = "PUBLISHED"
    REVOKED = "REVOKED"


class NotificationSeverity(StrEnum):
    """通知等级。"""

    INFO = "INFO"
    SUCCESS = "SUCCESS"
    WARNING = "WARNING"
    ERROR = "ERROR"


class MessageGroupStatus(StrEnum):
    """消息群组状态。"""

    ENABLED = "ENABLED"
    DISABLED = "DISABLED"


class MessageThreadType(StrEnum):
    """会话类型。"""

    DIRECT = "DIRECT"
    GROUP = "GROUP"
    SYSTEM = "SYSTEM"


class MessageThreadStatus(StrEnum):
    """会话状态。"""

    ACTIVE = "ACTIVE"
    CLOSED = "CLOSED"


class MessageSenderType(StrEnum):
    """消息发送方类型。"""

    USER = "USER"
    SYSTEM = "SYSTEM"


class TodoPriority(StrEnum):
    """待办优先级。"""

    LOW = "LOW"
    NORMAL = "NORMAL"
    HIGH = "HIGH"
    URGENT = "URGENT"


class TodoStatus(StrEnum):
    """待办状态。"""

    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"


class TodoAssigneeStatus(StrEnum):
    """待办处理状态。"""

    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"

class FriendStatus(StrEnum):
    """好友关系状态。"""

    ACTIVE = "ACTIVE"
    DELETED = "DELETED"


class FriendRequestStatus(StrEnum):
    """好友申请状态。"""

    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    REJECTED = "REJECTED"


class GroupJoinRequestStatus(StrEnum):
    """入群申请状态。"""

    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    REJECTED = "REJECTED"
