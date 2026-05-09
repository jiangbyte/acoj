from typing import Optional, List, Any, Dict
from datetime import datetime
from pydantic import BaseModel, ConfigDict, field_validator, model_serializer
from core.pojo import PageBounds


class DictVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    label: Optional[str] = None
    value: Optional[str] = None
    color: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
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


class DictTreeVO(BaseModel):
    """Dict tree node with children"""
    id: Optional[str] = None
    code: Optional[str] = None
    label: Optional[str] = None
    value: Optional[str] = None
    color: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["DictTreeVO"] = []


class DictPageParam(PageBounds):
    parent_id: Optional[str] = None
    category: Optional[str] = None
    keyword: Optional[str] = None


class DictListParam(BaseModel):
    parent_id: Optional[str] = None
    category: Optional[str] = None


class DictTreeParam(BaseModel):
    category: Optional[str] = None


class DictExportParam(BaseModel):
    export_type: str = "current"
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[List[str]] = None


class DictImportParam(BaseModel):
    data: List[DictVO]
