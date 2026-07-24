"""seed audit_alert config defaults into sys_config"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "seed_audit_alert_configs"
down_revision: str | Sequence[str] | None = "cfbf047505b2"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

CONFIG_KEYS = [
    {"id": "sys_config_audit_100", "config_key": "audit_alert.enabled", "config_value": "false", "category": "AUDIT_ALERT", "remark": "审计告警总开关", "sort_code": 1},
    {"id": "sys_config_audit_101", "config_key": "audit_alert.webhook_url", "config_value": "", "category": "AUDIT_ALERT", "remark": "Webhook 地址", "sort_code": 2},
    {"id": "sys_config_audit_102", "config_key": "audit_alert.webhook_secret", "config_value": "", "category": "AUDIT_ALERT", "remark": "Webhook 签名密钥(可选)", "sort_code": 3},
    {"id": "sys_config_audit_103", "config_key": "audit_alert.analysis_interval_seconds", "config_value": "300", "category": "AUDIT_ALERT", "remark": "分析周期(秒)", "sort_code": 4},
    {"id": "sys_config_audit_104", "config_key": "audit_alert.alert_cooldown_seconds", "config_value": "1800", "category": "AUDIT_ALERT", "remark": "告警冷却(秒)", "sort_code": 5},
    {"id": "sys_config_audit_105", "config_key": "audit_alert.rule_brute_force", "config_value": "true", "category": "AUDIT_ALERT", "remark": "暴力破解检测", "sort_code": 10},
    {"id": "sys_config_audit_106", "config_key": "audit_alert.rule_unusual_hours", "config_value": "true", "category": "AUDIT_ALERT", "remark": "异常时间操作检测", "sort_code": 11},
    {"id": "sys_config_audit_107", "config_key": "audit_alert.rule_sensitive_ops", "config_value": "true", "category": "AUDIT_ALERT", "remark": "敏感操作监控", "sort_code": 12},
    {"id": "sys_config_audit_108", "config_key": "audit_alert.rule_bulk_delete", "config_value": "true", "category": "AUDIT_ALERT", "remark": "批量删除检测", "sort_code": 13},
    {"id": "sys_config_audit_109", "config_key": "audit_alert.rule_ip_anomaly", "config_value": "true", "category": "AUDIT_ALERT", "remark": "IP 异常检测", "sort_code": 14},
    {"id": "sys_config_audit_110", "config_key": "audit_alert.brute_force_threshold", "config_value": "10", "category": "AUDIT_ALERT", "remark": "暴力破解阈值(次/分钟)", "sort_code": 20},
    {"id": "sys_config_audit_111", "config_key": "audit_alert.bulk_delete_threshold", "config_value": "20", "category": "AUDIT_ALERT", "remark": "批量删除阈值(次/5分钟)", "sort_code": 21},
    {"id": "sys_config_audit_112", "config_key": "audit_alert.ip_anomaly_threshold", "config_value": "3", "category": "AUDIT_ALERT", "remark": "IP异常阈值(不同IP数/15分钟)", "sort_code": 22},
]


def upgrade() -> None:
    conn = op.get_bind()
    for item in CONFIG_KEYS:
        stmt = sa.text(
            "INSERT INTO sys_config (id, config_key, config_value, category, remark, sort_code) "
            "VALUES (:id, :config_key, :config_value, :category, :remark, :sort_code) "
            ""
        )
        conn.execute(stmt, {
            "id": item["id"],
            "config_key": item["config_key"],
            "config_value": item["config_value"],
            "category": item["category"],
            "remark": item["remark"],
            "sort_code": item["sort_code"],
            
        })


def downgrade() -> None:
    conn = op.get_bind()
    ids = [item["id"] for item in CONFIG_KEYS]
    for item_id in ids:
        conn.execute(
            sa.text("DELETE FROM sys_config WHERE id = :id"),
            {"id": item_id},
        )
