import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from config.settings import settings
from core.db import verify_connection, redis_init, get_redis, dispose, redis_close, SessionLocal
from core.utils import init as sm2_init
from core.auth import HeiAuthTool, HeiClientAuthTool, HeiPermissionInterfaceManager, HeiPermissionInterface
from core.captcha import b_captcha, c_captcha
from modules.sys.auth import init_auth
from modules.client.auth.username import init_auth as init_client_auth
from modules.sys.user import LoginUserApiProvider as BLoginUserApiProvider
from modules.client.user import LoginUserApiProvider as CLoginUserApiProvider

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Verify database connectivity first — fail fast if either is unreachable
    await verify_connection()
    await redis_init()

    sm2_init(settings.sm2.private_key, settings.sm2.public_key)

    HeiAuthTool.init(
        secret=settings.jwt.secret_key,
        expire=settings.jwt.expire_seconds,
        token_name=settings.jwt.token_name
    )
    HeiClientAuthTool.init(
        secret=settings.jwt.secret_key,
        expire=settings.jwt.expire_seconds,
        token_name=settings.jwt.token_name
    )

    # Register permission interface (queries DB at runtime)
    HeiPermissionInterfaceManager.registerInterface(HeiPermissionInterface())

    # Auto-discover permissions from decorators and cache in Redis
    try:
        from core.auth.permission_scan import run_permission_scan
        await run_permission_scan(app)
    except Exception as e:
        logger.warning(f"Permission scan skipped: {e}")

    b_captcha.init(get_redis())
    c_captcha.init(get_redis())

    login_user_api = BLoginUserApiProvider(SessionLocal)
    init_auth(login_user_api)
    client_login_user_api = CLoginUserApiProvider(SessionLocal)
    init_client_auth(client_login_user_api)

    yield

    await redis_close()
    dispose()
