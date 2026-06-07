"""SysFile service — upload, chunk upload, download, page.

Mirrors hei-gin plugins/plugin-sys/file/service.go.
"""

from __future__ import annotations

import hashlib
import json
import logging
import os
import shutil
import tempfile
from datetime import datetime
from typing import Optional

from fastapi import UploadFile
from sqlalchemy import or_

from core.db import SessionLocal
from core.exception import BusinessException
from core.result import page_data
from core.utils import generate_id
from core.storage import get_storage, get_url, ChunkedUploader
from plugins.plugin_sys.file.models import SysFile
from plugins.plugin_sys.file.params import (
    FilePageParam, FileUploadResult,
    ChunkUploadInitParam, ChunkUploadPartParam,
    ChunkCompleteParam, ChunkAbortParam,
)

logger = logging.getLogger(__name__)


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


def _to_vo(entity: SysFile) -> FileUploadResult:
    return FileUploadResult(
        id=entity.id,
        engine=entity.engine,
        bucket=entity.bucket,
        file_key=entity.file_key,
        original_name=entity.name,
        file_suffix=entity.suffix or "",
        file_size_kb=entity.size_kb,
        size_info=entity.size_info or "",
        download_path=entity.download_path or "",
        thumbnail=entity.thumbnail or "",
    )


# ═════════════════════════════════════════════════════════════════════
# Upload
# ═════════════════════════════════════════════════════════════════════

async def upload(
    file: UploadFile,
    user_id: str,
    engine_type: str = "LOCAL",
    bucket: str = "DEFAULT",
) -> FileUploadResult:
    if not file.filename:
        raise BusinessException("文件名为空", 400)

    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise BusinessException(f"不支持的文件类型: {ext}", 400)

    content = await file.read()
    file_size = len(content)
    checksum = hashlib.sha256(content).hexdigest()
    file_key = generate_id() + ext
    size_kb, size_info = _format_size(file_size)
    thumbnail = file_key if _is_image_ext(ext) else ""

    eng = get_storage(engine_type)
    if not eng:
        raise BusinessException(f"不支持的存储类型: {engine_type}", 500)

    storage_path = eng.store_stream(bucket, file_key, content)
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

    db = SessionLocal()
    try:
        db.add(entity)
        db.commit()
        return _to_vo(entity)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Page
# ═════════════════════════════════════════════════════════════════════

def page(param: FilePageParam) -> dict:
    db = SessionLocal()
    try:
        q = db.query(SysFile)
        if param.keyword:
            like = f"%{param.keyword}%"
            q = q.filter(
                or_(SysFile.name.like(like), SysFile.file_key.like(like))
            )
        if param.engine:
            q = q.filter(SysFile.engine == param.engine)
        if param.bucket:
            q = q.filter(SysFile.bucket == param.bucket)

        total = q.count()
        records = q.order_by(SysFile.created_at.desc()).offset(
            (param.current - 1) * param.size
        ).limit(param.size).all()

        return page_data([_to_vo(r).__dict__ for r in records], total, param.current, param.size)
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Download
# ═════════════════════════════════════════════════════════════════════

def get_download_path(file_id: str) -> Optional[str]:
    db = SessionLocal()
    try:
        entity = db.query(SysFile).filter(SysFile.id == file_id).first()
        if not entity:
            raise BusinessException("文件不存在", 404)
        if entity.download_path:
            return entity.download_path
        if entity.storage_path:
            return entity.storage_path
        return None
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Chunk Upload
# ═════════════════════════════════════════════════════════════════════

def init_chunk_upload(param: ChunkUploadInitParam) -> dict:
    eng = get_storage(param.engine)
    if not eng:
        raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

    file_key = generate_id() + os.path.splitext(param.file_name)[1]

    if isinstance(eng, ChunkedUploader):
        upload_id = eng.init_chunk_upload(param.bucket, file_key, param.total_chunks)
    else:
        upload_id = generate_id()

    return {"upload_id": upload_id, "file_key": file_key}


