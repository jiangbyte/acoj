"""Home API — mirrors hei-gin plugin-sys/home/api/v1/api.go."""

from fastapi import APIRouter, Depends
from sdk.web.result import Result, success
from sdk.auth.decorator import CheckLogin
from sdk.log import SysLog
from sdk.shared.di import ActorContext, get_current_actor
from ...params import AddQuickActionParam, RemoveQuickActionParam, SortQuickActionParam
from ...service import HomeService, get_home_service

router = APIRouter()


@router.get("/api/v1/sys/home", summary="获取首页数据", response_model=Result)
@CheckLogin
async def get_home(
    service: HomeService = Depends(get_home_service),
    actor: ActorContext = Depends(get_current_actor),
):
    return success(await service.home(actor))


@router.post("/api/v1/sys/home/quick-actions/add", summary="添加快捷方式", response_model=Result)
@CheckLogin
@SysLog("添加快捷方式")
async def add_quick_action(
    param: AddQuickActionParam,
    service: HomeService = Depends(get_home_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.add_quick_action(param, actor)
    return success()


@router.post("/api/v1/sys/home/quick-actions/remove", summary="移除快捷方式", response_model=Result)
@CheckLogin
@SysLog("移除快捷方式")
async def remove_quick_action(
    param: RemoveQuickActionParam,
    service: HomeService = Depends(get_home_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.remove_quick_action(param, actor)
    return success()


@router.post("/api/v1/sys/home/quick-actions/sort", summary="排序快捷方式", response_model=Result)
@CheckLogin
@SysLog("排序快捷方式")
async def sort_quick_actions(
    param: SortQuickActionParam,
    service: HomeService = Depends(get_home_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.sort_quick_actions(param, actor)
    return success()
