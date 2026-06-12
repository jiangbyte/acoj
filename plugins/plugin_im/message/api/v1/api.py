"""
Message API routes — sys (admin) and client (consumer).
Mirrors hei-gin plugins/plugin-im/message/api/v1/api.go.
"""
from __future__ import annotations


from fastapi import APIRouter, Request, UploadFile, File, Form, Query as QueryParam
from sdk.auth import HeiAuthTool, HeiClientAuthTool
from sdk.auth.decorator import HeiCheckLogin, HeiClientCheckLogin, NoRepeat
from sdk.web.middleware import RateLimiter
from sdk.web.exception import BusinessException
from sdk.web.result import success, failure
from sdk.kernel.registry import register_router
from sdk.enums import LoginTypeEnum
from plugins.plugin_im.message import (
    MessagePageParam, MessageSendParam, RecallParam, ForwardParam, SearchParam,
    GetOrCreateConversationParam,
    send_message, page_messages, unread_count, detail_message,
    mark_read, mark_conversation_read, mark_all_read, remove_messages,
    recall_message, forward_message, search_messages,
    conversations, conversation_messages, get_or_create_conversation,
    upload_file, UploadFileResult, UnreadCountVO,
)
from plugins.plugin_im.group import messages as group_messages, mark_conversation_read as group_mark_read
router = APIRouter(prefix="/api/v1/sys/im", tags=["IM Message (Sys)"])
client_router = APIRouter(prefix="/api/v1/c/im", tags=["IM Message (Client)"])
async def _sys_user(request: Request) -> tuple[str, str]:
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    return uid or "", "BUSINESS"
async def _client_user(request: Request) -> tuple[str, str]:
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    return uid or "", "CONSUMER"
# ═════════════════════════════════════════════════════════════════════
# Sys Routes
# ═════════════════════════════════════════════════════════════════════
@router.get("/message/page")
@HeiCheckLogin
async def message_page_handler(request: Request, current: int = QueryParam(1),
                                size: int = QueryParam(10), status: str = QueryParam("")):
    uid, _ = await _sys_user(request)
    param = MessagePageParam(current=current, size=size, status=status)
    return page_messages(uid, param)
@router.get("/message/detail")
@HeiCheckLogin
async def message_detail_handler(request: Request, id: str = QueryParam("")):
    return success(detail_message(id).__dict__ if detail_message(id) else None)
@router.get("/message/unread-count")
@HeiCheckLogin
async def message_unread_count_handler(request: Request):
    uid, _ = await _sys_user(request)
    return success(UnreadCountVO(count=unread_count(uid)).__dict__)
@router.post("/message/send")
@RateLimiter("sys_send", 5, 20)
@NoRepeat(3000)
@HeiCheckLogin
async def message_send_handler(request: Request, p: MessageSendParam):
    uid, ut = await _sys_user(request)
    conv_ids = await send_message(p, uid, ut)
    data = {}
    if conv_ids:
        data["conversation_id"] = conv_ids[0]
    return success(data)
@router.post("/message/recall")
@HeiCheckLogin
async def message_recall_handler(request: Request, p: RecallParam):
    uid, ut = await _sys_user(request)
    recall_message(uid, ut, p)
    return success()
@router.post("/message/forward")
@HeiCheckLogin
async def message_forward_handler(request: Request, p: ForwardParam):
    uid, ut = await _sys_user(request)
    await forward_message(uid, ut, p)
    return success()
@router.post("/message/delete")
@HeiCheckLogin
async def message_delete_handler(request: Request, p: dict):
    uid, _ = await _sys_user(request)
    remove_messages(uid, p.get("ids", []))
    return success()
@router.get("/message/search")
@HeiCheckLogin
async def message_search_handler(request: Request, keyword: str = QueryParam(""),
                                  cursor: str = QueryParam(""), size: int = QueryParam(20)):
    uid, _ = await _sys_user(request)
    param = SearchParam(keyword=keyword, cursor=cursor, size=size)
    msgs, has_more = search_messages(uid, param)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})
@router.post("/message/mark-read")
@HeiCheckLogin
async def message_mark_read_handler(request: Request, p: dict):
    mark_read(p.get("id", ""))
    return success()
