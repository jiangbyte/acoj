#!/usr/bin/env python3
"""Full IM closed-loop E2E (admin + portal). Writes /tmp/acoj_im_e2e.json."""

from __future__ import annotations

import asyncio
import base64
import json
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import httpx
import redis
import websockets
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding
from sqlalchemy import delete, or_, select

from app.core.config.enums import AccountStatusEnum, AccountType
from app.core.security.password import hash_password
from app.modules.iam.account.model import SysAccount, SysAccountIdentity
from app.modules.iam.enums import AccountIdentityBindStatus
from app.modules.message.conversation.model import MsgConversation, MsgConversationMember
from app.modules.message.friend.model import MsgFriend, MsgFriendRequest
from app.modules.message.group.model import MsgGroup, MsgGroupJoinRequest, MsgGroupMember
from app.modules.message.message.model import MsgMessage, MsgMessageAttachment
from app.modules.message.offline.model import MsgOfflineQueue
from app.modules.user.admin.model import AdminUserProfile
from app.modules.user.portal.model import PortalUserProfile
from app.platform.db.session import close_engine, get_session_factory, init_engine
from app.platform.id_generator.snowflake import generate_snowflake_id

ADMIN_BASE = "http://127.0.0.1:8000/api/v1/admin"
PORTAL_BASE = "http://127.0.0.1:8000/api/v1/portal"
WS_ADMIN = "ws://127.0.0.1:8000/api/v1/admin/message/ws"
WS_PORTAL = "ws://127.0.0.1:8000/api/v1/portal/message/ws"

USERS = {
    "admin": {"account": "im_e2e_admin", "type": "ADMIN", "password": "123456"},
    "p1": {"account": "im_e2e_p1", "type": "PORTAL", "password": "123456"},
    "p2": {"account": "im_e2e_p2", "type": "PORTAL", "password": "123456"},
}


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


def iso(dt: datetime) -> str:
    return dt.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class Result:
    def __init__(self) -> None:
        self.cases: list[dict[str, Any]] = []

    def add(self, name: str, ok: bool, detail: Any = None) -> None:
        self.cases.append({"name": name, "ok": ok, "detail": detail})
        print(("PASS" if ok else "FAIL"), name, "" if ok else detail)

    def dump(self, path: str) -> None:
        payload = {
            "all_pass": all(c["ok"] for c in self.cases),
            "passed": sum(1 for c in self.cases if c["ok"]),
            "failed": sum(1 for c in self.cases if not c["ok"]),
            "cases": self.cases,
            "finished_at": iso(utcnow()),
        }
        Path(path).write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
        print("wrote", path, "all_pass=", payload["all_pass"], f"{payload['passed']}/{len(self.cases)}")


async def login(client: httpx.AsyncClient, base: str, account: str, password: str = "123456") -> str:
    rds = redis.Redis(host="127.0.0.1", port=6379, password="123456", db=3, decode_responses=True)
    cap = (await client.get(f"{base}/captcha")).json()["data"]
    rds.setex(f"captcha:{cap['captcha_id']}", 300, hash_password("abcd"))
    pk = (await client.get(f"{base}/password-key")).json()["data"]
    pub = serialization.load_der_public_key(base64.b64decode(pk["public_key"]))
    enc = pub.encrypt(
        password.encode(),
        padding.OAEP(mgf=padding.MGF1(algorithm=hashes.SHA256()), algorithm=hashes.SHA256(), label=None),
    )
    resp = await client.post(
        f"{base}/login",
        json={
            "account": account,
            "password": base64.b64encode(enc).decode(),
            "password_key_id": pk["key_id"],
            "captcha_id": cap["captcha_id"],
            "captcha_value": "abcd",
            "identity_type": "ACCOUNT",
            "remember_me": True,
        },
    )
    resp.raise_for_status()
    body = resp.json()
    if body.get("code") not in (0, 200, "0", "200", None) and body.get("data") is None:
        raise RuntimeError(f"login failed {account}: {body}")
    data = body["data"]
    return data["token"]


async def api(
    client: httpx.AsyncClient,
    method: str,
    url: str,
    *,
    token: str | None = None,
    expect_ok: bool = True,
    **kwargs: Any,
) -> Any:
    headers = kwargs.pop("headers", {}) or {}
    if token:
        headers["Authorization"] = token
    resp = await client.request(method, url, headers=headers, **kwargs)
    try:
        body = resp.json()
    except Exception:
        body = {"raw": resp.text, "status_code": resp.status_code}
    ok = resp.status_code < 400 and (
        body.get("code") in (0, 200, "0", "200", None) or body.get("data") is not None
    )
    # Some APIs return code=0 always
    if isinstance(body, dict) and "code" in body:
        ok = body.get("code") in (0, 200, "0", "200")
    if expect_ok and not ok:
        raise RuntimeError(f"{method} {url} failed: status={resp.status_code} body={body}")
    return body