async def upload_chunk(
    file: UploadFile,
    param: ChunkUploadPartParam,
) -> None:
    eng = get_storage(param.engine)
    if not eng:
        raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

    content = await file.read()

    if isinstance(eng, ChunkedUploader):
        await eng.upload_chunk(param.bucket, param.file_key, param.upload_id, param.chunk_index, content)
    else:
        tmp_dir = os.path.join(tempfile.gettempdir(), f"chunk_{param.upload_id}")
        os.makedirs(tmp_dir, exist_ok=True)
        chunk_file = os.path.join(tmp_dir, f"chunk_{param.chunk_index:06d}")
        with open(chunk_file, "wb") as f:
            f.write(content)


def complete_chunk_upload(param: ChunkCompleteParam) -> FileUploadResult:
    eng = get_storage(param.engine)
    if not eng:
        raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

    if not param.file_key:
        raise BusinessException("file_key 不能为空", 400)

    ext = os.path.splitext(param.name)[1].lower()
    size_kb, size_info = _format_size(param.file_size)

    if isinstance(eng, ChunkedUploader):
        storage_path = eng.complete_chunk_upload(param.bucket, param.file_key, param.upload_id)
    else:
        tmp_dir = os.path.join(tempfile.gettempdir(), f"chunk_{param.upload_id}")
        if not os.path.exists(tmp_dir):
            raise BusinessException("分片上传会话不存在", 400)

        chunks = sorted(
            [f for f in os.listdir(tmp_dir) if f.startswith("chunk_")],
            key=lambda x: int(x.split("_")[1]),
        )
        all_content = b""
        for chunk_name in chunks:
            chunk_path = os.path.join(tmp_dir, chunk_name)
            with open(chunk_path, "rb") as cf:
                all_content += cf.read()

        storage_path = eng.store_stream(param.bucket, param.file_key, all_content)
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

    db = SessionLocal()
    try:
        db.add(entity)
        db.commit()
        return _to_vo(entity)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def abort_chunk_upload(param: ChunkAbortParam) -> None:
    eng = get_storage(param.engine)
    if not eng:
        raise BusinessException(f"不支持的存储类型: {param.engine}", 500)

    if isinstance(eng, ChunkedUploader):
        eng.abort_chunk_upload(param.bucket, param.file_key, param.upload_id)
    else:
        tmp_dir = os.path.join(tempfile.gettempdir(), f"chunk_{param.upload_id}")
        shutil.rmtree(tmp_dir, ignore_errors=True)

# ═════════════════════════════════════════════════════════════════════
# Max upload size  —  mirrors hei-gin's maxUploadSize()
# ═════════════════════════════════════════════════════════════════════

def max_upload_size() -> int:
    """Return the configured max upload size in bytes."""
    from config.settings import settings
    return settings.app.upload_max_size or 50 << 20


# ═════════════════════════════════════════════════════════════════════
# Detail  —  mirrors hei-gin's Detail()
# ═════════════════════════════════════════════════════════════════════

def detail(file_id: str) -> Optional[FileUploadResult]:
    """Return file detail VO by ID.

    Mirrors hei-gin's Detail().
    """
    if not file_id:
        return None
    db = SessionLocal()
    try:
        entity = db.query(SysFile).filter(SysFile.id == file_id).first()
        if not entity:
            return None
        return _to_vo(entity)
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# Remove  —  mirrors hei-gin's Remove()
# ═════════════════════════════════════════════════════════════════════

def remove(ids: list[str]) -> None:
    """Remove file records by IDs (DB only, does not delete from storage).

    Mirrors hei-gin's Remove().
    """
    if not ids:
        return
    db = SessionLocal()
    try:
        db.query(SysFile).filter(SysFile.id.in_(ids)).delete(synchronize_session=False)
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


# ═════════════════════════════════════════════════════════════════════
# RemoveAbsolute  —  mirrors hei-gin's RemoveAbsolute()
# ═════════════════════════════════════════════════════════════════════

def remove_absolute(ids: list[str]) -> None:
    """Remove file records AND delete from storage engine.

    Mirrors hei-gin's RemoveAbsolute().
    """
    if not ids:
        return
    db = SessionLocal()
    try:
        files = db.query(SysFile).filter(SysFile.id.in_(ids)).all()
        for f in files:
            if f.engine:
                eng = get_storage(f.engine)
                if eng:
                    try:
                        eng.delete(f.bucket, f.file_key)
                    except Exception:
                        logger.warning("Failed to delete file from storage: %s/%s", f.bucket, f.file_key)
        db.query(SysFile).filter(SysFile.id.in_(ids)).delete(synchronize_session=False)
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()
