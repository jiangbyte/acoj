from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.account.schema import (
    AccountCreateRequest,
    AccountAdminPageQuery,
    AccountDeptAssignRequest,
    AccountGrantPermissionRequest,
    AccountGroupAssignRequest,
    AccountOwnPermissionResponse,
    AccountRoleAssignRequest,
    AccountUpdateRequest,
    SysAccountDeptRelSchema,
    SysAccountGroupRelSchema,
    SysAccountRoleRelSchema,
    SysAccountSchema,
)
from app.modules.iam.account.service import AccountService

router = APIRouter()


@router.post(
    "/sys/accounts/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: AccountCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AccountService(db).create(payload)
    return success()


@router.post(
    "/sys/accounts/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: AccountUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AccountService(db).update(payload)
    return success()


@router.post(
    "/sys/accounts/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AccountService(db).delete(payload)
    return success()


@router.get(
    "/sys/accounts/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:detail")),
    ],
    response_model=ApiResponse[SysAccountSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysAccountSchema]:
    return success(await AccountService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/accounts/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:page")),
    ],
    response_model=ApiResponse[PageData[SysAccountSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    account: str | None = Query(default=None, max_length=64),
    name: str | None = Query(default=None, max_length=64),
    phone: str | None = Query(default=None, max_length=32),
    email: str | None = Query(default=None, max_length=128),
    account_type: str | None = Query(default=None, max_length=32),
    account_status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysAccountSchema]]:
    query = AccountAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        account=account,
        name=name,
        phone=phone,
        email=email,
        account_type=account_type,
        account_status=account_status,
    )
    return success(await AccountService(db).page_admin(query))


@router.post(
    "/account-roles",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:grantrole")),
    ],
    response_model=ApiResponse[SysAccountRoleRelSchema],
)
async def assign_account_role(
    payload: AccountRoleAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountRoleRelSchema]:
    return success(await AccountService(db).assign_account_role(payload))


@router.post(
    "/account-groups",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:grantgroup")),
    ],
    response_model=ApiResponse[SysAccountGroupRelSchema],
)
async def assign_account_group(
    payload: AccountGroupAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountGroupRelSchema]:
    return success(await AccountService(db).assign_account_group(payload))


@router.post(
    "/account-depts",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:grantdept")),
    ],
    response_model=ApiResponse[SysAccountDeptRelSchema],
)
async def assign_account_dept(
    payload: AccountDeptAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysAccountDeptRelSchema]:
    return success(await AccountService(db).assign_account_dept(payload))


@router.get(
    "/sys/accounts/own-permission",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:ownpermission")),
    ],
    response_model=ApiResponse[AccountOwnPermissionResponse],
    summary="获取用户拥有权限",
)
async def own_permission(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[AccountOwnPermissionResponse]:
    return success(await AccountService(db).own_permission(IdQuery(id=id)))


@router.post(
    "/sys/accounts/grant-permission",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:account:grantpermission")),
    ],
    response_model=ApiResponse[None],
    summary="给用户授权权限",
)
async def grant_permission(
    payload: AccountGrantPermissionRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await AccountService(db).grant_permission(payload)
    return success()