@router.post("/message/mark-all-read")
@HeiCheckLogin
async def message_mark_all_read_handler(request: Request):
    uid, _ = await _sys_user(request)
    mark_all_read(uid)
    return success()
@router.post("/message/remove")
@HeiCheckLogin
async def message_remove_handler(request: Request, p: dict):
    uid, _ = await _sys_user(request)
    remove_messages(uid, p.get("ids", []))
    return success()
@router.get("/conversation/list")
@HeiCheckLogin
async def conversation_list_handler(request: Request, cursor: str = QueryParam(""),
                                     size: int = QueryParam(20)):
    uid, ut = await _sys_user(request)
    convs, has_more = conversations(uid, ut, cursor, size)
    return success({"records": [c.__dict__ for c in convs], "has_more": has_more})
@router.get("/conversation/messages")
@HeiCheckLogin
async def conversation_messages_handler(request: Request, conversation_id: str = QueryParam(""),
                                         cursor: str = QueryParam(""), size: int = QueryParam(20)):
    uid, _ = await _sys_user(request)
    if conversation_id.startswith("group:"):
        gid = conversation_id[6:]
        msgs, has_more = group_messages(gid, cursor, size)
        conv_msgs = [{
            "id": m.id, "sender_id": m.sender_id, "sender_type": m.sender_type,
            "content": m.content, "msg_type": m.msg_type, "extra": m.extra,
            "status": "", "file_url": "", "created_at": m.created_at,
        } for m in msgs]
        return success({"records": conv_msgs, "has_more": has_more})
    else:
        msgs, has_more = conversation_messages(uid, conversation_id, cursor, size)
        return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})
@router.post("/conversation/read")
@HeiCheckLogin
async def conversation_read_handler(request: Request, p: dict):
    uid, ut = await _sys_user(request)
    cid = p.get("conversation_id", "")
    if cid.startswith("group:"):
        group_mark_read(cid[6:], uid, ut)
    else:
        mark_conversation_read(uid, cid)
    return success()
@router.post("/conversation/get-or-create")
@HeiCheckLogin
async def conversation_get_or_create_handler(request: Request, p: GetOrCreateConversationParam):
    uid, ut = await _sys_user(request)
    cid, display_name = get_or_create_conversation(uid, ut, p)
    return success({"conversation_id": cid, "display_name": display_name})
@router.post("/file/upload")
@HeiCheckLogin
async def file_upload_handler(
    request: Request,
    file: UploadFile = File(...),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    conversation_id: str = Form(""),
    msg_type: str = Form(""),
):
    uid, ut = await _sys_user(request)
    try:
        result = await upload_file(file, uid, ut,
                                   engine_type=engine, bucket=bucket,
                                   conversation_id=conversation_id, msg_type=msg_type)
        return success(result.__dict__)
    except BusinessException as e:
        return failure(e.message, e.code)
# ═════════════════════════════════════════════════════════════════════
# Client Routes
# ═════════════════════════════════════════════════════════════════════
@client_router.get("/message/page")
@HeiClientCheckLogin
async def client_message_page_handler(request: Request, current: int = QueryParam(1),
                                       size: int = QueryParam(10), status: str = QueryParam("")):
    uid, _ = await _client_user(request)
    param = MessagePageParam(current=current, size=size, status=status)
    return page_messages(uid, param)
@client_router.get("/message/detail")
@HeiClientCheckLogin
async def client_message_detail_handler(request: Request, id: str = QueryParam("")):
    return success(detail_message(id).__dict__ if detail_message(id) else None)
@client_router.get("/message/unread-count")
@HeiClientCheckLogin
async def client_message_unread_count_handler(request: Request):
    uid, _ = await _client_user(request)
    return success(UnreadCountVO(count=unread_count(uid)).__dict__)
@client_router.post("/message/send")
@RateLimiter("c_send", 5, 20)
@HeiClientCheckLogin
async def client_send_handler(request: Request, p: MessageSendParam):
    uid, ut = await _client_user(request)
    conv_ids = await send_message(p, uid, ut)
    data = {}
    if conv_ids:
        data["conversation_id"] = conv_ids[0]
    return success(data)
