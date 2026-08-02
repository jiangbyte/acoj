from enum import StrEnum


class SubmissionKind(StrEnum):
    """提交类型。"""

    OFFICIAL = "OFFICIAL"
    TRIAL = "TRIAL"
    CONTEST = "CONTEST"


class SubmissionStatus(StrEnum):
    """判题流水线状态。"""

    QUEUED = "QUEUED"
    JUDGING = "JUDGING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


class SubmissionResult(StrEnum):
    """终态 verdict（与 worker 对齐的常用码）。"""

    AC = "AC"
    WA = "WA"
    TLE = "TLE"
    MLE = "MLE"
    RE = "RE"
    CE = "CE"
    OLE = "OLE"
    SE = "SE"
    IE = "IE"
