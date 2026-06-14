from __future__ import annotations

import json
import os
from pathlib import Path
from copy import deepcopy
from typing import Any
from urllib.parse import quote
from pydantic import BaseModel, Field


class DatabaseConfig(BaseModel):
    host: str = "localhost"
    port: int = 3306
    user: str = "root"
    password: str = "123456"
    database: str = "hei"
    pool_size: int = 20
    max_overflow: int = 10
    pool_recycle: int = 3600
    pool_pre_ping: bool = True
    pool_timeout: int = 30
    connect_timeout: int = 10
    echo: bool = False

    @property
    def url(self) -> str:
        return (
            f"mysql+pymysql://{self.user}:{quote(self.password)}"
            f"@{self.host}:{self.port}/{self.database}"
        )


class RedisConfig(BaseModel):
    host: str = "localhost"
    port: int = 6379
    password: str = "123456"
    database: int = 1
    max_connections: int = 200
    socket_connect_timeout: int = 10
    socket_timeout: int = 30
    retry_on_timeout: bool = True
    health_check_interval: int = 30

    @property
    def url(self) -> str:
        if self.password:
            return f"redis://:{quote(self.password)}@{self.host}:{self.port}/{self.database}"
        return f"redis://{self.host}:{self.port}/{self.database}"


class TokenConfig(BaseModel):
    expire_seconds: int = 2592000
    token_name: str = "Authorization"


class SM2Config(BaseModel):
    private_key: str = ""
    public_key: str = ""


class UserConfig(BaseModel):
    reset_password: str = "123456"


class CORSConfig(BaseModel):
    allow_origins: list[str] = Field(default_factory=lambda: ["*"])
    allow_methods: list[str] = Field(default_factory=lambda: ["*"])
    allow_headers: list[str] = Field(default_factory=lambda: ["*"])
    allow_credentials: bool = False


class SnowflakeConfig(BaseModel):
    instance: int = 1


class SwaggerConfig(BaseModel):
    enabled: bool = False
    username: str = ""
    password: str = ""


class AuthConfig(BaseModel):
    public_paths: list[str] = Field(
        default_factory=lambda: [
            "/api/v1/public/",
            "/favicon.ico",
            "/docs",
            "/redoc",
            "/openapi.json",
            "/v3/api-docs",
            "/swagger",
            "/swagger.json",
            "/api/v1/swagger/",
        ]
    )
    business_register_enabled: bool = False


class AppConfig(BaseModel):
    name: str = "hei-fastapi"
    version: str = "1.0.0"
    env: str = "dev"
    debug: bool = False
    host: str = "0.0.0.0"
    port: int = 18886
    upload_max_size: int = 52428800
    import_max_file_size_mb: int = 10
    timeout_keep_alive: int = 15
    threadpool_tokens: int = 0


class WSConfig(BaseModel):
    read_buffer_size: int = 1024
    write_buffer_size: int = 1024
    heartbeat_interval: int = 15
    instance_ttl: int = 60
    stale_clean_interval: int = 5
    rate_limit_window: int = 10
    rate_limit_max: int = 30
    dedup_ttl: int = 30
    poll_timeout: int = 2
    pong_timeout: int = 60
    write_timeout: int = 10
    online_broadcast_interval: int = 60
    max_clients_per_ip: int = 10
    max_clients_per_user: int = 3


class LocalStorageConfig(BaseModel):
    upload_folder: str = "./uploads"
    base_url: str = ""


class MinioStorageConfig(BaseModel):
    endpoint: str = ""
    access_key: str = ""
    secret_key: str = ""
    bucket: str = "hei"
    secure: bool = False
    region: str = "us-east-1"
    base_url: str = ""


class S3StorageConfig(BaseModel):
    endpoint: str = ""
    access_key: str = ""
    secret_key: str = ""
    bucket: str = "hei"
    region: str = "us-east-1"
    path_style: bool = True
    base_url: str = ""


class StorageConfig(BaseModel):
    default: str = "LOCAL"
    default_base_url: str = ""
    local: LocalStorageConfig = Field(default_factory=LocalStorageConfig)
    minio: MinioStorageConfig = Field(default_factory=MinioStorageConfig)
    s3: S3StorageConfig = Field(default_factory=S3StorageConfig)


