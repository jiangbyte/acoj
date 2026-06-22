from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_scope
from app.deps.db import get_db_session
from app.modules.banner.schema import (
    BannerAdminListQuery,
    BannerCreateRequest,
    BannerUpdateRequest,
    SysBannerSchema,
)
from app.modules.banner.service import BannerService

router = APIRouter()


@router.post(
    "/sys/banners/create",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:banner:create")),
    ],
    response_model=ApiResponse[SysBannerSchema],
)
async def create(
    payload: BannerCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysBannerSchema]:
    return success(await BannerService(db).create(payload))


@router.post(
    "/sys/banners/update",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:banner:update")),
    ],
    response_model=ApiResponse[SysBannerSchema],
)
async def update(
    payload: BannerUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[SysBannerSchema]:
    return success(await BannerService(db).update(payload))


@router.post(
    "/sys/banners/delete",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:banner:delete")),
    ],
    response_model=ApiResponse[list[str]],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[str]]:
    deleted_ids = await BannerService(db).delete(payload)
    return success(deleted_ids)


@router.get(
    "/sys/banners/detail",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:banner:detail")),
    ],
    response_model=ApiResponse[SysBannerSchema],
)
async def get(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysBannerSchema]:
    return success(await BannerService(db).get(IdQuery(id=id)))


@router.get(
    "/sys/banners/list",
    dependencies=[
        # Depends(require_scope(LoginScope.ADMIN)),
        # Depends(require_permission("sys:banner:list")),
    ],
    response_model=ApiResponse[PageData[SysBannerSchema]],
)
async def lists(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    display_scope: str | None = Query(default=None, max_length=32),
    category: str | None = Query(default=None, max_length=32),
    type: str | None = Query(default=None, max_length=32),
    position: str | None = Query(default=None, max_length=32),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysBannerSchema]]:
    query = BannerAdminListQuery(
        pagination=PageQuery(current=current, size=size),
        display_scope=display_scope,
        category=category,
        type=type,
        position=position,
        status=status,
    )
    return success(await BannerService(db).list_admin(query))
