from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
import logging

from fastapi import FastAPI

from app.core.config.settings import settings
from app.core.security.permission_registry import sync_permission_registry
from app.modules.sys.audit.queue import start_operation_audit_queue, stop_operation_audit_queue
from app.platform.cache.redis import close_redis, init_redis
from app.platform.db.session import close_engine, init_engine
from app.platform.http.client import close_http_client, init_http_client
from app.platform.module import (
    load_module_specs,
    run_shutdown_hooks,
    run_startup_hooks,
)
from app.platform.observability.tracing import shutdown_tracing

logger = logging.getLogger(__name__)


async def apply_db_config_overrides():
    """从 sys_config 表加载配置并覆盖 settings 单例。"""
    from app.modules.sys.config.config_reader import config_reader

    await config_reader.load_all()

    # AUTH_TOKEN
    settings.auth.token_ttl_seconds = config_reader.get_int("auth.token_ttl_seconds", settings.auth.token_ttl_seconds)
    settings.auth.refresh_ttl_seconds = config_reader.get_int("auth.refresh_ttl_seconds", settings.auth.refresh_ttl_seconds)
    settings.auth.password_reset_token_ttl_seconds = config_reader.get_int("auth.password_reset_token_ttl_seconds", settings.auth.password_reset_token_ttl_seconds)

    # AUTH_LOGIN
    settings.auth.login_failure_window_seconds = config_reader.get_int("auth.login_failure_window_seconds", settings.auth.login_failure_window_seconds)
    settings.auth.login_account_max_failures = config_reader.get_int("auth.login_account_max_failures", settings.auth.login_account_max_failures)
    settings.auth.login_ip_max_failures = config_reader.get_int("auth.login_ip_max_failures", settings.auth.login_ip_max_failures)
    settings.auth.login_lock_seconds = config_reader.get_int("auth.login_lock_seconds", settings.auth.login_lock_seconds)

    # AUTH_REGISTER
    settings.auth.admin_register_enabled = config_reader.get_bool("auth.admin_register_enabled", settings.auth.admin_register_enabled)
    settings.auth.portal_register_enabled = config_reader.get_bool("auth.portal_register_enabled", settings.auth.portal_register_enabled)

    # AUTH_PASSWORD
    settings.auth.default_password = config_reader.get("auth.default_password", settings.auth.default_password)


    # AUDIT_ALERT
    settings.audit_alert.enabled = config_reader.get_bool("audit_alert.enabled", settings.audit_alert.enabled)
    settings.audit_alert.webhook_url = config_reader.get("audit_alert.webhook_url", settings.audit_alert.webhook_url)
    settings.audit_alert.webhook_secret = config_reader.get("audit_alert.webhook_secret", settings.audit_alert.webhook_secret)
    settings.audit_alert.analysis_interval_seconds = config_reader.get_int("audit_alert.analysis_interval_seconds", settings.audit_alert.analysis_interval_seconds)
    settings.audit_alert.alert_cooldown_seconds = config_reader.get_int("audit_alert.alert_cooldown_seconds", settings.audit_alert.alert_cooldown_seconds)
    settings.audit_alert.rule_brute_force = config_reader.get_bool("audit_alert.rule_brute_force", settings.audit_alert.rule_brute_force)
    settings.audit_alert.rule_unusual_hours = config_reader.get_bool("audit_alert.rule_unusual_hours", settings.audit_alert.rule_unusual_hours)
    settings.audit_alert.rule_sensitive_ops = config_reader.get_bool("audit_alert.rule_sensitive_ops", settings.audit_alert.rule_sensitive_ops)
    settings.audit_alert.rule_bulk_delete = config_reader.get_bool("audit_alert.rule_bulk_delete", settings.audit_alert.rule_bulk_delete)
    settings.audit_alert.rule_ip_anomaly = config_reader.get_bool("audit_alert.rule_ip_anomaly", settings.audit_alert.rule_ip_anomaly)
    settings.audit_alert.brute_force_threshold = config_reader.get_int("audit_alert.brute_force_threshold", settings.audit_alert.brute_force_threshold)
    settings.audit_alert.bulk_delete_threshold = config_reader.get_int("audit_alert.bulk_delete_threshold", settings.audit_alert.bulk_delete_threshold)
    settings.audit_alert.ip_anomaly_threshold = config_reader.get_int("audit_alert.ip_anomaly_threshold", settings.audit_alert.ip_anomaly_threshold)
    # STORAGE — 从 sys_storage_config 加载（唯一数据源）
    active_storage = config_reader.get_active_storage()
    if active_storage:
        from app.core.config.enums import StorageProvider

        settings.storage.provider = StorageProvider(active_storage.get("provider", "local"))
        settings.storage.bucket = active_storage.get("bucket") or ""
        settings.storage.endpoint = active_storage.get("endpoint") or ""
        settings.storage.access_key = active_storage.get("access_key") or ""
        settings.storage.secret_key = active_storage.get("secret_key") or ""
        settings.storage.region = active_storage.get("region") or ""
        settings.storage.use_ssl = active_storage.get("use_ssl", False)
        settings.storage.base_url = active_storage.get("base_url") or ""
        settings.storage.public_path = active_storage.get("public_path") or "/api/v1/files"
        settings.storage.local_root = active_storage.get("local_root") or "storage"
    else:
        raise RuntimeError(
            "No active storage configuration found in sys_storage_config table. "
            "Set a default storage config via admin panel before starting the application."
        )

    # UPLOAD
    settings.storage.upload_max_bytes = config_reader.get_int("storage.upload_max_bytes", settings.storage.upload_max_bytes)
    settings.storage.public_upload_enabled = config_reader.get_bool("storage.public_upload_enabled", settings.storage.public_upload_enabled)
    settings.storage.presign_expire_seconds = config_reader.get_int("storage.presign_expire_seconds", settings.storage.presign_expire_seconds)
    settings.storage.upload_allowed_content_types = config_reader.get_list("storage.upload_allowed_content_types", settings.storage.upload_allowed_content_types)
    settings.storage.upload_allowed_extensions = config_reader.get_list("storage.upload_allowed_extensions", settings.storage.upload_allowed_extensions)
    settings.storage.upload_denied_extensions = config_reader.get_list("storage.upload_denied_extensions", settings.storage.upload_denied_extensions)
    settings.storage.upload_category_max_length = config_reader.get_int("storage.upload_category_max_length", settings.storage.upload_category_max_length)

    # MAIL
    settings.mail.host = config_reader.get("mail.host", settings.mail.host)
    settings.mail.port = config_reader.get_int("mail.port", settings.mail.port)
    settings.mail.username = config_reader.get("mail.username", settings.mail.username)
    settings.mail.password = config_reader.get("mail.password", settings.mail.password)
    settings.mail.from_email = config_reader.get("mail.from_email", settings.mail.from_email)
    settings.mail.from_name = config_reader.get("mail.from_name", settings.mail.from_name)
    settings.mail.use_tls = config_reader.get_bool("mail.use_tls", settings.mail.use_tls)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    logger.info("lifespan startup: app.routes count = %d", len(app.routes))
    module_specs = load_module_specs()
    init_engine()
    await init_redis()
    await start_operation_audit_queue()
    await sync_permission_registry(app)
    await apply_db_config_overrides()
    await init_http_client()
    await run_startup_hooks(module_specs)
    try:
        yield
    finally:
        await run_shutdown_hooks(module_specs)
        await stop_operation_audit_queue()
        await close_http_client()
        await close_redis()
        await close_engine()
        shutdown_tracing()
