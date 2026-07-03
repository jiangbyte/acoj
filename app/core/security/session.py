import json
from collections.abc import Awaitable, Callable
from dataclasses import asdict, dataclass, field
from typing import TypedDict

from app.core.config.enums import AccountType, DataScope
from app.core.config.settings import settings
from app.platform.cache.keys import login_account_tokens_key, login_token_key
from app.platform.cache.redis import get_redis


class PermissionGrantPayload(TypedDict):
    """会话中缓存的权限项结构，避免使用无约束的裸字典。"""

    permission_key: str
    data_scope: DataScope | str
    custom_scope_dept_ids: list[str]
    effect: str
    source_type: str
    source_id: str


@dataclass(slots=True)
class SessionPayload:
    """登录会话载荷，保存鉴权与数据权限判断所需的最小上下文。"""

    token: str
    account_id: str
    account_type: AccountType | str
    role_ids: list[str] = field(default_factory=list)
    dept_ids: list[str] = field(default_factory=list)
    group_ids: list[str] = field(default_factory=list)
    resource_ids: list[str] = field(default_factory=list)
    button_codes: list[str] = field(default_factory=list)
    permission_keys: list[str] = field(default_factory=list)
    permission_grants: list[PermissionGrantPayload] = field(default_factory=list)


class SessionStore:
    """会话存储门面，Redis 是运行必需依赖。"""

    async def set(self, payload: SessionPayload, ttl_seconds: int) -> None:
        """写入登录会话，并同步维护用户到 token 的反向索引。"""
        data = asdict(payload)
        redis = self._get_required_redis()
        await redis.setex(login_token_key(payload.token), ttl_seconds, json.dumps(data))
        await redis.sadd(
            login_account_tokens_key(str(payload.account_type), payload.account_id),
            payload.token,
        )

    async def get(self, token: str) -> SessionPayload | None:
        """按 token 从 Redis 读取会话。"""
        redis = self._get_required_redis()
        raw = await redis.get(login_token_key(token))
        if not raw:
            return None
        raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
        data = json.loads(raw_text)
        return SessionPayload(**data)

    async def get_account_tokens(self, account_type: str, account_id: str) -> list[str]:
        """读取指定账户当前在线 token 列表，用于授权变更后刷新会话权限。"""
        token_map = await self.get_accounts_tokens([(account_type, account_id)])
        return token_map.get((account_type, account_id), [])

    async def get_accounts_tokens(
        self,
        targets: list[tuple[str, str]],
    ) -> dict[tuple[str, str], list[str]]:
        """批量读取账户在线 token，避免多账号授权刷新时逐账号访问 Redis。"""
        redis = self._get_required_redis()
        unique_targets = list(dict.fromkeys(targets))
        if not unique_targets:
            return {}
        pipe = redis.pipeline()
        for account_type, account_id in unique_targets:
            pipe.smembers(login_account_tokens_key(account_type, account_id))
        rows = await pipe.execute()
        return {
            target: [
                value.decode("utf-8") if isinstance(value, bytes) else str(value)
                for value in values
            ]
            for target, values in zip(unique_targets, rows, strict=True)
        }

    async def refresh_account_sessions(
        self,
        account_type: str,
        account_id: str,
        payload_factory: Callable[[str], Awaitable[SessionPayload]],
    ) -> None:
        """刷新某个账户所有在线会话中的授权上下文，保留原 token 不变。"""
        await self.refresh_accounts_sessions(
            [(account_type, account_id)],
            {(account_type, account_id): payload_factory},
        )

    async def refresh_accounts_sessions(
        self,
        targets: list[tuple[str, str]],
        payload_factories: dict[tuple[str, str], Callable[[str], Awaitable[SessionPayload]]],
    ) -> None:
        """批量刷新多个账户在线会话，Redis 读写合并为批量操作。"""
        unique_targets = [
            target
            for target in dict.fromkeys(targets)
            if target in payload_factories
        ]
        if not unique_targets:
            return
        redis = self._get_required_redis()
        token_map = await self.get_accounts_tokens(unique_targets)
        token_targets: list[tuple[str, tuple[str, str]]] = []
        for target in unique_targets:
            token_targets.extend((token, target) for token in token_map.get(target, []))
        if not token_targets:
            return

        token_keys = [login_token_key(token) for token, _ in token_targets]
        existing_sessions = await redis.mget(token_keys)
        pipe = redis.pipeline()
        for (token, target), existing in zip(token_targets, existing_sessions, strict=True):
            if not existing:
                continue
            refreshed = await payload_factories[target](token)
            pipe.setex(
                login_token_key(token),
                settings.auth.token_ttl_seconds,
                json.dumps(asdict(refreshed)),
            )
            pipe.sadd(
                login_account_tokens_key(
                    str(refreshed.account_type),
                    refreshed.account_id,
                ),
                token,
            )
        if pipe.command_stack:
            await pipe.execute()

    async def delete(self, token: str) -> None:
        """删除会话，并在 Redis 模式下同步清理用户维度的 token 索引。"""
        redis = self._get_required_redis()
        payload = await self.get(token)
        await redis.delete(login_token_key(token))
        if payload:
            await redis.srem(
                login_account_tokens_key(str(payload.account_type), payload.account_id), token
            )

    async def delete_account_sessions(self, account_type: str, account_id: str) -> None:
        """删除指定账户的所有在线会话和账户维度 token 索引。"""
        await self.delete_accounts_sessions([(account_type, account_id)])

    async def delete_accounts_sessions(self, targets: list[tuple[str, str]]) -> None:
        """批量删除多个账户的所有在线会话和账户维度 token 索引。"""
        redis = self._get_required_redis()
        unique_targets = list(dict.fromkeys(targets))
        if not unique_targets:
            return
        token_map = await self.get_accounts_tokens(unique_targets)
        keys = {
            login_token_key(token)
            for tokens in token_map.values()
            for token in tokens
        }
        keys.update(
            login_account_tokens_key(account_type, account_id)
            for account_type, account_id in unique_targets
        )
        if keys:
            await redis.delete(*keys)

    def _get_required_redis(self):
        redis = get_redis()
        if redis is None:
            raise RuntimeError("Redis is required for session store")
        return redis


session_store = SessionStore()
