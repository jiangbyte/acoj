from app.core.config.enums import AccountStatusEnum, LoginScope, UserType
from app.core.security.session import SessionPayload, session_store
from app.modules.file.model import SysFile
from app.modules.iam.model import SysAccount


async def test_admin_file_list_uses_current_size_total_pages_records(client):
    override = client._transport.app.dependency_overrides
    get_db_session = next(iter(override))

    async for session in override[get_db_session]():
        account = SysAccount(
            account="admin_pager",
            password_hash="hashed",
            account_type=UserType.ADMIN.value,
            account_status=AccountStatusEnum.ENABLED.value,
            name="Admin Pager",
            nickname="Admin Pager",
        )
        file = SysFile(
            object_name="uploads/20260617/demo.txt",
            original_name="demo.txt",
            storage_provider="local",
            bucket=None,
            content_type="text/plain",
            size=4,
            url="http://testserver/storage/uploads/20260617/demo.txt",
        )
        session.add_all([account, file])
        await session.flush()
        await session_store.set(
            SessionPayload(
                token="admin-pagination-token",
                account_id=account.id,
                account_type=UserType.ADMIN.value,
                login_scope=LoginScope.ADMIN.value,
                role_ids=[],
                dept_ids=[],
                group_ids=[],
                permission_keys=["file:list"],
                permission_grants=[],
            ),
            ttl_seconds=3600,
        )
        await session.commit()
        break

    response = await client.get(
        "/api/v1/admin/file/list?current=1&size=20",
        headers={"Authorization": "admin-pagination-token"},
    )

    assert response.status_code == 200
    data = response.json()["data"]
    assert data["current"] == 1
    assert data["size"] == 20
    assert data["total"] == 1
    assert data["pages"] == 1
    assert isinstance(data["records"], list)
    assert data["records"][0]["object_name"] == "uploads/20260617/demo.txt"
    assert "page" not in data
    assert "page_size" not in data
    assert "items" not in data
