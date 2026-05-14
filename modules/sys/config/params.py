from typing import Optional, List
from pydantic import BaseModel, ConfigDict
from core.pojo import PageBounds, BaseExportParam


class ConfigVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    config_key: Optional[str] = None
    config_value: Optional[str] = None
    category: Optional[str] = None
    remark: Optional[str] = None
    sort_code: Optional[int] = None
    ext_json: Optional[str] = None


class ConfigPageParam(PageBounds):
    category: Optional[str] = None
    keyword: Optional[str] = None


class ConfigListParam(BaseModel):
    category: str


class ConfigBatchEditParam(BaseModel):
    configs: List[ConfigVO]


class ConfigCategoryEditParam(BaseModel):
    category: str
    configs: List[ConfigVO]


class ConfigExportParam(BaseExportParam):
    pass
