from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.resource.schema import (
    ResourceAdminPageQuery,
    ResourceCreateRequest,
    ResourcePermissionBindRequest,
    ResourceTreeNode,
    ResourceUpdateRequest,
    SysResourcePermissionRelSchema,
    SysResourceSchema,
)
from app.modules.iam.resource.service import ResourceService

router = APIRouter()


@router.post(
    "/sys/resources/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: ResourceCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ResourceService(db).create(payload)
    return success()


@router.post(
    "/sys/resources/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: ResourceUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ResourceService(db).update(payload)
    return success()


@router.post(
    "/sys/resources/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ResourceService(db).delete(payload)
    return success()


@router.get(
    "/sys/resources/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:detail")),
    ],
    response_model=ApiResponse[SysResourceSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysResourceSchema]:
    return success(await ResourceService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/resources/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:page")),
    ],
    response_model=ApiResponse[PageData[SysResourceSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    code: str | None = Query(default=None, max_length=64),
    name: str | None = Query(default=None, max_length=64),
    resource_type: str | None = Query(default=None, max_length=32),
    module: str | None = Query(default=None, max_length=64),
    parent_id: str | None = Query(default=None, max_length=64),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysResourceSchema]]:
    query = ResourceAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        code=code,
        name=name,
        resource_type=resource_type,
        module=module,
        parent_id=parent_id,
        status=status,
    )
    return success(await ResourceService(db).page_admin(query))


@router.get(
    "/sys/resources/current",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
    ],
    response_model=ApiResponse[list[SysResourceSchema]],
)
async def current_resources(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[SysResourceSchema]]:
    return success(await ResourceService(db).list_current_resources(session))


@router.get(
    "/sys/resources/tree",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:list")),
    ],
    response_model=ApiResponse[list[ResourceTreeNode]],
)
async def list_resource_tree(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[ResourceTreeNode]]:
    return success(await ResourceService(db).list_resource_tree(session))


@router.post(
    "/resource-permissions",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:resource:grant")),
    ],
    response_model=ApiResponse[SysResourcePermissionRelSchema],
)
async def bind_resource_permission(
    payload: ResourcePermissionBindRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload, Depends(get_current_session)],
) -> ApiResponse[SysResourcePermissionRelSchema]:
    return success(await ResourceService(db).bind_resource_permission(payload, session))
