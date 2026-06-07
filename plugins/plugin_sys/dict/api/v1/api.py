from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission, NoRepeat
from core.log import SysLog
from ...params import DictVO, DictPageParam, DictListParam, DictTreeParam
from ...service import DictService

router = APIRouter()


@router.get("/api/v1/sys/dict/page", summary="获取字典分页")
@HeiCheckPermission("sys:dict:page")
async def page(request: Request, param: DictPageParam = Depends(), db: Session = Depends(get_db)):
    service = DictService(db)
    return success(service.page(param))


@router.get("/api/v1/sys/dict/list", summary="获取字典列表")
@HeiCheckPermission("sys:dict:list")
async def dict_list(request: Request, param: DictListParam = Depends(), db: Session = Depends(get_db)):
    service = DictService(db)
    return success(service.list(param))


@router.get("/api/v1/sys/dict/tree", summary="获取字典树")
@HeiCheckPermission("sys:dict:tree")
async def dict_tree(request: Request, param: DictTreeParam = Depends(), db: Session = Depends(get_db)):
    service = DictService(db)
    return success(service.tree(param))


@router.post("/api/v1/sys/dict/create", summary="添加字典")
@SysLog("添加字典")
@HeiCheckPermission("sys:dict:create")
@NoRepeat(interval=3000)
async def create(request: Request, vo: DictVO, db: Session = Depends(get_db)):
    service = DictService(db)
    await service.create(vo, request)
    return success()


@router.post("/api/v1/sys/dict/modify", summary="编辑字典")
@SysLog("编辑字典")
@HeiCheckPermission("sys:dict:modify")
async def modify(request: Request, vo: DictVO, db: Session = Depends(get_db)):
    service = DictService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/dict/remove", summary="删除字典")
@SysLog("删除字典")
@HeiCheckPermission("sys:dict:remove")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    service = DictService(db)
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/dict/detail", summary="获取字典详情")
@HeiCheckPermission("sys:dict:detail")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = DictService(db)
    data = service.detail(id)
    return success(data if data else None)


@router.get("/api/v1/sys/dict/get-label", summary="根据字典类型和值获取标签")
async def get_label(request: Request, type_code: str = Query(...), value: str = Query(...), db: Session = Depends(get_db)):
    service = DictService(db)
    data = service.get_dict_label(type_code, value)
    return success(data)


@router.get("/api/v1/sys/dict/get-children", summary="根据字典类型获取子字典列表")
async def get_children(request: Request, type_code: str = Query(...), db: Session = Depends(get_db)):
    service = DictService(db)
    return success(service.get_dict_children(type_code))
