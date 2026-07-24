import json

from sqlalchemy import select

from app.modules.sys.config.crypto import decrypt_config_value, decrypt_storage_value
from app.modules.sys.config.model import SysConfig
from app.platform.db.session import get_session_factory


class ConfigReader:
    """系统配置读取器，启动时从 sys_config 表全量加载配置到内存缓存。"""

    def __init__(self) -> None:
        self._cache: dict[str, str] = {}
        self._active_storage: dict | None = None

    async def load_all(self) -> None:
        """从 DB 全量加载配置到内存缓存。"""
        cache: dict[str, str] = {}
        factory = get_session_factory()
        async with factory() as db:
            async with db as session:
                stmt = select(SysConfig).where(SysConfig.config_value.isnot(None))
                rows = (await session.execute(stmt)).scalars().all()
                for row in rows:
                    cache[row.config_key] = decrypt_config_value(row.config_key, row.config_value)
        self._cache = cache
        await self._load_active_storage()

    async def reload(self) -> None:
        """重新加载（管理后台修改配置后调用）。"""
        await self.load_all()
        # 重新覆盖 settings，确保实时生效
        from app.lifespan import apply_db_config_overrides

        await apply_db_config_overrides()

    async def _load_active_storage(self) -> None:
        """加载当前启用的存储配置。"""
        from app.modules.sys.config.storage_model import SysStorageConfig

        factory = get_session_factory()
        async with factory() as db:
            async with db as session:
                stmt = select(SysStorageConfig).where(
                    SysStorageConfig.is_default == True  # noqa: E712
                )
                row = (await session.execute(stmt)).scalar_one_or_none()
                if row is not None:
                    self._active_storage = {
                        col.name: decrypt_storage_value(col.name, getattr(row, col.name))
                        for col in row.__table__.columns
                    }
                else:
                    self._active_storage = None

    def get_active_storage(self) -> dict | None:
        return self._active_storage

    def get(self, key: str, default: str | None = None) -> str | None:
        return self._cache.get(key, default)

    def get_int(self, key: str, default: int = 0) -> int:
        val = self._cache.get(key)
        if val is None:
            return default
        try:
            return int(val)
        except (ValueError, TypeError):
            return default

    def get_bool(self, key: str, default: bool = False) -> bool:
        val = self._cache.get(key)
        if val is None:
            return default
        return val.lower() in ("true", "1", "yes")

    def get_list(self, key: str, default: list[str] | None = None) -> list[str]:
        """读取 JSON 数组类型的配置值。"""
        val = self._cache.get(key)
        if val is None:
            return default or []
        try:
            parsed = json.loads(val)
            if isinstance(parsed, list):
                return parsed
            return default or []
        except (json.JSONDecodeError, TypeError):
            return default or []


config_reader = ConfigReader()
