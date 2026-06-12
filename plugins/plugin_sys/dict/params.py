from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin
from .models import SysDict


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


def SysDictToDictVO(src: Optional[SysDict]) -> Optional[DictVO]:
    if src is None:
        return None
    return DictVO(
        id=src.id,
        code=src.code,
        label=src.label,
        value=src.value,
        color=src.color,
        category=src.category,
        parent_id=src.parent_id,
        status=src.status,
        sort_code=src.sort_code,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )


def SysDictToDictTreeVO(src: Optional[SysDict]) -> Optional[DictTreeVO]:
    if src is None:
        return None
    return DictTreeVO(
        id=src.id,
        code=src.code,
        label=src.label,
        value=src.value,
        color=src.color,
        category=src.category,
        parent_id=src.parent_id,
        status=src.status,
        sort_code=src.sort_code,
        children=[],
    )
