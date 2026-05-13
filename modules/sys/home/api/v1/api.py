from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session
from core.db import get_db
from core.result import Result, success
from ...service import HomeService
from ...params import AddQuickActionParam, RemoveQuickActionParam, SortQuickActionParam

router = APIRouter()


@router.get("/api/v1/sys/home", summary="获取首页数据", response_model=Result)
async def get_home(request: Request, db: Session = Depends(get_db)):
    service = HomeService(db)
    result = await service.home(request)
    return success(result.model_dump())


@router.post("/api/v1/sys/home/quick-actions/add", summary="添加快捷方式", response_model=Result)
async def add_quick_action(param: AddQuickActionParam, request: Request, db: Session = Depends(get_db)):
    service = HomeService(db)
    await service.add_quick_action(param, request)
    return success()


@router.post("/api/v1/sys/home/quick-actions/remove", summary="移除快捷方式", response_model=Result)
async def remove_quick_action(param: RemoveQuickActionParam, request: Request, db: Session = Depends(get_db)):
    service = HomeService(db)
    await service.remove_quick_action(param, request)
    return success()


@router.post("/api/v1/sys/home/quick-actions/sort", summary="排序快捷方式", response_model=Result)
async def sort_quick_actions(param: SortQuickActionParam, request: Request, db: Session = Depends(get_db)):
    service = HomeService(db)
    await service.sort_quick_actions(param, request)
    return success()
