from fastapi import APIRouter

from app.api.v1.admin import router as admin_router
from app.api.v1.internal import router as internal_router
from app.api.v1.portal import router as portal_router
from app.modules.sys.file.public_router import router as public_file_router

router = APIRouter()
router.include_router(public_file_router)
router.include_router(portal_router)
router.include_router(admin_router)
router.include_router(internal_router)
