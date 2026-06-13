"""
Message API routes — sys (admin) and client (consumer).
Mirrors hei-gin plugins/plugin-im/message/api/v1/api.go.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends, File, Form, Query as QueryParam, Request, UploadFile

from plugins.plugin_im.group.service import GroupService, get_group_service
from plugins.plugin_im.message.params import (
    ForwardParam,
    GetOrCreateConversationParam,
    MessagePageParam,
    MessageSendParam,
    RecallParam,
    SearchParam,
    UnreadCountVO,
)
from plugins.plugin_im.message.service import MessageService, get_message_service
from sdk.auth import Business, Consumer
from sdk.auth.decorator import CheckLogin, NoRepeat
from sdk.auth.enums import RealmID
from sdk.web.exception import BusinessException
from sdk.web.middleware import RateLimiter
from sdk.web.result import failure, success

router = APIRouter(prefix="/api/v1/sys/im", tags=["IM Message (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im", tags=["IM Message (Client)"])


async def _sys_user(request: Request) -> tuple[str, str]:
    uid = await Business.get_login_id(request)
    return uid or "", "BUSINESS"


async def _client_user(request: Request) -> tuple[str, str]:
    uid = await Consumer.get_login_id(request)
    return uid or "", "CONSUMER"


@router.get("/message/page")
@CheckLogin
async def message_page_handler(
    request: Request,
    current: int = QueryParam(1),
    size: int = QueryParam(10),
    status: str = QueryParam(""),
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _sys_user(request)
    param = MessagePageParam(current=current, size=size, status=status)
    return service.page_messages(uid, "BUSINESS", param)


@router.get("/message/detail")
@CheckLogin
async def message_detail_handler(
    request: Request,
    id: str = QueryParam(""),
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    data = service.detail_message(id, uid, ut)
    return success(data.__dict__ if data else None)


@router.get("/message/unread-count")
@CheckLogin
async def message_unread_count_handler(
    request: Request,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _sys_user(request)
    return success(UnreadCountVO(count=service.unread_count(uid, "BUSINESS")).__dict__)


@router.post("/message/send")
@RateLimiter("sys_send", 5, 20)
@NoRepeat(3000)
@CheckLogin
async def message_send_handler(
    request: Request,
    p: MessageSendParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    conv_ids = await service.send_message(p, uid, ut)
    data = {}
    if conv_ids:
        data["conversation_id"] = conv_ids[0]
    return success(data)


@router.post("/message/recall")
@CheckLogin
async def message_recall_handler(
    request: Request,
    p: RecallParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    service.recall_message(uid, ut, p)
    return success()


@router.post("/message/forward")
@CheckLogin
async def message_forward_handler(
    request: Request,
    p: ForwardParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    await service.forward_message(uid, ut, p)
    return success()


@router.post("/message/delete")
@CheckLogin
async def message_delete_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _sys_user(request)
    service.remove_messages(uid, p.get("ids", []))
    return success()


@router.get("/message/search")
@CheckLogin
async def message_search_handler(
    request: Request,
    keyword: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _sys_user(request)
    param = SearchParam(keyword=keyword, cursor=cursor, size=size)
    msgs, has_more = service.search_messages(uid, "BUSINESS", param)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@router.post("/message/mark-read")
@CheckLogin
async def message_mark_read_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    service.mark_read(p.get("id", ""), uid, ut)
    return success()


@router.post("/message/mark-all-read")
@CheckLogin
async def message_mark_all_read_handler(
    request: Request,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _sys_user(request)
    service.mark_all_read(uid, "BUSINESS")
    return success()


@router.post("/message/remove")
@CheckLogin
async def message_remove_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _sys_user(request)
    service.remove_messages(uid, p.get("ids", []))
    return success()


@router.get("/conversation/list")
@CheckLogin
async def conversation_list_handler(
    request: Request,
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: MessageService = Depends(get_message_service),
    group_service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    convs, has_more = service.conversations(uid, ut, cursor, size, group_service)
    return success({"records": [c.__dict__ for c in convs], "has_more": has_more})


@router.get("/conversation/messages")
@CheckLogin
async def conversation_messages_handler(
    request: Request,
    conversation_id: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: MessageService = Depends(get_message_service),
    group_service: GroupService = Depends(get_group_service),
):
    uid, _ = await _sys_user(request)
    if conversation_id.startswith("group:"):
        group_id = conversation_id[6:]
        msgs, has_more = group_service.messages(group_id, cursor, size)
        conv_msgs = [
            {
                "id": m.id,
                "sender_id": m.sender_id,
                "sender_type": m.sender_type,
                "content": m.content,
                "msg_type": m.msg_type,
                "extra": m.extra,
                "status": "",
                "file_url": "",
                "created_at": m.created_at,
            }
            for m in msgs
        ]
        return success({"records": conv_msgs, "has_more": has_more})
        msgs, has_more = service.conversation_messages(uid, ut, conversation_id, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@router.post("/conversation/read")
@CheckLogin
async def conversation_read_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
    group_service: GroupService = Depends(get_group_service),
):
    uid, ut = await _sys_user(request)
    conversation_id = p.get("conversation_id", "")
    if conversation_id.startswith("group:"):
        group_service.mark_conversation_read(conversation_id[6:], uid, ut)
    else:
        service.mark_conversation_read(uid, ut, conversation_id)
    return success()


@router.post("/conversation/get-or-create")
@CheckLogin
async def conversation_get_or_create_handler(
    request: Request,
    p: GetOrCreateConversationParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    conversation_id, display_name = service.get_or_create_conversation(uid, ut, p)
    return success({"conversation_id": conversation_id, "display_name": display_name})


@router.post("/file/upload")
@CheckLogin
async def file_upload_handler(
    request: Request,
    file: UploadFile = File(...),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    conversation_id: str = Form(""),
    msg_type: str = Form(""),
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _sys_user(request)
    try:
        result = await service.upload_file(file, uid, ut, engine, bucket, conversation_id, msg_type)
        return success(result.__dict__)
    except BusinessException as e:
        return failure(e.message, e.code)


@client_router.get("/message/page")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_message_page_handler(
    request: Request,
    current: int = QueryParam(1),
    size: int = QueryParam(10),
    status: str = QueryParam(""),
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _client_user(request)
    param = MessagePageParam(current=current, size=size, status=status)
    return service.page_messages(uid, "CONSUMER", param)


@client_router.post("/message/send")
@RateLimiter("c_send", 5, 20)
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_send_handler(
    request: Request,
    p: MessageSendParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _client_user(request)
    conv_ids = await service.send_message(p, uid, ut)
    data = {}
    if conv_ids:
        data["conversation_id"] = conv_ids[0]
    return success(data)


@client_router.post("/message/recall")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_recall_handler(
    request: Request,
    p: RecallParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _client_user(request)
    service.recall_message(uid, ut, p)
    return success()


@client_router.post("/message/forward")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_forward_handler(
    request: Request,
    p: ForwardParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _client_user(request)
    await service.forward_message(uid, ut, p)
    return success()


@client_router.post("/message/delete")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_delete_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _client_user(request)
    service.remove_messages(uid, p.get("ids", []))
    return success()


@client_router.post("/message/remove")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_remove_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _client_user(request)
    service.remove_messages(uid, p.get("ids", []))
    return success()


@client_router.get("/message/search")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_search_handler(
    request: Request,
    keyword: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _client_user(request)
    param = SearchParam(keyword=keyword, cursor=cursor, size=size)
    msgs, has_more = service.search_messages(uid, "CONSUMER", param)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@client_router.post("/message/mark-read")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_mark_read_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _client_user(request)
    service.mark_read(p.get("id", ""), uid, ut)
    return success()


@client_router.post("/message/mark-all-read")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_mark_all_read_handler(
    request: Request,
    service: MessageService = Depends(get_message_service),
):
    uid, _ = await _client_user(request)
    service.mark_all_read(uid, "CONSUMER")
    return success()


@client_router.get("/conversation/list")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_conversation_list_handler(
    request: Request,
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: MessageService = Depends(get_message_service),
    group_service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    convs, has_more = service.conversations(uid, ut, cursor, size, group_service)
    return success({"records": [c.__dict__ for c in convs], "has_more": has_more})


@client_router.get("/conversation/messages")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_conversation_messages_handler(
    request: Request,
    conversation_id: str = QueryParam(""),
    cursor: str = QueryParam(""),
    size: int = QueryParam(20),
    service: MessageService = Depends(get_message_service),
    group_service: GroupService = Depends(get_group_service),
):
    uid, _ = await _client_user(request)
    if conversation_id.startswith("group:"):
        group_id = conversation_id[6:]
        msgs, has_more = group_service.messages(group_id, cursor, size)
        conv_msgs = [
            {
                "id": m.id,
                "sender_id": m.sender_id,
                "sender_type": m.sender_type,
                "content": m.content,
                "msg_type": m.msg_type,
                "extra": m.extra,
                "status": "",
                "file_url": "",
                "created_at": m.created_at,
            }
            for m in msgs
        ]
        return success({"records": conv_msgs, "has_more": has_more})
        msgs, has_more = service.conversation_messages(uid, ut, conversation_id, cursor, size)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})


@client_router.post("/conversation/read")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_conversation_read_handler(
    request: Request,
    p: dict,
    service: MessageService = Depends(get_message_service),
    group_service: GroupService = Depends(get_group_service),
):
    uid, ut = await _client_user(request)
    conversation_id = p.get("conversation_id", "")
    if conversation_id.startswith("group:"):
        group_service.mark_conversation_read(conversation_id[6:], uid, ut)
    else:
        service.mark_conversation_read(uid, ut, conversation_id)
    return success()


@client_router.post("/conversation/get-or-create")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_get_or_create_conversation_handler(
    request: Request,
    p: GetOrCreateConversationParam,
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _client_user(request)
    conversation_id, display_name = service.get_or_create_conversation(uid, ut, p)
    return success({"conversation_id": conversation_id, "display_name": display_name})


@client_router.post("/file/upload")
@CheckLogin(realm_id=RealmID.CONSUMER)
async def client_file_upload_handler(
    request: Request,
    file: UploadFile = File(...),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    conversation_id: str = Form(""),
    msg_type: str = Form(""),
    service: MessageService = Depends(get_message_service),
):
    uid, ut = await _client_user(request)
    try:
        result = await service.upload_file(file, uid, ut, engine, bucket, conversation_id, msg_type)
        return success(result.__dict__)
    except BusinessException as e:
        return failure(e.message, e.code)
