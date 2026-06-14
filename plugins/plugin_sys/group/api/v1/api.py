from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import GroupVO, GroupPageParam, GroupTreeParam, GroupTreeVO, UnionTreeNode
from ...service import GroupService, get_group_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get("/api/v1/sys/group/page", summary="获取用户组分页", response_model=Result[PageData[GroupVO]])
@require_permissions("sys:group:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: GroupPageParam = Depends(), service: GroupService = Depends(get_group_service)):
    return success(await service.page(param))


@router.get("/api/v1/sys/group/tree", summary="获取用户组树", response_model=Result[list[GroupTreeVO]])
@require_permissions("sys:group:tree", realm=BUSINESS_REALM_ID)
async def group_tree(request: Request, param: GroupTreeParam = Depends(), service: GroupService = Depends(get_group_service)):
    return success(await service.tree(param))


@router.get("/api/v1/sys/group/union-tree", summary="获取组织用户组合并树", response_model=Result[list[UnionTreeNode]])
@require_permissions("sys:group:tree", realm=BUSINESS_REALM_ID)
async def union_tree(request: Request, service: GroupService = Depends(get_group_service)):
    return success(await service.union_tree())


@router.post("/api/v1/sys/group/create", summary="添加用户组")
@SysLog("添加用户组")
@require_permissions("sys:group:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def create(
    request: Request,
    vo: GroupVO,
    service: GroupService = Depends(get_group_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/group/modify", summary="编辑用户组")
@SysLog("编辑用户组")
@require_permissions("sys:group:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: GroupVO,
    service: GroupService = Depends(get_group_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/group/remove", summary="删除用户组")
@SysLog("删除用户组")
@require_permissions("sys:group:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: GroupService = Depends(get_group_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/group/detail", summary="获取用户组详情", response_model=Result[GroupVO])
@require_permissions("sys:group:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: GroupService = Depends(get_group_service)):
    return success(await service.detail(id))
