"""Analyze service — mirrors hei-gin's analyze/service.go 1:1."""

import platform
import socket
from datetime import datetime, timezone
from sqlalchemy.orm import Session
from sqlalchemy import func, text
from .dao import AnalyzeDao
from .params import (
    DashboardVO, DashboardStats, ClientStats, TrendItem,
    OrgUserDistribution, CategoryDistribution, SysInfo,
    LogAnalysisData,
)
from ..log.models import SysLog

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
        rows = db.execute(
            text(f"SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count "
                 f"FROM {table} WHERE created_at IS NOT NULL "
                 f"GROUP BY month ORDER BY month ASC LIMIT 12")
        ).fetchall()
        return [TrendItem(month=r[0], count=r[1]) for r in rows]
    except Exception:
        return []


def get_org_user_distribution(db: Session) -> list:
    from ..user.models import SysUser
    from ..org.models import SysOrg
    rows = db.query(
        SysUser.org_id, func.count(SysUser.id).label("count")
    ).filter(
        SysUser.org_id.isnot(None), SysUser.org_id != ""
    ).group_by(SysUser.org_id).all()
    org_ids = [r[0] for r in rows]
    org_names = {}
    if org_ids:
        org_rows = db.query(SysOrg.id, SysOrg.name).filter(SysOrg.id.in_(org_ids)).all()
        org_names = {r.id: r.name for r in org_rows}
    result = []
    for org_id, cnt in rows:
        name = org_names.get(org_id, "未分配")
        result.append(OrgUserDistribution(name=name, count=cnt))
    return result


def get_role_category_distribution(db: Session) -> list:
    from ..role.models import SysRole
    rows = db.query(
        SysRole.category, func.count(SysRole.id).label("count")
    ).group_by(SysRole.category).all()
    return [CategoryDistribution(category=r[0], count=r[1]) for r in rows]


# ── Standalone service functions ──

def page(db: Session, param) -> dict:
    """Log page query — mirrors Go's analyze.Page()."""
    from ..log.service import page as log_page
    return log_page(db, param)


def login_analysis(db: Session) -> LogAnalysisData:
    """Login statistics — mirrors Go's LoginAnalysis()."""
    login_total = db.query(func.count(SysLog.id)).filter(
        SysLog.category == "LOGIN"
    ).scalar() or 0

    login_failed = db.query(func.count(SysLog.id)).filter(
        SysLog.category == "LOGIN", SysLog.exe_status == "FAIL"
    ).scalar() or 0

    login_today = db.query(func.count(SysLog.id)).filter(
        SysLog.category == "LOGIN",
        func.DATE(SysLog.op_time) == func.CURDATE()
    ).scalar() or 0

    return LogAnalysisData(
        login_total=login_total,
        login_failed=login_failed,
        login_today=login_today,
    )


def log_analysis(db: Session) -> LogAnalysisData:
    """Log/exception statistics — mirrors Go's LogAnalysis()."""
    log_total = db.query(func.count(SysLog.id)).scalar() or 0
    exception_total = db.query(func.count(SysLog.id)).filter(
        SysLog.category == "EXCEPTION"
    ).scalar() or 0
    exception_today = db.query(func.count(SysLog.id)).filter(
        SysLog.category == "EXCEPTION",
        func.DATE(SysLog.op_time) == func.CURDATE()
    ).scalar() or 0

    data = login_analysis(db)
    data.log_total = log_total
    data.log_exception = exception_total
    data.exception_today = exception_today
    return data


def dashboard(db: Session) -> dict:
    """Main dashboard — mirrors Go's Dashboard()."""
    from ..user.models import SysUser
    from ..role.models import SysRole
    from ..org.models import SysOrg
    from ..config.models import SysConfig
    from ..notice.models import SysNotice
    from ...plugin_client.user.models import ClientUser

    stats = DashboardStats(
        total_users=db.query(func.count(SysUser.id)).scalar() or 0,
        active_users=db.query(func.count(SysUser.id)).filter(
            SysUser.status == "ENABLED"
        ).scalar() or 0,
        total_roles=db.query(func.count(SysRole.id)).scalar() or 0,
        total_orgs=db.query(func.count(SysOrg.id)).scalar() or 0,
        total_configs=db.query(func.count(SysConfig.id)).scalar() or 0,
        total_notices=db.query(func.count(SysNotice.id)).scalar() or 0,
    )

    client_stats = ClientStats(
        total_users=db.query(func.count(ClientUser.id)).scalar() or 0,
        active_users=db.query(func.count(ClientUser.id)).filter(
            ClientUser.status == "ENABLED"
        ).scalar() or 0,
    )

    user_trend = get_monthly_trend(db, "sys_user")
    client_trend = get_monthly_trend(db, "client_user")
    org_dist = get_org_user_distribution(db)
    role_dist = get_role_category_distribution(db)

    sys_info = SysInfo(
        os_name=f"{platform.system()} {platform.release()}",
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
        self.dao = AnalyzeDao(db)

    def _get_sys_info(self) -> SysInfo:
        return SysInfo(
            os_name=platform.system() + " " + platform.release(),
            server_ip=get_server_ip(),
            run_time=f"已运行 {format_duration(SERVER_START_TIME)}",
        )

    def dashboard(self) -> dict:
        return dashboard(self.db)

    def login_analysis(self) -> LogAnalysisData:
        return login_analysis(self.db)

    def log_analysis(self) -> LogAnalysisData:
        return log_analysis(self.db)
