from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class AppSettings(BaseSettings):
    model_config = SettingsConfigDict(extra="ignore")

    name: str = "hei-fastapi"
    host: str = "127.0.0.1"
    port: int = 8000
    debug: bool = True
    timezone: str = "Asia/Shanghai"


class DatabaseSettings(BaseSettings):
    url: str = "postgresql+asyncpg://postgres:postgres@127.0.0.1:5432/hei_fastapi"
    echo: bool = False
    pool_size: int = 10
    max_overflow: int = 20


class RedisSettings(BaseSettings):
    """Redis 配置，支持通过标准 URL 传递账号、密码、库编号等连接信息。"""

    url: str = "redis://localhost:6379/0"
    enabled: bool = False


class AuthSettings(BaseSettings):
    token_name: str = "Authorization"
    token_ttl_seconds: int = 60 * 60 * 24 * 30
    refresh_ttl_seconds: int = 60 * 60 * 24 * 30
    enable_memory_session_fallback: bool = True


class CorsSettings(BaseSettings):
    allow_origins: list[str] = [
        "http://localhost:5173",
        "http://127.0.0.1:5173",
    ]
    allow_credentials: bool = True
    allow_methods: list[str] = ["*"]
    allow_headers: list[str] = ["*"]


class CelerySettings(BaseSettings):
    broker_url: str = "amqp://guest:guest@127.0.0.1:5672//"


class StorageSettings(BaseSettings):
    provider: str = "s3"
    bucket: str = "hei-fastapi"
    endpoint: str = "http://127.0.0.1:9000"
    access_key: str = "minioadmin"
    secret_key: str = "minioadmin"
    region: str = "us-east-1"
    use_ssl: bool = False
    presign_expire_seconds: int = 3600
    base_url: str = ""
    local_root: str = "./storage"


class IdGeneratorSettings(BaseSettings):
    worker_id: int = 1
    datacenter_id: int = 1


class SwaggerSettings(BaseSettings):
    enabled: bool = True


class ObservabilitySettings(BaseSettings):
    enabled: bool = False
    service_name: str = "hei-fastapi"
    service_version: str = "0.1.0"
    environment: str = "dev"
    log_enabled: bool = True
    log_level: str = "INFO"
    log_json: bool = False
    metrics_enabled: bool = False
    metrics_path: str = "/metrics"
    tracing_enabled: bool = False
    otlp_enabled: bool = False
    otlp_endpoint: str = ""
    sample_ratio: float = 1.0
    celery_observability_enabled: bool = False
    db_observability_enabled: bool = False
    http_client_observability_enabled: bool = False


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_nested_delimiter="__",
        extra="ignore",
    )

    app: AppSettings = Field(default_factory=AppSettings)
    db: DatabaseSettings = Field(default_factory=DatabaseSettings)
    redis: RedisSettings = Field(default_factory=RedisSettings)
    auth: AuthSettings = Field(default_factory=AuthSettings)
    cors: CorsSettings = Field(default_factory=CorsSettings)
    celery: CelerySettings = Field(default_factory=CelerySettings)
    storage: StorageSettings = Field(default_factory=StorageSettings)
    id_generator: IdGeneratorSettings = Field(default_factory=IdGeneratorSettings)
    swagger: SwaggerSettings = Field(default_factory=SwaggerSettings)
    observability: ObservabilitySettings = Field(default_factory=ObservabilitySettings)


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
