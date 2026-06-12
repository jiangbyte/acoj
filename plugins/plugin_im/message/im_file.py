"""IM file upload and URL resolution.

Mirrors hei-gin plugins/plugin-im/message/im_file.go.
"""

from __future__ import annotations

import hashlib
import json
import os
from datetime import datetime
from typing import Optional

from fastapi import UploadFile

from core.db import SessionLocal
from core.utils import generate_id
from plugins.plugin_im.model.im_file import ImFile
from plugins.plugin_im.message.params import UploadFileResult
from plugins.plugin_im.message.im_file_repository import ImFileRepository
from core.exception import BusinessException

import logging
logger = logging.getLogger(__name__)


# Allowed file extensions
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


def _format_file_size(bytes_size: int) -> tuple[int, str]:
    if bytes_size < 1024:
        return 0, f"{bytes_size} B"
    kb = bytes_size // 1024
    if kb < 1024:
        return kb, f"{kb} KB"
    mb = kb / 1024
    return kb, f"{mb:.1f} MB"


async def upload_file(
    file: UploadFile,
    sender_id: str,
    sender_type: str,
    engine_type: str = "LOCAL",
    bucket: str = "DEFAULT",
    conversation_id: str = "",
    msg_type: str = "",
) -> UploadFileResult:
    """Upload a file for IM and store its metadata."""
    if not file.filename:
        raise BusinessException("文件名为空", 400)

    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise BusinessException(f"不支持的文件类型: {ext}", 400)

    if not msg_type:
        msg_type = "FILE"
    if _is_image_ext(ext):
        msg_type = "IMAGE"

    # Read file content
    content = await file.read()
    file_size = len(content)

    # Compute checksum
    checksum = hashlib.sha256(content).hexdigest()

    # Store locally (basic implementation — extend with storage engine integration)
    file_key = generate_id() + ext
    storage_path = f"uploads/im/{file_key}"

    # Ensure uploads directory exists
    os.makedirs(os.path.dirname(storage_path), exist_ok=True)
    with open(storage_path, "wb") as f:
        f.write(content)

    file_size_kb, size_info = _format_file_size(file_size)
    thumbnail = file_key if _is_image_ext(ext) else ""

    now = datetime.now()
    record = ImFile(
        id=generate_id(),
        engine=engine_type,
        bucket=bucket,
        file_key=file_key,
        name=file.filename,
        suffix=ext,
        size_kb=file_size_kb,
        size_info=size_info,
        storage_path=storage_path,
        download_path="",
        thumbnail=thumbnail,
        checksum=checksum,
        checksum_algo="sha256",
        conversation_id=conversation_id,
        sender_id=sender_id,
        sender_type=sender_type,
        msg_type=msg_type,
        created_at=now,
    )

    db = SessionLocal()
    try:
        ImFileRepository(db).insert(record)
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()

    return UploadFileResult(
        url=f"/{storage_path}",
        file_key=file_key,
        bucket=bucket,
        engine=engine_type,
        original_name=file.filename,
        file_size=file_size,
        file_type=ext,
    )


def resolve_file_url(content: str, extra: str) -> str:
    """Construct a full HTTP URL from message content and extra for IMAGE/FILE types."""
    if content.startswith("http"):
        return content
    if not content:
        return ""

    engine = "LOCAL"
    bucket = "DEFAULT"
    if extra:
        try:
            meta = json.loads(extra)
            if "engine" in meta:
                engine = meta["engine"]
            if "bucket" in meta:
                bucket = meta["bucket"]
        except (json.JSONDecodeError, TypeError):
            pass

    # Resolve URL from storage
    try:
        from core.storage.factory import get_url
        return get_url(engine, bucket, content)
    except (ImportError, Exception):
        return f"/uploads/im/{content}"
