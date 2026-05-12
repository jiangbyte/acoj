from typing import Optional, List
from urllib.parse import quote
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings, SettingsConfigDict
from core.enums import SoftDeleteEnum


class DatabaseConfig(BaseModel):
    host: str = "localhost"
    port: int = 3306
    user: str = "root"
    password: str = "123456"
    database: str = "hei_data"
    pool_size: int = 20
    max_overflow: int = 10
    pool_recycle: int = 3600
    pool_pre_ping: bool = True
    pool_timeout: int = 30
    connect_timeout: int = 10
    echo: bool = False
    soft_delete_enabled: bool = True
    soft_delete_field: str = "is_deleted"
    soft_delete_value_not_deleted: str = SoftDeleteEnum.NO.value
    soft_delete_value_deleted: str = SoftDeleteEnum.YES.value

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


class JWTConfig(BaseModel):
    secret_key: str = "hei-fastapi-jwt-secret-key-2026-please-change-in-production"
    algorithm: str = "HS256"
    expire_seconds: int = 2592000
    token_name: str = "Authorization"


class SM2Config(BaseModel):
    private_key: str = ""
    public_key: str = ""


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


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        env_nested_delimiter="__",
        extra="ignore"
    )

    app: AppConfig = AppConfig()
    db: DatabaseConfig = DatabaseConfig()
    redis: RedisConfig = RedisConfig()
    jwt: JWTConfig = JWTConfig()
    sm2: SM2Config = SM2Config()
    cors: CORSConfig = CORSConfig()
    snowflake: SnowflakeConfig = SnowflakeConfig()


settings = Settings()
