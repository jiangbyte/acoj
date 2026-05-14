from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel, ConfigDict, Field
from core.pojo import PageBounds
from core.pojo.datetime_mixin import DateTimeValidatorMixin
from modules.sys.log.params import LogBarChartData, LogPieChartData


class SessionAnalysisResult(DateTimeValidatorMixin, BaseModel):
    """会话分析统计结果"""
    total_count: int = 0
    max_token_count: int = 0
    one_hour_newly_added: int = 0
    proportion_of_b_and_c: str = "0/0"


class SessionPageResult(DateTimeValidatorMixin, BaseModel):
    """在线会话分页结果"""
    user_id: Optional[str] = None
    account: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    status: Optional[str] = None
    last_login_ip: Optional[str] = None
    last_login_address: Optional[str] = None
    last_login_time: Optional[datetime] = None
    session_create_time: Optional[str] = None
    session_timeout: Optional[str] = None
    session_timeout_seconds: Optional[int] = 0
    token_count: int = 1


class SessionExitParam(BaseModel):
    user_id: str = Field(..., description="用户ID")


class SessionPageParam(PageBounds):
    keyword: Optional[str] = None


class SessionChartData(BaseModel):
    """会话图表数据"""
    bar_chart: LogBarChartData = Field(default_factory=LogBarChartData)
    pie_chart: LogPieChartData = Field(default_factory=LogPieChartData)
