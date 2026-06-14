from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class ConfigVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    config_key: Optional[str] = None
    config_value: Optional[str] = None
    category: Optional[str] = None
    remark: Optional[str] = None
    sort_code: Optional[int] = None
    extra: Optional[str] = None
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class ConfigPageParam(BaseModel):
    current: int = 1
    size: int = 10
    category: Optional[str] = None
    keyword: Optional[str] = None


class ConfigListParam(BaseModel):
    category: str


class ConfigBatchEditItem(BaseModel):
    id: str
    config_key: Optional[str] = None
    config_value: Optional[str] = None
    remark: Optional[str] = None
    sort_code: Optional[int] = None


class ConfigBatchEditParam(BaseModel):
    configs: List[ConfigBatchEditItem]


class ConfigCategoryEditParam(BaseModel):
    """Matches Go's ConfigCategoryEditParam — single set of key/value."""
    category: str
    config_key: Optional[str] = None
    config_value: Optional[str] = None
    remark: Optional[str] = None
