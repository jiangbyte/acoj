from enum import Enum


class DataScopeEnum(str, Enum):
    ALL = "ALL"
    SELF = "SELF"
    ORG = "ORG"
    ORG_AND_BELOW = "ORG_AND_BELOW"
    CUSTOM_ORG = "CUSTOM_ORG"
    GROUP = "GROUP"
    GROUP_AND_BELOW = "GROUP_AND_BELOW"
    CUSTOM_GROUP = "CUSTOM_GROUP"

    @classmethod
    def most_restrictive(cls, *scopes: str) -> str:
        priority = {"SELF": 0, "CUSTOM_GROUP": 1, "CUSTOM_ORG": 2, "GROUP_AND_BELOW": 3, "GROUP": 4, "ORG_AND_BELOW": 5, "ORG": 6, "ALL": 7}
        return min(scopes, key=lambda s: priority.get(s, 99))
