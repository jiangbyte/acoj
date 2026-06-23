from enum import StrEnum


class JudgeNodeStatus(StrEnum):
    ONLINE = "ONLINE"
    OFFLINE = "OFFLINE"
    DISABLED = "DISABLED"


class JudgeTaskStatus(StrEnum):
    PENDING = "PENDING"
    DISPATCHED = "DISPATCHED"
    RUNNING = "RUNNING"
    FINISHED = "FINISHED"
    FAILED = "FAILED"
    ABORTED = "ABORTED"


class SubmissionStatus(StrEnum):
    PENDING = "PENDING"
    QUEUED = "QUEUED"
    JUDGING = "JUDGING"
    AC = "AC"
    WA = "WA"
    TLE = "TLE"
    MLE = "MLE"
    RTE = "RTE"
    OLE = "OLE"
    CE = "CE"
    SE = "SE"
    PA = "PA"
    SC = "SC"
    ABORTED = "ABORTED"

