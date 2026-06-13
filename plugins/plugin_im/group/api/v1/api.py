"""
Group chat API routes — both sys (admin) and client (consumer).

Mirrors hei-gin plugins/plugin-im/group/api/v1/api.go.
"""

from __future__ import annotations

from datetime import timedelta

from fastapi import APIRouter, Depends, Query as QueryParam, Request

from plugins.plugin_im.group.params import (
    CreateParam,
    HandleJoinRequestParam,
    InviteParam,
    KickParam,
    SendMessageParam,
    SetNicknameParam,
    SetRoleParam,
    TransferOwnerParam,
    UpdateParam,
)
from plugins.plugin_im.group.service import GroupService, get_group_service
from sdk.auth import Business, Consumer
from sdk.auth.decorator import CheckLogin, NoRepeat
from sdk.auth.enums import RealmID
from sdk.log import SysLog
from sdk.web.middleware import RateLimiter
from sdk.web.result import success

sys_router = APIRouter(prefix="/api/v1/sys/im/group", tags=["IM Group (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im/group", tags=["IM Group (Client)"])


async def _sys_user(request: Request) -> tuple[str, str]:
    uid = await Business.get_login_id(request)
    return uid or "", "BUSINESS"


async def _client_user(request: Request) -> tuple[str, str]:
    uid = await Consumer.get_login_id(request)
    return uid or "", "CONSUMER"


@sys_router.post("/create")
@NoRepeat(3000)
@CheckLogin
async def create_handler(
    request: Request,
    p: CreateParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    return success(service.create(uid, ut, p).__dict__)


@sys_router.get("/my-groups")
@CheckLogin
async def my_groups_handler(
    request: Request,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    return success([g.__dict__ for g in service.my_groups(uid, ut)])


@sys_router.get("/detail")
@CheckLogin
def detail_handler(
    request: Request,
    group_id: str = QueryParam(""),
    service: GroupService = Depends(get_group_service),
):
    data = service.detail(group_id)
    return success(data.__dict__ if data else None)


@sys_router.post("/update")
@CheckLogin
async def update_handler(
    request: Request,
    p: UpdateParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.update(uid, ut, p)
    return success()


@sys_router.post("/dissolve")
@SysLog("解散群")
@CheckLogin
async def dissolve_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, _ = await _sys_user(request)
    service.dissolve(uid, p.get("group_id", ""))
    return success()


@sys_router.post("/invite")
@NoRepeat(3000)
@CheckLogin
async def invite_handler(
    request: Request,
    p: InviteParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.invite(uid, ut, p)
    return success()


@sys_router.post("/join")
@CheckLogin
async def join_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.join_group(uid, ut, p.get("group_id", ""))
    return success()


@sys_router.get("/pending-join-requests")
@CheckLogin
def pending_join_requests_handler(
    request: Request,
    group_id: str = QueryParam(""),
    service: GroupService = Depends(get_group_service),
):
    return success(service.pending_join_requests(group_id))


@sys_router.post("/handle-join-request")
@CheckLogin
async def handle_join_request_handler(
    request: Request,
    p: HandleJoinRequestParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.handle_join_request(uid, ut, p)
    return success()


@sys_router.post("/leave")
@CheckLogin
async def leave_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.leave_group(uid, ut, p.get("group_id", ""))
    return success()


@sys_router.post("/kick")
@SysLog("踢出成员")
@CheckLogin
async def kick_handler(
    request: Request,
    p: KickParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.kick(uid, ut, p)
    return success()


@sys_router.post("/set-role")
@SysLog("设置角色")
@CheckLogin
async def set_role_handler(
    request: Request,
    p: SetRoleParam,
    service: GroupService = Depends(get_group_service),
):
    uid, _ = await _sys_user(request)
    service.set_role(uid, p)
    return success()


@sys_router.post("/transfer-owner")
@SysLog("转让群")
@CheckLogin
async def transfer_owner_handler(
    request: Request,
    p: TransferOwnerParam,
    service: GroupService = Depends(get_group_service),
):
    uid, _ = await _sys_user(request)
    service.transfer_owner(uid, p)
    return success()


@sys_router.post("/set-nickname")
@CheckLogin
async def set_nickname_handler(
    request: Request,
    p: SetNicknameParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.set_member_nickname(uid, ut, p)
    return success()


@sys_router.get("/members")
@CheckLogin
def members_handler(
    request: Request,
    group_id: str = QueryParam(""),
    service: GroupService = Depends(get_group_service),
):
    return success([m.__dict__ for m in service.members(group_id)])


@sys_router.get("/messages")
@CheckLogin
def messages_handler(
    request: Request,
    group_id: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: GroupService = Depends(get_group_service),
):
    msgs, has_more = service.messages(group_id, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@sys_router.get("/search")
@CheckLogin
def search_messages_handler(
    request: Request,
    group_id: str = QueryParam(""),
    keyword: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: GroupService = Depends(get_group_service),
):
    msgs, has_more = service.search_messages(group_id, keyword, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@sys_router.get("/search-groups")
@CheckLogin
def search_groups_handler(
    request: Request,
    keyword: str = QueryParam(""),
    size: int = QueryParam(20),
    service: GroupService = Depends(get_group_service),
):
    return success(service.search_groups(keyword, size))


@sys_router.post("/send")
@RateLimiter("sys_group_send", 3, 20)
@NoRepeat(3000)
@CheckLogin
async def send_handler(
    request: Request,
    p: SendMessageParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    return success(service.send_message(uid, ut, p).__dict__)


@sys_router.post("/recall")
@CheckLogin
async def recall_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.recall_message(p.get("group_id", ""), p.get("message_id", ""), uid, ut)
    return success()


@sys_router.post("/mark-read")
@CheckLogin
async def mark_read_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    service.mark_read(p.get("group_id", ""), uid, ut, p.get("message_id", ""))
    return success()


@sys_router.post("/mute")
@SysLog("禁言")
@CheckLogin
async def mute_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    duration = timedelta(minutes=p.get("duration", 60))
    kp = KickParam(
        group_id=p.get("group_id", ""),
        user_id=p.get("user_id", ""),
        user_type=p.get("user_type", ""),
    )
    service.mute_member(uid, ut, kp, duration)
    return success()


@sys_router.post("/unmute")
@SysLog("解禁")
@CheckLogin
async def unmute_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    kp = KickParam(
        group_id=p.get("group_id", ""),
        user_id=p.get("user_id", ""),
        user_type=p.get("user_type", ""),
    )
    service.unmute_member(uid, ut, kp)
    return success()


@client_router.post("/create")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_create_handler(
    request: Request,
    p: CreateParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    return success(service.create(uid, ut, p).__dict__)


@client_router.get("/my-groups")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_my_groups_handler(
    request: Request,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    return success([g.__dict__ for g in service.my_groups(uid, ut)])


@client_router.get("/detail")
@CheckLogin(realm_id=RealmID.CONSUMER)
def client_detail_handler(
    request: Request,
    group_id: str = QueryParam(""),
    service: GroupService = Depends(get_group_service),
):
    data = service.detail(group_id)
    return success(data.__dict__ if data else None)


@client_router.post("/join")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_join_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    service.join_group(uid, ut, p.get("group_id", ""))
    return success()


@client_router.post("/leave")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_leave_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    service.leave_group(uid, ut, p.get("group_id", ""))
    return success()


@client_router.get("/messages")
@CheckLogin(realm_id=RealmID.CONSUMER)
def client_messages_handler(
    request: Request,
    group_id: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: GroupService = Depends(get_group_service),
):
    msgs, has_more = service.messages(group_id, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@client_router.get("/search")
@CheckLogin(realm_id=RealmID.CONSUMER)
def client_search_messages_handler(
    request: Request,
    group_id: str = QueryParam(""),
    keyword: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: GroupService = Depends(get_group_service),
):
    msgs, has_more = service.search_messages(group_id, keyword, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@client_router.get("/search-groups")
@CheckLogin(realm_id=RealmID.CONSUMER)
def client_search_groups_handler(
    request: Request,
    keyword: str = QueryParam(""),
    size: int = QueryParam(20),
    service: GroupService = Depends(get_group_service),
):
    return success(service.search_groups(keyword, size))


@client_router.post("/send")
@RateLimiter("c_group_send", 3, 20)
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_send_handler(
    request: Request,
    p: SendMessageParam,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    return success(service.send_message(uid, ut, p).__dict__)


@client_router.post("/recall")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_recall_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    service.recall_message(p.get("group_id", ""), p.get("message_id", ""), uid, ut)
    return success()


@client_router.post("/mark-read")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_mark_read_handler(
    request: Request,
    p: dict,
    service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    service.mark_read(p.get("group_id", ""), uid, ut, p.get("message_id", ""))
    return success()


@client_router.get("/members")
@CheckLogin(realm_id=RealmID.CONSUMER)
def client_members_handler(
    request: Request,
    group_id: str = QueryParam(""),
    service: GroupService = Depends(get_group_service),
):
    return success([m.__dict__ for m in service.members(group_id)])
