from typing import Optional, List
from pydantic import BaseModel, ConfigDict
from .models import SysConfig


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


def SysConfigToConfigVO(src: Optional[SysConfig]) -> Optional[ConfigVO]:
    if src is None:
        return None
    return ConfigVO(
        id=src.id,
        config_key=src.config_key,
        config_value=src.config_value,
        category=src.category,
        remark=src.remark,
        sort_code=src.sort_code,
        extra=src.extra,
        created_at=src.created_at.strftime("%Y-%m-%d %H:%M:%S") if src.created_at else None,
        created_by=src.created_by,
        updated_at=src.updated_at.strftime("%Y-%m-%d %H:%M:%S") if src.updated_at else None,
        updated_by=src.updated_by,
    )
