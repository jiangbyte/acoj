from __future__ import annotations

import json
import os
from pathlib import Path
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
    enabled: bool = True
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
    business_register_enabled: bool = True


class AppConfig(BaseModel):
    name: str = "hei-fastapi"
    version: str = "1.0.0"
    env: str = "dev"
    debug: bool = True
    host: str = "0.0.0.0"
    port: int = 18886
    upload_max_size: int = 52428800
    import_max_file_size_mb: int = 10
    timeout_keep_alive: int = 15


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

    @property
    def user_config(self) -> UserConfig:
        return self.user

    def validate_runtime(self, redis_required: bool = True) -> None:
        missing: list[str] = []
        _require_string(missing, "app.host", self.app.host)
        _require_positive(missing, "app.port", self.app.port)
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

    def validate_migration(self) -> None:
        missing: list[str] = []
        _require_string(missing, "db.host", self.db.host)
        _require_positive(missing, "db.port", self.db.port)
        _require_string(missing, "db.user", self.db.user)
        _require_string(missing, "db.database", self.db.database)
        if missing:
            raise ValueError(f"invalid migration config: {', '.join(missing)}")


def _require_string(missing: list[str], key: str, value: str) -> None:
    if not str(value).strip():
        missing.append(key)


def _require_positive(missing: list[str], key: str, value: int) -> None:
    if value <= 0:
        missing.append(key)


def _load_dotenv_values() -> dict[str, str]:
    env_path = Path.cwd() / ".env"
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
    values = _load_dotenv_values()
    merged: dict[str, Any] = {}
    for source in (values, os.environ):
        for key, raw in source.items():
            if "__" not in key:
                continue
            path = key.lower().split("__")
            _set_nested(merged, path, _parse_env_value(str(raw), path))
    merged.setdefault("raw", {})
    return merged


settings = Settings.model_validate(_build_settings_data())
