"""
Global application configuration — mirrors hei-gin's ``sdk/config/config.go``.

Now includes:
- ``UserConfig`` (reset_password)
- ``WSConfig`` (WebSocket/IM settings)
- ``StorageConfig`` (default_base_url)
- All missing app fields (import_max_file_size_mb)
"""

from __future__ import annotations

from typing import Any, Optional, List
from urllib.parse import quote
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


# ═════════════════════════════════════════════════════════════════════
# Database
# ═════════════════════════════════════════════════════════════════════

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
        return f"mysql+pymysql://{self.user}:{quote(self.password)}@{self.host}:{self.port}/{self.database}"


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
    """User-level settings (mirrors hei-gin sdk/config/config.go UserConfig)."""
    reset_password: str = "123456"


class CORSConfig(BaseModel):
    allow_origins: List[str] = ["*"]
    allow_methods: List[str] = ["*"]
    allow_headers: List[str] = ["*"]
    allow_credentials: bool = False


class SnowflakeConfig(BaseModel):
    instance: int = 1


class AppConfig(BaseModel):
    name: str = "hei-fastapi"
    version: str = "1.0.0"
    debug: bool = True
    host: str = "0.0.0.0"
    port: int = 8081
    upload_max_size: int = 52428800  # 50MB
    import_max_file_size_mb: int = 10
    timeout_keep_alive: int = 15


# ═════════════════════════════════════════════════════════════════════
# WebSocket / IM config
# ═════════════════════════════════════════════════════════════════════

class WSConfig(BaseModel):
    """WebSocket configuration — mirrors hei-gin config.yaml ``ws:`` section."""
    read_buffer_size: int = 1024
    write_buffer_size: int = 1024
    heartbeat_interval: int = 15          # seconds
    instance_ttl: int = 60               # seconds
    stale_clean_interval: int = 5        # minutes
    rate_limit_window: int = 10          # seconds
    rate_limit_max: int = 30             # messages per window
    dedup_ttl: int = 30                  # seconds
    poll_timeout: int = 2                # seconds
    pong_timeout: int = 60               # seconds
    write_timeout: int = 10              # seconds
    online_broadcast_interval: int = 60  # seconds
    max_clients_per_ip: int = 10
    max_clients_per_user: int = 3


# ═════════════════════════════════════════════════════════════════════
# Storage config  —  mirrors hei-gin sdk/storage/config.go
# ═════════════════════════════════════════════════════════════════════

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
    local: LocalStorageConfig = LocalStorageConfig()
    minio: MinioStorageConfig = MinioStorageConfig()
    s3: S3StorageConfig = S3StorageConfig()


# ═════════════════════════════════════════════════════════════════════
# Main Settings
# ═════════════════════════════════════════════════════════════════════

class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        env_nested_delimiter="__",
        extra="ignore",
    )

    app: AppConfig = AppConfig()
    db: DatabaseConfig = DatabaseConfig()
    redis: RedisConfig = RedisConfig()
    token: TokenConfig = TokenConfig()
    sm2: SM2Config = SM2Config()
    user_config: UserConfig = UserConfig()
    cors: CORSConfig = CORSConfig()
    snowflake: SnowflakeConfig = SnowflakeConfig()
    storage: StorageConfig = StorageConfig()
    ws: WSConfig = WSConfig()

    # Raw holds any extra/inline config fields for plugin-specific settings.
    raw: dict[str, Any] = {}


settings = Settings()
