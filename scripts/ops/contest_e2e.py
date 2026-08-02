#!/usr/bin/env python3
"""Seed contest fixtures + run closed-loop contest E2E matrix. Writes /tmp/acoj_contest_e2e.json."""

from __future__ import annotations

import asyncio
import base64
import json
import time
from datetime import datetime, timedelta, timezone
from typing import Any

import httpx
import redis
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding
from sqlalchemy import select

from app.core.security.password import hash_password
from app.core.config.enums import AccountStatusEnum, AccountType
from app.modules.iam.account.model import SysAccount, SysAccountIdentity
from app.modules.iam.enums import AccountIdentityBindStatus
from app.modules.user.portal.model import PortalUserProfile
from app.platform.db.session import get_session_factory, init_engine
from app.platform.id_generator.snowflake import generate_snowflake_id

ADMIN_BASE = "http://127.0.0.1:8000/api/v1/admin"
PORTAL_BASE = "http://127.0.0.1:8000/api/v1/portal"

PID_ACM = "7489479821890818048"  # T10
PID_OI = "7489479821962121216"  # T11 partial
PID_IOI = "7489479822012452864"  # T12 batch
PID_ACM2 = "7489481903448395776"  # L10

# T10/T11/T12 seeded cases are echo problems (input line == output line).
SRC_ECHO_AC = """#include <bits/stdc++.h>
using namespace std;
int main(){ string s; getline(cin,s); cout<<s<<'\\n'; }
"""
SRC_ECHO_WA = """#include <bits/stdc++.h>
using namespace std;
int main(){ cout<<"zzz\\n"; }
"""
SRC_ECHO_PY = "print(input())\n"
# L10 is A+B
SRC_AB_AC = """#include <bits/stdc++.h>
using namespace std;
int main(){ long long a,b; cin>>a>>b; cout<<a+b<<endl; }
"""
SRC_AB_WA = """#include <bits/stdc++.h>
using namespace std;
int main(){ cout<<0<<endl; }
"""
SRC_AB_PY = "print(sum(map(int,input().split())))\n"
# OI partial: always print "a" => only case1 AC (40/100)
SRC_OI_PARTIAL = """#include <bits/stdc++.h>
using namespace std;
int main(){ string s; getline(cin,s); cout<<"a\\n"; }
"""


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


def iso(dt: datetime) -> str:
    return dt.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class Result:
    def __init__(self) -> None:
        self.cases: list[dict[str, Any]] = []

    def add(self, name: str, ok: bool, detail: Any = None) -> None:
        self.cases.append({"name": name, "ok": ok, "detail": detail})
        print(("PASS" if ok else "FAIL"), name, detail if not ok else "")

    def dump(self, path: str) -> None:
        payload = {
            "all_pass": all(c["ok"] for c in self.cases),
            "passed": sum(1 for c in self.cases if c["ok"]),
            "failed": sum(1 for c in self.cases if not c["ok"]),
            "cases": self.cases,
            "finished_at": iso(utcnow()),
        }
        with open(path, "w", encoding="utf-8") as f:
            json.dump(payload, f, ensure_ascii=False, indent=2)
        print("wrote", path, "all_pass=", payload["all_pass"])


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
    data = resp.json()["data"]
    # Authorization header expects raw token (no Bearer prefix).
    return data["token"]


async def ensure_portal_users() -> dict[str, str]:
    """Create contestant1..3 portal accounts; return username->account_id."""
    init_engine()
    sf = get_session_factory()
    users = {}
    async with sf() as db:
        for i in range(1, 4):
            username = f"contestant{i}"
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
                        account_type=AccountType.PORTAL.value,
                        account_status=AccountStatusEnum.ENABLED.value,
                        password_hash=hash_password("123456"),
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
                db.add(
                    PortalUserProfile(
                        account_id=account_id,
                        nickname=username,
                        name=username,
                        rating=1500,
                    )
                )
                await db.commit()
                users[username] = account_id
            else:
                users[username] = row.account_id
                # ensure profile
                prof = await db.get(PortalUserProfile, row.account_id)
                if prof is None:
                    db.add(PortalUserProfile(account_id=row.account_id, nickname=username, rating=1500))
                    await db.commit()
    return users


