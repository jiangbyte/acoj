from enum import Enum


class DataScopeEnum(str, Enum):
    ALL = "ALL"
    CUSTOM = "CUSTOM"
    ORG = "ORG"
    ORG_AND_BELOW = "ORG_AND_BELOW"
    SELF = "SELF"

    @classmethod
    def most_restrictive(cls, *scopes: str) -> str:
        priority = {"SELF": 0, "CUSTOM": 1, "ORG_AND_BELOW": 2, "ORG": 3, "ALL": 4}
        return min(scopes, key=lambda s: priority.get(s, 99))
