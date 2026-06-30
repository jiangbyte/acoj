from typing import Annotated

from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.exceptions.business import AuthorizationError
from app.core.response.schema import ApiResponse, success
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, require_account_type
from app.deps.db import get_db_session
from app.modules.message.realtime.schema import HeaderNoticeItem, MessageSummaryResponse
from app.modules.message.realtime.service import RealtimeService, get_session_by_token

admin_router = APIRouter()
portal_router = APIRouter()


def register_current_user_routes(router: APIRouter, account_type: AccountType) -> None:
    dependencies = [Depends(require_account_type(account_type))]

    @router.get(
        "/message/summary",
        dependencies=dependencies,
        response_model=ApiResponse[MessageSummaryResponse],
    )
    async def summary(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[MessageSummaryResponse]:
        return success(await RealtimeService(db).summary(session))

    @router.get(
        "/message/header-items",
        dependencies=dependencies,
        response_model=ApiResponse[list[HeaderNoticeItem]],
    )
    async def header_items(
        session: Annotated[SessionPayload, Depends(get_current_session)],
        db: Annotated[AsyncSession, Depends(get_db_session)],
    ) -> ApiResponse[list[HeaderNoticeItem]]:
        return success(await RealtimeService(db).header_items(session))

    @router.get(
        "/message/realtime/events",
        response_class=StreamingResponse,
    )
    async def events(
        db: Annotated[AsyncSession, Depends(get_db_session)],
        token: str | None = Query(default=None),
    ) -> StreamingResponse:
        session = await get_session_by_token(token)
        if str(session.account_type) != account_type.value:
            raise AuthorizationError("Account type is not allowed")
        return StreamingResponse(
            RealtimeService(db).event_stream(session),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "X-Accel-Buffering": "no",
            },
        )


register_current_user_routes(admin_router, AccountType.ADMIN)
register_current_user_routes(portal_router, AccountType.PORTAL)
