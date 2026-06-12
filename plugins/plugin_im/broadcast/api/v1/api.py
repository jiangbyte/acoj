"""
Broadcast API routes.

Mirrors hei-gin plugins/plugin-im/broadcast/api/v1/api.go 1:1.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends, Request, Query as QueryParam

from sdk.auth import HeiAuthTool, HeiClientAuthTool
from sdk.auth.decorator import HeiCheckLogin, HeiClientCheckLogin, NoRepeat
from sdk.log import SysLog
from sdk.web.result import success
from sdk.kernel.plugin import Perm
from plugins.plugin_im.broadcast.params import SendBroadcastParam
from plugins.plugin_im.broadcast.service import BroadcastService, get_broadcast_service

sys_router = APIRouter(prefix="/api/v1/sys/im/broadcast", tags=["IM Broadcast (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im/broadcast", tags=["IM Broadcast (Client)"])


# ── Sys admin routes ──

@sys_router.post("/send")
@Perm("sys:im:broadcast:send", "发送通知")
@SysLog("发送通知")
@NoRepeat(5000)
@HeiCheckLogin
async def send_handler(request: Request, p: SendBroadcastParam):
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    service = get_broadcast_service()
    service.send(uid or "", p)
    return success()


@sys_router.get("/list")
@Perm("sys:im:broadcast:list", "通知列表")
@HeiCheckLogin
async def list_handler(
    request: Request,
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: BroadcastService = Depends(get_broadcast_service),
):
    records, has_more = service.list(cursor, size)
    return success({"records": [r.__dict__ for r in records], "has_more": has_more})


@sys_router.get("/unread-list")
@HeiCheckLogin
async def unread_list_handler(
    request: Request,
    service: BroadcastService = Depends(get_broadcast_service),
):
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    records, _ = service.unread_list(uid or "", "BUSINESS")
    return success(records)


@sys_router.post("/read")
@HeiCheckLogin
async def read_handler(
    request: Request,
    p: dict,
    service: BroadcastService = Depends(get_broadcast_service),
):
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    service.mark_read(uid or "", "BUSINESS", p.get("broadcast_id", ""))
    return success()


@sys_router.get("/detail")
@HeiCheckLogin
async def detail_handler(
    request: Request,
    id: str = QueryParam(""),
    service: BroadcastService = Depends(get_broadcast_service),
):
    vo = service.detail(id)
    return success(vo.__dict__ if vo else None)


# ── Client (C-end) routes ──

@client_router.get("/unread-list")
@HeiClientCheckLogin
async def client_unread_list_handler(
    request: Request,
    service: BroadcastService = Depends(get_broadcast_service),
):
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    records, _ = service.unread_list(uid or "", "CONSUMER")
    return success(records)


@client_router.post("/read")
@HeiClientCheckLogin
async def client_read_handler(
    request: Request,
    p: dict,
    service: BroadcastService = Depends(get_broadcast_service),
):
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    service.mark_read(uid or "", "CONSUMER", p.get("broadcast_id", ""))
    return success()


@client_router.get("/detail")
@HeiClientCheckLogin
async def client_detail_handler(
    request: Request,
    id: str = QueryParam(""),
    service: BroadcastService = Depends(get_broadcast_service),
):
    vo = service.detail(id)
    return success(vo.__dict__ if vo else None)
