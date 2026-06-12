"""Log params — mirrors hei-gin plugin-sys/log/params.go."""

from typing import Optional, List
from pydantic import BaseModel, ConfigDict, Field
from .models import SysLog


class LogVO(BaseModel):
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
    op_time: str = ""
    trace_id: str = ""
    op_user: str = ""
    sign_data: str = ""
    created_at: str = ""
    created_by: str = ""
    updated_at: str = ""
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


def SysLogToLogVO(src: Optional[SysLog]) -> Optional[LogVO]:
    if src is None:
        return None
    return LogVO(
        id=src.id,
        category=src.category or "",
        name=src.name or "",
        exe_status=src.exe_status or "",
        exe_message=src.exe_message or "",
        op_ip=src.op_ip or "",
        op_address=src.op_address or "",
        op_browser=src.op_browser or "",
        op_os=src.op_os or "",
        class_name=src.class_name or "",
        method_name=src.method_name or "",
        req_method=src.req_method or "",
        req_url=src.req_url or "",
        param_json=src.param_json or "",
        result_json=src.result_json or "",
        op_time=src.op_time.strftime("%Y-%m-%d %H:%M:%S") if src.op_time else "",
        trace_id=src.trace_id or "",
        op_user=src.op_user or "",
        sign_data=src.sign_data or "",
        created_at=src.created_at.strftime("%Y-%m-%d %H:%M:%S") if src.created_at else "",
        created_by=src.created_by or "",
        updated_at=src.updated_at.strftime("%Y-%m-%d %H:%M:%S") if src.updated_at else "",
        updated_by=src.updated_by or "",
    )
