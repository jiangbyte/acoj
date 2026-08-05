"""Admin + portal routers for learning plans."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import PageData
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import IdQuery, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.study.learning_plan.schema import (
    LearningPlanAdminPageQuery,
    LearningPlanCreateRequest,
    LearningPlanPortalPageQuery,
    LearningPlanSchema,
    LearningPlanUpdateRequest,
)
from app.modules.biz.study.learning_plan.service import LearningPlanService

admin_router = APIRouter()
portal_router = APIRouter()


@admin_router.post(
    "/biz/learning-plan/create",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_plan_create(
    payload: LearningPlanCreateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await LearningPlanService(db).create(payload)})


@admin_router.post(
    "/biz/learning-plan/update",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_plan_update(
    payload: LearningPlanUpdateRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await LearningPlanService(db).update(payload)
    return success()


@admin_router.post(
    "/biz/learning-plan/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_plan_delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await LearningPlanService(db).delete(payload)
    return success()


@admin_router.get(
    "/biz/learning-plan/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[PageData[LearningPlanSchema]],
)
async def admin_plan_page(
    query: Annotated[LearningPlanAdminPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[LearningPlanSchema]]:
    return success(await LearningPlanService(db).page_admin(query))


@admin_router.get(
    "/biz/learning-plan/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[LearningPlanSchema],
)
async def admin_plan_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[LearningPlanSchema]:
    return success(await LearningPlanService(db).detail_admin(query))


@portal_router.get("/biz/learning-plan/page", response_model=ApiResponse[PageData[LearningPlanSchema]])
async def portal_plan_page(
    query: Annotated[LearningPlanPortalPageQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[PageData[LearningPlanSchema]]:
    return success(await LearningPlanService(db).page_portal(query))


@portal_router.get("/biz/learning-plan/detail", response_model=ApiResponse[LearningPlanSchema])
async def portal_plan_detail(
    query: Annotated[IdQuery, Depends()],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[LearningPlanSchema]:
    return success(await LearningPlanService(db).detail_portal(query, session=session))
