from typing import Optional, List, Any, Dict
from datetime import datetime
from pydantic import BaseModel, ConfigDict, field_validator, model_serializer
from core.pojo import PageBounds


class ModuleVO(BaseModel):
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
    is_deleted: Optional[str] = "NO"
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None

    @field_validator('created_at', 'updated_at', mode='before')
    @classmethod
    def parse_datetime(cls, v):
        if v is None:
            return None
        if isinstance(v, datetime):
            return v
        if isinstance(v, str):
            formats = ['%Y-%m-%d %H:%M:%S', '%Y-%m-%dT%H:%M:%S', '%Y-%m-%d']
            for fmt in formats:
                try:
                    return datetime.strptime(v, fmt)
                except ValueError:
                    continue
            raise ValueError(f"Invalid datetime format: {v}. Expected 'YYYY-MM-DD HH:MM:SS'")
        return v

    @model_serializer
    def serialize(self) -> Dict[str, Any]:
        result = {}
        for field_name, field_info in self.model_fields.items():
            value = getattr(self, field_name)
            if isinstance(value, datetime):
                result[field_name] = value.strftime('%Y-%m-%d %H:%M:%S')
            else:
                result[field_name] = value
        return result


class ResourceVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    type: str
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
    is_hidden: Optional[str] = None
    is_breadcrumb: Optional[str] = None
    external_url: Optional[str] = None
    extra: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    is_deleted: Optional[str] = "NO"
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None

    @field_validator('created_at', 'updated_at', mode='before')
    @classmethod
    def parse_datetime(cls, v):
        if v is None:
            return None
        if isinstance(v, datetime):
            return v
        if isinstance(v, str):
            formats = ['%Y-%m-%d %H:%M:%S', '%Y-%m-%dT%H:%M:%S', '%Y-%m-%d']
            for fmt in formats:
                try:
                    return datetime.strptime(v, fmt)
                except ValueError:
                    continue
            raise ValueError(f"Invalid datetime format: {v}. Expected 'YYYY-MM-DD HH:MM:SS'")
        return v

    @model_serializer
    def serialize(self) -> Dict[str, Any]:
        result = {}
        for field_name, field_info in self.model_fields.items():
            value = getattr(self, field_name)
            if isinstance(value, datetime):
                result[field_name] = value.strftime('%Y-%m-%d %H:%M:%S')
            else:
                result[field_name] = value
        return result


class ResourcePageParam(PageBounds):
    pass


class ResourceExportParam(BaseModel):
    export_type: str = "current"
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[List[str]] = None


class ResourceImportParam(BaseModel):
    data: List[ResourceVO]


class ModulePageParam(PageBounds):
    pass


class ModuleExportParam(BaseModel):
    export_type: str = "current"
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[List[str]] = None


class ModuleImportParam(BaseModel):
    data: List[ModuleVO]
