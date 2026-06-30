from fastapi import APIRouter

from app.modules.auth.router import admin_router as auth_admin_router
from app.modules.sys.banner.router import router as banner_router
from app.modules.sys.dict.router import router as dict_router
from app.modules.sys.file.router import router as file_router
from app.modules.iam.account.router import router as account_router
from app.modules.iam.dept.router import router as dept_router
from app.modules.iam.group.router import router as group_router
from app.modules.iam.permission.router import router as permission_router
from app.modules.iam.position.router import router as position_router
from app.modules.iam.resource.router import router as resource_router
from app.modules.iam.role.router import router as role_router
from app.modules.message.message.router import admin_router as message_router
from app.modules.message.notification.router import admin_router as notification_router
from app.modules.message.realtime.router import admin_router as message_realtime_router
from app.modules.message.todo.router import admin_router as todo_router
from app.modules.user.admin.router import router as admin_profile_router

router = APIRouter(prefix="/admin", tags=["admin"])
router.include_router(auth_admin_router)
router.include_router(account_router)
router.include_router(dept_router)
router.include_router(group_router)
router.include_router(role_router)
router.include_router(resource_router)
router.include_router(position_router)
router.include_router(permission_router)
router.include_router(file_router)
router.include_router(admin_profile_router)
router.include_router(banner_router)
router.include_router(dict_router)
router.include_router(notification_router)
router.include_router(message_router)
router.include_router(todo_router)
router.include_router(message_realtime_router)
