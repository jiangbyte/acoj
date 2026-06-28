from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_account_type
from app.deps.db import get_db_session
from app.modules.iam.position.schema import (
    PositionAdminPageQuery,
    PositionCreateRequest,
    PositionUpdateRequest,
    SysPositionSchema,
)
from app.modules.iam.position.service import PositionService

router = APIRouter()


@router.post(
    "/sys/positions/create",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:position:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: PositionCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PositionService(db).create(payload)
    return success()


@router.post(
    "/sys/positions/update",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:position:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: PositionUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PositionService(db).update(payload)
    return success()


@router.post(
    "/sys/positions/delete",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:position:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await PositionService(db).delete(payload)
    return success()


@router.get(
    "/sys/positions/detail",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:position:detail")),
    ],
    response_model=ApiResponse[SysPositionSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysPositionSchema]:
    return success(await PositionService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/positions/page",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("iam:position:page")),
    ],
    response_model=ApiResponse[PageData[SysPositionSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    name: str | None = Query(default=None, max_length=64),
    code: str | None = Query(default=None, max_length=64),
    category: str | None = Query(default=None, max_length=32),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysPositionSchema]]:
    query = PositionAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        name=name,
        code=code,
        category=category,
        status=status,
    )
    return success(await PositionService(db).page_admin(query))
