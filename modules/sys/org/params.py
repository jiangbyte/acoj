from typing import Optional, List, Any, Dict
from datetime import datetime
from pydantic import BaseModel, ConfigDict, field_validator, model_serializer
from core.pojo import PageBounds


class OrgVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    parent_id: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    extra: Optional[str] = None
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


class GrantOrgRoleParam(BaseModel):
    org_id: str
    role_ids: List[str]
    scope: Optional[str] = None
    custom_scope_group_ids: Optional[str] = None


class OrgTreeVO(BaseModel):
    """Org tree node with children"""
    id: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["OrgTreeVO"] = []


class OrgTreeParam(BaseModel):
    category: Optional[str] = None


class OrgPageParam(PageBounds):
    parent_id: Optional[str] = None
    keyword: Optional[str] = None


class OrgExportParam(BaseModel):
    export_type: str = "current"
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class OrgImportParam(BaseModel):
    data: List[OrgVO]
