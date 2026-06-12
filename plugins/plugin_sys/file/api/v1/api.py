"""SysFile API routes — mirrors hei-gin plugins/plugin-sys/file/api/v1/api.go."""

from __future__ import annotations

from fastapi import APIRouter, Depends, Request, UploadFile, File, Form, Query as QueryParam
from fastapi.responses import FileResponse, RedirectResponse

from sdk.auth import HeiAuthTool, HeiClientAuthTool
from sdk.auth.decorator import HeiCheckLogin, HeiClientCheckLogin, HeiCheckPermission
from sdk.web.result import success, failure
from sdk.shared.types import IdsParam
from plugins.plugin_sys.file.params import (
    FilePageParam, FileUploadResult, FileVO, ChunkUploadInitParam,
    ChunkUploadPartParam, ChunkCompleteParam, ChunkAbortParam,
)
from plugins.plugin_sys.file.service import FileService, get_file_service

router = APIRouter(prefix="/api/v1/sys/file", tags=["Sys File"])


@router.post("/upload", summary="上传文件")
@HeiCheckPermission("sys:file:upload")
@HeiCheckLogin
async def upload_handler(request: Request, file: UploadFile = File(...),
                          engine: str = Form("LOCAL"), bucket: str = Form("DEFAULT"),
                          service: FileService = Depends(get_file_service)):
    uid = await HeiAuthTool.getLoginIdDefaultNull(request)
    try:
        result = await service.upload(file, uid or "", engine, bucket)
        return success(result.__dict__)
    except Exception as e:
        return failure(str(e), 400)


@router.get("/page", summary="文件分页")
@HeiCheckPermission("sys:file:page")
async def page_handler(request: Request, current: int = QueryParam(1), size: int = QueryParam(10),
                        keyword: str = QueryParam(""), engine: str = QueryParam(""),
                        bucket: str = QueryParam(""), service: FileService = Depends(get_file_service)):
    param = FilePageParam(current=current, size=size, keyword=keyword,
                          engine=engine, bucket=bucket)
    return success(service.page(param))


@router.get("/detail", summary="文件详情")
@HeiCheckPermission("sys:file:detail")
async def detail_handler(request: Request, id: str = QueryParam(...), service: FileService = Depends(get_file_service)):
    data = service.detail(id)
    return success(data if data else None)


@router.get("/download", summary="下载文件")
@HeiCheckPermission("sys:file:download")
async def download_handler(request: Request, id: str = QueryParam(...), service: FileService = Depends(get_file_service)):
    try:
        entity = service.detail(id)
        if not entity:
            return failure("文件不存在", 404)
        if entity.get("download_path"):
            return RedirectResponse(url=entity["download_path"])
        if entity.get("storage_path"):
            return FileResponse(entity["storage_path"], filename=entity.get("name") or "download")
        return failure("文件路径为空", 404)
    except Exception as e:
        return failure(str(e), 400)


@router.post("/remove", summary="删除文件记录（保留存储文件）")
@HeiCheckPermission("sys:file:remove")
@HeiCheckLogin
async def remove_handler(request: Request, p: IdsParam, service: FileService = Depends(get_file_service)):
    service.remove(p.ids)
    return success()


@router.post("/remove-absolute", summary="删除文件（含存储文件）")
@HeiCheckPermission("sys:file:remove-absolute")
@HeiCheckLogin
async def remove_absolute_handler(request: Request, p: IdsParam, service: FileService = Depends(get_file_service)):
    service.remove_absolute(p.ids)
    return success()


@router.post("/upload/init", summary="初始化分片上传")
@HeiCheckPermission("sys:file:upload")
@HeiCheckLogin
async def chunk_init_handler(request: Request, p: ChunkUploadInitParam, service: FileService = Depends(get_file_service)):
    result = service.init_chunk_upload(p)
    return success(result)


@router.post("/upload/chunk", summary="上传分片")
@HeiCheckPermission("sys:file:upload")
@HeiCheckLogin
async def chunk_upload_handler(
    request: Request,
    file: UploadFile = File(...),
    upload_id: str = Form(...),
    chunk_index: int = Form(...),
    total_chunks: int = Form(...),
    checksum: str = Form(""),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    file_key: str = Form(""),
    service: FileService = Depends(get_file_service),
):
    param = ChunkUploadPartParam(
        upload_id=upload_id,
        chunk_index=chunk_index,
        total_chunks=total_chunks,
        checksum=checksum,
        engine=engine,
        bucket=bucket,
        file_key=file_key,
    )
    await service.upload_chunk(file, param)
    return success()


@router.post("/upload/complete", summary="完成分片上传")
@HeiCheckPermission("sys:file:upload")
@HeiCheckLogin
async def chunk_complete_handler(request: Request, p: ChunkCompleteParam, service: FileService = Depends(get_file_service)):
    result = service.complete_chunk_upload(p)
    return success(result.__dict__)


@router.post("/upload/abort", summary="中止分片上传")
@HeiCheckPermission("sys:file:upload")
@HeiCheckLogin
async def chunk_abort_handler(request: Request, p: ChunkAbortParam, service: FileService = Depends(get_file_service)):
    service.abort_chunk_upload(p)
    return success()


# ── Client file upload (consumer) ───────────────────────────────────
client_router = APIRouter(prefix="/api/v1/c/file", tags=["Client File"])


@client_router.post("/upload", summary="客户端上传文件")
@HeiClientCheckLogin
async def client_upload_handler(request: Request, file: UploadFile = File(...),
                                  engine: str = Form("LOCAL"), bucket: str = Form("DEFAULT"),
                                  service: FileService = Depends(get_file_service)):
    uid = await HeiClientAuthTool.getLoginIdDefaultNull(request)
    try:
        result = await service.upload(file, uid or "", engine, bucket)
        return success(result.__dict__)
    except Exception as e:
        return failure(str(e), 400)
