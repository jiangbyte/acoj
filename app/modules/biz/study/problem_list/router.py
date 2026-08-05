"""Admin + portal routers for problem lists."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import IdQuery, IdsRequest, ProblemIdQuery
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.study.problem_list.schema import (
    OfficialProblemListCreateRequest,
    ProblemListAdminPageQuery,
    ProblemListCreateRequest,
    ProblemListItemMutation,
    ProblemListOfficialPageQuery,
    ProblemListReorderRequest,
    ProblemListSchema,
    ProblemListUpdateRequest,
)
from app.modules.biz.study.problem_list.service import ProblemListService

admin_router = APIRouter()
portal_router = APIRouter()


@admin_router.post(
    "/biz/problem-list/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_list_create(
    payload: OfficialProblemListCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await ProblemListService(db).admin_create(payload)})


@admin_router.post(
    "/biz/problem-list/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_list_update(
    payload: ProblemListUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).admin_update(payload)
    return success()


@admin_router.post(
    "/biz/problem-list/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_list_delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).admin_delete(payload)
    return success()


@admin_router.get(
    "/biz/problem-list/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[PageData[ProblemListSchema]],
)
async def admin_list_page(
    query: Annotated[ProblemListAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[ProblemListSchema]]:
    return success(await ProblemListService(db).admin_page(query))


@admin_router.get(
    "/biz/problem-list/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[ProblemListSchema],
)
async def admin_list_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[ProblemListSchema]:
    return success(await ProblemListService(db).admin_detail(query))


@portal_router.get("/biz/problem-list/mine", response_model=ApiResponse[list[ProblemListSchema]])
async def portal_list_mine(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    _: Annotated[None, Depends(require_account_type(AccountType.PORTAL))] = None,
) -> ApiResponse[list[ProblemListSchema]]:
    return success(await ProblemListService(db).mine(session))


@portal_router.get("/biz/problem-list/page", response_model=ApiResponse[PageData[ProblemListSchema]])
async def portal_list_page(
    query: Annotated[ProblemListOfficialPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[ProblemListSchema]]:
    return success(await ProblemListService(db).official_page(query))


@portal_router.get("/biz/problem-list/detail", response_model=ApiResponse[ProblemListSchema])
async def portal_list_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[ProblemListSchema]:
    return success(await ProblemListService(db).detail(query, session=session))


@portal_router.post(
    "/biz/problem-list/create",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_create(
    payload: ProblemListCreateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await ProblemListService(db).portal_create(session, payload)})


@portal_router.post(
    "/biz/problem-list/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_update(
    payload: ProblemListUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).portal_update(session, payload)
    return success()


@portal_router.post(
    "/biz/problem-list/delete",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_delete(
    payload: IdsRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).portal_delete(session, payload)
    return success()


@portal_router.post(
    "/biz/problem-list/item/add",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_item_add(
    payload: ProblemListItemMutation,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).add_item(session, payload)
    return success()


@portal_router.post(
    "/biz/problem-list/item/remove",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_item_remove(
    payload: ProblemListItemMutation,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).remove_item(session, payload)
    return success()


@portal_router.post(
    "/biz/problem-list/item/reorder",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_item_reorder(
    payload: ProblemListReorderRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).reorder(session, payload)
    return success()


@portal_router.get(
    "/biz/problem-list/favorite/status",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_favorite_status(
    query: Annotated[ProblemIdQuery, Depends()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict[str, bool]]:
    favorited = await ProblemListService(db).is_favorited(session, query)
    return success({"favorited": favorited})


@portal_router.post(
    "/biz/problem-list/favorite/add",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_favorite_add(
    payload: ProblemIdQuery,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).add_favorite(session, payload)
    return success()


@portal_router.post(
    "/biz/problem-list/favorite/remove",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_favorite_remove(
    payload: ProblemIdQuery,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).remove_favorite(session, payload)
    return success()
