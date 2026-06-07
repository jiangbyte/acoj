"""Client session params — standalone, mirrors hei-gin plugin-client/session/params.go."""

from typing import Optional, List
from pydantic import BaseModel, Field


class SessionPageResult(BaseModel):
    """Online session page result — mirrors Go SessionPageResult."""
    user_id: str = ""
    username: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    status: Optional[str] = None
    last_login_ip: Optional[str] = None
    last_login_time: str = ""
    token_count: int = 0
    session_create_time: str = ""
    session_timeout: str = ""
    session_timeout_seconds: int = 0


class SessionAnalysisResult(BaseModel):
    """Session analysis statistics — mirrors Go SessionAnalysisResult."""
    total_count: int = 0
    max_token_count: int = 0
    one_hour_newly_added: int = 0
    proportion_of_b_and_c: str = "0/0"


class SessionPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None


class SessionExitParam(BaseModel):
    user_id: str = Field(..., description="用户ID")


class SessionExitTokenParam(BaseModel):
    user_id: str = Field(..., description="用户ID")
    token: str = Field(..., description="令牌")


class SessionTokenResult(BaseModel):
    """Session token detail — mirrors Go SessionTokenResult."""
    token: str = ""
    created_at: str = ""
    timeout: str = ""
    timeout_seconds: int = 0
    device_type: Optional[str] = None
    device_id: Optional[str] = None


class CategoryTotal(BaseModel):
    category: str = ""
    total: int = 0


class CategorySeries(BaseModel):
    name: str = ""
    data: List[int] = Field(default_factory=list)


class BarChartData(BaseModel):
    days: List[str] = Field(default_factory=list)
    series: List[CategorySeries] = Field(default_factory=list)


class PieChartData(BaseModel):
    data: List[CategoryTotal] = Field(default_factory=list)


class SessionChartData(BaseModel):
    """Session chart data — mirrors Go SessionChartData."""
    bar_chart: BarChartData = Field(default_factory=BarChartData)
    pie_chart: PieChartData = Field(default_factory=PieChartData)
