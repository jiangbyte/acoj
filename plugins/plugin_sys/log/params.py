"""Log params — mirrors hei-gin plugin-sys/log/params.go."""

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin


class LogVO(DateTimeValidatorMixin, BaseModel):
    """Mirrors Go LogVO — all fields are strings including datetimes."""
    model_config = ConfigDict(from_attributes=True)

    id: str = ""
    category: str = ""
    name: str = ""
    exe_status: str = ""
    exe_message: str = ""
    op_ip: str = ""
    op_address: str = ""
    op_browser: str = ""
    op_os: str = ""
    class_name: str = ""
    method_name: str = ""
    req_method: str = ""
    req_url: str = ""
    param_json: str = ""
    result_json: str = ""
    op_time: Optional[datetime] = None
    trace_id: str = ""
    op_user: str = ""
    sign_data: str = ""
    created_at: Optional[datetime] = None
    created_by: str = ""
    updated_at: Optional[datetime] = None
    updated_by: str = ""


class LogPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None
    category: Optional[str] = None
    exe_status: Optional[str] = None


class LogDeleteByCategoryParam(BaseModel):
    category: str


# ---- Chart / Statistics result models ----
# Mirrors Go's BarChartData / PieChartData / CategorySeries / CategoryTotal

class LogCategoryTotal(BaseModel):
    category: str = ""
    total: int = 0


class LogCategorySeries(BaseModel):
    name: str = ""
    data: List[int] = Field(default_factory=list)


class LogBarChartData(BaseModel):
    days: List[str] = Field(default_factory=list)
    series: List["LogCategorySeries"] = Field(default_factory=list)


class LogPieChartData(BaseModel):
    data: List[LogCategoryTotal] = Field(default_factory=list)