@client_router.post("/message/recall")
@HeiClientCheckLogin
async def client_recall_handler(request: Request, p: RecallParam):
    uid, ut = await _client_user(request)
    recall_message(uid, ut, p)
    return success()
@client_router.post("/message/forward")
@HeiClientCheckLogin
async def client_forward_handler(request: Request, p: ForwardParam):
    uid, ut = await _client_user(request)
    await forward_message(uid, ut, p)
    return success()
@client_router.post("/message/delete")
@HeiClientCheckLogin
async def client_delete_handler(request: Request, p: dict):
    uid, _ = await _client_user(request)
    remove_messages(uid, p.get("ids", []))
    return success()
@client_router.post("/message/remove")
@HeiClientCheckLogin
async def client_remove_handler(request: Request, p: dict):
    uid, _ = await _client_user(request)
    remove_messages(uid, p.get("ids", []))
    return success()
@client_router.get("/message/search")
@HeiClientCheckLogin
async def client_search_handler(request: Request, keyword: str = QueryParam(""),
                                 cursor: str = QueryParam(""), size: int = QueryParam(20)):
    uid, _ = await _client_user(request)
    param = SearchParam(keyword=keyword, cursor=cursor, size=size)
    msgs, has_more = search_messages(uid, param)
    return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})
@client_router.post("/message/mark-read")
@HeiClientCheckLogin
async def client_mark_read_handler(request: Request, p: dict):
    mark_read(p.get("id", ""))
    return success()
@client_router.post("/message/mark-all-read")
@HeiClientCheckLogin
async def client_mark_all_read_handler(request: Request):
    uid, _ = await _client_user(request)
    mark_all_read(uid)
    return success()
@client_router.get("/conversation/list")
@HeiClientCheckLogin
async def client_conversation_list_handler(request: Request, cursor: str = QueryParam(""),
                                            size: int = QueryParam(20)):
    uid, ut = await _client_user(request)
    convs, has_more = conversations(uid, ut, cursor, size)
    return success({"records": [c.__dict__ for c in convs], "has_more": has_more})
@client_router.get("/conversation/messages")
@HeiClientCheckLogin
async def client_conversation_messages_handler(request: Request, conversation_id: str = QueryParam(""),
                                                cursor: str = QueryParam(""), size: int = QueryParam(20)):
    uid, _ = await _client_user(request)
    if conversation_id.startswith("group:"):
        gid = conversation_id[6:]
        msgs, has_more = group_messages(gid, cursor, size)
        conv_msgs = [{
            "id": m.id, "sender_id": m.sender_id, "sender_type": m.sender_type,
            "content": m.content, "msg_type": m.msg_type, "extra": m.extra,
            "status": "", "file_url": "", "created_at": m.created_at,
        } for m in msgs]
        return success({"records": conv_msgs, "has_more": has_more})
    else:
        msgs, has_more = conversation_messages(uid, conversation_id, cursor, size)
        return success({"records": [m.__dict__ for m in msgs], "has_more": has_more})
@client_router.post("/conversation/read")
@HeiClientCheckLogin
async def client_conversation_read_handler(request: Request, p: dict):
    uid, ut = await _client_user(request)
    cid = p.get("conversation_id", "")
    if cid.startswith("group:"):
        group_mark_read(cid[6:], uid, ut)
    else:
        mark_conversation_read(uid, cid)
    return success()
@client_router.post("/conversation/get-or-create")
@HeiClientCheckLogin
async def client_get_or_create_conversation_handler(request: Request, p: GetOrCreateConversationParam):
    uid, ut = await _client_user(request)
    cid, display_name = get_or_create_conversation(uid, ut, p)
    return success({"conversation_id": cid, "display_name": display_name})
@client_router.post("/file/upload")
@HeiClientCheckLogin
async def client_file_upload_handler(
    request: Request,
    file: UploadFile = File(...),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    conversation_id: str = Form(""),
    msg_type: str = Form(""),
):
    uid, ut = await _client_user(request)
    try:
        result = await upload_file(file, uid, ut,
                                   engine_type=engine, bucket=bucket,
                                   conversation_id=conversation_id, msg_type=msg_type)
        return success(result.__dict__)
    except BusinessException as e:
        return failure(e.message, e.code)
register_router(router)
register_router(client_router)
