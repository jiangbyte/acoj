from fastapi import APIRouter, Depends, Query
from sdk.web.result import success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.auth.decorator import HeiCheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import GroupVO, GroupPageParam, GroupTreeParam
from ...service import GroupService, get_group_service

router = APIRouter()


@router.get("/api/v1/sys/group/page", summary="获取用户组分页")
@HeiCheckPermission("sys:group:page")
async def page(param: GroupPageParam = Depends(), service: GroupService = Depends(get_group_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/group/tree", summary="获取用户组树")
@HeiCheckPermission("sys:group:tree")
async def group_tree(param: GroupTreeParam = Depends(), service: GroupService = Depends(get_group_service)):
    return success(service.tree(param))


@router.get("/api/v1/sys/group/union-tree", summary="获取组织用户组合并树")
@HeiCheckPermission("sys:group:tree")
async def union_tree(service: GroupService = Depends(get_group_service)):
    return success(service.union_tree())


@router.post("/api/v1/sys/group/create", summary="添加用户组")
@SysLog("添加用户组")
@HeiCheckPermission("sys:group:create")
@NoRepeat(interval=3000)
async def create(
    vo: GroupVO,
    service: GroupService = Depends(get_group_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/group/modify", summary="编辑用户组")
@SysLog("编辑用户组")
@HeiCheckPermission("sys:group:modify")
async def modify(
    vo: GroupVO,
    service: GroupService = Depends(get_group_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/group/remove", summary="删除用户组")
@SysLog("删除用户组")
@HeiCheckPermission("sys:group:remove")
async def remove(param: IdsParam, service: GroupService = Depends(get_group_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/group/detail", summary="获取用户组详情")
@HeiCheckPermission("sys:group:detail")
async def detail(id: str = Query(...), service: GroupService = Depends(get_group_service)):
    data = service.detail(id)
    return success(data if data else None)
