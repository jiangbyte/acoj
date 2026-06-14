from fastapi import APIRouter, Depends, Query
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import GroupVO, GroupPageParam, GroupTreeParam, GroupTreeVO, UnionTreeNode
from ...service import GroupService, get_group_service

router = APIRouter()


@router.get("/api/v1/sys/group/page", summary="获取用户组分页", response_model=Result[PageData[GroupVO]])
@CheckPermission("sys:group:page")
def page(param: GroupPageParam = Depends(), service: GroupService = Depends(get_group_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/group/tree", summary="获取用户组树", response_model=Result[list[GroupTreeVO]])
@CheckPermission("sys:group:tree")
def group_tree(param: GroupTreeParam = Depends(), service: GroupService = Depends(get_group_service)):
    return success(service.tree(param))


@router.get("/api/v1/sys/group/union-tree", summary="获取组织用户组合并树", response_model=Result[list[UnionTreeNode]])
@CheckPermission("sys:group:tree")
def union_tree(service: GroupService = Depends(get_group_service)):
    return success(service.union_tree())


@router.post("/api/v1/sys/group/create", summary="添加用户组")
@SysLog("添加用户组")
@CheckPermission("sys:group:create")
@NoRepeat(interval=3000)
def create(
    vo: GroupVO,
    service: GroupService = Depends(get_group_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/group/modify", summary="编辑用户组")
@SysLog("编辑用户组")
@CheckPermission("sys:group:modify")
def modify(
    vo: GroupVO,
    service: GroupService = Depends(get_group_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/group/remove", summary="删除用户组")
@SysLog("删除用户组")
@CheckPermission("sys:group:remove")
def remove(param: IdsParam, service: GroupService = Depends(get_group_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/group/detail", summary="获取用户组详情", response_model=Result[GroupVO])
@CheckPermission("sys:group:detail")
def detail(id: str = Query(...), service: GroupService = Depends(get_group_service)):
    return success(service.detail(id))
