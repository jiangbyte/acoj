"""SysFile API routes — mirrors hei-gin plugins/plugin-sys/file/api/v1/api.go."""

from __future__ import annotations

from fastapi import APIRouter, Depends, File, Form, Request, UploadFile
from fastapi.responses import FileResponse, RedirectResponse
from micosauth.decorators import require_login, require_permissions

from sdk.auth import BUSINESS_REALM_ID, CONSUMER_REALM_ID, get_current_login_id
from sdk.shared.types import IdsParam
from sdk.web.result import failure, success
from plugins.plugin_sys.file.params import (
    ChunkAbortParam,
    ChunkCompleteParam,
    ChunkUploadInitParam,
    ChunkUploadPartParam,
    FilePageParam,
)
from plugins.plugin_sys.file.service import FileService, get_file_service

router = APIRouter(prefix="/api/v1/sys/file", tags=["Sys File"])


@router.post("/upload", summary="上传文件")
@require_permissions("sys:file:upload", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
async def upload_handler(
    request: Request,
    file: UploadFile = File(...),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    service: FileService = Depends(get_file_service),
):
    uid = await get_current_login_id(request)
    try:
        result = await service.upload(file, uid or "", engine, bucket)
        return success(result)
    except Exception as exc:
        return failure(str(exc), 400)


@router.get("/page", summary="文件分页")
@require_permissions("sys:file:page", realm=BUSINESS_REALM_ID)
async def page_handler(request: Request, param: FilePageParam = Depends(), service: FileService = Depends(get_file_service)):
    return success(await service.page(param))


@router.get("/detail", summary="文件详情")
@require_permissions("sys:file:detail", realm=BUSINESS_REALM_ID)
async def detail_handler(request: Request, id: str, service: FileService = Depends(get_file_service)):
    return success(await service.detail(id))


@router.get("/download", summary="下载文件")
@require_permissions("sys:file:download", realm=BUSINESS_REALM_ID)
async def download_handler(request: Request, id: str, service: FileService = Depends(get_file_service)):
    try:
        entity = await service.detail(id)
        if not entity:
            return failure("文件不存在", 404)
        if entity.get("download_path"):
            return RedirectResponse(url=entity["download_path"])
        if entity.get("storage_path"):
            return FileResponse(entity["storage_path"], filename=entity.get("name") or "download")
        return failure("文件路径为空", 404)
    except Exception as exc:
        return failure(str(exc), 400)


@router.post("/remove", summary="删除文件记录（保留存储文件）")
@require_permissions("sys:file:remove", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
async def remove_handler(request: Request, p: IdsParam, service: FileService = Depends(get_file_service)):
    await service.remove(p.ids)
    return success()


@router.post("/remove-absolute", summary="删除文件（含存储文件）")
@require_permissions("sys:file:remove-absolute", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
async def remove_absolute_handler(request: Request, p: IdsParam, service: FileService = Depends(get_file_service)):
    await service.remove_absolute(p.ids)
    return success()


@router.post("/upload/init", summary="初始化分片上传")
@require_permissions("sys:file:upload", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
def chunk_init_handler(request: Request, p: ChunkUploadInitParam, service: FileService = Depends(get_file_service)):
    result = service.init_chunk_upload(p)
    return success(result)


@router.post("/upload/chunk", summary="上传分片")
@require_permissions("sys:file:upload", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
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
@require_permissions("sys:file:upload", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
async def chunk_complete_handler(request: Request, p: ChunkCompleteParam, service: FileService = Depends(get_file_service)):
    result = await service.complete_chunk_upload(p)
    return success(result)


@router.post("/upload/abort", summary="中止分片上传")
@require_permissions("sys:file:upload", realm=BUSINESS_REALM_ID)
@require_login(realm=BUSINESS_REALM_ID)
def chunk_abort_handler(request: Request, p: ChunkAbortParam, service: FileService = Depends(get_file_service)):
    service.abort_chunk_upload(p)
    return success()


client_router = APIRouter(prefix="/api/v1/c/file", tags=["Client File"])


@client_router.post("/upload", summary="客户端上传文件")
@require_login(realm=CONSUMER_REALM_ID)
async def client_upload_handler(
    request: Request,
    file: UploadFile = File(...),
    engine: str = Form("LOCAL"),
    bucket: str = Form("DEFAULT"),
    service: FileService = Depends(get_file_service),
):
    uid = await get_current_login_id(request)
    try:
        result = await service.upload(file, uid or "", engine, bucket)
        return success(result)
    except Exception as exc:
        return failure(str(exc), 400)
