import logging
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query, Request, UploadFile, File, Form
from sqlalchemy.orm import Session
from core.result import success
from core.pojo import IdsParam
from core.db import get_db
from core.auth.decorator import HeiCheckPermission
from core.auth.permission import HeiPermissionTool
from ...params import FileVO, FilePageParam, FileIdParam
from ...service import FileService

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post("/api/v1/sys/file/upload", summary="上传文件")
async def upload(
    request: Request,
    file: UploadFile = File(...),
    engine: Optional[str] = Form(default=None),
    db: Session = Depends(get_db),
):
    logger.info(f"Upload request: file={file.filename}, engine={engine}, content_type={request.headers.get('content-type')}")
    if not await HeiPermissionTool.hasPermissionAnd("sys:file:upload", request=request):
        raise HTTPException(status_code=403, detail="缺少权限: sys:file:upload")
    service = FileService(db)
    result = await service.upload(file, request, engine=engine)
    return success(result)
upload._hei_permission = "sys:file:upload"


@router.get("/api/v1/sys/file/download", summary="下载文件")
@HeiCheckPermission("sys:file:download")
async def download(
    request: Request,
    id: str = Query(...),
    db: Session = Depends(get_db),
):
    service = FileService(db)
    return await service.download(FileIdParam(id=id))


@router.get("/api/v1/sys/file/page", summary="获取文件分页")
@HeiCheckPermission("sys:file:page")
async def page(
    request: Request,
    param: FilePageParam = Depends(),
    db: Session = Depends(get_db),
):
    service = FileService(db)
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
    await service.remove_absolute(param)
    return success()
