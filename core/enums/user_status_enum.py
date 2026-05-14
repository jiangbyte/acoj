from enum import Enum


class UserStatusEnum(str, Enum):
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    LOCKED = "LOCKED"

    @property
    def desc(self) -> str:
        descriptions = {
            UserStatusEnum.ACTIVE: "正常",
            UserStatusEnum.INACTIVE: "停用",
            UserStatusEnum.LOCKED: "锁定",
        }
        return descriptions.get(self, "")
