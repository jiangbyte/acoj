from enum import Enum


class LoginTypeEnum(str, Enum):
    BUSINESS = "BUSINESS"
    CONSUMER = "CONSUMER"

    @property
    def desc(self) -> str:
        descriptions = {
            LoginTypeEnum.BUSINESS: "后台登录",
            LoginTypeEnum.CONSUMER: "客户端登录",
        }
        return descriptions.get(self, "")
