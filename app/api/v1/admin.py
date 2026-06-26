from fastapi import APIRouter

from app.modules.auth.router import admin_router as auth_admin_router
from app.modules.banner.router import router as banner_router
from app.modules.dict.router import router as dict_router
from app.modules.file.router import router as file_router
from app.modules.iam.router import router as iam_router
from app.modules.user.admin.router import router as admin_profile_router

router = APIRouter(prefix="/admin", tags=["admin"])
router.include_router(auth_admin_router, prefix="/auth")
router.include_router(iam_router, prefix="/iam")
router.include_router(file_router, prefix="/file")
router.include_router(admin_profile_router, prefix="/profile")
router.include_router(banner_router, prefix="/banner")
router.include_router(dict_router, prefix="/dict")
