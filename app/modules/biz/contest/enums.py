from enum import IntEnum, StrEnum


class ContestStaffRole(StrEnum):
    """竞赛人员角色。"""

    AUTHOR = "AUTHOR"
    CURATOR = "CURATOR"
    TESTER = "TESTER"
    SPECTATOR = "SPECTATOR"


class ScoreboardVisibility(StrEnum):
    """榜单可见性。"""

    VISIBLE = "VISIBLE"
    AFTER_CONTEST = "AFTER_CONTEST"
    AFTER_PARTICIPATION = "AFTER_PARTICIPATION"
    HIDDEN = "HIDDEN"


class ContestParticipationVirtual(IntEnum):
    """参赛虚拟状态。"""

    SPECTATE = -1
    LIVE = 0


class ContestFormat(StrEnum):
    """竞赛赛制。"""

    DEFAULT = "default"
    ACM = "acm"
    ICPC = "icpc"
    ATCODER = "atcoder"
    OI = "oi"
    IOI = "ioi"


class ContestLifecycleStatus(StrEnum):
    """竞赛生命周期（计算得出，不落库）。"""

    SCHEDULED = "SCHEDULED"
    RUNNING = "RUNNING"
    ENDED = "ENDED"
    LOCKED = "LOCKED"


class ClarificationThreadStatus(StrEnum):
    """答疑提问线程状态。"""

    OPEN = "OPEN"
    ANSWERED = "ANSWERED"
    CLOSED = "CLOSED"
