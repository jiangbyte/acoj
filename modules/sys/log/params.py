from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict
from core.enums import SoftDeleteEnum
from core.pojo import PageBounds, BaseExportParam
from core.pojo.datetime_mixin import DateTimeValidatorMixin


class LogVO(DateTimeValidatorMixin, BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: Optional[str] = None
    category: Optional[str] = None
    name: Optional[str] = None
    exe_status: Optional[str] = None
    exe_message: Optional[str] = None
    op_ip: Optional[str] = None
    op_address: Optional[str] = None
    op_browser: Optional[str] = None
    op_os: Optional[str] = None
    class_name: Optional[str] = None
    method_name: Optional[str] = None
    req_method: Optional[str] = None
    req_url: Optional[str] = None
    param_json: Optional[str] = None
    result_json: Optional[str] = None
    op_time: Optional[datetime] = None
    op_user: Optional[str] = None
    sign_data: Optional[str] = None
    is_deleted: Optional[str] = SoftDeleteEnum.NO.value
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_at: Optional[datetime] = None
    updated_by: Optional[str] = None


class LogPageParam(PageBounds):
    keyword: Optional[str] = None
    category: Optional[str] = None
    exe_status: Optional[str] = None


class LogExportParam(BaseExportParam):
    pass


class LogImportParam(BaseModel):
    data: List[LogVO]


class LogDeleteByCategoryParam(BaseModel):
    category: str
