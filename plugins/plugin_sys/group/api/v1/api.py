from fastapi import APIRouter, Depends, Query, Request
from sqlalchemy.orm import Session
from core.result import success
from core.pojo import IdParam, IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission, NoRepeat
from core.log import SysLog
from ...params import GroupVO, GroupPageParam, GroupTreeParam
from ...service import GroupService

router = APIRouter()


@router.get("/api/v1/sys/group/page", summary="获取用户组分页")
@HeiCheckPermission("sys:group:page")
async def page(request: Request, param: GroupPageParam = Depends(), db: Session = Depends(get_db)):
    service = GroupService(db)
    return success(service.page(param))


@router.get("/api/v1/sys/group/tree", summary="获取用户组树")
@HeiCheckPermission("sys:group:tree")
async def group_tree(request: Request, param: GroupTreeParam = Depends(), db: Session = Depends(get_db)):
    service = GroupService(db)
    return success(service.tree(param))


@router.get("/api/v1/sys/group/union-tree", summary="获取组织用户组合并树")
@HeiCheckPermission("sys:group:tree")
async def union_tree(request: Request, db: Session = Depends(get_db)):
    service = GroupService(db)
    return success(service.union_tree())


@router.post("/api/v1/sys/group/create", summary="添加用户组")
@SysLog("添加用户组")
@HeiCheckPermission("sys:group:create")
@NoRepeat(interval=3000)
async def create(request: Request, vo: GroupVO, db: Session = Depends(get_db)):
    service = GroupService(db)
    await service.create(vo, request)
    return success()


@router.post("/api/v1/sys/group/modify", summary="编辑用户组")
@SysLog("编辑用户组")
@HeiCheckPermission("sys:group:modify")
async def modify(request: Request, vo: GroupVO, db: Session = Depends(get_db)):
    service = GroupService(db)
    await service.modify(vo, request)
    return success()


@router.post("/api/v1/sys/group/remove", summary="删除用户组")
@SysLog("删除用户组")
@HeiCheckPermission("sys:group:remove")
async def remove(request: Request, param: IdsParam, db: Session = Depends(get_db)):
    service = GroupService(db)
    service.remove(param.ids)
    return success()


@router.get("/api/v1/sys/group/detail", summary="获取用户组详情")
@HeiCheckPermission("sys:group:detail")
async def detail(request: Request, id: str = Query(...), db: Session = Depends(get_db)):
    service = GroupService(db)
    data = service.detail(id)
    return success(data if data else None)
