from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin
from .models import SysGroup


class GroupVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    parent_id: Optional[str] = None
    org_id: str
    description: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    org_names: Optional[List[str]] = None
    extra: Optional[str] = None
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class GroupTreeVO(BaseModel):
    """Group tree node with children"""
    id: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    org_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["GroupTreeVO"] = []


class GroupTreeParam(BaseModel):
    category: Optional[str] = None
    org_id: Optional[str] = None


class GroupPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    category: Optional[str] = None
    org_id: Optional[str] = None


def SysGroupToGroupVO(src: Optional[SysGroup]) -> Optional[GroupVO]:
    if src is None:
        return None
    return GroupVO(
        id=src.id,
        code=src.code,
        name=src.name,
        category=src.category,
        parent_id=src.parent_id,
        org_id=src.org_id,
        description=src.description,
        status=src.status,
        sort_code=src.sort_code,
        extra=src.extra,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )


def SysGroupToGroupTreeVO(src: Optional[SysGroup]) -> Optional[GroupTreeVO]:
    if src is None:
        return None
    return GroupTreeVO(
        id=src.id,
        code=src.code,
        name=src.name,
        category=src.category,
        parent_id=src.parent_id,
        org_id=src.org_id,
        status=src.status,
        sort_code=src.sort_code,
        children=[],
    )
