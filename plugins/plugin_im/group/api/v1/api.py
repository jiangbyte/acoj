"""
Group chat API routes — both sys (admin) and client (consumer).

Mirrors hei-gin plugins/plugin-im/group/api/v1/api.go.
"""

from __future__ import annotations

from datetime import timedelta
from typing import Optional

from fastapi import APIRouter, Request, Query as QueryParam

from core.auth import HeiAuthTool, HeiClientAuthTool
from core.auth.decorator import HeiCheckLogin, HeiClientCheckLogin, NoRepeat
from core.result import success
from core.plugin.registry import register_router
from plugins.plugin_im.group import (
    CreateParam, UpdateParam, InviteParam, KickParam, SetRoleParam,
    SendMessageParam, HandleJoinRequestParam, TransferOwnerParam, SetNicknameParam,
    create, update_group, dissolve, detail, my_groups,
    invite, join_group, pending_join_requests, handle_join_request,
    leave_group, kick, set_role, transfer_owner, set_member_nickname,
    members, messages, search_messages, search_groups,
    send_message, recall_message, mark_read, mute_member, unmute_member,
)

sys_router = APIRouter(prefix="/api/v1/sys/im/group", tags=["IM Group (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im/group", tags=["IM Group (Client)"])


async def _sys_user(request: Request) -> tuple[str, str]:
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    return uid or "", "BUSINESS"


async def _client_user(request: Request) -> tuple[str, str]:
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    return uid or "", "CONSUMER"


# ═════════════════════════════════════════════════════════════════════
# Sys routes
# ═════════════════════════════════════════════════════════════════════

@sys_router.post("/create")
@NoRepeat(3000)
@HeiCheckLogin
async def create_handler(request: Request, p: CreateParam):
    uid, ut = await _sys_user(request)
    return success(create(uid, ut, p).__dict__)


@sys_router.get("/my-groups")
@HeiCheckLogin
async def my_groups_handler(request: Request):
    uid, ut = await _sys_user(request)
    return success([g.__dict__ for g in my_groups(uid, ut)])


@sys_router.get("/detail")
@HeiCheckLogin
async def detail_handler(request: Request, group_id: str = QueryParam("")):
    return success(detail(group_id).__dict__ if detail(group_id) else None)


@sys_router.post("/update")
@HeiCheckLogin
async def update_handler(request: Request, p: UpdateParam):
    uid, ut = await _sys_user(request)
    update_group(uid, ut, p)
    return success()


@sys_router.post("/dissolve")
@HeiCheckLogin
async def dissolve_handler(request: Request, p: dict):
    uid, _ = await _sys_user(request)
    dissolve(uid, p.get("group_id", ""))
    return success()


@sys_router.post("/invite")
@NoRepeat(3000)
@HeiCheckLogin
async def invite_handler(request: Request, p: InviteParam):
    uid, ut = await _sys_user(request)
    invite(uid, ut, p)
    return success()


@sys_router.post("/join")
@HeiCheckLogin
async def join_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    join_group(uid, ut, p.get("group_id", ""))
    return success()


@sys_router.get("/pending-join-requests")
@HeiCheckLogin
async def pending_join_requests_handler(request: Request, group_id: str = QueryParam("")):
    return success(pending_join_requests(group_id))


@sys_router.post("/handle-join-request")
@HeiCheckLogin
async def handle_join_request_handler(request: Request, p: HandleJoinRequestParam):
    uid, ut = await _sys_user(request)
    handle_join_request(uid, ut, p)
    return success()


@sys_router.post("/leave")
@HeiCheckLogin
async def leave_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    leave_group(uid, ut, p.get("group_id", ""))
    return success()


@sys_router.post("/kick")
@HeiCheckLogin
async def kick_handler(request: Request, p: KickParam):
    uid, ut = await _sys_user(request)
    kick(uid, ut, p)
    return success()


@sys_router.post("/set-role")
@HeiCheckLogin
async def set_role_handler(request: Request, p: SetRoleParam):
    uid, _ = await _sys_user(request)
    set_role(uid, p)
    return success()


@sys_router.post("/transfer-owner")
@HeiCheckLogin
async def transfer_owner_handler(request: Request, p: TransferOwnerParam):
    uid, _ = await _sys_user(request)
    transfer_owner(uid, p)
    return success()


@sys_router.post("/set-nickname")
@HeiCheckLogin
async def set_nickname_handler(request: Request, p: SetNicknameParam):
    uid, ut = await _sys_user(request)
    set_member_nickname(uid, ut, p)
    return success()


@sys_router.get("/members")
@HeiCheckLogin
async def members_handler(request: Request, group_id: str = QueryParam("")):
    return success([m.__dict__ for m in members(group_id)])


@sys_router.get("/messages")
@HeiCheckLogin
async def messages_handler(request: Request, group_id: str = QueryParam(""),
                            cursor: str = QueryParam(""), size: int = QueryParam(20)):
    msgs, has_more = messages(group_id, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@sys_router.get("/search")
@HeiCheckLogin
async def search_messages_handler(request: Request, group_id: str = QueryParam(""),
                                   keyword: str = QueryParam(""), cursor: str = QueryParam(""),
                                   size: int = QueryParam(20)):
    msgs, has_more = search_messages(group_id, keyword, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@sys_router.get("/search-groups")
@HeiCheckLogin
async def search_groups_handler(request: Request, keyword: str = QueryParam(""),
                                 size: int = QueryParam(20)):
    return success(search_groups(keyword, size))


@sys_router.post("/send")
@NoRepeat(3000)
@HeiCheckLogin
async def send_handler(request: Request, p: SendMessageParam):
    uid, ut = await _sys_user(request)
    return success(send_message(uid, ut, p).__dict__)


@sys_router.post("/recall")
@HeiCheckLogin
async def recall_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    recall_message(p.get("group_id", ""), p.get("message_id", ""), uid, ut)
    return success()


@sys_router.post("/mark-read")
@HeiCheckLogin
async def mark_read_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    mark_read(p.get("group_id", ""), uid, ut, p.get("message_id", ""))
    return success()


@sys_router.post("/mute")
@HeiCheckLogin
async def mute_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    duration = timedelta(minutes=p.get("duration", 60))
    kp = KickParam(group_id=p.get("group_id", ""), user_id=p.get("user_id", ""),
                   user_type=p.get("user_type", ""))
    mute_member(uid, ut, kp, duration)
    return success()


@sys_router.post("/unmute")
@HeiCheckLogin
async def unmute_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    kp = KickParam(group_id=p.get("group_id", ""), user_id=p.get("user_id", ""),
                   user_type=p.get("user_type", ""))
    unmute_member(uid, ut, kp)
    return success()


# ═════════════════════════════════════════════════════════════════════
# Client routes
# ═════════════════════════════════════════════════════════════════════

@client_router.post("/create")
@HeiClientCheckLogin
async def client_create_handler(request: Request, p: CreateParam):
    uid, ut = await _client_user(request)
    return success(create(uid, ut, p).__dict__)


@client_router.get("/my-groups")
@HeiClientCheckLogin
async def client_my_groups_handler(request: Request):
    uid, ut = await _client_user(request)
    return success([g.__dict__ for g in my_groups(uid, ut)])


@client_router.get("/detail")
@HeiClientCheckLogin
async def client_detail_handler(request: Request, group_id: str = QueryParam("")):
    return success(detail(group_id).__dict__ if detail(group_id) else None)


@client_router.post("/join")
@HeiClientCheckLogin
async def client_join_handler(request: Request, p: dict):
    uid, ut = await _client_user(request)
    join_group(uid, ut, p.get("group_id", ""))
    return success()


@client_router.post("/leave")
@HeiClientCheckLogin
async def client_leave_handler(request: Request, p: dict):
    uid, ut = await _client_user(request)
    leave_group(uid, ut, p.get("group_id", ""))
    return success()


@client_router.get("/messages")
@HeiClientCheckLogin
async def client_messages_handler(request: Request, group_id: str = QueryParam(""),
                                   cursor: str = QueryParam(""), size: int = QueryParam(20)):
    msgs, has_more = messages(group_id, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@client_router.get("/search")
@HeiClientCheckLogin
async def client_search_messages_handler(request: Request, group_id: str = QueryParam(""),
                                          keyword: str = QueryParam(""), cursor: str = QueryParam(""),
                                          size: int = QueryParam(20)):
    msgs, has_more = search_messages(group_id, keyword, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@client_router.get("/search-groups")
@HeiClientCheckLogin
async def client_search_groups_handler(request: Request, keyword: str = QueryParam(""),
                                        size: int = QueryParam(20)):
    return success(search_groups(keyword, size))


@client_router.post("/send")
@HeiClientCheckLogin
async def client_send_handler(request: Request, p: SendMessageParam):
    uid, ut = await _client_user(request)
    return success(send_message(uid, ut, p).__dict__)


@client_router.post("/recall")
@HeiClientCheckLogin
async def client_recall_handler(request: Request, p: dict):
    uid, ut = await _client_user(request)
    recall_message(p.get("group_id", ""), p.get("message_id", ""), uid, ut)
    return success()


@client_router.post("/mark-read")
@HeiClientCheckLogin
async def client_mark_read_handler(request: Request, p: dict):
    uid, ut = await _client_user(request)
    mark_read(p.get("group_id", ""), uid, ut, p.get("message_id", ""))
    return success()


@client_router.get("/members")
@HeiClientCheckLogin
async def client_members_handler(request: Request, group_id: str = QueryParam("")):
    return success([m.__dict__ for m in members(group_id)])


register_router(sys_router)
register_router(client_router)
