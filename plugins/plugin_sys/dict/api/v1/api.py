from fastapi import APIRouter, Depends, Query
from sdk.shared.di import ActorContext, get_current_actor
from sdk.web.result import Result, PageData, success
from sdk.shared.types import IdsParam
from sdk.auth.decorator import CheckPermission, NoRepeat
from sdk.log import SysLog
from ...params import DictVO, DictPageParam, DictListParam, DictTreeParam, DictTreeVO
from ...service import DictService, get_dict_service

router = APIRouter()


@router.get("/api/v1/sys/dict/page", summary="获取字典分页", response_model=Result[PageData[DictVO]])
@CheckPermission("sys:dict:page")
def page(param: DictPageParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(service.page(param))


@router.get("/api/v1/sys/dict/list", summary="获取字典列表", response_model=Result[list[DictVO]])
@CheckPermission("sys:dict:list")
def dict_list(param: DictListParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(service.list(param))


@router.get("/api/v1/sys/dict/tree", summary="获取字典树", response_model=Result[list[DictTreeVO]])
def dict_tree(param: DictTreeParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(service.tree(param))


@router.post("/api/v1/sys/dict/create", summary="添加字典")
@SysLog("添加字典")
@CheckPermission("sys:dict:create")
@NoRepeat(interval=3000)
def create(
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
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/remove", summary="删除字典")
@SysLog("删除字典")
@CheckPermission("sys:dict:remove")
def remove(param: IdsParam, service: DictService = Depends(get_dict_service)):
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/dict/detail", summary="获取字典详情", response_model=Result[DictVO])
@CheckPermission("sys:dict:detail")
def detail(id: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(service.detail(id))


@router.get("/api/v1/sys/dict/get-label", summary="根据字典类型和值获取标签")
@CheckPermission("sys:dict:get-label")
def get_label(type_code: str = Query(...), value: str = Query(...), service: DictService = Depends(get_dict_service)):
    data = service.get_dict_label(type_code, value)
    return success(data)


@router.get("/api/v1/sys/dict/get-children", summary="根据字典类型获取子字典列表", response_model=Result[list[DictVO]])
@CheckPermission("sys:dict:get-children")
def get_children(type_code: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(service.get_dict_children(type_code))
