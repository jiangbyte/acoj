import platform
import socket
import datetime
from sqlalchemy.orm import Session
from .dao import AnalyzeDao
from .params import DashboardVO, DashboardStats, TrendItem, OrgUserDistribution, CategoryDistribution, SysInfo, RecentLogin

SERVER_START_TIME = datetime.datetime.now()


class AnalyzeService:
    def __init__(self, db: Session):
        self.dao = AnalyzeDao(db)

    def _get_sys_info(self) -> SysInfo:
        try:
            hostname = socket.gethostname()
            ip = socket.gethostbyname(hostname)
        except Exception:
            ip = "unknown"

        uptime = datetime.datetime.now() - SERVER_START_TIME
        days = uptime.days
        hours, remainder = divmod(uptime.seconds, 3600)
        minutes = remainder // 60
        if days > 0:
            run_time = f"{days}天 {hours}小时 {minutes}分钟"
        else:
            run_time = f"{hours}小时 {minutes}分钟"

        return SysInfo(
            python_version=platform.python_version(),
            os_name=platform.system() + " " + platform.release(),
            server_ip=ip,
            run_time=run_time,
        )

    def _get_recent_logins(self) -> list[RecentLogin]:
        return [RecentLogin(**item) for item in self.dao.get_recent_logins()]

    def dashboard(self) -> DashboardVO:
        stats = DashboardStats(
            total_users=self.dao.count_users(),
            active_users=self.dao.count_active_users(),
            total_roles=self.dao.count_roles(),
            total_orgs=self.dao.count_orgs(),
            total_configs=self.dao.count_configs(),
            total_notices=self.dao.count_notices(),
        )
        user_trend = [TrendItem(**item) for item in self.dao.user_trend()]
        org_dist = [OrgUserDistribution(**item) for item in self.dao.org_user_distribution()]
        role_dist = [CategoryDistribution(**item) for item in self.dao.role_category_distribution()]

        return DashboardVO(
            stats=stats,
            user_trend=user_trend,
            org_user_distribution=org_dist,
            role_category_distribution=role_dist,
            sys_info=self._get_sys_info(),
            recent_logins=self._get_recent_logins(),
        )
