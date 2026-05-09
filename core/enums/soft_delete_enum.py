from enum import Enum


class SoftDeleteEnum(str, Enum):
    NO = "NO"
    YES = "YES"

    @property
    def desc(self) -> str:
        descriptions = {
            SoftDeleteEnum.NO: "否",
            SoftDeleteEnum.YES: "是",
        }
        return descriptions.get(self, "")
