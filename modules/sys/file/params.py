from typing import Optional, List
from pydantic import BaseModel, ConfigDict
from core.pojo import PageBounds


class FileVO(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    engine: Optional[str] = None
    bucket: Optional[str] = None
    file_key: Optional[str] = None
    name: Optional[str] = None
    suffix: Optional[str] = None
    size_kb: Optional[int] = None
    size_info: Optional[str] = None
    obj_name: Optional[str] = None
    storage_path: Optional[str] = None
    download_path: Optional[str] = None
    is_download_auth: Optional[int] = None
    thumbnail: Optional[str] = None
    ext_json: Optional[str] = None
    created_at: Optional[str] = None


class FilePageParam(PageBounds):
    engine: Optional[str] = None
    keyword: Optional[str] = None
    date_range_start: Optional[str] = None
    date_range_end: Optional[str] = None


class FileIdParam(BaseModel):
    id: str
