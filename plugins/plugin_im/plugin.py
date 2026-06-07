"""
IMPlugin — lifecycle hooks for the instant messaging plugin.

Mirrors hei-gin's ``plugins/plugin-im/plugin.go``.
"""

from __future__ import annotations

import asyncio
import logging

from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Request

from core.plugin import HeiPlugin, PluginInfo
from core.plugin.registry import register_router
from core.auth import HeiAuthTool, HeiClientAuthTool
from plugins.plugin_im.ws import GlobalHub, GlobalCrossHub, CrossHub
from plugins.plugin_im.model.migrate import register_all_models

logger = logging.getLogger(__name__)


# ── WebSocket routes ───────────────────────────────────────────────────

ws_router = APIRouter()


@ws_router.websocket("/api/v1/sys/im/ws")
async def sys_ws_endpoint(websocket: WebSocket, token: str = ""):
    """Business/admin WebSocket endpoint."""
    if not token:
        await websocket.close(code=1008)
        return

    user_id = await HeiAuthTool.getLoginIdByToken(token)
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

    user_id = await HeiClientAuthTool.getLoginIdByToken(token)
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
        # Register all IM models for migration
        register_all_models()

        # Register WebSocket routes
        register_router(ws_router)

        # Initialize CrossHub (Redis-backed cross-instance messaging)
        ch = CrossHub(GlobalHub)
        import plugins.plugin_im.ws as ws_mod
        ws_mod.GlobalCrossHub = ch

        logger.info("[IMPlugin] Models registered, CrossHub initialized")

    async def on_start(self):
        """Start background tasks."""
        # Start online count broadcast
        asyncio.ensure_future(GlobalHub.start_online_broadcast())

        # Start CrossHub poll loop and heartbeat if Redis is available
        import plugins.plugin_im.ws as ws_mod
        ch = ws_mod.GlobalCrossHub
        if ch and ch._rdb:
            asyncio.ensure_future(ch.start_poll_loop())
            asyncio.ensure_future(ch.start_heartbeat())

        logger.info("[IMPlugin] Background tasks started")

    async def on_stop(self):
        """Clean up resources."""
        import plugins.plugin_im.ws as ws_mod
        ch = ws_mod.GlobalCrossHub
        if ch:
            await ch.close()
        logger.info("[IMPlugin] Stopped")
