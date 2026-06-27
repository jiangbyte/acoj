from pydantic import Field

from app.core.config.enums import LoginScope
from app.core.schema.base import ApiSchema


class PermissionRegistryRouteResponse(ApiSchema):
    path: str
    methods: list[str] = Field(default_factory=list)
    login_scopes: list[LoginScope | str] = Field(default_factory=list)


class PermissionRegistryResponse(ApiSchema):
    permission_key: str
    module: str
    source: str
    methods: list[str] = Field(default_factory=list)
    login_scopes: list[LoginScope | str] = Field(default_factory=list)
    routes: list[PermissionRegistryRouteResponse] = Field(default_factory=list)
