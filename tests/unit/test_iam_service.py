from app.core.config.enums import UserType
from app.modules.iam.account.schema import AccountCreateRequest
from app.modules.iam.account.service import AccountService
from app.modules.user.admin.service import AdminUserProfileService


async def test_create_admin_account_creates_profile(db_session):
    account = await AccountService(db_session).create_account(
        AccountCreateRequest(
            account="admin2",
            password="Admin@123456",
            account_type=UserType.ADMIN.value,
            name="Admin 2",
            nickname="Admin 2",
            avatar=None,
            signature=None,
            phone=None,
            email=None,
        )
    )
    await db_session.commit()
    assert account.id is not None
    profile = await AdminUserProfileService(db_session).get_profile(account.id)
    assert profile is not None
