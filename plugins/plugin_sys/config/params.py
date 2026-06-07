from typing import Optional, List
from pydantic import BaseModel, ConfigDict


class ConfigVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    config_key: Optional[str] = None
    config_value: Optional[str] = None
    category: Optional[str] = None
    remark: Optional[str] = None
    sort_code: Optional[int] = None
    extra: Optional[str] = None
    created_at: Optional[str] = None
    created_by: Optional[str] = None
    updated_at: Optional[str] = None
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
