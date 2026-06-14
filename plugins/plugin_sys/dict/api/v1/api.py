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
async def page(param: DictPageParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(await service.page(param))


@router.get("/api/v1/sys/dict/list", summary="获取字典列表", response_model=Result[list[DictVO]])
@CheckPermission("sys:dict:list")
async def dict_list(param: DictListParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(await service.list(param))


@router.get("/api/v1/sys/dict/tree", summary="获取字典树", response_model=Result[list[DictTreeVO]])
async def dict_tree(param: DictTreeParam = Depends(), service: DictService = Depends(get_dict_service)):
    return success(await service.tree(param))


@router.post("/api/v1/sys/dict/create", summary="添加字典")
@SysLog("添加字典")
@CheckPermission("sys:dict:create")
@NoRepeat(interval=3000)
async def create(
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    await service.create(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/modify", summary="编辑字典")
@SysLog("编辑字典")
@CheckPermission("sys:dict:modify")
async def modify(
    vo: DictVO,
    actor: ActorContext = Depends(get_current_actor),
    service: DictService = Depends(get_dict_service),
):
    await service.modify(vo, actor)
    return success()


@router.post("/api/v1/sys/dict/remove", summary="删除字典")
@SysLog("删除字典")
@CheckPermission("sys:dict:remove")
async def remove(param: IdsParam, service: DictService = Depends(get_dict_service)):
    await service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/dict/detail", summary="获取字典详情", response_model=Result[DictVO])
@CheckPermission("sys:dict:detail")
async def detail(id: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(await service.detail(id))


@router.get("/api/v1/sys/dict/get-label", summary="根据字典类型和值获取标签")
@CheckPermission("sys:dict:get-label")
async def get_label(type_code: str = Query(...), value: str = Query(...), service: DictService = Depends(get_dict_service)):
    data = await service.get_dict_label(type_code, value)
    return success(data)


@router.get("/api/v1/sys/dict/get-children", summary="根据字典类型获取子字典列表", response_model=Result[list[DictVO]])
@CheckPermission("sys:dict:get-children")
async def get_children(type_code: str = Query(...), service: DictService = Depends(get_dict_service)):
    return success(await service.get_dict_children(type_code))
