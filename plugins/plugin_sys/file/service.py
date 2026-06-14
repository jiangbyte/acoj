"""Sys file service."""

from __future__ import annotations

import hashlib
import os
import shutil
import tempfile
import time
from datetime import datetime
from pathlib import Path
from typing import Optional

from fastapi import Depends, UploadFile
from fastapi.responses import FileResponse, RedirectResponse, Response
from starlette.concurrency import run_in_threadpool
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.infra.storage import ChunkInfo, ChunkedUploader, get_storage, get_url
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import SysFile
from .params import (
    ChunkAbortParam,
    ChunkCompleteParam,
    ChunkUploadInitParam,
    ChunkUploadPartParam,
    FilePageParam,
    FileVO,
    FileUploadResult,
)
from .repository import FileRepository

ALLOWED_EXTENSIONS = {
    ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".ico",
    ".bmp", ".tiff",
    ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".pdf",
    ".txt", ".csv", ".md",
    ".zip", ".rar", ".7z", ".tar", ".gz",
    ".mp3", ".wav", ".ogg",
    ".mp4", ".avi", ".mkv", ".mov", ".webm",
    ".json", ".xml", ".yaml", ".yml",
}

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".ico", ".bmp", ".tiff"}
CHUNK_SIZE = 5 << 20
READ_BLOCK_SIZE = 1 << 20
CHUNK_TEMP_PREFIX = "chunk_"


def _is_image_ext(ext: str) -> bool:
    return ext.lower() in IMAGE_EXTENSIONS


def _format_size(bytes_size: int) -> tuple[int, str]:
    if bytes_size < 1024:
        return 0, f"{bytes_size} B"
    kb = bytes_size // 1024
    if kb < 1024:
        return kb, f"{kb} KB"
    mb = kb / 1024
    return kb, f"{mb:.1f} MB"


def _validate_upload_meta(file_name: str, file_size: int, max_upload_size: int) -> str:
    if not file_name:
        raise BusinessException("文件名为空", 400)
    ext = os.path.splitext(file_name)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise BusinessException(f"不支持的文件类型: {ext}", 400)
    if file_size > max_upload_size:
        raise BusinessException(f"文件大小超过限制 ({max_upload_size // (1 << 20)} MB)", 400)
    return ext


async def _spool_upload(file: UploadFile, max_size: int) -> tuple[str, int, str]:
    checksum = hashlib.sha256()
    size = 0
    fd, path = tempfile.mkstemp(prefix="hei_upload_")
    os.close(fd)
    try:
        with open(path, "wb") as target:
            while True:
                chunk = await _read_upload_chunk(file)
                if not chunk:
                    break
                size += len(chunk)
                if size > max_size:
                    raise BusinessException(f"文件大小超过限制 ({max_size // (1 << 20)} MB)", 400)
                checksum.update(chunk)
                await run_in_threadpool(target.write, chunk)
        return path, size, checksum.hexdigest()
    except Exception:
        try:
            os.remove(path)
        except OSError:
            pass
        raise


async def _read_upload_chunk(file: UploadFile) -> bytes:
    try:
        return await file.read(READ_BLOCK_SIZE)
    except TypeError:
        return await file.read()


def _merge_chunks_to_temp(tmp_dir: str, total_chunks: int) -> tuple[str, list[str]]:
    chunk_paths = [os.path.join(tmp_dir, f"chunk_{index:06d}") for index in range(total_chunks)]
    missing = [path for path in chunk_paths if not os.path.isfile(path)]
    if missing:
        raise BusinessException("分片不完整", 400)

    fd, merged_path = tempfile.mkstemp(prefix="hei_chunk_merge_")
    os.close(fd)
    try:
        with open(merged_path, "wb") as target:
            for chunk_path in chunk_paths:
                with open(chunk_path, "rb") as source:
                    shutil.copyfileobj(source, target, READ_BLOCK_SIZE)
        return merged_path, chunk_paths
    except Exception:
        try:
            os.remove(merged_path)
        except OSError:
            pass
        raise


def _chunk_tmp_dir(upload_id: str) -> str:
    return os.path.join(tempfile.gettempdir(), f"{CHUNK_TEMP_PREFIX}{upload_id}")


def _cleanup_chunk_upload(upload_id: str) -> None:
    if not upload_id:
        return
    shutil.rmtree(_chunk_tmp_dir(upload_id), ignore_errors=True)


