from __future__ import annotations

from fastapi import APIRouter

from sdk.web.result import success

from .service import plugin_service

router = APIRouter(prefix="/debug/plugins", tags=["Plugin Debug"])


@router.get("", summary="插件清单与运行态")
async def list_plugins():
    return success(plugin_service.list_plugins())


@router.get("/extensions", summary="插件扩展点注册表")
async def list_extensions():
    return success(plugin_service.list_extensions())
