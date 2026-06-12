"""Analyze service — mirrors hei-gin's analyze/service.go 1:1."""

import platform
import socket
from datetime import datetime, timezone
from sqlalchemy.orm import Session
from .repository import AnalyzeRepository
from .params import (
    DashboardVO, DashboardStats, ClientStats, TrendItem,
    OrgUserDistribution, CategoryDistribution, SysInfo,
    LogAnalysisData,
)

SERVER_START_TIME = datetime.now(timezone.utc)


def get_server_ip() -> str:
    try:
        hostname = socket.gethostname()
        ip = socket.gethostbyname(hostname)
        if ip and ip != "0.0.0.0" and not ip.startswith("169.254"):
            return ip
        addrs = socket.getaddrinfo(socket.gethostname(), None)
        for addr in addrs:
            ip = addr[4][0]
            if ip and not ip.startswith("127.") and not ip.startswith("169.254"):
                return ip
    except Exception:
        pass
    return ""


def format_duration(d: datetime) -> str:
    diff = datetime.now(timezone.utc) - d
    days = diff.days
    hours, remainder = divmod(diff.seconds, 3600)
    minutes = remainder // 60
    if days > 0:
        return f"{days}天{hours}小时{minutes}分钟"
    if hours > 0:
        return f"{hours}小时{minutes}分钟"
    return f"{minutes}分钟"


def get_monthly_trend(db: Session, table: str) -> list:
    try:
        rows = AnalyzeRepository(db).monthly_trend(table)
        return [TrendItem(month=r[0], count=r[1]) for r in rows]
    except Exception:
        return []


def get_org_user_distribution(db: Session) -> list:
    return [OrgUserDistribution(**item) for item in AnalyzeRepository(db).org_user_distribution_with_names()]


def get_role_category_distribution(db: Session) -> list:
    return [CategoryDistribution(**item) for item in AnalyzeRepository(db).role_category_distribution_with_counts()]


# ── Standalone service functions ──

def page(db: Session, param) -> dict:
    """Log page query — mirrors Go's analyze.Page()."""
    from ..log.service import page as log_page
    return log_page(db, param)


def login_analysis(db: Session) -> LogAnalysisData:
    """Login statistics — mirrors Go's LoginAnalysis()."""
    return LogAnalysisData(**AnalyzeRepository(db).login_stats())


def log_analysis(db: Session) -> LogAnalysisData:
    """Log/exception statistics — mirrors Go's LogAnalysis()."""
    data = login_analysis(db)
    stats = AnalyzeRepository(db).log_stats()
    data.log_total = stats["log_total"]
    data.log_exception = stats["log_exception"]
    data.exception_today = stats["exception_today"]
    return data


def dashboard(db: Session) -> dict:
    """Main dashboard — mirrors Go's Dashboard()."""
    repository = AnalyzeRepository(db)
    stats = DashboardStats(
        total_users=repository.count_users(),
        active_users=repository.count_active_users(),
        total_roles=repository.count_roles(),
        total_orgs=repository.count_orgs(),
        total_configs=repository.count_configs(),
        total_notices=repository.count_notices(),
    )

    client_stats = ClientStats(
        total_users=repository.count_client_users(),
        active_users=repository.count_active_client_users(),
    )

    user_trend = get_monthly_trend(db, "sys_user")
    client_trend = get_monthly_trend(db, "client_user")
    org_dist = get_org_user_distribution(db)
    role_dist = get_role_category_distribution(db)

    sys_info = SysInfo(
        os_name=platform.system().lower(),
        server_ip=get_server_ip(),
        run_time=f"已运行 {format_duration(SERVER_START_TIME)}",
    )

    return DashboardVO(
        stats=stats,
        client_stats=client_stats,
        user_trend=user_trend,
        client_trend=client_trend,
        org_user_distribution=org_dist,
        role_category_distribution=role_dist,
        sys_info=sys_info,
    ).model_dump()


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class
# ═════════════════════════════════════════════════════════════════════

class AnalyzeService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = AnalyzeRepository(db)

    def _get_sys_info(self) -> SysInfo:
        return SysInfo(
            os_name=platform.system().lower(),
            server_ip=get_server_ip(),
            run_time=f"已运行 {format_duration(SERVER_START_TIME)}",
        )

    def dashboard(self) -> dict:
        return dashboard(self.db)

    def login_analysis(self) -> LogAnalysisData:
        return login_analysis(self.db)

    def log_analysis(self) -> LogAnalysisData:
        return log_analysis(self.db)
