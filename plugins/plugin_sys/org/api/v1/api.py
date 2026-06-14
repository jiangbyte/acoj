from fastapi import APIRouter, Depends, Query, Request
from sdk.web.result import Result, PageData, success
from sdk.shared.di import ActorContext, get_current_actor
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import OrgVO, OrgPageParam, OrgTreeParam
from ...params import OrgTreeVO
from ...service import OrgService, get_org_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get(
    "/api/v1/sys/org/page",
    summary="获取组织分页",
    response_model=Result[PageData[OrgVO]]
)
@require_permissions("sys:org:page", realm=BUSINESS_REALM_ID)
async def page(
    request: Request,
    param: OrgPageParam = Depends(),
    service: OrgService = Depends(get_org_service),
):
    return success(await service.page(param))


@router.get(
    "/api/v1/sys/org/tree",
    summary="获取组织树",
    response_model=Result[list[OrgTreeVO]]
)
@require_permissions("sys:org:tree", realm=BUSINESS_REALM_ID)
async def tree(
    request: Request,
    param: OrgTreeParam = Depends(),
    service: OrgService = Depends(get_org_service),
):
    return success(await service.tree(param))


@router.post(
    "/api/v1/sys/org/create",
    summary="添加组织",
    response_model=Result
)
@SysLog("添加组织")
@require_permissions("sys:org:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def create(
    request: Request,
    vo: OrgVO,
    service: OrgService = Depends(get_org_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.create(vo, actor)
    return success()


@router.post(
    "/api/v1/sys/org/modify",
    summary="编辑组织",
    response_model=Result
)
@SysLog("编辑组织")
@require_permissions("sys:org:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: OrgVO,
    service: OrgService = Depends(get_org_service),
    actor: ActorContext = Depends(get_current_actor),
):
    await service.modify(vo, actor)
    return success()


@router.post(
    "/api/v1/sys/org/remove",
    summary="删除组织",
    response_model=Result
)
@SysLog("删除组织")
@require_permissions("sys:org:remove", realm=BUSINESS_REALM_ID)
async def remove(
    request: Request,
    param: IdsParam,
    service: OrgService = Depends(get_org_service),
):
    await service.remove(param.ids)
    return success()


@router.get(
    "/api/v1/sys/org/detail",
    summary="获取组织详情",
    response_model=Result[OrgVO]
)
@require_permissions("sys:org:detail", realm=BUSINESS_REALM_ID)
async def detail(
    request: Request,
    id: str = Query(...),
    service: OrgService = Depends(get_org_service),
):
    return success(await service.detail(id))
