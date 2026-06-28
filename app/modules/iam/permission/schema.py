from pydantic import Field

from app.core.config.enums import AccountType
from app.core.schema.base import ApiSchema


class PermissionRegistryRouteResponse(ApiSchema):
    path: str
    methods: list[str] = Field(default_factory=list)
    account_types: list[AccountType | str] = Field(default_factory=list)


class PermissionRegistryResponse(ApiSchema):
    permission_key: str
    module: str
    source: str
    methods: list[str] = Field(default_factory=list)
    account_types: list[AccountType | str] = Field(default_factory=list)
    routes: list[PermissionRegistryRouteResponse] = Field(default_factory=list)
