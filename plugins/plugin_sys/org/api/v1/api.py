from fastapi import APIRouter, Depends, Query
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import OrgVO, OrgPageParam, OrgTreeParam
from ...service import OrgService, get_org_service

router = APIRouter()


@router.get(
    "/api/v1/sys/org/page",
    summary="获取组织分页",
    response_model=Result[PageData[OrgVO]]
)
@CheckPermission("sys:org:page")
def page(
    param: OrgPageParam = Depends(),
    service: OrgService = Depends(get_org_service),
):
    return success(service.page(param))


@router.get(
    "/api/v1/sys/org/tree",
    summary="获取组织树",
    response_model=Result[list[dict]]
)
@CheckPermission("sys:org:tree")
def tree(
    param: OrgTreeParam = Depends(),
    service: OrgService = Depends(get_org_service),
):
    return success(service.tree(param))


@router.post(
    "/api/v1/sys/org/create",
    summary="添加组织",
    response_model=Result
)
@SysLog("添加组织")
@CheckPermission("sys:org:create")
@NoRepeat(interval=3000)
def create(
    vo: OrgVO,
    service: OrgService = Depends(get_org_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.create(vo, actor)
    return success()


@router.post(
    "/api/v1/sys/org/modify",
    summary="编辑组织",
    response_model=Result
)
@SysLog("编辑组织")
@CheckPermission("sys:org:modify")
def modify(
    vo: OrgVO,
    service: OrgService = Depends(get_org_service),
    actor: ActorContext = Depends(get_current_actor),
):
    service.modify(vo, actor)
    return success()


@router.post(
    "/api/v1/sys/org/remove",
    summary="删除组织",
    response_model=Result
)
@SysLog("删除组织")
@CheckPermission("sys:org:remove")
def remove(
    param: IdsParam,
    service: OrgService = Depends(get_org_service),
):
    service.remove(param.ids)
    return success()


@router.get(
    "/api/v1/sys/org/detail",
    summary="获取组织详情",
    response_model=Result[OrgVO]
)
@CheckPermission("sys:org:detail")
def detail(
    id: str = Query(...),
    service: OrgService = Depends(get_org_service),
):
    data = service.detail(id)
    return success(data if data else None)
