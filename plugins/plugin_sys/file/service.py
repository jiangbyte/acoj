"""Sys file service."""

from __future__ import annotations

import hashlib
import io
import os
import shutil
import tempfile
from datetime import datetime
from typing import Optional

from fastapi import Depends, UploadFile
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.infra.storage import ChunkedUploader, get_storage, get_url
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import page_data

from .models import SysFile
from .params import (
    ChunkAbortParam,
    ChunkCompleteParam,
    ChunkUploadInitParam,
    ChunkUploadPartParam,
    FilePageParam,
    FileUploadResult,
    SysFileToFileUploadResult,
    SysFileToFileVO,
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


class FileService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, FileRepository):
            self.repository = repository_or_db
        else:
            self.repository = FileRepository(repository_or_db)
        self.db = self.repository.db

    @classmethod
    def from_db(cls, db: Session) -> "FileService":
        return cls(FileRepository(db))

    async def upload(
        self,
        file: UploadFile,
        user_id: str,
        engine_type: str = "LOCAL",
        bucket: str = "DEFAULT",
    ) -> FileUploadResult:
        content = await file.read()
        file_size = len(content)
        ext = _validate_upload_meta(file.filename or "", file_size, self.max_upload_size())
        checksum = hashlib.sha256(content).hexdigest()
        file_key = generate_id() + ext
        size_kb, size_info = _format_size(file_size)
        thumbnail = file_key if _is_image_ext(ext) else ""

        engine = get_storage(engine_type)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {engine_type}", 500)

        storage_path = engine.store_stream(bucket, file_key, io.BytesIO(content))
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
        return SysFileToFileUploadResult(self.repository.insert(entity))

    def page(self, param: FilePageParam) -> dict:
        param.current = max(1, param.current)
        param.size = max(1, min(param.size, 100))
        records, total = self.repository.page(param)
        return page_data([SysFileToFileVO(record) for record in records], total, param.current, param.size)

    def detail(self, file_id: str) -> Optional[dict]:
        if not file_id:
            return None
        entity = self.repository.find_by_id(file_id)
        if not entity:
            return None
        return SysFileToFileVO(entity).model_dump()

    def get_download_path(self, file_id: str) -> Optional[str]:
        entity = self.repository.find_by_id(file_id)
        if not entity:
            raise BusinessException("文件不存在", 404)
        return entity.download_path or entity.storage_path

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
        return {"upload_id": upload_id, "file_key": file_key}

    async def upload_chunk(
        self,
        file: UploadFile,
        param: ChunkUploadPartParam,
    ) -> None:
        engine = get_storage(param.engine)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

        content = await file.read()
        if param.total_chunks <= 0:
            raise BusinessException("total_chunks 与初始化信息不一致", 400)
        if param.chunk_index < 0 or param.chunk_index >= param.total_chunks:
            raise BusinessException("chunk_index 超出范围", 400)
        if len(content) <= 0 or len(content) > CHUNK_SIZE:
            raise BusinessException("分片大小不合法", 400)
        if isinstance(engine, ChunkedUploader):
            await engine.upload_chunk(
                param.bucket,
                param.file_key,
                param.upload_id,
                param.chunk_index,
                content,
            )
            return

        tmp_dir = os.path.join(tempfile.gettempdir(), f"chunk_{param.upload_id}")
        os.makedirs(tmp_dir, exist_ok=True)
        chunk_file = os.path.join(tmp_dir, f"chunk_{param.chunk_index:06d}")
        with open(chunk_file, "wb") as target:
            target.write(content)

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
            tmp_dir = os.path.join(tempfile.gettempdir(), f"chunk_{param.upload_id}")
            if not os.path.exists(tmp_dir):
                raise BusinessException("分片上传会话不存在", 400)

            chunks = sorted(
                [name for name in os.listdir(tmp_dir) if name.startswith("chunk_")],
                key=lambda name: int(name.split("_")[1]),
            )
            all_content = b""
            for chunk_name in chunks:
                with open(os.path.join(tmp_dir, chunk_name), "rb") as chunk_file:
                    all_content += chunk_file.read()

            storage_path = engine.store_stream(param.bucket, param.file_key, all_content)
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
        return SysFileToFileUploadResult(self.repository.insert(entity))

    def abort_chunk_upload(self, param: ChunkAbortParam) -> None:
        engine = get_storage(param.engine)
        if not engine:
            raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

        if isinstance(engine, ChunkedUploader):
            engine.abort_chunk_upload(param.bucket, param.file_key, param.upload_id)
            return

        tmp_dir = os.path.join(tempfile.gettempdir(), f"chunk_{param.upload_id}")
        shutil.rmtree(tmp_dir, ignore_errors=True)

    def max_upload_size(self) -> int:
        from sdk.config.settings import settings

        return settings.app.upload_max_size or 50 << 20


def get_file_service(db: Session = Depends(get_db)) -> FileService:
    return FileService.from_db(db)
