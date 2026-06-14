from typing import List
from pydantic import BaseModel, Field


class TrendItem(BaseModel):
    month: str
    count: int


class OrgUserDistribution(BaseModel):
    name: str
    count: int


class CategoryDistribution(BaseModel):
    category: str
    count: int


class DashboardStats(BaseModel):
    total_users: int = 0
    active_users: int = 0
    total_roles: int = 0
    total_orgs: int = 0
    total_configs: int = 0
    total_notices: int = 0


class SysInfo(BaseModel):
    os_name: str = ""
    server_ip: str = ""
    run_time: str = ""


class ClientStats(BaseModel):
    total_users: int = 0
    active_users: int = 0


class DashboardVO(BaseModel):
    stats: DashboardStats
    client_stats: ClientStats = Field(default_factory=ClientStats)
    user_trend: List[TrendItem] = Field(default_factory=list)
    client_trend: List[TrendItem] = Field(default_factory=list)
    org_user_distribution: List[OrgUserDistribution] = Field(default_factory=list)
    role_category_distribution: List[CategoryDistribution] = Field(default_factory=list)
    sys_info: SysInfo = Field(default_factory=SysInfo)


class LogAnalysisData(BaseModel):
    """Login/log analysis stats — mirrors Go's LogAnalysisData."""
    login_total: int = 0
    login_failed: int = 0
    login_today: int = 0
    log_total: int = 0
    log_exception: int = 0
    exception_today: int = 0
