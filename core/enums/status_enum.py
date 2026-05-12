from enum import Enum


class StatusEnum(str, Enum):
    YES = "YES"
    NO = "NO"
    ACTIVE = "ACTIVE"
    ENABLE = "ENABLED"
    DISABLE = "DISABLE"

    @property
    def desc(self) -> str:
        descriptions = {
            StatusEnum.YES: "是",
            StatusEnum.NO: "否",
            StatusEnum.ACTIVE: "正常",
            StatusEnum.ENABLE: "启用",
            StatusEnum.DISABLE: "禁用",
        }
        return descriptions.get(self, "")