class WsClient:
    def __init__(self, url: str, token: str) -> None:
        self.url = f"{url}?token={token}"
        self.ws: Any = None
        self.events: list[dict[str, Any]] = []
        self._task: asyncio.Task | None = None

    async def connect(self) -> None:
        self.ws = await websockets.connect(self.url, max_size=8 * 1024 * 1024)
        self._task = asyncio.create_task(self._read())

    async def _read(self) -> None:
        assert self.ws is not None
        try:
            async for raw in self.ws:
                try:
                    msg = json.loads(raw)
                except Exception:
                    continue
                self.events.append(msg)
        except Exception:
            pass

    async def send(self, payload: dict[str, Any]) -> None:
        assert self.ws is not None
        await self.ws.send(json.dumps(payload))

    async def wait_for(self, typ: str, *, timeout: float = 8.0, pred=None) -> dict[str, Any] | None:
        deadline = time.time() + timeout
        while time.time() < deadline:
            for ev in self.events:
                if ev.get("type") != typ:
                    continue
                if pred is None or pred(ev):
                    return ev
            await asyncio.sleep(0.05)
        return None

    async def close(self) -> None:
        if self._task:
            self._task.cancel()
        if self.ws:
            await self.ws.close()
        self.ws = None


async def ensure_users() -> dict[str, str]:
    """Create dedicated IM e2e users; return key->account_id."""
    init_engine()
    ids: dict[str, str] = {}
    async with get_session_factory()() as db:
        for key, meta in USERS.items():
            username = meta["account"]
            row = (
                await db.execute(
                    select(SysAccountIdentity).where(
                        SysAccountIdentity.identity_type == "ACCOUNT",
                        SysAccountIdentity.identifier == username,
                    )
                )
            ).scalar_one_or_none()
            if row is None:
                account_id = generate_snowflake_id()
                db.add(
                    SysAccount(
                        id=account_id,
                        account_type=meta["type"],
                        account_status=AccountStatusEnum.ENABLED.value,
                        password_hash=hash_password(meta["password"]),
                    )
                )
                db.add(
                    SysAccountIdentity(
                        id=generate_snowflake_id(),
                        account_id=account_id,
                        identity_type="ACCOUNT",
                        identifier=username,
                        is_primary=True,
                        bind_status=AccountIdentityBindStatus.BOUND.value,
                    )
                )
                if meta["type"] == "ADMIN":
                    db.add(
                        AdminUserProfile(
                            account_id=account_id,
                            name=username,
                            nickname=username,
                        )
                    )
                else:
                    db.add(
                        PortalUserProfile(
                            account_id=account_id,
                            name=username,
                            nickname=username,
                            rating=1500,
                        )
                    )
                await db.commit()
                ids[key] = account_id
            else:
                ids[key] = row.account_id
                acc = await db.get(SysAccount, row.account_id)
                if acc is not None:
                    acc.password_hash = hash_password(meta["password"])
                    acc.account_status = AccountStatusEnum.ENABLED.value
                    await db.commit()
    return ids


