from fastapi import FastAPI

from .health import router as health_router
from modules.sys.auth import captcha_router, sm2_public_key_router, username_auth_router
from modules.client.auth.captcha import router as client_captcha_router
from modules.client.auth.username import router as client_username_router
from modules.sys.banner import router as banner_router
from modules.sys.dict import router as dict_router
from modules.sys.group import router as group_router
from modules.sys.notice import router as notice_router
from modules.sys.org import router as org_router
from modules.sys.position import router as position_router
from modules.sys.resource import router as resource_router
from modules.sys.role import router as role_router
from modules.sys.permission import router as permission_router
from modules.sys.user import router as user_router
from modules.client.user import router as client_user_router
from modules.sys.config import router as config_router
from modules.sys.file import router as file_router
from modules.sys.analyze import router as analyze_router
from modules.sys.home import router as home_router
from modules.sys.log import router as log_router
from modules.sys.session import router as sys_session_router
from modules.client.session import router as client_session_router


def setup_routers(app: FastAPI):
    app.include_router(health_router)
    app.include_router(captcha_router)
    app.include_router(sm2_public_key_router)
    app.include_router(username_auth_router)
    app.include_router(client_captcha_router)
    app.include_router(client_username_router)
    app.include_router(banner_router)
    app.include_router(dict_router)
    app.include_router(group_router)
    app.include_router(notice_router)
    app.include_router(org_router)
    app.include_router(position_router)
    app.include_router(resource_router)
    app.include_router(role_router)
    app.include_router(permission_router)
    app.include_router(user_router)
    app.include_router(client_user_router)
    app.include_router(config_router)
    app.include_router(file_router)
    app.include_router(analyze_router)
    app.include_router(home_router)
    app.include_router(log_router)
    app.include_router(sys_session_router)
    app.include_router(client_session_router)
