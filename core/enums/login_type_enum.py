from enum import Enum


class LoginTypeEnum(str, Enum):
    LOGIN = "login"
    CLIENT = "client"

    @property
    def desc(self) -> str:
        descriptions = {
            LoginTypeEnum.LOGIN: "后台登录",
            LoginTypeEnum.CLIENT: "客户端登录",
        }
        return descriptions.get(self, "")
