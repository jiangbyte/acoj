"""Admin + portal routers for study features."""

from __future__ import annotations

from datetime import date
from typing import Annotated

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import AccountType
from app.core.response.pagination import Current, PageData, PageQuery, Size
from app.core.response.schema import ApiResponse, success
from app.core.schema.base import Id, IdsRequest
from app.core.security.session import SessionPayload
from app.deps.auth import get_current_session, get_optional_session, require_account_type
from app.deps.db import get_db_session
from app.modules.biz.study.daily_service import (
    DailyAdminPageQuery,
    DailyCalendarSchema,
    DailyProblemBrief,
    DailyService,
    DailyTodaySchema,
    DailyUpsertRequest,
)
from app.modules.biz.study.enums import LearningPlanCategory, ProblemListKind
from app.modules.biz.study.learning_plan_schema import (
    LearningPlanAdminPageQuery,
    LearningPlanCreateRequest,
    LearningPlanSchema,
    LearningPlanUpdateRequest,
)
from app.modules.biz.study.learning_plan_service import LearningPlanService
from app.modules.biz.study.problem_list_schema import (
    OfficialProblemListCreateRequest,
    ProblemListAdminPageQuery,
    ProblemListCreateRequest,
    ProblemListItemMutation,
    ProblemListReorderRequest,
    ProblemListSchema,
    ProblemListUpdateRequest,
)
from app.modules.biz.study.problem_list_service import ProblemListService
from app.modules.biz.study.user_stats_service import (
    RecentSolvedItem,
    UserHeatmapSchema,
    UserStatsSchema,
    UserStatsService,
)

admin_router = APIRouter()
portal_router = APIRouter()


# ---------- Admin: problem list ----------
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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    title: str | None = None,
    code: str | None = None,
    status: str | None = None,
) -> ApiResponse[PageData[ProblemListSchema]]:
    return success(
        await ProblemListService(db).admin_page(
            ProblemListAdminPageQuery(
                pagination=PageQuery(current=current, size=size),
                title=title,
                code=code,
                kind=ProblemListKind.OFFICIAL,
                status=status,
            )
        )
    )


@admin_router.get(
    "/biz/problem-list/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[ProblemListSchema],
)
async def admin_list_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[ProblemListSchema]:
    return success(await ProblemListService(db).admin_detail(id))


# ---------- Admin: learning plan ----------
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
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    title: str | None = None,
    code: str | None = None,
    category: LearningPlanCategory | None = None,
    status: str | None = None,
) -> ApiResponse[PageData[LearningPlanSchema]]:
    return success(
        await LearningPlanService(db).page_admin(
            LearningPlanAdminPageQuery(
                pagination=PageQuery(current=current, size=size),
                title=title,
                code=code,
                category=category,
                status=status,
            )
        )
    )


@admin_router.get(
    "/biz/learning-plan/detail",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[LearningPlanSchema],
)
async def admin_plan_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
) -> ApiResponse[LearningPlanSchema]:
    return success(await LearningPlanService(db).detail_admin(id))


# ---------- Admin: daily ----------
@admin_router.post(
    "/biz/daily/upsert",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_daily_upsert(
    payload: DailyUpsertRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await DailyService(db).upsert(payload)})


@admin_router.post(
    "/biz/daily/delete",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
)
async def admin_daily_delete(
    payload: IdsRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await DailyService(db).delete(payload)
    return success()


@admin_router.get(
    "/biz/daily/page",
    dependencies=[Depends(require_account_type(AccountType.ADMIN))],
    response_model=ApiResponse[PageData[DailyProblemBrief]],
)
async def admin_daily_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
    from_date: date | None = None,
    to_date: date | None = None,
) -> ApiResponse[PageData[DailyProblemBrief]]:
    return success(
        await DailyService(db).page_admin(
            DailyAdminPageQuery(
                pagination=PageQuery(current=current, size=size),
                from_date=from_date,
                to_date=to_date,
            )
        )
    )


# ---------- Portal: problem list ----------
@portal_router.get("/biz/problem-list/mine", response_model=ApiResponse[list[ProblemListSchema]])
async def portal_list_mine(
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
    _: Annotated[None, Depends(require_account_type(AccountType.PORTAL))] = None,
) -> ApiResponse[list[ProblemListSchema]]:
    return success(await ProblemListService(db).mine(session.account_id))


@portal_router.get("/biz/problem-list/page", response_model=ApiResponse[PageData[ProblemListSchema]])
async def portal_list_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 20,
) -> ApiResponse[PageData[ProblemListSchema]]:
    return success(await ProblemListService(db).official_page(PageQuery(current=current, size=size)))