class Settings(BaseModel):
    app: AppConfig = Field(default_factory=AppConfig)
    db: DatabaseConfig = Field(default_factory=DatabaseConfig)
    redis: RedisConfig = Field(default_factory=RedisConfig)
    token: TokenConfig = Field(default_factory=TokenConfig)
    sm2: SM2Config = Field(default_factory=SM2Config)
    auth: AuthConfig = Field(default_factory=AuthConfig)
    cors: CORSConfig = Field(default_factory=CORSConfig)
    user: UserConfig = Field(default_factory=UserConfig)
    snowflake: SnowflakeConfig = Field(default_factory=SnowflakeConfig)
    swagger: SwaggerConfig = Field(default_factory=SwaggerConfig)
    storage: StorageConfig = Field(default_factory=StorageConfig)
    ws: WSConfig = Field(default_factory=WSConfig)
    raw: dict[str, Any] = Field(default_factory=dict)
    loaded_env_files: list[str] = Field(default_factory=list)

    @property
    def user_config(self) -> UserConfig:
        return self.user

    def get_namespace(self, *path: str, default: Any = None) -> Any:
        current: Any = self.raw
        for part in path:
            if not isinstance(current, dict):
                return default
            current = current.get(str(part))
            if current is None:
                return default
        return current

    def get_plugin_settings(self, plugin_name: str, settings_prefix: str | None = None) -> dict[str, Any]:
        if settings_prefix:
            normalized = [segment for segment in settings_prefix.replace(".", "__").split("__") if segment]
            data = self.get_namespace(*[segment.lower() for segment in normalized], default={})
            return dict(data) if isinstance(data, dict) else {}
        data = self.get_namespace("plugins", str(plugin_name).lower(), default={})
        return dict(data) if isinstance(data, dict) else {}

    def validate_runtime(self, redis_required: bool = True) -> None:
        missing: list[str] = []
        _require_string(missing, "app.host", self.app.host)
        _require_positive(missing, "app.port", self.app.port)
        if self.app.threadpool_tokens < 0:
            missing.append("app.threadpool_tokens")
        _require_string(missing, "db.host", self.db.host)
        _require_positive(missing, "db.port", self.db.port)
        _require_string(missing, "db.user", self.db.user)
        _require_string(missing, "db.database", self.db.database)
        _require_positive(missing, "token.expire_seconds", self.token.expire_seconds)
        _require_string(missing, "token.token_name", self.token.token_name)
        if redis_required:
            _require_string(missing, "redis.host", self.redis.host)
            _require_positive(missing, "redis.port", self.redis.port)
        if missing:
            raise ValueError(f"invalid runtime config: {', '.join(missing)}")
        self.validate_production()

    def validate_migration(self) -> None:
        missing: list[str] = []
        _require_string(missing, "db.host", self.db.host)
        _require_positive(missing, "db.port", self.db.port)
        _require_string(missing, "db.user", self.db.user)
        _require_string(missing, "db.database", self.db.database)
        if missing:
            raise ValueError(f"invalid migration config: {', '.join(missing)}")

    def production_warnings(self) -> list[str]:
        if self.app.env.lower() in {"dev", "local", "test"}:
            return []

        warnings: list[str] = []
        if self.app.debug:
            warnings.append("app.debug is enabled")
        if self.swagger.enabled:
            warnings.append("swagger.enabled is enabled")
        if "*" in self.cors.allow_origins:
            warnings.append("cors.allow_origins contains '*'")
        if self.auth.business_register_enabled:
            warnings.append("auth.business_register_enabled is enabled")
        if self.storage.default.upper() == "LOCAL":
            warnings.append("storage.default is LOCAL; use shared/object storage for multi-instance production")
        return warnings

    def validate_production(self) -> None:
        if self.app.env.lower() not in {"prod", "production"}:
            return

        invalid: list[str] = []
        if self.app.debug:
            invalid.append("app.debug must be false")
        if self.swagger.enabled:
            invalid.append("swagger.enabled must be false")
        if "*" in self.cors.allow_origins:
            invalid.append("cors.allow_origins must not contain '*'")
        if self.auth.business_register_enabled:
            invalid.append("auth.business_register_enabled must be false")
        if _is_weak_secret(self.db.password):
            invalid.append("db.password must not use a default/weak value")
        if _is_weak_secret(self.redis.password):
            invalid.append("redis.password must not use a default/weak value")
        if _is_weak_secret(self.user.reset_password):
            invalid.append("user.reset_password must not use a default/weak value")
        if not self.sm2.private_key.strip():
            invalid.append("sm2.private_key is required")
        if not self.sm2.public_key.strip():
            invalid.append("sm2.public_key is required")

        if invalid:
            raise ValueError(f"invalid production config: {', '.join(invalid)}")


