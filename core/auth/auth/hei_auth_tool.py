from contextvars import ContextVar
from datetime import datetime, timedelta, timezone
from typing import Optional, Any, Dict, List, Union
import jwt
import json
from fastapi import Request, HTTPException, status

from config.settings import settings
from core.db.redis import get_client
from core.enums import LoginTypeEnum
from core.constants import TOKEN_PREFIX_LOGIN, SESSION_PREFIX_LOGIN, DISABLE_KEY_LOGIN

TYPE = LoginTypeEnum.LOGIN
TOKEN_PREFIX = TOKEN_PREFIX_LOGIN
SESSION_PREFIX = SESSION_PREFIX_LOGIN


class HeiAuthTool:
    _secret: str = settings.jwt.secret_key
    _algorithm: str = settings.jwt.algorithm
    _expire: int = settings.jwt.expire_seconds
    _token_name: str = settings.jwt.token_name
    _request_var: ContextVar[Optional[Request]] = ContextVar('hei_auth_request', default=None)

    @classmethod
    def init(cls, secret: str = None, algorithm: str = None, expire: int = None, token_name: str = None):
        if secret:
            cls._secret = secret
        if algorithm:
            cls._algorithm = algorithm
        if expire:
            cls._expire = expire
        if token_name:
            cls._token_name = token_name

    @classmethod
    def getLoginType(cls) -> str:
        return TYPE

    @classmethod
    def getTokenName(cls) -> str:
        return cls._token_name

    @classmethod
    def _get_redis(cls):
        return get_client()

    @classmethod
    def _get_token_key(cls, token: str) -> str:
        return f"{TOKEN_PREFIX}{token}"

    @classmethod
    def _get_session_key(cls, user_id: str) -> str:
        return f"{SESSION_PREFIX}{user_id}"

    @classmethod
    def setRequest(cls, request: Request):
        cls._request_var.set(request)

    @classmethod
    def getRequest(cls) -> Optional[Request]:
        return cls._request_var.get()

    @classmethod
    async def setTokenValue(cls, token_value: str, request: Request = None):
        req = request or cls._request_var.get()
        if req:
            req.state.token_value = token_value

    @classmethod
    async def getTokenValue(cls, request: Request = None) -> Optional[str]:
        req = request or cls._request_var.get()
        if not req:
            return None
        token = req.headers.get(cls._token_name)
        return token

    @classmethod
    async def getTokenInfo(cls, request: Request = None) -> Optional[Dict]:
        token = await cls.getTokenValue(request)
        if not token:
            return None
        return await cls._get_token_data(token)

    @classmethod
    async def login(cls, id: Union[str, int], request: Request = None, extra: Dict = None) -> str:
        now = datetime.now(timezone.utc)
        user_id = str(id)
        payload = {
            "sub": user_id,
            "type": TYPE,
            "exp": now + timedelta(seconds=cls._expire),
            "iat": now,
        }
        if extra:
            payload.update(extra)
        
        token = jwt.encode(payload, cls._secret, algorithm=cls._algorithm)
        
        redis_client = cls._get_redis()
        token_data = {
            "user_id": user_id,
            "type": TYPE,
            "created_at": now.isoformat(),
            "extra": extra or {}
        }
        
        await redis_client.setex(
            cls._get_token_key(token),
            cls._expire,
            json.dumps(token_data, ensure_ascii=False)
        )
        
        session_key = cls._get_session_key(user_id)
        await redis_client.setex(
            session_key,
            cls._expire,
            token
        )
        
        return token

    @classmethod
    async def logout(cls, login_id: Union[str, int] = None, request: Request = None):
        if login_id is not None:
            await cls.kickout(login_id)
            return
        
        token = await cls.getTokenValue(request)
        if not token:
            return
        
        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        
        token_data = await redis_client.get(token_key)
        if token_data:
            try:
                data = json.loads(token_data)
                user_id = data.get("user_id")
                if user_id:
                    session_key = cls._get_session_key(user_id)
                    await redis_client.delete(session_key)
            except:
                pass
        
        await redis_client.delete(token_key)

    @classmethod
    async def kickout(cls, login_id: Union[str, int]):
        redis_client = cls._get_redis()
        user_id = str(login_id)
        session_key = cls._get_session_key(user_id)
        token = await redis_client.get(session_key)
        
        if token:
            token_key = cls._get_token_key(token)
            await redis_client.delete(token_key)
        
        await redis_client.delete(session_key)

    @classmethod
    async def isLogin(cls, request: Request = None) -> bool:
        try:
            login_id = await cls.getLoginIdDefaultNull(request)
            return login_id is not None
        except:
            return False

    @classmethod
    async def checkLogin(cls, request: Request = None):
        if not await cls.isLogin(request):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未授权/未登录")

    @classmethod
    async def getLoginId(cls, request: Request = None) -> Optional[str]:
        return await cls.getLoginIdDefaultNull(request)

    @classmethod
    async def getLoginIdDefaultNull(cls, request: Request = None) -> Optional[str]:
        token = await cls.getTokenValue(request)
        if not token:
            return None
        
        payload = await cls._decode_token(token)
        if payload:
            return payload.get("sub")
        return None

    @classmethod
    async def getLoginIdAsString(cls, request: Request = None) -> Optional[str]:
        return await cls.getLoginIdDefaultNull(request)

    @classmethod
    async def getLoginIdAsInt(cls, request: Request = None) -> Optional[int]:
        login_id = await cls.getLoginIdDefaultNull(request)
        if login_id:
            try:
                return int(login_id)
            except:
                return None
        return None

    @classmethod
    async def getLoginIdAsLong(cls, request: Request = None) -> Optional[int]:
        return await cls.getLoginIdAsInt(request)

    @classmethod
    async def getLoginIdByToken(cls, token_value: str) -> Optional[str]:
        if not token_value:
            return None
        payload = await cls._decode_token(token_value)
        if payload:
            return payload.get("sub")
        return None

    @classmethod
    async def _decode_token(cls, token: str) -> Optional[Dict]:
        if not token:
            return None
        
        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        
        exists = await redis_client.exists(token_key)
        if not exists:
            return None
        
        try:
            payload = jwt.decode(token, cls._secret, algorithms=[cls._algorithm])
            return payload
        except jwt.ExpiredSignatureError:
            await redis_client.delete(token_key)
            return None
        except jwt.InvalidTokenError:
            return None

    @classmethod
    async def _get_token_data(cls, token: str) -> Optional[Dict]:
        if not token:
            return None
        
        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        token_data = await redis_client.get(token_key)
        
        if token_data:
            try:
                return json.loads(token_data)
            except:
                return None
        return None

    @classmethod
    async def getExtra(cls, key: str, request: Request = None) -> Optional[Any]:
        token = await cls.getTokenValue(request)
        if not token:
            return None
        
        payload = await cls._decode_token(token)
        if payload:
            return payload.get(key)
        return None

    @classmethod
    async def getSession(cls, request: Request = None) -> Optional[Dict]:
        token = await cls.getTokenValue(request)
        if not token:
            return None
        return await cls._get_token_data(token)

    @classmethod
    async def getTokenSession(cls, request: Request = None) -> Optional[Dict]:
        return await cls.getSession(request)

    @classmethod
    async def getTokenTimeout(cls, request: Request = None) -> int:
        token = await cls.getTokenValue(request)
        if not token:
            return -1
        
        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        ttl = await redis_client.ttl(token_key)
        return ttl if ttl > 0 else -1

    @classmethod
    async def getSessionTimeout(cls, request: Request = None) -> int:
        login_id = await cls.getLoginIdDefaultNull(request)
        if not login_id:
            return -1
        
        redis_client = cls._get_redis()
        session_key = cls._get_session_key(login_id)
        ttl = await redis_client.ttl(session_key)
        return ttl if ttl > 0 else -1

    @classmethod
    async def renewTimeout(cls, timeout: int = None, request: Request = None):
        token = await cls.getTokenValue(request)
        if not token:
            return
        
        new_timeout = timeout or cls._expire
        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        
        await redis_client.expire(token_key, new_timeout)
        
        login_id = await cls.getLoginIdByToken(token)
        if login_id:
            session_key = cls._get_session_key(login_id)
            await redis_client.expire(session_key, new_timeout)

    @classmethod
    async def getTokenValueByLoginId(cls, login_id: Union[str, int]) -> Optional[str]:
        redis_client = cls._get_redis()
        session_key = cls._get_session_key(str(login_id))
        return await redis_client.get(session_key)

    @classmethod
    async def disable(cls, login_id: Union[str, int], time: int):
        redis_client = cls._get_redis()
        disable_key = f"{DISABLE_KEY_LOGIN}{login_id}"
        await redis_client.setex(disable_key, time, "1")

    @classmethod
    async def isDisable(cls, login_id: Union[str, int]) -> bool:
        redis_client = cls._get_redis()
        disable_key = f"{DISABLE_KEY_LOGIN}{login_id}"
        return await redis_client.exists(disable_key) > 0

    @classmethod
    async def checkDisable(cls, login_id: Union[str, int]):
        if await cls.isDisable(login_id):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="账号已被禁用")

    @classmethod
    async def getDisableTime(cls, login_id: Union[str, int]) -> int:
        redis_client = cls._get_redis()
        disable_key = f"{DISABLE_KEY_LOGIN}{login_id}"
        ttl = await redis_client.ttl(disable_key)
        return ttl if ttl > 0 else -1

    @classmethod
    async def untieDisable(cls, login_id: Union[str, int]):
        redis_client = cls._get_redis()
        disable_key = f"{DISABLE_KEY_LOGIN}{login_id}"
        await redis_client.delete(disable_key)
