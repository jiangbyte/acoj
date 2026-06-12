"""
Friend API routes — both sys (admin) and client (consumer).

Mirrors hei-gin plugins/plugin-im/friend/api/v1/api.go.
"""

from __future__ import annotations

from fastapi import APIRouter, Request, Depends
from fastapi import Query as QueryParam

from sdk.enums import LoginTypeEnum
from sdk.auth import HeiAuthTool, HeiClientAuthTool
from sdk.auth.decorator import HeiCheckLogin, HeiClientCheckLogin, NoRepeat
from sdk.web.result import success, failure
from sdk.kernel.registry import register_router
from plugins.plugin_im.friend import (
    SendRequestParam, HandleRequestParam, BlockParam, RemarkParam,
    send_request, accept_request, reject_request,
    friend_list, pending_requests, remove_friend,
    search_users, block_user, unblock_user, block_list, update_friend_remark,
)

# ── Routers ────────────────────────────────────────────────────────────

sys_router = APIRouter(prefix="/api/v1/sys/im/friend", tags=["IM Friend (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im/friend", tags=["IM Friend (Client)"])


async def _sys_user(request: Request) -> tuple[str, str]:
    user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
    return user_id or "", "BUSINESS"


async def _client_user(request: Request) -> tuple[str, str]:
    user_id = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    return user_id or "", "CONSUMER"


# ═════════════════════════════════════════════════════════════════════
# Sys routes
# ═════════════════════════════════════════════════════════════════════

@sys_router.post("/send-request")
@NoRepeat(3000)
@HeiCheckLogin
async def send_request_handler(request: Request, p: SendRequestParam):
    uid, ut = await _sys_user(request)
    send_request(uid, ut, p)
    return success()


@sys_router.post("/accept")
@HeiCheckLogin
async def accept_handler(request: Request, p: HandleRequestParam):
    uid, ut = await _sys_user(request)
    accept_request(uid, ut, p)
    return success()


@sys_router.post("/reject")
@HeiCheckLogin
async def reject_handler(request: Request, p: HandleRequestParam):
    uid, ut = await _sys_user(request)
    reject_request(uid, ut, p)
    return success()


@sys_router.get("/list")
@HeiCheckLogin
async def list_handler(request: Request):
    uid, ut = await _sys_user(request)
    return success(friend_list(uid, ut))


@sys_router.get("/pending-requests")
@HeiCheckLogin
async def pending_handler(request: Request):
    uid, ut = await _sys_user(request)
    incoming, outgoing = pending_requests(uid, ut)
    return success({"incoming": incoming, "outgoing": outgoing})


@sys_router.post("/remove")
@HeiCheckLogin
async def remove_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    remove_friend(uid, ut, p.get("friend_id", ""), p.get("friend_type", ""))
    return success()


@sys_router.post("/block")
@HeiCheckLogin
async def block_handler(request: Request, p: BlockParam):
    uid, ut = await _sys_user(request)
    block_user(uid, ut, p.blocked_id, p.blocked_type)
    return success()


@sys_router.post("/unblock")
@HeiCheckLogin
async def unblock_handler(request: Request, p: BlockParam):
    uid, ut = await _sys_user(request)
    unblock_user(uid, ut, p.blocked_id, p.blocked_type)
    return success()


@sys_router.get("/block-list")
@HeiCheckLogin
async def block_list_handler(request: Request):
    uid, ut = await _sys_user(request)
    return success(block_list(uid, ut))


@sys_router.post("/remark")
@HeiCheckLogin
async def remark_handler(request: Request, p: RemarkParam):
    uid, ut = await _sys_user(request)
    update_friend_remark(uid, ut, p.friend_id, p.friend_type, p.remark)
    return success()


@sys_router.get("/search")
@HeiCheckLogin
async def search_handler(keyword: str = QueryParam(""), size: int = QueryParam(20)):
    results = search_users(keyword, size)
    return success(results)


# ═════════════════════════════════════════════════════════════════════
# Client routes
# ═════════════════════════════════════════════════════════════════════

@client_router.post("/send-request")
@NoRepeat(3000)
@HeiClientCheckLogin
async def client_send_request_handler(request: Request, p: SendRequestParam):
    uid, ut = await _client_user(request)
    send_request(uid, ut, p)
    return success()


@client_router.post("/accept")
@HeiClientCheckLogin
async def client_accept_handler(request: Request, p: HandleRequestParam):
    uid, ut = await _client_user(request)
    accept_request(uid, ut, p)
    return success()


@client_router.post("/reject")
@HeiClientCheckLogin
async def client_reject_handler(request: Request, p: HandleRequestParam):
    uid, ut = await _client_user(request)
    reject_request(uid, ut, p)
    return success()


@client_router.get("/list")
@HeiClientCheckLogin
async def client_list_handler(request: Request):
    uid, ut = await _client_user(request)
    return success(friend_list(uid, ut))


@client_router.get("/pending-requests")
@HeiClientCheckLogin
async def client_pending_handler(request: Request):
    uid, ut = await _client_user(request)
    incoming, outgoing = pending_requests(uid, ut)
    return success({"incoming": incoming, "outgoing": outgoing})


@client_router.post("/remove")
@HeiClientCheckLogin
async def client_remove_handler(request: Request, p: dict):
    uid, ut = await _client_user(request)
    remove_friend(uid, ut, p.get("friend_id", ""), p.get("friend_type", ""))
    return success()


@client_router.post("/block")
@HeiClientCheckLogin
async def client_block_handler(request: Request, p: BlockParam):
    uid, ut = await _client_user(request)
    block_user(uid, ut, p.blocked_id, p.blocked_type)
    return success()


@client_router.post("/unblock")
@HeiClientCheckLogin
async def client_unblock_handler(request: Request, p: BlockParam):
    uid, ut = await _client_user(request)
    unblock_user(uid, ut, p.blocked_id, p.blocked_type)
    return success()


@client_router.get("/block-list")
@HeiClientCheckLogin
async def client_block_list_handler(request: Request):
    uid, ut = await _client_user(request)
    return success(block_list(uid, ut))


@client_router.post("/remark")
@HeiClientCheckLogin
async def client_remark_handler(request: Request, p: RemarkParam):
    uid, ut = await _client_user(request)
    update_friend_remark(uid, ut, p.friend_id, p.friend_type, p.remark)
    return success()


@client_router.get("/search")
@HeiClientCheckLogin
async def client_search_handler(keyword: str = QueryParam(""), size: int = QueryParam(20)):
    results = search_users(keyword, size)
    return success(results)


# ── Auto-register ──────────────────────────────────────────────────────

register_router(sys_router)
register_router(client_router)
