from fastapi import APIRouter

from app.modules.auth.router import portal_router as auth_portal_router
from app.modules.banner.portal.router import router as banner_router
from app.modules.user.portal.router import router as profile_router

router = APIRouter(prefix="/portal", tags=["portal"])
router.include_router(auth_portal_router)
router.include_router(profile_router)
router.include_router(banner_router)