def _require_string(missing: list[str], key: str, value: str) -> None:
    if not str(value).strip():
        missing.append(key)


def _require_positive(missing: list[str], key: str, value: int) -> None:
    if value <= 0:
        missing.append(key)


def _is_weak_secret(value: str) -> bool:
    normalized = str(value or "").strip().lower()
    return normalized in {"", "123456", "change-me", "changeme", "password", "admin", "root"}


def _load_dotenv_file(env_path: Path) -> dict[str, str]:
    if not env_path.is_file():
        return {}
    values: dict[str, str] = {}
    for raw_line in env_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def _env_name(values: dict[str, str]) -> str:
    return (
        os.environ.get("APP_ENV")
        or os.environ.get("APP__ENV")
        or values.get("APP_ENV")
        or values.get("APP__ENV")
        or ""
    ).strip()


def _load_dotenv_values() -> dict[str, str]:
    global _LOADED_ENV_FILES
    _LOADED_ENV_FILES = []

    base_path = Path.cwd() / ".env"
    base_values = _load_dotenv_file(base_path)
    if base_path.is_file():
        _LOADED_ENV_FILES.append(str(base_path))
    env = _env_name(base_values)
    if not env:
        return base_values

    env_path = Path.cwd() / f".env.{env}"
    env_values = _load_dotenv_file(env_path)
    if env_path.is_file():
        _LOADED_ENV_FILES.append(str(env_path))
    return {**base_values, **env_values}


def _parse_env_value(raw: str, key_path: list[str]) -> Any:
    value = raw.strip()
    leaf = key_path[-1] if key_path else ""
    if leaf in {"password", "private_key", "public_key", "reset_password", "token_name"}:
        return value
    if value.lower() in {"true", "false"}:
        return value.lower() == "true"
    if value.lower() in {"null", "none"}:
        return None
    if value.startswith(("{", "[")):
        try:
            return json.loads(value)
        except json.JSONDecodeError:
            return value
    try:
        if value.startswith("0") and value not in {"0", "0.0"} and not value.startswith("0."):
            return value
        return int(value)
    except ValueError:
        try:
            return float(value)
        except ValueError:
            return value


def _set_nested(mapping: dict[str, Any], path: list[str], value: Any) -> None:
    current = mapping
    for part in path[:-1]:
        node = current.get(part)
        if not isinstance(node, dict):
            node = {}
            current[part] = node
        current = node
    current[path[-1]] = value


def _deep_merge(target: dict[str, Any], source: dict[str, Any]) -> dict[str, Any]:
    for key, value in source.items():
        if isinstance(value, dict) and isinstance(target.get(key), dict):
            _deep_merge(target[key], value)
        else:
            target[key] = value
    return target


def _build_settings_data() -> dict[str, Any]:
    merged: dict[str, Any] = {}
    values = _load_dotenv_values()
    app_env = _env_name(values)
    if app_env:
        values.setdefault("APP__ENV", app_env)

    for source in (values, os.environ):
        for key, raw in source.items():
            if key == "APP_ENV":
                _set_nested(merged, ["app", "env"], _parse_env_value(str(raw), ["app", "env"]))
                continue
            if "__" not in key:
                continue
            path = key.lower().split("__")
            _set_nested(merged, path, _parse_env_value(str(raw), path))
    merged["raw"] = deepcopy(merged)
    merged["loaded_env_files"] = list(_LOADED_ENV_FILES)
    return merged


_LOADED_ENV_FILES: list[str] = []
settings = Settings.model_validate(_build_settings_data())
