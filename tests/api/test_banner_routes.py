from app.core.config.enums import AccountStatusEnum, LoginScope, UserType
from app.core.security.session import SessionPayload, session_store
from app.deps.db import get_db_session
from app.modules.iam.model import SysAccount


async def _seed_admin(client, token: str, permissions: list[str]) -> None:
    override = client._transport.app.dependency_overrides[get_db_session]
    async for session in override():
        account = SysAccount(
            account=f"{token}_account",
            password_hash="hashed",
            account_type=UserType.ADMIN.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Banner Admin",
            nickname="Banner Admin",
        )
        session.add(account)
        await session.flush()
        await session_store.set(
            SessionPayload(
                token=token,
                account_id=account.id,
                account_type=UserType.ADMIN.value,
                login_scope=LoginScope.ADMIN.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=permissions,
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await session.commit()
        break


def _payload(**overrides):
    data = {
        "title": "Home Banner",
        "image": "https://example.com/banner.png",
        "url": "https://example.com",
        "link_type": "URL",
        "summary": "Summary",
        "description": "Description",
        "category": "home",
        "type": "carousel",
        "position": "home_top",
        "display_scope": "PORTAL",
        "sort": 1,
        "status": "ENABLED",
    }
    data.update(overrides)
    return data


async def test_admin_banner_create_page_detail_update_delete(client):
    token = "admin-banner-token"
    await _seed_admin(
        client,
        token,
        [
            "sys:banner:create",
            "sys:banner:page",
            "sys:banner:detail",
            "sys:banner:update",
            "sys:banner:delete",
        ],
    )
    headers = {"Authorization": token}

    create_response = await client.post(
        "/api/v1/admin/banner/sys/banners/create",
        headers=headers,
        json=_payload(),
    )
    assert create_response.status_code == 200
    banner_id = create_response.json()["data"]["id"]

    page_response = await client.get(
        "/api/v1/admin/banner/sys/banners/page?current=1&size=20&display_scope=PORTAL&position=home_top",
        headers=headers,
    )
    assert page_response.status_code == 200
    assert page_response.json()["data"]["total"] == 1

    detail_response = await client.get(
        f"/api/v1/admin/banner/sys/banners/detail?id={banner_id}",
        headers=headers,
    )
    assert detail_response.status_code == 200
    assert detail_response.json()["data"]["title"] == "Home Banner"

    update_response = await client.post(
        "/api/v1/admin/banner/sys/banners/update",
        headers=headers,
        json=_payload(id=banner_id, title="Updated Banner"),
    )
    assert update_response.status_code == 200
    assert update_response.json()["data"]["title"] == "Updated Banner"

    delete_response = await client.post(
        "/api/v1/admin/banner/sys/banners/delete",
        headers=headers,
        json={"ids": [banner_id]},
    )
    assert delete_response.status_code == 200
    assert delete_response.json()["data"] == [banner_id]


async def test_admin_banner_delete_accepts_id_array(client):
    token = "admin-banner-batch-delete-token"
    await _seed_admin(client, token, ["sys:banner:create", "sys:banner:page", "sys:banner:delete"])
    headers = {"Authorization": token}
    first = await client.post(
        "/api/v1/admin/banner/sys/banners/create",
        headers=headers,
        json=_payload(title="First Banner"),
    )
    second = await client.post(
        "/api/v1/admin/banner/sys/banners/create",
        headers=headers,
        json=_payload(title="Second Banner", sort=2),
    )
    first_id = first.json()["data"]["id"]
    second_id = second.json()["data"]["id"]

    response = await client.post(
        "/api/v1/admin/banner/sys/banners/delete",
        headers=headers,
        json={"ids": [first_id, second_id]},
    )

    assert response.status_code == 200
    assert response.json()["data"] == [first_id, second_id]

    page_response = await client.get(
        "/api/v1/admin/banner/sys/banners/page?current=1&size=20&display_scope=PORTAL&position=home_top",
        headers=headers,
    )
    assert page_response.json()["data"]["total"] == 0


async def test_public_banner_list_does_not_require_admin_session(client):
    token = "admin-banner-public-seed-token"
    await _seed_admin(client, token, ["sys:banner:create"])
    await client.post(
        "/api/v1/admin/banner/sys/banners/create",
        headers={"Authorization": token},
        json=_payload(title="Public Banner"),
    )

    response = await client.get("/api/v1/portal/banner/sys/banners/list?position=home_top")

    assert response.status_code == 200
    payload = response.json()
    assert payload["code"] == 200
    assert [item["title"] for item in payload["data"]] == ["Public Banner"]


async def test_admin_banner_page_requires_permission(client):
    token = "admin-banner-no-permission-token"
    await _seed_admin(client, token, [])

    response = await client.get(
        "/api/v1/admin/banner/sys/banners/page",
        headers={"Authorization": token},
    )

    assert response.status_code == 403
    assert response.json()["message"] == "Permission denied: sys:banner:page"
