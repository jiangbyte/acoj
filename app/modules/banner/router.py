from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.banner.schema import (
    BannerAdminPageQuery,
    BannerCreateRequest,
    BannerUpdateRequest,
    SysBannerSchema,
)
from app.modules.banner.service import BannerService

router = APIRouter()


@router.post(
    "/sys/banners/create",
    dependencies=[
        # Depends(require_account_type(AccountType.ADMIN)),
        # Depends(require_permission("sys:banner:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: BannerCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await BannerService(db).create(payload)
    return success()


@router.post(
    "/sys/banners/update",
    dependencies=[
        # Depends(require_account_type(AccountType.ADMIN)),
        # Depends(require_permission("sys:banner:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: BannerUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await BannerService(db).update(payload)
    return success()


@router.post(
    "/sys/banners/delete",
    dependencies=[
        # Depends(require_account_type(AccountType.ADMIN)),
        # Depends(require_permission("sys:banner:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await BannerService(db).delete(payload)
    return success()


@router.get(
    "/sys/banners/detail",
    dependencies=[
        # Depends(require_account_type(AccountType.ADMIN)),
        # Depends(require_permission("sys:banner:detail")),
    ],
    response_model=ApiResponse[SysBannerSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysBannerSchema]:
    return success(await BannerService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/banners/page",
    dependencies=[
        # Depends(require_account_type(AccountType.ADMIN)),
        # Depends(require_permission("sys:banner:page")),
    ],
    response_model=ApiResponse[PageData[SysBannerSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    display_scope: str | None = Query(default=None, max_length=32),
    category: str | None = Query(default=None, max_length=32),
    type: str | None = Query(default=None, max_length=32),
    position: str | None = Query(default=None, max_length=32),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysBannerSchema]]:
    query = BannerAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        display_scope=display_scope,
        category=category,
        type=type,
        position=position,
        status=status,
    )
    return success(await BannerService(db).page_admin(query))
