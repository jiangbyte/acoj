from typing import Annotated

from fastapi import APIRouter, Depends, Header
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.settings import settings
from app.core.exceptions.business import AuthorizationError
from app.core.response.schema import ApiResponse, success
from app.deps.db import get_db_session
from app.modules.judge.schema import (
    JudgeNodeHeartbeatRequest,
    JudgeNodeRegisterRequest,
    JudgeNodeSchema,
    JudgeTaskEventRequest,
    JudgeTaskFinishRequest,
    JudgeTaskPollRequest,
    JudgeTaskSchema,
)
from app.modules.judge.service import JudgeService

router = APIRouter(prefix="/judge", tags=["internal-judge"])


def require_judge_token(x_acoj_judger_token: Annotated[str | None, Header()] = None) -> None:
    if x_acoj_judger_token != settings.judge.node_token:
        raise AuthorizationError("Invalid judge node token")


@router.post(
    "/nodes/register",
    dependencies=[Depends(require_judge_token)],
    response_model=ApiResponse[JudgeNodeSchema],
)
async def register_node(
    payload: JudgeNodeRegisterRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[JudgeNodeSchema]:
    return success(await JudgeService(db).register_node(payload))


@router.post(
    "/nodes/heartbeat",
    dependencies=[Depends(require_judge_token)],
    response_model=ApiResponse[JudgeNodeSchema],
)
async def heartbeat(
    payload: JudgeNodeHeartbeatRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[JudgeNodeSchema]:
    return success(await JudgeService(db).heartbeat(payload))


@router.post(
    "/tasks/poll",
    dependencies=[Depends(require_judge_token)],
    response_model=ApiResponse[JudgeTaskSchema | None],
)
async def poll_task(
    payload: JudgeTaskPollRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[JudgeTaskSchema | None]:
    return success(await JudgeService(db).poll_task(payload))


@router.post(
    "/tasks/{task_id}/events",
    dependencies=[Depends(require_judge_token)],
    response_model=ApiResponse[dict],
)
async def record_event(
    task_id: str,
    payload: JudgeTaskEventRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[dict]:
    return success(await JudgeService(db).record_event(task_id, payload))


@router.post(
    "/tasks/{task_id}/finish",
    dependencies=[Depends(require_judge_token)],
    response_model=ApiResponse[JudgeTaskSchema],
)
async def finish_task(
    task_id: str,
    payload: JudgeTaskFinishRequest,
    db: Annotated[AsyncSession, Depends(get_db_session)],
) -> ApiResponse[JudgeTaskSchema]:
    return success(await JudgeService(db).finish_task(task_id, payload))