@portal_router.get("/biz/problem-list/detail", response_model=ApiResponse[ProblemListSchema])
async def portal_list_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[ProblemListSchema]:
    return success(await ProblemListService(db).detail(id, session.account_id if session else None))


@portal_router.post(
    "/biz/problem-list/create",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_create(
    payload: ProblemListCreateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success({"id": await ProblemListService(db).portal_create(session.account_id, payload)})


@portal_router.post(
    "/biz/problem-list/update",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_list_update(
    payload: ProblemListUpdateRequest,
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).portal_update(session.account_id, payload)
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
    await ProblemListService(db).portal_delete(session.account_id, payload)
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
    await ProblemListService(db).add_item(session.account_id, payload)
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
    await ProblemListService(db).remove_item(session.account_id, payload)
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
    await ProblemListService(db).reorder(session.account_id, payload)
    return success()


@portal_router.get(
    "/biz/problem-list/favorite/status",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_favorite_status(
    problem_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict[str, bool]]:
    favorited = await ProblemListService(db).is_favorited(session.account_id, problem_id)
    return success({"favorited": favorited})


@portal_router.post(
    "/biz/problem-list/favorite/add",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_favorite_add(
    problem_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).add_favorite(session.account_id, problem_id)
    return success()


@portal_router.post(
    "/biz/problem-list/favorite/remove",
    dependencies=[Depends(require_account_type(AccountType.PORTAL))],
)
async def portal_favorite_remove(
    problem_id: Annotated[Id, Query()],
    session: Annotated[SessionPayload, Depends(get_current_session)],
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[None]:
    await ProblemListService(db).remove_favorite(session.account_id, problem_id)
    return success()


# ---------- Portal: learning plan ----------
@portal_router.get("/biz/learning-plan/page", response_model=ApiResponse[PageData[LearningPlanSchema]])
async def portal_plan_page(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    current: Current = 1,
    size: Size = 50,
    category: str | None = None,
) -> ApiResponse[PageData[LearningPlanSchema]]:
    return success(
        await LearningPlanService(db).page_portal(PageQuery(current=current, size=size), category=category)
    )


@portal_router.get("/biz/learning-plan/detail", response_model=ApiResponse[LearningPlanSchema])
async def portal_plan_detail(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    id: Annotated[Id, Query()],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[LearningPlanSchema]:
    return success(
        await LearningPlanService(db).detail_portal(id, session.account_id if session else None)
    )


# ---------- Portal: daily ----------
@portal_router.get("/biz/daily/today", response_model=ApiResponse[DailyTodaySchema])
async def portal_daily_today(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[DailyTodaySchema]:
    return success(await DailyService(db).today(session.account_id if session else None))


@portal_router.get("/biz/daily/calendar", response_model=ApiResponse[DailyCalendarSchema])
async def portal_daily_calendar(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    year: int = Query(...),
    month: int = Query(...),
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
) -> ApiResponse[DailyCalendarSchema]:
    return success(await DailyService(db).calendar(year, month, session.account_id if session else None))


# ---------- Portal: user stats ----------
@portal_router.get("/biz/user/stats", response_model=ApiResponse[UserStatsSchema])
async def portal_user_stats(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    account_id: str | None = Query(default=None),
) -> ApiResponse[UserStatsSchema]:
    target = account_id or (session.account_id if session else None)
    if not target:
        from app.core.exceptions.business import AuthenticationError

        raise AuthenticationError("需要登录或指定 account_id")
    return success(await UserStatsService(db).stats(target))


@portal_router.get("/biz/user/heatmap", response_model=ApiResponse[UserHeatmapSchema])
async def portal_user_heatmap(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    account_id: str | None = Query(default=None),
    year: int = Query(...),
) -> ApiResponse[UserHeatmapSchema]:
    target = account_id or (session.account_id if session else None)
    if not target:
        from app.core.exceptions.business import AuthenticationError

        raise AuthenticationError("需要登录或指定 account_id")
    return success(await UserStatsService(db).heatmap(target, year))


@portal_router.get("/biz/user/recent-solved", response_model=ApiResponse[PageData[RecentSolvedItem]])
async def portal_user_recent_solved(
    db: Annotated[AsyncSession, Depends(get_db_session)],
    session: Annotated[SessionPayload | None, Depends(get_optional_session)] = None,
    account_id: str | None = Query(default=None),
    current: Current = 1,
    size: Size = 20,
) -> ApiResponse[PageData[RecentSolvedItem]]:
    target = account_id or (session.account_id if session else None)
    if not target:
        from app.core.exceptions.business import AuthenticationError

        raise AuthenticationError("需要登录或指定 account_id")
    return success(
        await UserStatsService(db).recent_solved(target, PageQuery(current=current, size=size))
    )
