from fastapi import APIRouter, Depends, Query, Request
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import success
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import DictVO, DictPageParam, DictListParam, DictTreeParam
from ...service import DictService, get_dict_service

router = APIRouter()


@router.get("/api/v1/sys/dict/page", summary="获取字典分页")
@CheckPermission("sys:dict:page")
def page(request: Request, param: DictPageParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/dict/list", summary="获取字典列表")
@CheckPermission("sys:dict:list")
def dict_list(request: Request, param: DictListParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(service.list(param))


@router.get("/api/v1/sys/dict/tree", summary="获取字典树")
def dict_tree(request: Request, param: DictTreeParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(service.tree(param))


@router.post("/api/v1/sys/dict/create", summary="添加字典")
@SysLog("添加字典")
@CheckPermission("sys:dict:create")
@NoRepeat(interval=3000)
def create(
    request: Request,
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/modify", summary="编辑字典")
@SysLog("编辑字典")
@CheckPermission("sys:dict:modify")
def modify(
    request: Request,
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/remove", summary="删除字典")
@SysLog("删除字典")
@CheckPermission("sys:dict:remove")
def remove(request: Request, param: IdsParam, service: DictService = Depends(get_dict_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/dict/detail", summary="获取字典详情")
@CheckPermission("sys:dict:detail")
def detail(request: Request, id: str = Query(...), service: DictService = Depends(get_dict_service)):
    data = service.detail(id)
    return success(data if data else None)


@router.get("/api/v1/sys/dict/get-label", summary="根据字典类型和值获取标签")
@CheckPermission("sys:dict:get-label")
def get_label(request: Request, type_code: str = Query(...), value: str = Query(...), service: DictService = Depends(get_dict_service)):
    data = service.get_dict_label(type_code, value)
    return success(data)


@router.get("/api/v1/sys/dict/get-children", summary="根据字典类型获取子字典列表")
@CheckPermission("sys:dict:get-children")
def get_children(request: Request, type_code: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(service.get_dict_children(type_code))
