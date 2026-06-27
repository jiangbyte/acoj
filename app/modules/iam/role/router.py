from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_scope
from app.deps.db import get_db_session
from app.modules.iam.role.schema import (
    RoleAdminPageQuery,
    RoleCreateRequest,
    RoleUpdateRequest,
    SysRoleSchema,
)
from app.modules.iam.role.service import RoleService

router = APIRouter()


@router.post(
    "/sys/roles/create",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("iam:role:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: RoleCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await RoleService(db).create(payload)
    return success()


@router.post(
    "/sys/roles/update",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("iam:role:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: RoleUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await RoleService(db).update(payload)
    return success()


@router.post(
    "/sys/roles/delete",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("iam:role:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await RoleService(db).delete(payload)
    return success()


@router.get(
    "/sys/roles/detail",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("iam:role:detail")),
    ],
    response_model=ApiResponse[SysRoleSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysRoleSchema]:
    return success(await RoleService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/roles/page",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("iam:role:page")),
    ],
    response_model=ApiResponse[PageData[SysRoleSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    code: str | None = Query(default=None, max_length=64),
    name: str | None = Query(default=None, max_length=64),
    category: str | None = Query(default=None, max_length=64),
    scope_type: str | None = Query(default=None, max_length=32),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysRoleSchema]]:
    query = RoleAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        code=code,
        name=name,
        category=category,
        scope_type=scope_type,
        status=status,
    )
    return success(await RoleService(db).page_admin(query))
