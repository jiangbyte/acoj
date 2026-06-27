from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.deps.auth import require_permission, require_scope
from app.deps.db import get_db_session
from app.modules.dict.schema import (
    DictAdminPageQuery,
    DictCreateRequest,
    DictId,
    DictIdQuery,
    DictIdsRequest,
    DictTreeQuery,
    DictUpdateRequest,
    SysDictSchema,
    SysDictTreeNode,
)
from app.modules.dict.service import DictService

router = APIRouter()


@router.post(
    "/sys/dicts/create",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:dict:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: DictCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DictService(db).create(payload)
    return success()


@router.post(
    "/sys/dicts/update",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:dict:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: DictUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DictService(db).update(payload)
    return success()


@router.post(
    "/sys/dicts/delete",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:dict:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: DictIdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DictService(db).delete(payload)
    return success()


@router.get(
    "/sys/dicts/detail",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:dict:detail")),
    ],
    response_model=ApiResponse[SysDictSchema],
)
async def get(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[DictId, Query()],
) -> ApiResponse[SysDictSchema]:
    return success(await DictService(db).get(DictIdQuery(id=id)))


@router.get(
    "/sys/dicts/page",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:dict:page")),
    ],
    response_model=ApiResponse[PageData[SysDictSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    code: str | None = Query(default=None, max_length=50),
    category: str | None = Query(default=None, max_length=64),
    parent_id: str | None = Query(default=None, max_length=32),
    status: str | None = Query(default=None, max_length=16),
) -> ApiResponse[PageData[SysDictSchema]]:
    query = DictAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        code=code,
        category=category,
        parent_id=parent_id,
        status=status,
    )
    return success(await DictService(db).page_admin(query))


@router.get(
    "/sys/dicts/tree",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:dict:tree")),
    ],
    response_model=ApiResponse[list[SysDictTreeNode]],
)
async def tree(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    category: str | None = Query(default=None, max_length=64),
) -> ApiResponse[list[SysDictTreeNode]]:
    return success(await DictService(db).list_tree(DictTreeQuery(category=category)))
