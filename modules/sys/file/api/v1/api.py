from typing import Optional
from fastapi import APIRouter, Depends, Query, Request, UploadFile, File, Form
from sqlalchemy.orm import Session
from core.result import success
from core.pojo import IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission
from ...params import FileVO, FilePageParam, FileIdParam
from ...service import FileService

router = APIRouter()


@router.post("/api/v1/sys/file/upload", summary="上传文件")
@HeiCheckPermission("sys:file:upload")
async def upload(
    request: Request,
    file: UploadFile = File(...),
    engine: Optional[str] = Form(default=None),
    db: Session = Depends(get_db),
):
    service = FileService(db)
    result = await service.upload(file, request, engine=engine)
    return success(result)


@router.get("/api/v1/sys/file/download", summary="下载文件")
@HeiCheckPermission("sys:file:download")
async def download(
    request: Request,
    id: str = Query(...),
    db: Session = Depends(get_db),
):
    service = FileService(db)
    return service.download(FileIdParam(id=id))


@router.get("/api/v1/sys/file/page", summary="获取文件分页")
@HeiCheckPermission("sys:file:page")
async def page(
    request: Request,
    current: int = Query(default=1),
    size: int = Query(default=10),
    engine: Optional[str] = Query(default=None),
    keyword: Optional[str] = Query(default=None),
    date_range_start: Optional[str] = Query(default=None),
    date_range_end: Optional[str] = Query(default=None),
    db: Session = Depends(get_db),
):
    service = FileService(db)
    param = FilePageParam(
        current=current, size=size,
        engine=engine, keyword=keyword,
        date_range_start=date_range_start,
        date_range_end=date_range_end,
    )
    return success(service.page(param))


@router.get("/api/v1/sys/file/detail", summary="获取文件详情")
@HeiCheckPermission("sys:file:detail")
async def detail(
    request: Request,
    id: str = Query(...),
    db: Session = Depends(get_db),
):
    service = FileService(db)
    data = service.detail(FileIdParam(id=id))
    return success(data)


@router.post("/api/v1/sys/file/remove", summary="删除文件（软删除）")
@HeiCheckPermission("sys:file:remove")
async def remove(
    request: Request,
    param: IdsParam,
    db: Session = Depends(get_db),
):
    service = FileService(db)
    service.remove(param)
    return success()


@router.post("/api/v1/sys/file/remove-absolute", summary="删除文件（物理删除）")
@HeiCheckPermission("sys:file:remove")
async def remove_absolute(
    request: Request,
    param: IdsParam,
    db: Session = Depends(get_db),
):
    service = FileService(db)
    service.remove_absolute(param)
    return success()
