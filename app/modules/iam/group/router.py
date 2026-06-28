from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.group.schema import (
    GroupAdminPageQuery,
    GroupCreateRequest,
    GroupRoleAssignRequest,
    GroupUpdateRequest,
    SysGroupRoleRelSchema,
    SysGroupSchema,
)
from app.modules.iam.group.service import GroupService

router = APIRouter()


@router.post(
    "/sys/groups/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:group:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: GroupCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await GroupService(db).create(payload)
    return success()


@router.post(
    "/sys/groups/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:group:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: GroupUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await GroupService(db).update(payload)
    return success()


@router.post(
    "/sys/groups/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:group:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await GroupService(db).delete(payload)
    return success()


@router.get(
    "/sys/groups/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:group:detail")),
    ],
    response_model=ApiResponse[SysGroupSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysGroupSchema]:
    return success(await GroupService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/groups/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:group:page")),
    ],
    response_model=ApiResponse[PageData[SysGroupSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    name: str | None = Query(default=None, max_length=64),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysGroupSchema]]:
    query = GroupAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        name=name,
        status=status,
    )
    return success(await GroupService(db).page_admin(query))


@router.post(
    "/group-roles",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:group:grantrole")),
    ],
    response_model=ApiResponse[SysGroupRoleRelSchema],
)
async def assign_group_role(
    payload: GroupRoleAssignRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysGroupRoleRelSchema]:
    return success(await GroupService(db).assign_group_role(payload))
