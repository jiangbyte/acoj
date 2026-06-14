"""Analyze service — class-based service with DI-friendly provider."""

import platform
import socket
from datetime import datetime, timezone

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db

from .params import ClientStats, DashboardStats, DashboardVO, LogAnalysisData, SysInfo, TrendItem
from .repository import AnalyzeRepository

SERVER_START_TIME = datetime.now(timezone.utc)


def get_server_ip() -> str:
    try:
        hostname = socket.gethostname()
        ip = socket.gethostbyname(hostname)
        if ip and ip != "0.0.0.0" and not ip.startswith("169.254"):
            return ip
        for addr in socket.getaddrinfo(socket.gethostname(), None):
            ip = addr[4][0]
            if ip and not ip.startswith("127.") and not ip.startswith("169.254"):
                return ip
    except Exception:
        pass
    return ""


def format_duration(start_time: datetime) -> str:
    diff = datetime.now(timezone.utc) - start_time
    days = diff.days
    hours, remainder = divmod(diff.seconds, 3600)
    minutes = remainder // 60
    if days > 0:
        return f"{days}天{hours}小时{minutes}分钟"
    if hours > 0:
        return f"{hours}小时{minutes}分钟"
    return f"{minutes}分钟"


class AnalyzeService:
    def __init__(self, repository: AnalyzeRepository):
        self.repository = repository
        self.db = repository.db

    async def _get_monthly_trend(self, table: str) -> list[TrendItem]:
        try:
            rows = await self.repository.monthly_trend(table)
            return [TrendItem(month=row[0], count=row[1]) for row in rows]
        except Exception:
            return []

    async def dashboard(self) -> DashboardVO:
        stats = DashboardStats(
            total_users=await self.repository.count_users(),
            active_users=await self.repository.count_active_users(),
            total_roles=await self.repository.count_roles(),
            total_orgs=await self.repository.count_orgs(),
            total_configs=await self.repository.count_configs(),
            total_notices=await self.repository.count_notices(),
        )
        client_stats = ClientStats(
            total_users=await self.repository.count_client_users(),
            active_users=await self.repository.count_active_client_users(),
        )
        return DashboardVO(
            stats=stats,
            client_stats=client_stats,
            user_trend=await self._get_monthly_trend("sys_user"),
            client_trend=await self._get_monthly_trend("client_user"),
            org_user_distribution=await self.repository.org_user_distribution_with_names(),
            role_category_distribution=await self.repository.role_category_distribution_with_counts(),
            sys_info=SysInfo(
                os_name=platform.system().lower(),
                server_ip=get_server_ip(),
                run_time=f"已运行 {format_duration(SERVER_START_TIME)}",
            ),
        )

    async def login_analysis(self) -> LogAnalysisData:
        return LogAnalysisData(**await self.repository.login_stats())

    async def log_analysis(self) -> LogAnalysisData:
        data = await self.login_analysis()
        stats = await self.repository.log_stats()
        data.log_total = stats["log_total"]
        data.log_exception = stats["log_exception"]
        data.exception_today = stats["exception_today"]
        return data


def get_analyze_service(db: AsyncSession = Depends(get_db)) -> AnalyzeService:
    return AnalyzeService(AnalyzeRepository(db))
