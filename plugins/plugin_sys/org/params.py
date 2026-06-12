from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.pojo.datetime_mixin import DateTimeValidatorMixin
from .models import SysOrg


class OrgVO(DateTimeValidatorMixin, BaseModel):
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
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


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


class OrgPageParam(BaseModel):
    current: int = 1
    size: int = 10
    parent_id: Optional[str] = None
    keyword: Optional[str] = None


def SysOrgToOrgVO(src: Optional[SysOrg]) -> Optional[OrgVO]:
    if src is None:
        return None
    return OrgVO(
        id=src.id,
        code=src.code,
        name=src.name,
        category=src.category,
        parent_id=src.parent_id,
        description=src.description,
        status=src.status,
        sort_code=src.sort_code,
        extra=src.extra,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )


def SysOrgToOrgTreeVO(src: Optional[SysOrg]) -> Optional[OrgTreeVO]:
    if src is None:
        return None
    return OrgTreeVO(
        id=src.id,
        code=src.code,
        name=src.name,
        category=src.category,
        parent_id=src.parent_id,
        status=src.status,
        sort_code=src.sort_code,
        children=[],
    )
