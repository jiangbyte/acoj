from enum import StrEnum


class OjProblemType(StrEnum):
    """题目类型。"""

    PROGRAM = "PROGRAM"
    OUTPUT_ONLY = "OUTPUT_ONLY"
    FUNCTION = "FUNCTION"
    INTERACTIVE = "INTERACTIVE"
    OBJECTIVE = "OBJECTIVE"


class OjJudgeMode(StrEnum):
    """判题方式。"""

    STANDARD = "STANDARD"
    SPECIAL_JUDGE = "SPECIAL_JUDGE"
    INTERACTIVE = "INTERACTIVE"
    OUTPUT_ONLY = "OUTPUT_ONLY"
    FUNCTION = "FUNCTION"
    OBJECTIVE = "OBJECTIVE"
    REMOTE = "REMOTE"


class OjProblemVisibility(StrEnum):
    """题目可见性。"""

    PUBLIC = "PUBLIC"
    PRIVATE = "PRIVATE"
    CONTEST_ONLY = "CONTEST_ONLY"
    ORG_ONLY = "ORG_ONLY"


class OjProblemMemberRole(StrEnum):
    """题目成员角色。"""

    AUTHOR = "AUTHOR"
    CURATOR = "CURATOR"
    TESTER = "TESTER"
    VIEWER = "VIEWER"
    BANNED = "BANNED"


class OjTestCaseType(StrEnum):
    """测试点类型。"""

    NORMAL = "NORMAL"
    BATCH_START = "BATCH_START"
    BATCH_END = "BATCH_END"


class OjObjectiveAnswerType(StrEnum):
    """客观题答案类型。"""

    SINGLE = "SINGLE"
    MULTIPLE = "MULTIPLE"
    FILL = "FILL"
    JUDGE = "JUDGE"


class OjSubmitStatus(StrEnum):
    """提交状态。"""

    QUEUED = "QUEUED"
    DISPATCHED = "DISPATCHED"
    RUNNING = "RUNNING"
    JUDGING = "JUDGING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class OjJudgeResult(StrEnum):
    """判题结果。"""

    AC = "AC"
    WA = "WA"
    TLE = "TLE"
    MLE = "MLE"
    OLE = "OLE"
    RE = "RE"
    CE = "CE"
    PE = "PE"
    IE = "IE"
    SE = "SE"
    SKIPPED = "SKIPPED"
    PARTIAL = "PARTIAL"


class OjJudgeNodeStatus(StrEnum):
    """判题机状态。"""

    ENABLED = "ENABLED"
    DISABLED = "DISABLED"
    BLOCKED = "BLOCKED"


class OjJudgeTaskType(StrEnum):
    """判题任务类型。"""

    JUDGE = "JUDGE"
    REJUDGE = "REJUDGE"
    PRETEST = "PRETEST"


class OjJudgeTaskStatus(StrEnum):
    """判题任务状态。"""

    PENDING = "PENDING"
    LOCKED = "LOCKED"
    RUNNING = "RUNNING"
    DONE = "DONE"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class OjContestFormat(StrEnum):
    """比赛赛制。"""

    ICPC = "ICPC"
    IOI = "IOI"
    OI = "OI"
    ACM = "ACM"
    CUSTOM = "CUSTOM"


class OjContestVisibility(StrEnum):
    """比赛可见性。"""

    PUBLIC = "PUBLIC"
    PRIVATE = "PRIVATE"
    ORG_ONLY = "ORG_ONLY"


class OjScoreboardVisibility(StrEnum):
    """榜单可见性。"""

    VISIBLE = "VISIBLE"
    AFTER_CONTEST = "AFTER_CONTEST"
    AFTER_PARTICIPATION = "AFTER_PARTICIPATION"
    HIDDEN = "HIDDEN"


class OjContestMemberRole(StrEnum):
    """比赛成员角色。"""

    AUTHOR = "AUTHOR"
    CURATOR = "CURATOR"
    TESTER = "TESTER"
    SPECTATOR = "SPECTATOR"
    CONTESTANT = "CONTESTANT"
    BANNED = "BANNED"


class OjParticipationType(StrEnum):
    """参赛类型。"""

    LIVE = "LIVE"
    VIRTUAL = "VIRTUAL"
    SPECTATE = "SPECTATE"


class OjContentStatus(StrEnum):
    """内容状态。"""

    DRAFT = "DRAFT"
    PUBLISHED = "PUBLISHED"
    HIDDEN = "HIDDEN"
    DELETED = "DELETED"


class OjContentType(StrEnum):
    """内容格式。"""

    TEXT = "TEXT"
    MARKDOWN = "MARKDOWN"
    RICH = "RICH"


class OjCommentTargetType(StrEnum):
    """评论目标类型。"""

    PROBLEM = "PROBLEM"
    SOLUTION = "SOLUTION"
    CONTEST = "CONTEST"
    ANNOUNCEMENT = "ANNOUNCEMENT"


class OjVoteTargetType(StrEnum):
    """投票目标类型。"""

    PROBLEM = "PROBLEM"
    SOLUTION = "SOLUTION"
    COMMENT = "COMMENT"


class OjVoteType(StrEnum):
    """投票类型。"""

    LIKE = "LIKE"
    DISLIKE = "DISLIKE"


class OjFavoriteTargetType(StrEnum):
    """收藏目标类型。"""

    PROBLEM = "PROBLEM"
    SOLUTION = "SOLUTION"
    CONTEST = "CONTEST"


class OjClarificationStatus(StrEnum):
    """答疑状态。"""

    OPEN = "OPEN"
    ANSWERED = "ANSWERED"
    CLOSED = "CLOSED"


class OjAnnouncementScope(StrEnum):
    """公告范围。"""

    GLOBAL = "GLOBAL"
    CONTEST = "CONTEST"
