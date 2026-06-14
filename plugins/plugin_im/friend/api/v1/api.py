"""
Friend API routes — both sys (admin) and client (consumer).

Mirrors hei-gin plugins/plugin-im/friend/api/v1/api.go.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends, Request
from fastapi import Query as QueryParam

from plugins.plugin_im.friend.params import PendingRequestsResult
from sdk.auth.enums import RealmID
from sdk.auth import Business, Consumer
from sdk.auth.decorator import CheckLogin, NoRepeat
from sdk.web.result import success
from plugins.plugin_im.friend.params import (
    BlockParam,
    HandleRequestParam,
    RemarkParam,
    RemoveFriendParam,
    SendRequestParam,
)
from plugins.plugin_im.friend.service import FriendService, get_friend_service

# ── Routers ────────────────────────────────────────────────────────────

sys_router = APIRouter(prefix="/api/v1/sys/im/friend", tags=["IM Friend (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im/friend", tags=["IM Friend (Client)"])


# ═════════════════════════════════════════════════════════════════════
# Sys routes
# ═════════════════════════════════════════════════════════════════════

@sys_router.post("/send-request")
@NoRepeat(3000)
@CheckLogin
async def send_request_handler(
    request: Request,
    p: SendRequestParam,
    service: FriendService = Depends(get_friend_service),
):
    service.send_request((await Business.get_login_id(request)) or "", "BUSINESS", p)
    return success()


@sys_router.post("/accept")
@CheckLogin
async def accept_handler(
    request: Request,
    p: HandleRequestParam,
    service: FriendService = Depends(get_friend_service),
):
    service.accept_request((await Business.get_login_id(request)) or "", "BUSINESS", p)
    return success()


@sys_router.post("/reject")
@CheckLogin
async def reject_handler(
    request: Request,
    p: HandleRequestParam,
    service: FriendService = Depends(get_friend_service),
):
    service.reject_request((await Business.get_login_id(request)) or "", "BUSINESS", p)
    return success()


@sys_router.get("/list")
@CheckLogin
async def list_handler(request: Request, service: FriendService = Depends(get_friend_service)):
    return success(service.friend_list((await Business.get_login_id(request)) or "", "BUSINESS"))


@sys_router.get("/pending-requests")
@CheckLogin
async def pending_handler(request: Request, service: FriendService = Depends(get_friend_service)):
    incoming, outgoing = service.pending_requests((await Business.get_login_id(request)) or "", "BUSINESS")
    return success(PendingRequestsResult(incoming=incoming, outgoing=outgoing))


@sys_router.post("/remove")
@CheckLogin
async def remove_handler(
    request: Request,
    p: RemoveFriendParam,
    service: FriendService = Depends(get_friend_service),
):
    service.remove_friend(
        (await Business.get_login_id(request)) or "",
        "BUSINESS",
        p.friend_id,
        p.friend_type,
    )
    return success()


@sys_router.post("/block")
@CheckLogin
async def block_handler(
    request: Request,
    p: BlockParam,
    service: FriendService = Depends(get_friend_service),
):
    service.block_user((await Business.get_login_id(request)) or "", "BUSINESS", p.blocked_id, p.blocked_type)
    return success()


@sys_router.post("/unblock")
@CheckLogin
async def unblock_handler(
    request: Request,
    p: BlockParam,
    service: FriendService = Depends(get_friend_service),
):
    service.unblock_user((await Business.get_login_id(request)) or "", "BUSINESS", p.blocked_id, p.blocked_type)
    return success()


@sys_router.get("/block-list")
@CheckLogin
async def block_list_handler(request: Request, service: FriendService = Depends(get_friend_service)):
    return success(service.block_list((await Business.get_login_id(request)) or "", "BUSINESS"))


@sys_router.post("/remark")
@CheckLogin
async def remark_handler(
    request: Request,
    p: RemarkParam,
    service: FriendService = Depends(get_friend_service),
):
    service.update_friend_remark(
        (await Business.get_login_id(request)) or "",
        "BUSINESS",
        p.friend_id,
        p.friend_type,
        p.remark,
    )
    return success()


@sys_router.get("/search")
@CheckLogin
def search_handler(
    keyword: str = QueryParam(""),
    size: int = QueryParam(20),
    service: FriendService = Depends(get_friend_service),
):
    results = service.search_users(keyword, size)
    return success(results)


# ═════════════════════════════════════════════════════════════════════
# Client routes
# ═════════════════════════════════════════════════════════════════════

@client_router.post("/send-request")
@NoRepeat(3000)
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_send_request_handler(
    request: Request,
    p: SendRequestParam,
    service: FriendService = Depends(get_friend_service),
):
    service.send_request((await Consumer.get_login_id(request)) or "", "CONSUMER", p)
    return success()


@client_router.post("/accept")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_accept_handler(
    request: Request,
    p: HandleRequestParam,
    service: FriendService = Depends(get_friend_service),
):
    service.accept_request((await Consumer.get_login_id(request)) or "", "CONSUMER", p)
    return success()


@client_router.post("/reject")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_reject_handler(
    request: Request,
    p: HandleRequestParam,
    service: FriendService = Depends(get_friend_service),
):
    service.reject_request((await Consumer.get_login_id(request)) or "", "CONSUMER", p)
    return success()


@client_router.get("/list")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_list_handler(request: Request, service: FriendService = Depends(get_friend_service)):
    return success(service.friend_list((await Consumer.get_login_id(request)) or "", "CONSUMER"))


@client_router.get("/pending-requests")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_pending_handler(request: Request, service: FriendService = Depends(get_friend_service)):
    incoming, outgoing = service.pending_requests((await Consumer.get_login_id(request)) or "", "CONSUMER")
    return success(PendingRequestsResult(incoming=incoming, outgoing=outgoing))


@client_router.post("/remove")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_remove_handler(
    request: Request,
    p: RemoveFriendParam,
    service: FriendService = Depends(get_friend_service),
):
    service.remove_friend(
        (await Consumer.get_login_id(request)) or "",
        "CONSUMER",
        p.friend_id,
        p.friend_type,
    )
    return success()


@client_router.post("/block")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_block_handler(
    request: Request,
    p: BlockParam,
    service: FriendService = Depends(get_friend_service),
):
    service.block_user((await Consumer.get_login_id(request)) or "", "CONSUMER", p.blocked_id, p.blocked_type)
    return success()


@client_router.post("/unblock")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_unblock_handler(
    request: Request,
    p: BlockParam,
    service: FriendService = Depends(get_friend_service),
):
    service.unblock_user((await Consumer.get_login_id(request)) or "", "CONSUMER", p.blocked_id, p.blocked_type)
    return success()


@client_router.get("/block-list")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_block_list_handler(request: Request, service: FriendService = Depends(get_friend_service)):
    return success(service.block_list((await Consumer.get_login_id(request)) or "", "CONSUMER"))


@client_router.post("/remark")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_remark_handler(
    request: Request,
    p: RemarkParam,
    service: FriendService = Depends(get_friend_service),
):
    service.update_friend_remark(
        (await Consumer.get_login_id(request)) or "",
        "CONSUMER",
        p.friend_id,
        p.friend_type,
        p.remark,
    )
    return success()


@client_router.get("/search")
@CheckLogin(realm_id=RealmID.CONSUMER)
def client_search_handler(
    keyword: str = QueryParam(""),
    size: int = QueryParam(20),
    service: FriendService = Depends(get_friend_service),
):
    results = service.search_users(keyword, size)
    return success(results)
