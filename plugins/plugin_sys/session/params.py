from typing import Optional, Any
from datetime import datetime
from pydantic import BaseModel, ConfigDict, Field
from sdk.shared.types.datetime_mixin import DateTimeValidatorMixin
from plugins.plugin_sys.log.params import LogBarChartData, LogPieChartData


class SessionAnalysisResult(DateTimeValidatorMixin, BaseModel):
    """会话分析统计结果"""
    total_count: int = 0
    max_token_count: int = 0
    one_hour_newly_added: int = 0
    proportion_of_b_and_c: str = "0/0"


class SessionPageResult(DateTimeValidatorMixin, BaseModel):
    """在线会话分页结果"""
    user_id: Optional[str] = None
    username: Optional[str] = None
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    status: Optional[str] = None
    last_login_ip: Optional[str] = None
    last_login_address: Optional[str] = None
    last_login_time: Optional[datetime] = None
    session_create_time: Optional[datetime] = None
    session_timeout: Optional[str] = None
    session_timeout_seconds: Optional[int] = 0
    token_count: int = 0


class SessionExitParam(BaseModel):
    user_id: str = Field(..., description="用户ID")


class SessionExitTokenParam(BaseModel):
    user_id: str = Field(..., description="用户ID")
    token: str = Field(..., description="令牌")


class SessionTokenResult(DateTimeValidatorMixin, BaseModel):
    """会话令牌详情"""
    token: str = ""
    created_at: Optional[datetime] = None
    timeout: str = ""
    timeout_seconds: int = 0
    device_type: Optional[str] = None
    device_id: Optional[str] = None


class SessionPageParam(BaseModel):
    current: int = 1
    size: int = 10
    keyword: Optional[str] = None


class SessionChartData(BaseModel):
    """会话图表数据"""
    bar_chart: LogBarChartData = Field(default_factory=LogBarChartData)
    pie_chart: LogPieChartData = Field(default_factory=LogPieChartData)


def SessionInfoToSessionPageResult(
    info: dict[str, Any],
    session_timeout: str,
    *,
    nickname: Optional[str] = None,
    avatar: Optional[str] = None,
    status: Optional[str] = None,
    last_login_ip: Optional[str] = None,
    last_login_address: Optional[str] = None,
    last_login_time: Optional[datetime] = None,
) -> SessionPageResult:
    return SessionPageResult(
        user_id=info.get("user_id"),
        username=info.get("username"),
        nickname=nickname or info.get("nickname"),
        avatar=avatar,
        status=status,
        last_login_ip=last_login_ip,
        last_login_address=last_login_address,
        last_login_time=last_login_time,
        session_create_time=info.get("session_create_time"),
        session_timeout=session_timeout,
        session_timeout_seconds=info.get("session_timeout_seconds", 0),
        token_count=info.get("token_count", 0),
    )


def SessionTokenInfoToSessionTokenResult(
    token_info: dict[str, Any],
    timeout: str,
) -> SessionTokenResult:
    created_at = token_info.get("created_at", "")
    created_at_dt = None
    if created_at:
        try:
            created_at_dt = datetime.fromisoformat(created_at)
        except ValueError:
            try:
                created_at_dt = datetime.strptime(created_at, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                created_at_dt = None
    return SessionTokenResult(
        token=token_info["token"],
        created_at=created_at_dt,
        timeout=timeout,
        timeout_seconds=token_info.get("timeout_seconds", 0),
        device_type=token_info.get("device_type"),
        device_id=token_info.get("device_id"),
    )