def cleanup_stale_chunk_uploads(max_age_seconds: int = 86400) -> int:
    tmp_root = Path(tempfile.gettempdir())
    now = time.time()
    cleaned = 0
    for path in tmp_root.glob(f"{CHUNK_TEMP_PREFIX}*"):
        if not path.is_dir():
            continue
        try:
            if now - path.stat().st_mtime < max_age_seconds:
                continue
            shutil.rmtree(path, ignore_errors=True)
            cleaned += 1
        except OSError:
            continue
    return cleaned


class FileService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, FileRepository):
            self.repository = repository_or_db
        else:
            self.repository = FileRepository(repository_or_db)
        self.db = self.repository.db

    async def upload(
        self,
        file: UploadFile,
        user_id: str,
        engine_type: str = "LOCAL",
        bucket: str = "DEFAULT",
    ) -> FileUploadResult:
        max_size = self.max_upload_size()
        ext = _validate_upload_meta(file.filename or "", 0, max_size)
        tmp_path, file_size, checksum = await _spool_upload(file, max_size)
        file_key = generate_id() + ext
        size_kb, size_info = _format_size(file_size)
        thumbnail = file_key if _is_image_ext(ext) else ""

        engine = get_storage(engine_type)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {engine_type}", 500)

        try:
            with open(tmp_path, "rb") as stream:
                storage_path = await run_in_threadpool(engine.store_stream, bucket, file_key, stream)
        finally:
            try:
                os.remove(tmp_path)
            except OSError:
                pass
        download_path = get_url(engine_type, bucket, file_key)

        now = datetime.now()
        entity = SysFile(
            id=generate_id(),
            engine=engine_type,
            bucket=bucket,
            file_key=file_key,
            name=file.filename,
            suffix=ext,
            size_kb=size_kb,
            size_info=size_info,
            obj_name=file_key,
            storage_path=storage_path,
            download_path=download_path,
            thumbnail=thumbnail,
            checksum=checksum,
            checksum_algo="sha256",
            created_at=now,
            created_by=user_id,
            updated_at=now,
            updated_by=user_id,
        )
        return FileUploadResult.from_entity(self.repository.insert(entity))

    def page(self, param: FilePageParam) -> dict:
        param.current = max(1, param.current)
        param.size = max(1, min(param.size, 100))
        return map_page_data(self.repository.page(param), FileVO.model_validate, param.current, param.size)

    def detail(self, file_id: str) -> Optional[FileVO]:
        if not file_id:
            return None
        entity = self.repository.find_by_id(file_id)
        if not entity:
            return None
        return FileVO.model_validate(entity)

    def get_download_path(self, file_id: str) -> Optional[str]:
        entity = self.repository.find_by_id(file_id)
        if not entity:
            raise BusinessException("文件不存在", 404)
        return entity.download_path or entity.storage_path

    def download_by_key(self, bucket: str, file_key: str) -> Response:
        entity = self.repository.find_by_key(bucket, file_key)
        if not entity:
            raise BusinessException("文件不存在", 404)
        if (entity.engine or "").upper() == "LOCAL" and entity.storage_path:
            return FileResponse(entity.storage_path, filename=entity.name or file_key)
        if entity.download_path:
            return RedirectResponse(entity.download_path, status_code=302)
        raise BusinessException("文件路径为空", 404)

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        self.repository.delete_by_ids(ids)

    def remove_absolute(self, ids: list[str]) -> None:
        if not ids:
            return
        files = self.repository.find_by_ids(ids)
        for file_item in files:
            if file_item.engine:
                engine = get_storage(file_item.engine)
                if engine:
                    try:
                        if file_item.file_key:
                            engine.delete(file_item.bucket or "DEFAULT", file_item.file_key)
                    except Exception:
                        pass
        self.repository.delete_by_ids(ids)

    def init_chunk_upload(self, param: ChunkUploadInitParam) -> dict:
        _validate_upload_meta(param.file_name, param.file_size, self.max_upload_size())
        if param.total_chunks <= 0:
            raise BusinessException("total_chunks 必须大于0", 400)
        expected_chunks = int((param.file_size + CHUNK_SIZE - 1) / CHUNK_SIZE)
        if param.total_chunks != expected_chunks:
            raise BusinessException("total_chunks 与文件大小不匹配", 400)
        engine = get_storage(param.engine)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

        file_key = generate_id() + os.path.splitext(param.file_name)[1]
        if isinstance(engine, ChunkedUploader):
            upload_id = engine.init_chunk_upload(param.bucket, file_key, param.total_chunks)
        else:
            upload_id = generate_id()
            _cleanup_chunk_upload(upload_id)
        return {"upload_id": upload_id, "file_key": file_key}

    async def upload_chunk(
        self,
        file: UploadFile,
        param: ChunkUploadPartParam,
    ) -> None:
        engine = get_storage(param.engine)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

        if param.total_chunks <= 0:
            raise BusinessException("total_chunks 与初始化信息不一致", 400)
        if param.chunk_index < 0 or param.chunk_index >= param.total_chunks:
            raise BusinessException("chunk_index 超出范围", 400)

        tmp_dir = _chunk_tmp_dir(param.upload_id)
        os.makedirs(tmp_dir, exist_ok=True)
        chunk_file = os.path.join(tmp_dir, f"chunk_{param.chunk_index:06d}")
        size = 0
        with open(chunk_file, "wb") as target:
            while True:
                content = await _read_upload_chunk(file)
                if not content:
                    break
                size += len(content)
                if size > CHUNK_SIZE:
                    try:
                        os.remove(chunk_file)
                    except OSError:
                        pass
                    raise BusinessException("分片大小不合法", 400)
                await run_in_threadpool(target.write, content)
        if size <= 0:
            raise BusinessException("分片大小不合法", 400)
        if isinstance(engine, ChunkedUploader):
            with open(chunk_file, "rb") as source:
                await run_in_threadpool(
                    engine.upload_chunk,
                    param.bucket,
                    param.file_key,
                    param.upload_id,
                    ChunkInfo(
                        upload_id=param.upload_id,
                        chunk_index=param.chunk_index,
                        total_chunks=param.total_chunks,
                        checksum=param.checksum,
                        size=size,
                        data=source,
                    ),
                )
            try:
                os.remove(chunk_file)
            except OSError:
                pass
            try:
                Path(tmp_dir).rmdir()
            except OSError:
                pass
            return

        return

    def complete_chunk_upload(self, param: ChunkCompleteParam) -> FileUploadResult:
        engine = get_storage(param.engine)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {param.engine}", 500)
        if not param.file_key:
            raise BusinessException("file_key 不能为空", 400)

        ext = os.path.splitext(param.name)[1].lower()
        size_kb, size_info = _format_size(param.file_size)

        if isinstance(engine, ChunkedUploader):
            storage_path = engine.complete_chunk_upload(param.bucket, param.file_key, param.upload_id)
        else:
            tmp_dir = _chunk_tmp_dir(param.upload_id)
            if not os.path.exists(tmp_dir):
                raise BusinessException("分片上传会话不存在", 400)

            expected_chunks = int((param.file_size + CHUNK_SIZE - 1) / CHUNK_SIZE)
            merged_path, _ = _merge_chunks_to_temp(tmp_dir, expected_chunks)
            try:
                with open(merged_path, "rb") as merged:
                    storage_path = engine.store_stream(param.bucket, param.file_key, merged)
            finally:
                try:
                    os.remove(merged_path)
                except OSError:
                    pass
                shutil.rmtree(tmp_dir, ignore_errors=True)

        download_path = get_url(param.engine, param.bucket, param.file_key)
        thumbnail = download_path if _is_image_ext(ext) else ""
        now = datetime.now()
        entity = SysFile(
            id=generate_id(),
            engine=param.engine,
            bucket=param.bucket,
            file_key=param.file_key,
            name=param.name,
            suffix=ext,
            size_kb=size_kb,
            size_info=size_info,
            obj_name=param.file_key,
            storage_path=storage_path,
            download_path=download_path,
            thumbnail=thumbnail,
            created_at=now,
            updated_at=now,
        )
        return FileUploadResult.from_entity(self.repository.insert(entity))

    def abort_chunk_upload(self, param: ChunkAbortParam) -> None:
        engine = get_storage(param.engine)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

        if isinstance(engine, ChunkedUploader):
            engine.abort_chunk_upload(param.bucket, param.file_key, param.upload_id)
            return

        _cleanup_chunk_upload(param.upload_id)

    def max_upload_size(self) -> int:
        from sdk.config.settings import settings

        return settings.app.upload_max_size or 50 << 20


def get_file_service(db: Session = Depends(get_db)) -> FileService:
    return FileService(FileRepository(db))
