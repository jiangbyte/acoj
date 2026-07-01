from fastapi import APIRouter

from app.modules.auth.router import portal_router as auth_portal_router
from app.modules.iam.resource.portal.router import router as resource_router
from app.modules.sys.banner.portal.router import router as banner_router
from app.modules.sys.dict.portal.router import router as dict_router
from app.modules.message.message.router import portal_router as message_router
from app.modules.message.notification.router import portal_router as notification_router
from app.modules.message.realtime.router import portal_router as message_realtime_router
from app.modules.message.todo.router import portal_router as todo_router
from app.modules.user.portal.router import router as profile_router

router = APIRouter(prefix="/portal", tags=["portal"])
router.include_router(auth_portal_router)
router.include_router(profile_router)
router.include_router(banner_router)
router.include_router(dict_router)
router.include_router(resource_router)
router.include_router(notification_router)
router.include_router(message_router)
router.include_router(todo_router)
router.include_router(message_realtime_router)
