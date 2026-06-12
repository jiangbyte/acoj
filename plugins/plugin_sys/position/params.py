from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin
from .models import SysPosition


class PositionVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    code: str
    name: str
    category: str
    org_id: Optional[str] = None
    group_id: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None
    sort_code: Optional[int] = 0
    org_names: Optional[List[str]] = None
    group_names: Optional[List[str]] = None
    extra: Optional[str] = None
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class PositionPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    category: Optional[str] = None
    org_id: Optional[str] = None


def SysPositionToPositionVO(src: Optional[SysPosition]) -> Optional[PositionVO]:
    if src is None:
        return None
    return PositionVO(
        id=src.id,
        code=src.code,
        name=src.name,
        category=src.category,
        org_id=src.org_id,
        group_id=src.group_id,
        description=src.description,
        status=src.status,
        sort_code=src.sort_code,
        extra=src.extra,
        created_at=src.created_at,
        created_by=src.created_by,
        updated_at=src.updated_at,
        updated_by=src.updated_by,
    )
