"""
Broadcast API routes.

Mirrors hei-gin plugins/plugin-im/broadcast/api/v1/api.go 1:1.
"""

from __future__ import annotations

from fastapi import APIRouter, Request, Query as QueryParam

from sdk.auth import HeiAuthTool, HeiClientAuthTool
from sdk.auth.decorator import HeiCheckLogin, HeiClientCheckLogin, NoRepeat
from sdk.log import SysLog
from sdk.web.result import success
from sdk.kernel.plugin import Perm
from sdk.kernel.registry import register_router
from plugins.plugin_im.broadcast import (
    SendBroadcastParam, send, list_broadcasts, unread_list, mark_read, detail,
)

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
    send(uid or "", p)
    return success()


@sys_router.get("/list")
@Perm("sys:im:broadcast:list", "通知列表")
@HeiCheckLogin
async def list_handler(request: Request, cursor: str = QueryParam(""), size: int = QueryParam(20)):
    records, has_more = list_broadcasts(cursor, size)
    return success({"records": [r.__dict__ for r in records], "has_more": has_more})


@sys_router.get("/unread-list")
@HeiCheckLogin
async def unread_list_handler(request: Request):
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    records, _ = unread_list(uid or "", "BUSINESS")
    return success(records)


@sys_router.post("/read")
@HeiCheckLogin
async def read_handler(request: Request, p: dict):
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    mark_read(uid or "", "BUSINESS", p.get("broadcast_id", ""))
    return success()


@sys_router.get("/detail")
@HeiCheckLogin
async def detail_handler(request: Request, id: str = QueryParam("")):
    vo = detail(id)
    return success(vo.__dict__ if vo else None)


# ── Client (C-end) routes ──

@client_router.get("/unread-list")
@HeiClientCheckLogin
async def client_unread_list_handler(request: Request):
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    records, _ = unread_list(uid or "", "CONSUMER")
    return success(records)


@client_router.post("/read")
@HeiClientCheckLogin
async def client_read_handler(request: Request, p: dict):
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    mark_read(uid or "", "CONSUMER", p.get("broadcast_id", ""))
    return success()


@client_router.get("/detail")
@HeiClientCheckLogin
async def client_detail_handler(request: Request, id: str = QueryParam("")):
    vo = detail(id)
    return success(vo.__dict__ if vo else None)


register_router(sys_router)
register_router(client_router)
