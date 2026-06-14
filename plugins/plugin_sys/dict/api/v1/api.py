from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import Result, PageData, success
from sdk.shared.types import IdsParam
from sdk.web.middleware import RateLimiter
from sdk.log import SysLog
from ...params import DictVO, DictPageParam, DictListParam, DictTreeParam, DictTreeVO
from ...service import DictService, get_dict_service
from micosauth.decorators import require_permissions
from sdk.auth import BUSINESS_REALM_ID

router = APIRouter()


@router.get("/api/v1/sys/dict/page", summary="获取字典分页", response_model=Result[PageData[DictVO]])
@require_permissions("sys:dict:page", realm=BUSINESS_REALM_ID)
async def page(request: Request, param: DictPageParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(await service.page(param))


@router.get("/api/v1/sys/dict/list", summary="获取字典列表", response_model=Result[list[DictVO]])
@require_permissions("sys:dict:list", realm=BUSINESS_REALM_ID)
async def dict_list(request: Request, param: DictListParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(await service.list(param))


@router.get("/api/v1/sys/dict/tree", summary="获取字典树", response_model=Result[list[DictTreeVO]])
async def dict_tree(request: Request, param: DictTreeParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(await service.tree(param))


@router.post("/api/v1/sys/dict/create", summary="添加字典")
@SysLog("添加字典")
@require_permissions("sys:dict:create", realm=BUSINESS_REALM_ID)
@RateLimiter("norepeat", window=3, max_requests=1)
async def create(
    request: Request,
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/modify", summary="编辑字典")
@SysLog("编辑字典")
@require_permissions("sys:dict:modify", realm=BUSINESS_REALM_ID)
async def modify(
    request: Request,
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/remove", summary="删除字典")
@SysLog("删除字典")
@require_permissions("sys:dict:remove", realm=BUSINESS_REALM_ID)
async def remove(request: Request, param: IdsParam, service: DictService = Depends(get_dict_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/dict/detail", summary="获取字典详情", response_model=Result[DictVO])
@require_permissions("sys:dict:detail", realm=BUSINESS_REALM_ID)
async def detail(request: Request, id: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(await service.detail(id))


@router.get("/api/v1/sys/dict/get-label", summary="根据字典类型和值获取标签")
@require_permissions("sys:dict:get-label", realm=BUSINESS_REALM_ID)
async def get_label(request: Request, type_code: str = Query(...), value: str = Query(...), service: DictService = Depends(get_dict_service)):
    data = await service.get_dict_label(type_code, value)
    return success(data)


@router.get("/api/v1/sys/dict/get-children", summary="根据字典类型获取子字典列表", response_model=Result[list[DictVO]])
@require_permissions("sys:dict:get-children", realm=BUSINESS_REALM_ID)
async def get_children(request: Request, type_code: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(await service.get_dict_children(type_code))
