from fastapi import APIRouter

from config.settings import settings

router = APIRouter()


@router.get("/", summary="Health Check")
async def health_check():
    return {"message": f"{settings.app.name} is running", "version": settings.app.version}
