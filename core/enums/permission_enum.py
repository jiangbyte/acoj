from enum import Enum


class PermissionCategoryEnum(str, Enum):
    BACKEND = "BACKEND"
    FRONTEND = "FRONTEND"

    @property
    def desc(self) -> str:
        descriptions = {
            PermissionCategoryEnum.BACKEND: "后端权限",
            PermissionCategoryEnum.FRONTEND: "前端权限",
        }
        return descriptions.get(self, "")


class PermissionScopeEnum(str, Enum):
    ALL = "ALL"
    ORG = "ORG"
    ORG_AND_BELOW = "ORG_AND_BELOW"
    SELF = "SELF"

    @property
    def desc(self) -> str:
        descriptions = {
            PermissionScopeEnum.ALL: "全部",
            PermissionScopeEnum.ORG: "本组织",
            PermissionScopeEnum.ORG_AND_BELOW: "本组织及以下",
            PermissionScopeEnum.SELF: "本人",
        }
        return descriptions.get(self, "")
