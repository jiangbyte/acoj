from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.message.friend.schema import (
    FriendApplyRequest,
    FriendHandleRequest,
    FriendRemoveRequest,
    FriendRequestCountResponse,
    FriendRequestSchema,
    FriendSchema,
    FriendSearchSchema,
    FriendSetRemarkRequest,
)
from app.modules.message.friend.service import FriendService

admin_router = APIRouter()
portal_router = APIRouter()


def register_current_user_routes(router: APIRouter, account_type: AccountType) -> None:
    dependencies = [Depends(require_account_type(account_type))]

    @router.get(
        "/friends/my-list",
        dependencies=dependencies,
        response_model=ApiResponse[list[FriendSchema]],
    )
    async def my_friends(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[list[FriendSchema]]:
        return success(await FriendService(db).list_my_friends(session))

    @router.get(
        "/friends/search",
        dependencies=dependencies,
        response_model=ApiResponse[list[FriendSearchSchema]],
    )
    async def search_users(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
        keyword: str = Query(min_length=1, max_length=100),
    ) -> ApiResponse[list[FriendSearchSchema]]:
        return success(await FriendService(db).search_users(keyword, session))

    @router.post(
        "/friends/apply",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def apply_friend(
        payload: FriendApplyRequest,
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[None]:
        await FriendService(db).apply_friend(payload, session)
        return success()

    @router.post(
        "/friends/handle-request",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def handle_friend_request(
        payload: FriendHandleRequest,
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[None]:
        await FriendService(db).handle_friend_request(payload, session)
        return success()

    @router.post(
        "/friends/remove",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def remove_friend(
        payload: FriendRemoveRequest,
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[None]:
        await FriendService(db).remove_friend(payload, session)
        return success()

    @router.post(
        "/friends/set-remark",
        dependencies=dependencies,
        response_model=ApiResponse[None],
    )
    async def set_remark(
        payload: FriendSetRemarkRequest,
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[None]:
        await FriendService(db).set_remark(payload, session)
        return success()

    @router.get(
        "/friends/my-requests",
        dependencies=dependencies,
        response_model=ApiResponse[list[FriendRequestSchema]],
    )
    async def my_requests(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[list[FriendRequestSchema]]:
        return success(await FriendService(db).my_requests(session))

    @router.get(
        "/friends/my-request-count",
        dependencies=dependencies,
        response_model=ApiResponse[FriendRequestCountResponse],
    )
    async def pending_request_count(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[FriendRequestCountResponse]:
        return success(await FriendService(db).pending_request_count(session))


register_current_user_routes(admin_router, AccountType.ADMIN)
register_current_user_routes(portal_router, AccountType.PORTAL)
