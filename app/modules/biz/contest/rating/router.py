"""Admin rating settlement routes."""

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import ContestIdQuery, to_schema_list
from app.deps.auth import require_account_type, require_permission
from app.deps.db import get_db_session
from app.modules.biz.contest.rating.schema import (
    OjContestRateResultSchema,
    OjContestRatingSchema,
    OjContestUndoRateResultSchema,
)
from app.modules.biz.contest.rating.service import OjContestRatingService

router = APIRouter()


@router.post(
    "/biz/contest/rating/rate",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:rating:rate")),
    ],
    response_model=ApiResponse[OjContestRateResultSchema],
)
async def rate(
    query: Annotated[ContestIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjContestRateResultSchema]:
    result = await OjContestRatingService(db).rate_contest(query)
    return success(OjContestRateResultSchema.model_validate(result))


@router.post(
    "/biz/contest/rating/undo",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:rating:undo")),
    ],
    response_model=ApiResponse[OjContestUndoRateResultSchema],
)
async def undo(
    query: Annotated[ContestIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[OjContestUndoRateResultSchema]:
    result = await OjContestRatingService(db).undo_rate(query)
    return success(OjContestUndoRateResultSchema.model_validate(result))


@router.get(
    "/biz/contest/rating/list",
    dependencies=[
        Depends(require_account_type(AccountType.ADMIN)),
        Depends(require_permission("biz:contest:rating:list")),
    ],
    response_model=ApiResponse[list[OjContestRatingSchema]],
)
async def list_ratings(
    query: Annotated[ContestIdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[list[OjContestRatingSchema]]:
    items = await OjContestRatingService(db).list_ratings(query)
    return success(to_schema_list(OjContestRatingSchema, items))