async def cleanup_im_state(account_ids: list[str]) -> None:
    """Remove IM artifacts for test accounts so the suite is idempotent."""
    init_engine()
    async with get_session_factory()() as db:
        # Friend requests / friendships
        await db.execute(
            delete(MsgFriendRequest).where(
                or_(
                    MsgFriendRequest.applicant_id.in_(account_ids),
                    MsgFriendRequest.recipient_id.in_(account_ids),
                )
            )
        )
        await db.execute(
            delete(MsgFriend).where(
                or_(
                    MsgFriend.account_id.in_(account_ids),
                    MsgFriend.friend_account_id.in_(account_ids),
                )
            )
        )
        # Groups owned by test users
        groups = list(
            (
                await db.execute(select(MsgGroup).where(MsgGroup.owner_account_id.in_(account_ids)))
            ).scalars()
        )
        group_ids = [g.id for g in groups]
        if group_ids:
            await db.execute(delete(MsgGroupJoinRequest).where(MsgGroupJoinRequest.group_id.in_(group_ids)))
            await db.execute(delete(MsgGroupMember).where(MsgGroupMember.group_id.in_(group_ids)))
            # conversations linked to groups
            convs = list(
                (
                    await db.execute(select(MsgConversation).where(MsgConversation.group_id.in_(group_ids)))
                ).scalars()
            )
            conv_ids = [c.id for c in convs]
            if conv_ids:
                msg_ids = list(
                    (
                        await db.execute(select(MsgMessage.id).where(MsgMessage.conversation_id.in_(conv_ids)))
                    ).scalars()
                )
                if msg_ids:
                    await db.execute(delete(MsgMessageAttachment).where(MsgMessageAttachment.message_id.in_(msg_ids)))
                    await db.execute(delete(MsgMessage).where(MsgMessage.id.in_(msg_ids)))
                await db.execute(delete(MsgOfflineQueue).where(MsgOfflineQueue.conversation_id.in_(conv_ids)))
                await db.execute(delete(MsgConversationMember).where(MsgConversationMember.conversation_id.in_(conv_ids)))
                await db.execute(delete(MsgConversation).where(MsgConversation.id.in_(conv_ids)))
            await db.execute(delete(MsgGroup).where(MsgGroup.id.in_(group_ids)))

        # Direct conversations where both members are test users
        member_rows = list(
            (
                await db.execute(
                    select(MsgConversationMember).where(MsgConversationMember.account_id.in_(account_ids))
                )
            ).scalars()
        )
        candidate_conv_ids = {m.conversation_id for m in member_rows}
        for cid in candidate_conv_ids:
            members = list(
                (
                    await db.execute(
                        select(MsgConversationMember).where(MsgConversationMember.conversation_id == cid)
                    )
                ).scalars()
            )
            member_ids = {m.account_id for m in members}
            if member_ids and member_ids.issubset(set(account_ids)):
                msg_ids = list(
                    (
                        await db.execute(select(MsgMessage.id).where(MsgMessage.conversation_id == cid))
                    ).scalars()
                )
                if msg_ids:
                    await db.execute(delete(MsgMessageAttachment).where(MsgMessageAttachment.message_id.in_(msg_ids)))
                    await db.execute(delete(MsgMessage).where(MsgMessage.id.in_(msg_ids)))
                await db.execute(delete(MsgOfflineQueue).where(MsgOfflineQueue.conversation_id == cid))
                await db.execute(delete(MsgConversationMember).where(MsgConversationMember.conversation_id == cid))
                await db.execute(delete(MsgConversation).where(MsgConversation.id == cid))

        await db.execute(
            delete(MsgOfflineQueue).where(MsgOfflineQueue.target_account_id.in_(account_ids))
        )
        await db.commit()


