"""
IMPlugin — lifecycle hooks for the instant messaging plugin.

Mirrors hei-gin's ``plugins/plugin-im/plugin.go``.
"""

from __future__ import annotations
import logging

from fastapi import APIRouter, Depends, WebSocket

from sdk.auth import Business, Consumer
from sdk.web.result import failure
from sdk.kernel.plugin import HeiPlugin, PluginInfo
from plugins.plugin_im.ws import CrossHub, GlobalHub, get_global_cross_hub, set_global_cross_hub
from plugins.plugin_im.migrate import register_all_models
from plugins.plugin_im.message.service import MessageService, get_message_service
from plugins.plugin_sys.file.service import FileService, get_file_service

logger = logging.getLogger(__name__)


# ── WebSocket routes ───────────────────────────────────────────────────

ws_router = APIRouter()


@ws_router.get("/uploads/{bucket}/{file_key}")
async def upload_handler(
    bucket: str,
    file_key: str,
    file_service: FileService = Depends(get_file_service),
    message_service: MessageService = Depends(get_message_service),
):
    try:
        return file_service.download_by_key(bucket, file_key)
    except Exception as exc:
        if str(exc) == "未授权/未登录":
            return failure(str(exc), 401)
    try:
        return message_service.serve_uploaded_file(bucket, file_key)
    except Exception as exc:
        return failure(str(exc), 404)


@ws_router.websocket("/api/v1/sys/im/ws")
async def sys_ws_endpoint(websocket: WebSocket, token: str = ""):
    """Business/admin WebSocket endpoint."""
    if not token:
        await websocket.close(code=1008)
        return

    user_id = await Business.get_login_id_by_token(token)
    if not user_id:
        await websocket.close(code=1008)
        return

    ip = websocket.client.host if websocket.client else ""
    await GlobalHub.handle_websocket(websocket, user_id, "BUSINESS", ip)


@ws_router.websocket("/api/v1/c/im/ws")
async def client_ws_endpoint(websocket: WebSocket, token: str = ""):
    """Consumer WebSocket endpoint."""
    if not token:
        await websocket.close(code=1008)
        return

    user_id = await Consumer.get_login_id_by_token(token)
    if not user_id:
        await websocket.close(code=1008)
        return

    ip = websocket.client.host if websocket.client else ""
    await GlobalHub.handle_websocket(websocket, user_id, "CONSUMER", ip)


# ── Plugin class ───────────────────────────────────────────────────────

class IMPlugin(HeiPlugin):
    @classmethod
    def info(cls) -> PluginInfo:
        return PluginInfo(
            name="plugin_im",
            version="1.0.0",
            description="Instant messaging plugin (WebSocket + group chat + friend + broadcast)",
        )

    def on_init(self):
        """Register models and initialize CrossHub."""
        register_all_models()
        ch = CrossHub(GlobalHub)
        set_global_cross_hub(ch)

        logger.info("[IMPlugin] Models registered, CrossHub initialized")

    async def on_start(self):
        """Start background tasks."""
        ch = get_global_cross_hub()
        if ch:
            ch.refresh_redis()
            ch.create_task(GlobalHub.start_online_broadcast())
            if ch._rdb:
                ch.create_task(ch.start_poll_loop())
                ch.create_task(ch.start_heartbeat())
                ch.create_task(ch.start_stale_cleanup())
                ch.create_task(ch.start_msg_list_cleanup())

        logger.info("[IMPlugin] Background tasks started")

    async def on_stop(self):
        """Clean up resources."""
        ch = get_global_cross_hub()
        if ch:
            await ch.close()
        logger.info("[IMPlugin] Stopped")