async def api_ok(client: httpx.AsyncClient, method: str, url: str, *, headers=None, **kwargs) -> Any:
    resp = await client.request(method, url, headers=headers, **kwargs)
    try:
        body = resp.json()
    except Exception:
        body = {"raw": resp.text}
    code = body.get("code") if isinstance(body, dict) else None
    if resp.status_code >= 400 or (code is not None and code != 200):
        raise RuntimeError(f"{method} {url} -> {resp.status_code} {body}")
    if isinstance(body, dict) and "data" in body:
        return body["data"]
    return body


def contest_payload(key: str, name: str, fmt: str, *, start: datetime, end: datetime, **extra: Any) -> dict[str, Any]:
    base = {
        "key": key,
        "name": name,
        "description": name,
        "summary": name,
        "start_time": iso(start),
        "end_time": iso(end),
        "time_limit_seconds": None,
        "freeze_seconds": extra.pop("freeze_seconds", None),
        "is_visible": True,
        "is_private": False,
        "access_code": extra.pop("access_code", None),
        "is_rated": extra.pop("is_rated", False),
        "rating_floor": None,
        "rating_ceiling": None,
        "rate_all": extra.pop("rate_all", False),
        "scoreboard_visibility": "VISIBLE",
        "format_name": fmt,
        "format_config": extra.pop("format_config", {}),
        "points_precision": 0,
        "hide_problem_tags": False,
        "hide_problem_authors": False,
        "run_pretests_only": extra.pop("run_pretests_only", False),
        "use_clarifications": extra.pop("use_clarifications", True),
        "tester_see_scoreboard": True,
        "tester_see_submissions": True,
        "locked_after": extra.pop("locked_after", None),
        "tag_ids": [],
        "extra": {},
    }
    base.update(extra)
    return base


async def add_problem(client, headers, contest_id: str, problem_id: str, *, sort: int, points: int, partial: bool, label: str) -> str:
    await api_ok(
        client,
        "POST",
        f"{ADMIN_BASE}/biz/contest/problem/create",
        headers=headers,
        params={"contest_id": contest_id},
        json={
            "contest_id": contest_id,
            "problem_id": problem_id,
            "points": points,
            "partial": partial,
            "is_pretested": False,
            "sort": sort,
            "label": label,
            "max_submissions": None,
            "output_prefix_override": None,
        },
    )
    page = await api_ok(
        client,
        "GET",
        f"{ADMIN_BASE}/biz/contest/problem/page",
        headers=headers,
        params={"contest_id": contest_id, "current": 1, "size": 50},
    )
    for row in page.get("records") or page.get("list") or []:
        if row["problem_id"] == problem_id:
            return row["id"]
    raise RuntimeError("contest problem not found after create")


async def wait_submission(client, headers, submission_id: str, timeout: int = 90) -> dict[str, Any]:
    deadline = time.time() + timeout
    while time.time() < deadline:
        detail = await api_ok(
            client,
            "GET",
            f"{ADMIN_BASE}/biz/submission/submission/detail",
            headers=headers,
            params={"id": submission_id},
        )
        if detail.get("status") in {"COMPLETED", "FAILED"}:
            return detail
        await asyncio.sleep(0.5)
    raise TimeoutError(submission_id)


