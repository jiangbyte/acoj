from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class DictVO(DateTimeValidatorMixin, BaseModel):
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
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class DictTreeVO(DateTimeValidatorMixin, BaseModel):
    """Dict tree node with children"""
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: Optional[str] = None
    label: Optional[str] = None
    value: Optional[str] = None
    color: Optional[str] = None
    category: Optional[str] = None
    parent_id: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    children: List["DictTreeVO"] = Field(default_factory=list)


class DictPageParam(BaseModel):
    current: int = 1
    size: int = 10
    dict_group: Optional[str] = None
    parent_id: Optional[str] = None
    category: Optional[str] = None
    keyword: Optional[str] = None


class DictListParam(BaseModel):
    category: Optional[str] = None
    keyword: Optional[str] = None


class DictTreeParam(BaseModel):
    category: Optional[str] = None
    dict_group: Optional[str] = None
