from typing import Optional, List, Any, Dict
from datetime import datetime, date
from pydantic import BaseModel, ConfigDict, field_validator, model_serializer
from core.pojo import PageBounds


class UserVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    account: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    motto: Optional[str] = None
    gender: Optional[str] = None
    birthday: Optional[date] = None
    email: Optional[str] = None
    github: Optional[str] = None
    phone: Optional[str] = None
    org_id: Optional[str] = None
    position_id: Optional[str] = None
    status: Optional[str] = None
    last_login_at: Optional[datetime] = None
    last_login_ip: Optional[str] = None
    login_count: Optional[int] = 0
    is_deleted: Optional[str] = "NO"
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None
    role_ids: Optional[List[str]] = None
    group_ids: Optional[List[str]] = None

    @field_validator('created_at', 'updated_at', 'last_login_at', mode='before')
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
            elif isinstance(value, date):
                result[field_name] = value.isoformat()
            else:
                result[field_name] = value
        return result


class UserPageParam(PageBounds):
    keyword: Optional[str] = None
    status: Optional[str] = None


class UserExportParam(BaseModel):
    export_type: str = "current"
    current: Optional[int] = None
    size: Optional[int] = None
    selected_id: Optional[str] = None

    @property
    def selected_ids(self) -> Optional[List[str]]:
        return self.selected_id.split(",") if self.selected_id else None


class UserImportParam(BaseModel):
    data: List[UserVO]


class GrantRoleParam(BaseModel):
    user_id: str
    role_ids: List[str]
    scope: Optional[str] = None
    custom_scope_group_ids: Optional[str] = None


class GrantGroupParam(BaseModel):
    user_id: str
    group_ids: List[str]
