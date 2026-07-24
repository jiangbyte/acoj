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

    # STORAGE — 优先从 sys_storage_config 加载
    active_storage = config_reader.get_active_storage()
    if active_storage:
        from app.core.config.enums import StorageProvider

        settings.storage.provider = StorageProvider(active_storage.get("provider", settings.storage.provider.value))  # type: ignore[assignment]
        settings.storage.bucket = active_storage.get("bucket", settings.storage.bucket)
        settings.storage.endpoint = active_storage.get("endpoint", settings.storage.endpoint)
        settings.storage.access_key = active_storage.get("access_key", settings.storage.access_key)
        settings.storage.secret_key = active_storage.get("secret_key", settings.storage.secret_key)
        settings.storage.region = active_storage.get("region", settings.storage.region)
        settings.storage.use_ssl = active_storage.get("use_ssl", settings.storage.use_ssl)
        settings.storage.base_url = active_storage.get("base_url", settings.storage.base_url)
        settings.storage.public_path = active_storage.get("public_path", settings.storage.public_path)
        settings.storage.local_root = active_storage.get("local_root", settings.storage.local_root)
    else:
        # fallback: 从 sys_config 读取
        settings.storage.provider = config_reader.get("storage.provider", settings.storage.provider)  # type: ignore[assignment]
        settings.storage.bucket = config_reader.get("storage.bucket", settings.storage.bucket)
        settings.storage.endpoint = config_reader.get("storage.endpoint", settings.storage.endpoint)
        settings.storage.access_key = config_reader.get("storage.access_key", settings.storage.access_key)
        settings.storage.secret_key = config_reader.get("storage.secret_key", settings.storage.secret_key)
        settings.storage.region = config_reader.get("storage.region", settings.storage.region)
        settings.storage.use_ssl = config_reader.get_bool("storage.use_ssl", settings.storage.use_ssl)
        settings.storage.base_url = config_reader.get("storage.base_url", settings.storage.base_url)
        settings.storage.public_path = config_reader.get("storage.public_path", settings.storage.public_path)
        settings.storage.local_root = config_reader.get("storage.local_root", settings.storage.local_root)

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
