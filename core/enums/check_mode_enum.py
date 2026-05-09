from enum import Enum


class CheckModeEnum(str, Enum):
    AND = "AND"
    OR = "OR"

    @property
    def desc(self) -> str:
        descriptions = {
            CheckModeEnum.AND: "且",
            CheckModeEnum.OR: "或",
        }
        return descriptions.get(self, "")
