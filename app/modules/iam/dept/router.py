from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import LoginScope
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdQuery, IdsRequest
from app.deps.auth import require_permission, require_scope
from app.deps.db import get_db_session
from app.modules.iam.dept.schema import (
    DeptAdminPageQuery,
    DeptCreateRequest,
    DeptTreeNode,
    DeptUpdateRequest,
    SysDeptSchema,
)
from app.modules.iam.dept.service import DeptService

router = APIRouter()


@router.post(
    "/sys/depts/create",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:create")),
    ],
    response_model=ApiResponse[None],
)
async def create(
    payload: DeptCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DeptService(db).create(payload)
    return success()


@router.post(
    "/sys/depts/update",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:update")),
    ],
    response_model=ApiResponse[None],
)
async def update(
    payload: DeptUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DeptService(db).update(payload)
    return success()


@router.post(
    "/sys/depts/delete",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:delete")),
    ],
    response_model=ApiResponse[None],
)
async def delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DeptService(db).delete(payload)
    return success()


@router.get(
    "/sys/depts/detail",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:detail")),
    ],
    response_model=ApiResponse[SysDeptSchema],
)
async def detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[SysDeptSchema]:
    return success(await DeptService(db).detail(IdQuery(id=id)))


@router.get(
    "/sys/depts/page",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:page")),
    ],
    response_model=ApiResponse[PageData[SysDeptSchema]],
)
async def page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    name: str | None = Query(default=None, max_length=64),
    code: str | None = Query(default=None, max_length=64),
    category: str | None = Query(default=None, max_length=64),
    parent_id: str | None = Query(default=None, max_length=64),
    status: str | None = Query(default=None, max_length=32),
) -> ApiResponse[PageData[SysDeptSchema]]:
    query = DeptAdminPageQuery(
        pagination=PageQuery(current=current, size=size),
        name=name,
        code=code,
        category=category,
        parent_id=parent_id,
        status=status,
    )
    return success(await DeptService(db).page_admin(query))


@router.get(
    "/sys/depts/tree",
    dependencies=[
        Depends(require_scope(LoginScope.ADMIN)),
        Depends(require_permission("iam:dept:list")),
    ],
    response_model=ApiResponse[list[DeptTreeNode]],
)
async def list_dept_tree(
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[DeptTreeNode]]:
    return success(await DeptService(db).list_dept_tree())
