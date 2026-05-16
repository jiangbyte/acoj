from enum import Enum


class StatusEnum(str, Enum):
    YES = "YES"
    NO = "NO"
    ENABLED = "ENABLED"
    DISABLED = "DISABLED"

    @property
    def desc(self) -> str:
        descriptions = {
            StatusEnum.YES: "是",
            StatusEnum.NO: "否",
            StatusEnum.ENABLED: "启用",
            StatusEnum.DISABLED: "禁用",
        }
        return descriptions.get(self, "")
