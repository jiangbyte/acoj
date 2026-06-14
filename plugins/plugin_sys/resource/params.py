from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class ModuleVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    icon: Optional[str] = None
    color: Optional[str] = None
    description: Optional[str] = None
    is_visible: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class ResourceVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    category: Optional[str] = None
    type: Optional[str] = None
    description: Optional[str] = None
    parent_id: Optional[str] = None
    route_path: Optional[str] = None
    component_path: Optional[str] = None
    redirect_path: Optional[str] = None
    icon: Optional[str] = None
    color: Optional[str] = None
    is_visible: Optional[str] = None
    is_cache: Optional[str] = None
    is_affix: Optional[str] = None
    is_breadcrumb: Optional[str] = None
    external_url: Optional[str] = None
    extra: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None

    children: List["ResourceVO"] = Field(default_factory=list)


class ResourcePageParam(BaseModel):
    current: int = 1
    size: int = 10


class ResourceMenuVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    type: Optional[str] = None
    category: Optional[str] = None
    description: Optional[str] = None
    parent_id: Optional[str] = None
    route_path: Optional[str] = None
    component_path: Optional[str] = None
    redirect_path: Optional[str] = None
    icon: Optional[str] = None
    color: Optional[str] = None
    is_visible: Optional[str] = None
    is_cache: Optional[str] = None
    is_affix: Optional[str] = None
    is_breadcrumb: Optional[str] = None
    external_url: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["ResourceMenuVO"] = Field(default_factory=list)


class ModulePageParam(BaseModel):
    current: int = 1
    size: int = 10
