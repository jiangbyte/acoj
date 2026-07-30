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
    ICPC = "icpc"
    IOI = "ioi"
    ATOCCODER = "atcoder"
    ECOLE = "ecole"