async def main() -> None:
    result = Result()
    users = await ensure_portal_users()
    now = utcnow()

    async with httpx.AsyncClient(timeout=120.0) as client:
        admin_token = await login(client, ADMIN_BASE, "superadmin")
        ah = {"Authorization": admin_token}

        # cleanup previous e2e contests by key prefix
        page = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/page", headers=ah, params={"current": 1, "size": 100, "key": "C-"})
        records = page.get("records") or page.get("list") or []
        old_ids = [r["id"] for r in records if str(r.get("key", "")).startswith("C-")]
        if old_ids:
            try:
                await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/delete", headers=ah, json={"ids": old_ids})
            except Exception as exc:  # noqa: BLE001
                print("cleanup warn", exc)

        # --- create contests ---
        contests: dict[str, str] = {}

        async def create_c(key: str, name: str, fmt: str, **kw) -> str:
            start = kw.pop("start", now - timedelta(minutes=30))
            end = kw.pop("end", now + timedelta(hours=2))
            payload = contest_payload(key, name, fmt, start=start, end=end, **kw)
            cid = await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/create", headers=ah, json=payload)
            contests[key] = cid
            return cid

        # freeze_seconds=7200 with end=+2h => freeze starts immediately
        cid_icpc = await create_c("C-ICPC", "E2E ICPC", "icpc", freeze_seconds=7200, is_rated=True, format_config={"penalty_minutes": 20})
        cid_atc = await create_c("C-ATCODER", "E2E AtCoder", "atcoder", format_config={"penalty_minutes": 5})
        cid_oi = await create_c("C-OI", "E2E OI", "oi", format_config={"cumtime": False})
        cid_ioi = await create_c("C-IOI", "E2E IOI", "ioi", format_config={"use_batch": True, "cumtime": False})
        cid_def = await create_c("C-DEFAULT", "E2E Default", "default")
        cid_ended = await create_c(
            "C-ENDED",
            "E2E Ended Rated",
            "icpc",
            start=now - timedelta(hours=3),
            end=now - timedelta(minutes=5),
            is_rated=True,
            rate_all=True,
            format_config={"penalty_minutes": 20},
        )

        # problems
        await add_problem(client, ah, cid_icpc, PID_ACM, sort=1, points=100, partial=False, label="A")
        await add_problem(client, ah, cid_icpc, PID_ACM2, sort=2, points=100, partial=False, label="B")
        await add_problem(client, ah, cid_atc, PID_ACM, sort=1, points=100, partial=False, label="A")
        await add_problem(client, ah, cid_oi, PID_OI, sort=1, points=100, partial=True, label="A")
        await add_problem(client, ah, cid_ioi, PID_IOI, sort=1, points=100, partial=True, label="A")
        await add_problem(client, ah, cid_def, PID_OI, sort=1, points=100, partial=True, label="A")
        await add_problem(client, ah, cid_ended, PID_ACM, sort=1, points=100, partial=False, label="A")

        # staff / private / banned / clarification seed via admin
        # staff: use superadmin account id 1
        try:
            await api_ok(
                client,
                "POST",
                f"{ADMIN_BASE}/biz/contest/staff/create",
                headers=ah,
                params={"contest_id": cid_icpc},
                json={"contest_id": cid_icpc, "account_id": "1", "role": "AUTHOR"},
            )
        except Exception as exc:  # noqa: BLE001
            print("staff seed", exc)

        await api_ok(
            client,
            "POST",
            f"{ADMIN_BASE}/biz/contest/clarification/create",
            headers=ah,
            params={"contest_id": cid_icpc},
            json={"title": "Welcome", "body": "Read statements carefully", "problem_id": None},
        )

        # portal logins
        portal_tokens = {}
        for u in users:
            portal_tokens[u] = await login(client, PORTAL_BASE, u)

        # join ICPC
        for u, tok in portal_tokens.items():
            ph = {"Authorization": tok}
            await api_ok(
                client,
                "POST",
                f"{PORTAL_BASE}/biz/contest/join",
                headers=ph,
                params={"contest_id": cid_icpc},
                json={},
            )

        # lifecycle check
        detail = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/detail", headers=ah, params={"id": cid_icpc})
        result.add("lifecycle RUNNING", detail.get("lifecycle_status") == "RUNNING", detail.get("lifecycle_status"))
        detail_ended = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/detail", headers=ah, params={"id": cid_ended})
        result.add("lifecycle ENDED", detail_ended.get("lifecycle_status") == "ENDED", detail_ended.get("lifecycle_status"))

        # --- ICPC submissions: c1 AC A, c2 WA then AC A, c3 AC both with python ---
        async def portal_submit(user: str, contest_id: str, problem_id: str, language: str, source: str) -> dict[str, Any]:
            ph = {"Authorization": portal_tokens[user]}
            snap = await api_ok(
                client,
                "POST",
                f"{PORTAL_BASE}/biz/contest/submit",
                params={"contest_id": contest_id},
                headers=ph,
                json={
                    "problem_id": problem_id,
                    "language_key": language,
                    "source": source,
                    "wait": True,
                    "wait_timeout_sec": 90,
                },
            )
            return snap

        s1 = await portal_submit("contestant1", cid_icpc, PID_ACM, "cpp17", SRC_ECHO_AC)
        result.add("icpc c1 AC", s1.get("result") == "AC", s1)
        s2w = await portal_submit("contestant2", cid_icpc, PID_ACM, "cpp17", SRC_ECHO_WA)
        result.add("icpc c2 WA", s2w.get("result") == "WA", s2w)
        await asyncio.sleep(2)
        s2a = await portal_submit("contestant2", cid_icpc, PID_ACM, "cpp17", SRC_ECHO_AC)
        result.add("icpc c2 AC", s2a.get("result") == "AC", s2a)
        s3a = await portal_submit("contestant3", cid_icpc, PID_ACM, "python3", SRC_ECHO_PY)
        s3b = await portal_submit("contestant3", cid_icpc, PID_ACM2, "python3", SRC_AB_PY)
        result.add("icpc multilang", s3a.get("result") == "AC" and s3b.get("result") == "AC", {"a": s3a, "b": s3b})

        board = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_icpc})
        rows = board.get("rows") or []
        # c3 should be rank1 with 2 solves
        top = rows[0] if rows else {}
        result.add("icpc scoreboard top solves", float(top.get("score") or 0) == 2.0, top)
        await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/rescore", headers=ah, json={"contest_id": cid_icpc})
        board2 = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_icpc})
        result.add("icpc rescore stable", (board2.get("rows") or [{}])[0].get("score") == top.get("score"), board2.get("rows"))

        # freeze flag
        result.add("icpc freeze flag", board.get("is_frozen") is True, board.get("is_frozen"))

        # ATCODER penalty config difference — join + WA then AC
        for u, tok in portal_tokens.items():
            try:
                await api_ok(
                    client,
                    "POST",
                    f"{PORTAL_BASE}/biz/contest/join",
                    headers={"Authorization": tok},
                    params={"contest_id": cid_atc},
                    json={},
                )
            except Exception:
                pass
        await portal_submit("contestant1", cid_atc, PID_ACM, "cpp17", SRC_ECHO_WA)
        await asyncio.sleep(1)
        await portal_submit("contestant1", cid_atc, PID_ACM, "cpp17", SRC_ECHO_AC)
        await portal_submit("contestant2", cid_atc, PID_ACM, "cpp17", SRC_ECHO_AC)
        b_atc = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_atc})
        # find c1 cumtime should include 5*60 penalty
        c1_row = next((r for r in b_atc.get("rows") or [] if r.get("account_id") == users["contestant1"]), None)
        c2_row = next((r for r in b_atc.get("rows") or [] if r.get("account_id") == users["contestant2"]), None)
        ok_pen = (
            c1_row
            and c2_row
            and int(c1_row["cumtime"]) - int(c2_row["cumtime"]) >= 290
            and (c1_row.get("format_data") or {})
            and any(v.get("display") == 5 for v in (c1_row.get("format_data") or {}).values() if isinstance(v, dict))
        )
        result.add("atcoder penalty 5min", bool(ok_pen), {"c1": c1_row, "c2": c2_row})

        # OI partial
        await api_ok(
            client,
            "POST",
            f"{PORTAL_BASE}/biz/contest/join",
            headers={"Authorization": portal_tokens["contestant1"]},
            params={"contest_id": cid_oi},
            json={},
        )
        soi = await portal_submit("contestant1", cid_oi, PID_OI, "cpp17", SRC_OI_PARTIAL)
        boi = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_oi})
        oi_score = float((boi.get("rows") or [{}])[0].get("score") or 0)
        result.add("oi partial score>0", oi_score > 0 or soi.get("result") == "AC", {"snap": soi, "score": oi_score})

        # switch DEFAULT rescore semantics on C-DEFAULT
        await api_ok(
            client,
            "POST",
            f"{PORTAL_BASE}/biz/contest/join",
            headers={"Authorization": portal_tokens["contestant1"]},
            params={"contest_id": cid_def},
            json={},
        )
        await portal_submit("contestant1", cid_def, PID_OI, "cpp17", SRC_OI_PARTIAL)
        await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/rescore", headers=ah, json={"contest_id": cid_def})
        bdef = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_def})
        result.add("default scoreboard rows", bool(bdef.get("rows")), bdef)

        # IOI batch
        await api_ok(
            client,
            "POST",
            f"{PORTAL_BASE}/biz/contest/join",
            headers={"Authorization": portal_tokens["contestant1"]},
            params={"contest_id": cid_ioi},
            json={},
        )
        sioi = await portal_submit("contestant1", cid_ioi, PID_IOI, "cpp17", SRC_ECHO_AC)
        bioi = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_ioi})
        ioi_row = (bioi.get("rows") or [{}])[0]
        result.add("ioi batch scored", float(ioi_row.get("score") or 0) > 0 or sioi.get("result") == "AC", {"snap": sioi, "row": ioi_row})

        # clarifications
        ph1 = {"Authorization": portal_tokens["contestant1"]}
        clars = await api_ok(
            client,
            "GET",
            f"{PORTAL_BASE}/biz/contest/clarifications",
            headers=ph1,
            params={"contest_id": cid_icpc},
        )
        result.add("clar broadcast readable", any(c.get("title") == "Welcome" for c in clars), clars)
        thread = await api_ok(
            client,
            "POST",
            f"{PORTAL_BASE}/biz/contest/clarification-threads",
            params={"contest_id": cid_icpc},
            headers=ph1,
            json={"title": "Q1", "body": "Is N<=1e5?", "problem_id": None},
        )
        replied = await api_ok(
            client,
            "POST",
            f"{ADMIN_BASE}/biz/contest/clarification/thread/reply",
            headers=ah,
            params={"contest_id": cid_icpc},
            json={"thread_id": thread["id"], "body": "Yes", "set_answered": True},
        )
        result.add("clar reply answered", replied.get("status") == "ANSWERED", replied)
        promo = await api_ok(
            client,
            "POST",
            f"{ADMIN_BASE}/biz/contest/clarification/thread/promote",
            headers=ah,
            params={"contest_id": cid_icpc},
            json={"thread_id": thread["id"], "title": "FAQ N", "body": "N<=1e5"},
        )
        clars2 = await api_ok(
            client,
            "GET",
            f"{PORTAL_BASE}/biz/contest/clarifications",
            headers=ph1,
            params={"contest_id": cid_icpc},
        )
        result.add("clar promote public", any(c.get("id") == promo for c in clars2), clars2)

        # DQ
        parts = await api_ok(
            client,
            "GET",
            f"{ADMIN_BASE}/biz/contest/participation/page",
            headers=ah,
            params={"contest_id": cid_icpc, "current": 1, "size": 50},
        )
        part_rows = parts.get("records") or parts.get("list") or []
        p2 = next((p for p in part_rows if p.get("account_id") == users["contestant2"]), None)
        if p2:
            await api_ok(
                client,
                "POST",
                f"{ADMIN_BASE}/biz/contest/participation/update",
                headers=ah,
                params={"contest_id": cid_icpc},
                json={**p2, "is_disqualified": True},
            )
            try:
                await portal_submit("contestant2", cid_icpc, PID_ACM, "cpp17", SRC_ECHO_AC)
                result.add("dq submit rejected", False, "expected error")
            except Exception:
                result.add("dq submit rejected", True)
            board_dq = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/scoreboard", headers=ah, params={"contest_id": cid_icpc})
            dq_row = next((r for r in board_dq.get("rows") or [] if r.get("account_id") == users["contestant2"]), None)
            result.add("dq bottom", bool(dq_row and dq_row.get("is_disqualified")), dq_row)
        else:
            result.add("dq setup", False, "participation missing")

        # Rating on ended contest — join ended as virtual? For rate_all create LIVE participations via admin
        for u, aid in users.items():
            try:
                await api_ok(
                    client,
                    "POST",
                    f"{ADMIN_BASE}/biz/contest/participation/create",
                    headers=ah,
                    params={"contest_id": cid_ended},
                    json={
                        "contest_id": cid_ended,
                        "account_id": aid,
                        "real_start": iso(now - timedelta(hours=2)),
                        "score": 1 if u != "contestant3" else 0,
                        "cumtime": 100 if u == "contestant1" else 200,
                        "tiebreaker": 100,
                        "is_disqualified": False,
                        "virtual": 0,
                        "rate_exclude": u == "contestant3",
                        "format_data": {},
                    },
                )
            except Exception as exc:  # noqa: BLE001
                print("ended part", u, exc)
        # admin proxy submit for ended? skip — scores already set; rescore then rate
        await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/rescore", headers=ah, json={"contest_id": cid_ended})
        rate_res = await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/rate", headers=ah, json={"contest_id": cid_ended})
        ratings = await api_ok(
            client,
            "GET",
            f"{ADMIN_BASE}/biz/contest/rating/list",
            headers=ah,
            params={"contest_id": cid_ended},
        )
        result.add("rating settled", len(ratings) >= 2 and all(r["account_id"] != users["contestant3"] for r in ratings), {"rate": rate_res, "ratings": ratings})

        # lock / unlock / clone / negatives
        try:
            await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/unlock", headers=ah, json={"contest_id": cid_icpc})
        except Exception:
            pass
        locked = await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/lock", headers=ah, json={"contest_id": cid_icpc})
        result.add("lock status", locked.get("lifecycle_status") == "LOCKED", locked.get("lifecycle_status"))
        try:
            await api_ok(
                client,
                "POST",
                f"{ADMIN_BASE}/biz/contest/contest/submit",
                headers=ah,
                json={
                    "contest_id": cid_icpc,
                    "participation_id": part_rows[0]["id"],
                    "problem_id": PID_ACM,
                    "language_key": "cpp17",
                    "source": SRC_ECHO_AC,
                },
            )
            result.add("locked submit rejected", False, "expected error")
        except Exception:
            result.add("locked submit rejected", True)
        await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/unlock", headers=ah, json={"contest_id": cid_icpc})

        # clone (delete previous clone key if exists)
        page_clone = await api_ok(
            client, "GET", f"{ADMIN_BASE}/biz/contest/contest/page", headers=ah, params={"current": 1, "size": 20, "key": "C-CLONE"}
        )
        for row in page_clone.get("records") or []:
            if row.get("key") == "C-CLONE":
                await api_ok(client, "POST", f"{ADMIN_BASE}/biz/contest/contest/delete", headers=ah, json={"ids": [row["id"]]})
        new_id = await api_ok(
            client,
            "POST",
            f"{ADMIN_BASE}/biz/contest/contest/clone",
            headers=ah,
            json={"contest_id": cid_icpc, "new_key": "C-CLONE", "copy_staff": True},
        )
        cloned = await api_ok(client, "GET", f"{ADMIN_BASE}/biz/contest/contest/detail", headers=ah, params={"id": new_id})
        probs = await api_ok(
            client,
            "GET",
            f"{ADMIN_BASE}/biz/contest/problem/page",
            headers=ah,
            params={"contest_id": new_id, "current": 1, "size": 20},
        )
        result.add(
            "clone invisible+problems",
            cloned.get("is_visible") is False and len(probs.get("records") or probs.get("list") or []) >= 2,
            {"cloned": cloned.get("key"), "n": len(probs.get("records") or probs.get("list") or [])},
        )

        # negative: submit without join on fresh contest
        cid_neg = await create_c("C-NEG", "E2E Neg", "icpc")
        await add_problem(client, ah, cid_neg, PID_ACM, sort=1, points=100, partial=False, label="A")
        try:
            await portal_submit("contestant1", cid_neg, PID_ACM, "cpp17", SRC_ECHO_AC)
            result.add("neg no-join submit", False, "expected error")
        except Exception:
            result.add("neg no-join submit", True)

        # private + access code
        cid_priv = await create_c("C-PRIV", "E2E Priv", "icpc", is_private=True, access_code="secret")
        try:
            await api_ok(
                client,
                "POST",
                f"{PORTAL_BASE}/biz/contest/join",
                params={"contest_id": cid_priv},
                headers=ph1,
                json={"access_code": "wrong"},
            )
            result.add("neg private/access", False, "expected error")
        except Exception:
            result.add("neg private/access", True)

    result.dump("/tmp/acoj_contest_e2e.json")


if __name__ == "__main__":
    asyncio.run(main())
