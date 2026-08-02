from enum import StrEnum


class ProblemStatus(StrEnum):
    """题目发布状态。"""

    DRAFT = "draft"
    READY = "ready"
    PUBLISHED = "published"


class ProblemStaffRole(StrEnum):
    """题目人员角色。"""

    AUTHOR = "AUTHOR"
    CURATOR = "CURATOR"
    TESTER = "TESTER"


class SubmissionSourceVisibility(StrEnum):
    """提交源码可见性。"""

    FOLLOW = "FOLLOW"
    ALWAYS = "ALWAYS"
    SOLVED = "SOLVED"
    ONLY_OWN = "ONLY_OWN"


class TestCaseType(StrEnum):
    """测试点类型。"""

    NORMAL = "NORMAL"
    BATCH_START = "BATCH_START"
    BATCH_END = "BATCH_END"


class TestCaseDataMode(StrEnum):
    """测例数据来源。"""

    FILE = "file"
    INLINE = "inline"


class JudgeMode(StrEnum):
    """Worker 判题模式（与 acoj-worker MODE_REGISTRY 对齐）。"""

    STANDARD = "STANDARD"
    SPECIAL_JUDGE = "SPECIAL_JUDGE"
    INTERACTIVE = "INTERACTIVE"