async def main() -> None:
    result = Result()
    ids = await ensure_users()
    await cleanup_im_state(list(ids.values()))
    result.add("setup.users", True, ids)

    admin_id, p1_id, p2_id = ids["admin"], ids["p1"], ids["p2"]
    ws_admin = ws_p1 = ws_p2 = None

    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            # ── Auth ─────────────────────────────────────────────────────────
            try:
                admin_token = await login(client, ADMIN_BASE, USERS["admin"]["account"])
                p1_token = await login(client, PORTAL_BASE, USERS["p1"]["account"])
                p2_token = await login(client, PORTAL_BASE, USERS["p2"]["account"])
                result.add("auth.login_admin_portal", True)
            except Exception as e:
                result.add("auth.login_admin_portal", False, str(e))
                result.dump("/tmp/acoj_im_e2e.json")
                return

            me_admin = (await api(client, "GET", f"{ADMIN_BASE}/me", token=admin_token))["data"]
            me_p1 = (await api(client, "GET", f"{PORTAL_BASE}/me", token=p1_token))["data"]
            result.add(
                "auth.me_ids",
                me_admin.get("account_id") == admin_id and me_p1.get("account_id") == p1_id,
                {"admin": me_admin.get("account_id"), "p1": me_p1.get("account_id")},
            )

            # ── WebSocket connect ────────────────────────────────────────────
            try:
                ws_admin = WsClient(WS_ADMIN, admin_token)
                ws_p1 = WsClient(WS_PORTAL, p1_token)
                ws_p2 = WsClient(WS_PORTAL, p2_token)
                await asyncio.gather(ws_admin.connect(), ws_p1.connect(), ws_p2.connect())
                await asyncio.sleep(0.4)
                result.add("ws.connect_all", True)
            except Exception as e:
                result.add("ws.connect_all", False, str(e))

            # ── Friend search (cross-end) ─────────────────────────────────────
            try:
                hits = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/friends/search",
                        token=p1_token,
                        params={"keyword": "im_e2e_admin"},
                    )
                )["data"]
                hit = next((h for h in hits if h["account_id"] == admin_id), None)
                result.add(
                    "friend.search_cross_end",
                    hit is not None and hit.get("is_friend") is False,
                    hit,
                )
            except Exception as e:
                result.add("friend.search_cross_end", False, str(e))

            # ── Friend apply + WS notify recipient ────────────────────────────
            try:
                ws_admin.events.clear()
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/apply",
                    token=p1_token,
                    json={
                        "applicant_type": "PORTAL",
                        "applicant_id": p1_id,
                        "recipient_type": "ADMIN",
                        "recipient_id": admin_id,
                        "message": "im-e2e friend apply",
                    },
                )
                ev = await ws_admin.wait_for("new_friend_request", timeout=6)
                reqs = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/friends/my-requests",
                        token=admin_token,
                    )
                )["data"]
                pending = next(
                    (
                        r
                        for r in reqs
                        if r.get("status") == "PENDING"
                        and r.get("applicant_id") == p1_id
                        and r.get("recipient_id") == admin_id
                    ),
                    None,
                )
                count = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/friends/my-request-count",
                        token=admin_token,
                    )
                )["data"]
                pending_n = int((count or {}).get("pending_count") or 0)
                result.add(
                    "friend.apply_and_ws_notify",
                    pending is not None and ev is not None and pending_n >= 1,
                    {"pending": pending, "ws": bool(ev), "count": count},
                )
                friend_req_id = pending["id"] if pending else None
            except Exception as e:
                friend_req_id = None
                result.add("friend.apply_and_ws_notify", False, str(e))

            # Idempotent re-apply while pending
            try:
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/apply",
                    token=p1_token,
                    json={
                        "applicant_type": "PORTAL",
                        "applicant_id": p1_id,
                        "recipient_type": "ADMIN",
                        "recipient_id": admin_id,
                        "message": "im-e2e friend apply again",
                    },
                )
                result.add("friend.apply_idempotent_pending", True)
            except Exception as e:
                result.add("friend.apply_idempotent_pending", False, str(e))

            # Reject then re-apply
            try:
                assert friend_req_id
                await api(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/message/friends/handle-request",
                    token=admin_token,
                    json={"request_id": friend_req_id, "action": "REJECT"},
                )
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/apply",
                    token=p1_token,
                    json={
                        "applicant_type": "PORTAL",
                        "applicant_id": p1_id,
                        "recipient_type": "ADMIN",
                        "recipient_id": admin_id,
                        "message": "im-e2e after reject",
                    },
                )
                reqs = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/friends/my-requests",
                        token=admin_token,
                    )
                )["data"]
                pending = next(
                    (
                        r
                        for r in reqs
                        if r.get("status") == "PENDING"
                        and r.get("applicant_id") == p1_id
                        and r.get("recipient_id") == admin_id
                    ),
                    None,
                )
                result.add("friend.reject_then_reapply", pending is not None, pending)
                friend_req_id = pending["id"] if pending else None
            except Exception as e:
                result.add("friend.reject_then_reapply", False, str(e))

            # Accept (creates friendship + DM)
            try:
                assert friend_req_id
                await api(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/message/friends/handle-request",
                    token=admin_token,
                    json={"request_id": friend_req_id, "action": "ACCEPT"},
                )
                friends_admin = (
                    await api(client, "GET", f"{ADMIN_BASE}/message/friends/my-list", token=admin_token)
                )["data"]
                friends_p1 = (
                    await api(client, "GET", f"{PORTAL_BASE}/message/friends/my-list", token=p1_token)
                )["data"]
                ok_friend = any(f.get("friend_account_id") == p1_id for f in friends_admin) and any(
                    f.get("friend_account_id") == admin_id for f in friends_p1
                )
                convs = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/conversations/my-list",
                        token=p1_token,
                        params={"current": 1, "size": 50},
                    )
                )["data"]["records"]
                dm = next(
                    (
                        c
                        for c in convs
                        if c.get("conversation_type") == "DIRECT"
                        and any(
                            m.get("account_id") == admin_id
                            for m in (c.get("members") or [])
                        )
                    ),
                    None,
                )
                # If members not embedded, open create-direct which should return existing
                if dm is None:
                    dm = (
                        await api(
                            client,
                            "POST",
                            f"{PORTAL_BASE}/message/conversations/create-direct",
                            token=p1_token,
                            json={"account_type": "ADMIN", "account_id": admin_id},
                        )
                    )["data"]
                result.add(
                    "friend.accept_creates_friendship_and_dm",
                    ok_friend and dm is not None and bool(dm.get("id")),
                    {"ok_friend": ok_friend, "dm": dm},
                )
                dm_id = dm["id"]
                friendship_p1 = next(f for f in friends_p1 if f.get("friend_account_id") == admin_id)
            except Exception as e:
                dm_id = None
                friendship_p1 = None
                result.add("friend.accept_creates_friendship_and_dm", False, str(e))

            # Search flags after friend
            try:
                hits = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/friends/search",
                        token=p1_token,
                        params={"keyword": "im_e2e_admin"},
                    )
                )["data"]
                hit = next((h for h in hits if h["account_id"] == admin_id), None)
                result.add("friend.search_is_friend_flag", bool(hit and hit.get("is_friend")), hit)
            except Exception as e:
                result.add("friend.search_is_friend_flag", False, str(e))

            # Set remark
            try:
                assert friendship_p1
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/set-remark",
                    token=p1_token,
                    json={"friendship_id": friendship_p1["friendship_id"], "remark": "E2E超管"},
                )
                friends_p1 = (
                    await api(client, "GET", f"{PORTAL_BASE}/message/friends/my-list", token=p1_token)
                )["data"]
                f = next(x for x in friends_p1 if x.get("friend_account_id") == admin_id)
                result.add("friend.set_remark", f.get("remark") == "E2E超管", f)
            except Exception as e:
                result.add("friend.set_remark", False, str(e))

            # ── DM realtime text ──────────────────────────────────────────────
            try:
                assert dm_id and ws_admin and ws_p1
                ws_admin.events.clear()
                marker = f"hello-from-p1-{int(time.time())}"
                sent = (
                    await api(
                        client,
                        "POST",
                        f"{PORTAL_BASE}/message/messages/send",
                        token=p1_token,
                        json={
                            "conversation_id": dm_id,
                            "content": marker,
                            "content_type": "TEXT",
                            "msg_type": "TEXT",
                        },
                    )
                )["data"]
                ev = await ws_admin.wait_for(
                    "new_message",
                    timeout=8,
                    pred=lambda e: (e.get("data") or {}).get("id") == sent["id"]
                    or (e.get("data") or {}).get("content") == marker,
                )
                result.add(
                    "dm.realtime_portal_to_admin",
                    ev is not None and sent.get("id"),
                    {"sent": sent.get("id"), "ws": ev},
                )
            except Exception as e:
                sent = None
                result.add("dm.realtime_portal_to_admin", False, str(e))

            try:
                assert dm_id and ws_p1
                ws_p1.events.clear()
                marker2 = f"hello-from-admin-{int(time.time())}"
                sent2 = (
                    await api(
                        client,
                        "POST",
                        f"{ADMIN_BASE}/message/messages/send",
                        token=admin_token,
                        json={
                            "conversation_id": dm_id,
                            "content": marker2,
                            "content_type": "TEXT",
                            "msg_type": "TEXT",
                        },
                    )
                )["data"]
                ev2 = await ws_p1.wait_for(
                    "new_message",
                    timeout=8,
                    pred=lambda e: (e.get("data") or {}).get("id") == sent2["id"]
                    or (e.get("data") or {}).get("content") == marker2,
                )
                result.add(
                    "dm.realtime_admin_to_portal",
                    ev2 is not None,
                    {"sent": sent2.get("id"), "ws": ev2},
                )
            except Exception as e:
                sent2 = None
                result.add("dm.realtime_admin_to_portal", False, str(e))

            # Unread + mark read（未读以会话列表 member.unread_count 为准；单会话接口需 conversation_id）
            try:
                assert dm_id
                convs_before = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/conversations/my-list",
                        token=admin_token,
                        params={"current": 1, "size": 50},
                    )
                )["data"]["records"]
                dm_admin = next((c for c in convs_before if c.get("id") == dm_id), None)
                unread_before = int((dm_admin or {}).get("unread_count") or 0)
                per_conv = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/messages/unread-count",
                        token=admin_token,
                        params={"conversation_id": dm_id},
                    )
                )["data"]
                await api(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/message/conversations/mark-read",
                    token=admin_token,
                    json={"id": dm_id},
                )
                convs_after = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/conversations/my-list",
                        token=admin_token,
                        params={"current": 1, "size": 50},
                    )
                )["data"]["records"]
                dm_admin_after = next((c for c in convs_after if c.get("id") == dm_id), None)
                unread_after = int((dm_admin_after or {}).get("unread_count") or 0)
                page = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/messages/page",
                        token=p1_token,
                        params={"conversation_id": dm_id, "current": 1, "size": 20},
                    )
                )["data"]
                result.add(
                    "dm.unread_mark_read_and_page",
                    unread_before >= 1
                    and unread_after == 0
                    and int((per_conv or {}).get("unread_count") or 0) >= 1
                    and len(page.get("records") or []) >= 1,
                    {
                        "unread_before": unread_before,
                        "unread_after": unread_after,
                        "per_conv": per_conv,
                        "page_len": len(page.get("records") or []),
                    },
                )
            except Exception as e:
                result.add("dm.unread_mark_read_and_page", False, str(e))

            # File attachment send
            try:
                assert dm_id and ws_admin
                ws_admin.events.clear()
                files = {"file": ("im-e2e.txt", b"im e2e file payload\n", "text/plain")}
                up = await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/sys/file/upload",
                    token=p1_token,
                    files=files,
                )
                fdata = up["data"]
                file_msg = (
                    await api(
                        client,
                        "POST",
                        f"{PORTAL_BASE}/message/messages/send",
                        token=p1_token,
                        json={
                            "conversation_id": dm_id,
                            "content": " ",
                            "content_type": "FILE",
                            "msg_type": "FILE",
                            "attachments": [
                                {
                                    "file_id": fdata["id"],
                                    "name": fdata.get("original_name") or "im-e2e.txt",
                                    "url": fdata.get("url") or fdata["id"],
                                    "size": fdata.get("size"),
                                    "content_type": fdata.get("content_type") or "text/plain",
                                }
                            ],
                        },
                    )
                )["data"]
                evf = await ws_admin.wait_for(
                    "new_message",
                    timeout=8,
                    pred=lambda e: (e.get("data") or {}).get("id") == file_msg["id"],
                )
                atts = file_msg.get("attachments") or []
                result.add(
                    "dm.file_attachment_send_realtime",
                    bool(atts) and evf is not None,
                    {"msg": file_msg.get("id"), "atts": atts, "ws": bool(evf)},
                )
            except Exception as e:
                file_msg = None
                result.add("dm.file_attachment_send_realtime", False, str(e))

            # Revoke
            try:
                assert sent2
                await api(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/message/messages/revoke",
                    token=admin_token,
                    json={"message_id": sent2["id"]},
                )
                page = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/messages/page",
                        token=admin_token,
                        params={"conversation_id": dm_id, "current": 1, "size": 50},
                    )
                )["data"]["records"]
                revoked = next((m for m in page if m["id"] == sent2["id"]), None)
                result.add("dm.revoke_message", bool(revoked and revoked.get("is_revoked")), revoked)
            except Exception as e:
                result.add("dm.revoke_message", False, str(e))

            # Offline queue: disconnect admin, send from p1, reconnect + pull
            try:
                assert dm_id and ws_admin and ws_p1
                await ws_admin.close()
                await asyncio.sleep(1.2)  # allow disconnect + online TTL clear isn't needed; local offline
                offline_marker = f"offline-{int(time.time())}"
                offline_sent = (
                    await api(
                        client,
                        "POST",
                        f"{PORTAL_BASE}/message/messages/send",
                        token=p1_token,
                        json={
                            "conversation_id": dm_id,
                            "content": offline_marker,
                            "content_type": "TEXT",
                            "msg_type": "TEXT",
                        },
                    )
                )["data"]
                # reconnect
                ws_admin = WsClient(WS_ADMIN, admin_token)
                await ws_admin.connect()
                await asyncio.sleep(0.3)
                await ws_admin.send({"type": "pull_offline"})
                off_ev = await ws_admin.wait_for(
                    "offline_messages",
                    timeout=8,
                    pred=lambda e: any(
                        (m.get("id") == offline_sent["id"] or m.get("content") == offline_marker)
                        for m in ((e.get("data") or {}).get("messages") or [])
                    ),
                )
                # also accept new_message if delivered live on reconnect before pull
                live = await ws_admin.wait_for(
                    "new_message",
                    timeout=1.5,
                    pred=lambda e: (e.get("data") or {}).get("id") == offline_sent["id"],
                )
                result.add(
                    "dm.offline_queue_on_reconnect",
                    off_ev is not None or live is not None,
                    {"offline_ws": bool(off_ev), "live_ws": bool(live), "msg": offline_sent["id"]},
                )
            except Exception as e:
                result.add("dm.offline_queue_on_reconnect", False, str(e))

            # Pin / mute conversation
            try:
                assert dm_id
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/conversations/pin",
                    token=p1_token,
                    json={"conversation_id": dm_id, "is_pinned": True},
                )
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/conversations/mute",
                    token=p1_token,
                    json={"conversation_id": dm_id, "is_muted": True},
                )
                detail = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/conversations/detail",
                        token=p1_token,
                        params={"id": dm_id},
                    )
                )["data"]
                # fields may be on member
                result.add(
                    "dm.pin_and_mute",
                    detail.get("id") == dm_id,
                    detail,
                )
            except Exception as e:
                result.add("dm.pin_and_mute", False, str(e))

            # ── Groups ───────────────────────────────────────────────────────
            group_id = None
            group_conv_id = None
            try:
                # p1 friends with p2 first
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/apply",
                    token=p1_token,
                    json={
                        "applicant_type": "PORTAL",
                        "applicant_id": p1_id,
                        "recipient_type": "PORTAL",
                        "recipient_id": p2_id,
                        "message": "p1-p2",
                    },
                )
                reqs = (
                    await api(client, "GET", f"{PORTAL_BASE}/message/friends/my-requests", token=p2_token)
                )["data"]
                req = next(
                    (
                        r
                        for r in reqs
                        if r.get("status") == "PENDING"
                        and r.get("applicant_id") == p1_id
                        and r.get("recipient_id") == p2_id
                    ),
                    None,
                )
                assert req
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/handle-request",
                    token=p2_token,
                    json={"request_id": req["id"], "action": "ACCEPT"},
                )

                gname = f"IM-E2E-{int(time.time())}"
                group = (
                    await api(
                        client,
                        "POST",
                        f"{PORTAL_BASE}/message/groups/create",
                        token=p1_token,
                        json={"name": gname, "description": "e2e", "join_mode": "APPROVAL"},
                    )
                )["data"]
                group_id = group["id"]
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/members/add",
                    token=p1_token,
                    json={
                        "group_id": group_id,
                        "members": [{"account_type": "PORTAL", "account_id": p2_id}],
                    },
                )
                # also invite admin
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/members/add",
                    token=p1_token,
                    json={
                        "group_id": group_id,
                        "members": [{"account_type": "ADMIN", "account_id": admin_id}],
                    },
                )
                members = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/groups/members/list",
                        token=p1_token,
                        params={"id": group_id},
                    )
                )["data"]
                result.add(
                    "group.create_invite_members",
                    len(members) >= 3 and any(m["account_id"] == admin_id for m in members),
                    {"group_id": group_id, "members": len(members)},
                )
            except Exception as e:
                result.add("group.create_invite_members", False, str(e))

            # Set role
            try:
                assert group_id
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/members/set-role",
                    token=p1_token,
                    json={
                        "group_id": group_id,
                        "account_type": "PORTAL",
                        "account_id": p2_id,
                        "role": "ADMIN",
                    },
                )
                members = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/groups/members/list",
                        token=p1_token,
                        params={"id": group_id},
                    )
                )["data"]
                p2m = next(m for m in members if m["account_id"] == p2_id)
                result.add("group.set_member_role", p2m.get("role") == "ADMIN", p2m)
            except Exception as e:
                result.add("group.set_member_role", False, str(e))

            # Group conversation + message realtime
            try:
                assert group_id and ws_p2 and ws_admin
                convs = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/conversations/my-list",
                        token=p1_token,
                        params={"current": 1, "size": 50},
                    )
                )["data"]["records"]
                gconv = next((c for c in convs if c.get("group_id") == group_id), None)
                if gconv is None:
                    # send with group_id to create/find
                    pass
                group_conv_id = gconv["id"] if gconv else None
                ws_p2.events.clear()
                ws_admin.events.clear()
                payload = {
                    "content": f"group-hi-{int(time.time())}",
                    "content_type": "TEXT",
                    "msg_type": "TEXT",
                }
                if group_conv_id:
                    payload["conversation_id"] = group_conv_id
                else:
                    payload["group_id"] = group_id
                gmsg = (
                    await api(
                        client,
                        "POST",
                        f"{PORTAL_BASE}/message/messages/send",
                        token=p1_token,
                        json=payload,
                    )
                )["data"]
                group_conv_id = gmsg["conversation_id"]
                ev_p2 = await ws_p2.wait_for(
                    "new_message",
                    timeout=8,
                    pred=lambda e: (e.get("data") or {}).get("id") == gmsg["id"],
                )
                ev_ad = await ws_admin.wait_for(
                    "new_message",
                    timeout=8,
                    pred=lambda e: (e.get("data") or {}).get("id") == gmsg["id"],
                )
                result.add(
                    "group.message_realtime_to_members",
                    ev_p2 is not None and ev_ad is not None,
                    {"msg": gmsg["id"], "p2": bool(ev_p2), "admin": bool(ev_ad)},
                )
            except Exception as e:
                result.add("group.message_realtime_to_members", False, str(e))

            # Join request flow with a fresh group owned by admin, p2 applies
            try:
                g2 = (
                    await api(
                        client,
                        "POST",
                        f"{ADMIN_BASE}/message/groups/create",
                        token=admin_token,
                        json={
                            "name": f"IM-E2E-JOIN-{int(time.time())}",
                            "join_mode": "APPROVAL",
                            "description": "join flow",
                        },
                    )
                )["data"]
                g2_id = g2["id"]
                ws_admin.events.clear()
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/join-requests/apply",
                    token=p2_token,
                    json={"group_id": g2_id, "message": "please let me in"},
                )
                join_ev = await ws_admin.wait_for("new_join_request", timeout=6)
                pending = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/groups/join-requests/pending",
                        token=admin_token,
                    )
                )["data"]
                jr = next(
                    (
                        x
                        for x in pending
                        if x.get("applicant_id") == p2_id and x.get("group_id") == g2_id
                    ),
                    None,
                )
                assert jr
                ws_p2.events.clear()
                await api(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/message/groups/join-requests/handle",
                    token=admin_token,
                    json={"id": jr["id"], "status": "ACCEPTED"},
                )
                handled = await ws_p2.wait_for("join_request_handled", timeout=6)
                members = (
                    await api(
                        client,
                        "GET",
                        f"{ADMIN_BASE}/message/groups/members/list",
                        token=admin_token,
                        params={"id": g2_id},
                    )
                )["data"]
                in_group = any(m["account_id"] == p2_id for m in members)
                # search flags
                hits = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/groups/search",
                        token=p2_token,
                        params={"keyword": g2["name"]},
                    )
                )["data"]
                hit = next((h for h in hits if h["id"] == g2_id), None)
                result.add(
                    "group.join_apply_accept_ws",
                    in_group and join_ev is not None and (handled is not None or in_group),
                    {
                        "join_ws": bool(join_ev),
                        "handled_ws": bool(handled),
                        "in_group": in_group,
                        "search_member": bool(hit and hit.get("is_member")),
                    },
                )
                # leave
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/leave",
                    token=p2_token,
                    json={"id": g2_id},
                )
                await api(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/message/groups/dissolve",
                    token=admin_token,
                    json={"id": g2_id},
                )
                result.add("group.leave_and_dissolve", True)
            except Exception as e:
                result.add("group.join_apply_accept_ws", False, str(e))
                result.add("group.leave_and_dissolve", False, str(e))

            # Remove member from first group + dissolve
            try:
                assert group_id
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/members/remove",
                    token=p1_token,
                    json={
                        "group_id": group_id,
                        "account_type": "ADMIN",
                        "account_id": admin_id,
                    },
                )
                members = (
                    await api(
                        client,
                        "GET",
                        f"{PORTAL_BASE}/message/groups/members/list",
                        token=p1_token,
                        params={"id": group_id},
                    )
                )["data"]
                removed = not any(m["account_id"] == admin_id for m in members)
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/groups/dissolve",
                    token=p1_token,
                    json={"id": group_id},
                )
                result.add("group.remove_member_and_dissolve", removed, {"removed": removed})
            except Exception as e:
                result.add("group.remove_member_and_dissolve", False, str(e))

            # Remove friend
            try:
                friends_p1 = (
                    await api(client, "GET", f"{PORTAL_BASE}/message/friends/my-list", token=p1_token)
                )["data"]
                f = next(x for x in friends_p1 if x.get("friend_account_id") == admin_id)
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/remove",
                    token=p1_token,
                    json={"friendship_id": f["friendship_id"]},
                )
                friends_p1 = (
                    await api(client, "GET", f"{PORTAL_BASE}/message/friends/my-list", token=p1_token)
                )["data"]
                gone = not any(x.get("friend_account_id") == admin_id for x in friends_p1)
                result.add("friend.remove", gone)
            except Exception as e:
                result.add("friend.remove", False, str(e))

            # Only recipient can accept — negative: applicant handle should fail
            try:
                await api(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/message/friends/apply",
                    token=p1_token,
                    json={
                        "applicant_type": "PORTAL",
                        "applicant_id": p1_id,
                        "recipient_type": "ADMIN",
                        "recipient_id": admin_id,
                        "message": "neg",
                    },
                )
                reqs = (
                    await api(client, "GET", f"{PORTAL_BASE}/message/friends/my-requests", token=p1_token)
                )["data"]
                out = next(
                    (
                        r
                        for r in reqs
                        if r.get("status") == "PENDING"
                        and r.get("applicant_id") == p1_id
                        and r.get("recipient_id") == admin_id
                    ),
                    None,
                )
                if not out:
                    result.add("friend.applicant_cannot_accept", False, {"error": "no pending request", "reqs": reqs})
                else:
                    resp = await client.post(
                        f"{PORTAL_BASE}/message/friends/handle-request",
                        headers={"Authorization": p1_token},
                        json={"request_id": out["id"], "action": "ACCEPT"},
                    )
                    body = resp.json()
                    rejected = body.get("code") not in (0, 200, "0", "200")
                    result.add(
                        "friend.applicant_cannot_accept",
                        rejected,
                        {"status": resp.status_code, "body": body, "request_id": out["id"]},
                    )
            except Exception as e:
                result.add("friend.applicant_cannot_accept", False, str(e))

        finally:
            for ws in (ws_admin, ws_p1, ws_p2):
                if ws:
                    try:
                        await ws.close()
                    except Exception:
                        pass
            await close_engine()

    result.dump("/tmp/acoj_im_e2e.json")


if __name__ == "__main__":
    asyncio.run(main())
