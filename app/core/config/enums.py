from enum import StrEnum


class UserType(StrEnum):
    ADMIN = "ADMIN"
    PORTAL = "PORTAL"
    APP = "APP"
    MERCHANT = "MERCHANT"
    PARTNER = "PARTNER"


class LoginScope(StrEnum):
    ADMIN = "admin"
    PORTAL = "portal"


class DataScope(StrEnum):
    ALL = "ALL"
    DEPT_AND_CHILD = "DEPT_AND_CHILD"
    DEPT = "DEPT"
    SELF = "SELF"
    CUSTOM = "CUSTOM"


class RoleScopeType(StrEnum):
    PLATFORM = "PLATFORM"
    DEPT = "DEPT"


class ResourceType(StrEnum):
    CATALOG = "CATALOG"
    MENU = "MENU"
    PAGE = "PAGE"
    BUTTON = "BUTTON"
    ACTION = "ACTION"
    API_GROUP = "API_GROUP"


class GrantSubjectType(StrEnum):
    ROLE = "ROLE"
    ACCOUNT = "ACCOUNT"
    GROUP = "GROUP"


class GrantMode(StrEnum):
    DIRECT = "DIRECT"
    CASCADE = "CASCADE"


class GrantEffect(StrEnum):
    ALLOW = "ALLOW"
    DENY = "DENY"


class StatusEnum(StrEnum):
    ENABLED = "ENABLED"
    DISABLED = "DISABLED"


class AccountStatusEnum(StrEnum):
    ENABLED = "ENABLED"
    DISABLED = "DISABLED"
    CANCELLED = "CANCELLED"
