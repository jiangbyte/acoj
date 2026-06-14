from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


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


class GroupTreeVO(DateTimeValidatorMixin, BaseModel):
    """Group tree node with children"""
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    org_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["GroupTreeVO"] = Field(default_factory=list)


class GroupTreeParam(BaseModel):
    category: Optional[str] = None
    org_id: Optional[str] = None


class GroupPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    category: Optional[str] = None
    org_id: Optional[str] = None


class UnionTreeNode(BaseModel):
    id: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    org_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    type: str = ""
    children: List["UnionTreeNode"] = Field(default_factory=list)
