from enum import Enum


class ResourceTypeEnum(str, Enum):
    DIRECTORY = "DIRECTORY"
    MENU = "MENU"
    BUTTON = "BUTTON"
    INTERNAL_LINK = "INTERNAL_LINK"
    EXTERNAL_LINK = "EXTERNAL_LINK"

    @property
    def desc(self) -> str:
        descriptions = {
            ResourceTypeEnum.DIRECTORY: "目录",
            ResourceTypeEnum.MENU: "菜单",
            ResourceTypeEnum.BUTTON: "按钮",
            ResourceTypeEnum.INTERNAL_LINK: "内链",
            ResourceTypeEnum.EXTERNAL_LINK: "外链",
        }
        return descriptions.get(self, "")


class ResourceCategoryEnum(str, Enum):
    BACKEND_MENU = "BACKEND_MENU"
    FRONTEND_MENU = "FRONTEND_MENU"
    BACKEND_BUTTON = "BACKEND_BUTTON"
    FRONTEND_BUTTON = "FRONTEND_BUTTON"

    @property
    def desc(self) -> str:
        descriptions = {
            ResourceCategoryEnum.BACKEND_MENU: "后台菜单",
            ResourceCategoryEnum.FRONTEND_MENU: "前台菜单",
            ResourceCategoryEnum.BACKEND_BUTTON: "后台按钮",
            ResourceCategoryEnum.FRONTEND_BUTTON: "前台按钮",
        }
        return descriptions.get(self, "")
