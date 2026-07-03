import re
from datetime import UTC, datetime
from pathlib import PurePosixPath
from uuid import uuid4

from fastapi.responses import FileResponse, RedirectResponse, Response
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.enums import StorageProvider
from app.core.config.settings import settings
from app.core.exceptions.business import BusinessError, NotFoundError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import IdQuery, IdsRequest, to_schema, to_schema_list
from app.core.security.data_scope import build_data_scope_filter
from app.core.security.session import SessionPayload
from app.modules.sys.file.model import SysFile
from app.modules.sys.file.repository import FileRepository
from app.modules.sys.file.schema import (
    FileAdminPageQuery,
    FileRecordCreate,
    FileUpdateRequest,
    FileUploadRequest,
    SysFileSchema,
)
from app.platform.db.transaction import transactional
from app.platform.observability.metrics import record_file_upload_rejected
from app.platform.storage.local import LocalStorage
from app.platform.storage.manager import get_storage
from app.platform.storage.url import is_external_url, normalize_object_name, resolve_file_url


class FileService:
    """文件服务，负责对象存储写入与文件元数据落库的一致性编排。"""

    def __init__(self, db: AsyncSession) -> None:
        self.db = db
        self.repo = FileRepository(db)
        self.storage = get_storage()

    def build_object_name(self, filename: str, category: str = "uploads") -> str:
        """构造对象存储路径，内部保留按日期分片的紧凑命名格式。"""
        safe_name = PurePosixPath(filename).name
        suffix = PurePosixPath(safe_name).suffix.lower()
        stem = PurePosixPath(safe_name).stem or "file"
        now = datetime.now(UTC)
        category = self._normalize_category(category)
        return (
            f"{category}/{now:%Y}/{now:%m}/{now:%d}/"
            f"{stem}-{uuid4().hex}{suffix}"
        )

    async def upload(self, payload: FileUploadRequest) -> SysFileSchema:
        """上传文件并创建元数据记录，参数通过对象统一承载。"""
        self._validate_upload(payload)
        object_name = payload.object_name or self.build_object_name(
            payload.filename,
            payload.category,
        )
        object_name = self._validate_object_name(object_name)
        url = self.storage.upload_bytes(
            object_name,
            payload.content,
            content_type=payload.content_type,
        )
        async with transactional(self.db):
            entity = await self.repo.create(
                FileRecordCreate(
                    object_name=object_name,
                    original_name=PurePosixPath(payload.filename).name,
                    storage_provider=settings.storage.provider,
                    bucket=(
                        settings.storage.bucket
                        if settings.storage.provider != StorageProvider.LOCAL
                        else None
                    ),
                    content_type=payload.content_type,
                    size=len(payload.content),
                    url=resolve_file_url(object_name) or url,
                )
            )
            return self._with_resolved_url(to_schema(SysFileSchema, entity))

    async def update(self, payload: FileUpdateRequest) -> None:
        async with transactional(self.db):
            await self.repo.update(payload)

    async def delete(self, payload: IdsRequest) -> None:
        """按文件 ID 批量删除对象存储文件和文件元数据。"""
        unique_ids = list(dict.fromkeys(payload.ids))
        entities = await self.repo.list_by_ids(unique_ids)
        if len(entities) != len(unique_ids):
            raise NotFoundError("File not found")
        async with transactional(self.db):
            for entity in entities:
                self.storage.delete_object(entity.object_name)
            await self.repo.delete_many(unique_ids)

    async def delete_by_object_name(self, object_name: str) -> None:
        """按对象存储路径删除文件和元数据，供业务表引用清理使用。"""
        normalized = normalize_object_name(object_name)
        if not normalized or is_external_url(normalized):
            raise NotFoundError("File not found")
        entity = await self.repo.get_by_object_name(normalized)
        async with transactional(self.db):
            self.storage.delete_object(normalized)
            if entity:
                await self.repo.delete(entity)

    async def detail(self, query: IdQuery) -> SysFileSchema:
        return self._with_resolved_url(
            to_schema(SysFileSchema, await self.repo.get_required(query.id))
        )

    async def get_url(self, object_name: str) -> str:
        """优先返回已落库的稳定 URL，不存在时退化为存储层实时构造。"""
        normalized = normalize_object_name(object_name)
        if not normalized:
            raise NotFoundError("File not found")
        url = resolve_file_url(normalized)
        if url:
            return url
        return str(self.storage.get_object_url(normalized))

    async def get_presigned_url(self, object_name: str) -> str:
        """获取对象的签名访问地址。"""
        normalized = normalize_object_name(object_name)
        if not normalized:
            raise NotFoundError("File not found")
        if is_external_url(normalized):
            return normalized
        return str(self.storage.get_presigned_url(normalized))

    async def response(self, object_name: str) -> Response:
        normalized = normalize_object_name(object_name)
        if not normalized:
            raise NotFoundError("File not found")
        entity = await self.repo.get_by_object_name(normalized)
        if isinstance(self.storage, LocalStorage):
            path = self.storage.get_path(normalized)
            if not path.exists() or not path.is_file():
                raise NotFoundError("File not found")
            return FileResponse(
                path,
                media_type=entity.content_type if entity else None,
                filename=entity.original_name if entity else None,
                headers={"X-Content-Type-Options": "nosniff"},
            )
        return RedirectResponse(
            url=self.storage.get_object_url(normalized),
            headers={"X-Content-Type-Options": "nosniff"},
        )

    async def page(
        self,
        query: FileAdminPageQuery | PageQuery,
        session: SessionPayload | None = None,
    ) -> PageData[SysFileSchema]:
        """分页列出文件元数据记录。"""
        page_query = (
            query
            if isinstance(query, FileAdminPageQuery)
            else FileAdminPageQuery(pagination=query)
        )
        data_scope_filter = None
        if session is not None:
            data_scope_filter = await build_data_scope_filter(
                self.db,
                session,
                "sys:file:page",
                owner_column=SysFile.created_by,
            )
        items, total = await self.repo.list_files(
            page_query,
            data_scope_filter,
        )
        schemas = [
            self._with_resolved_url(schema)
            for schema in to_schema_list(SysFileSchema, items)
        ]
        return build_page(page_query.pagination, total, schemas)

    def _with_resolved_url(self, schema: SysFileSchema) -> SysFileSchema:
        schema.url = resolve_file_url(schema.object_name) or schema.url
        return schema

    def _validate_upload(self, payload: FileUploadRequest) -> None:
        safe_name = PurePosixPath(payload.filename).name
        suffix = PurePosixPath(safe_name).suffix.lower()
        if not safe_name or safe_name in {".", ".."}:
            self._reject_upload("invalid_filename", "Invalid filename")
        if len(payload.content) > settings.storage.upload_max_bytes:
            self._reject_upload("too_large", "File is too large")
        denied_extensions = {item.lower() for item in settings.storage.upload_denied_extensions}
        if suffix and suffix in denied_extensions:
            self._reject_upload("denied_extension", "File extension is not allowed")
        allowed_extensions = {
            item.lower() for item in settings.storage.upload_allowed_extensions if item
        }
        if allowed_extensions and suffix not in allowed_extensions:
            self._reject_upload("extension_not_allowed", "File extension is not allowed")
        allowed_content_types = {
            item.lower() for item in settings.storage.upload_allowed_content_types if item
        }
        if allowed_content_types and payload.content_type.lower() not in allowed_content_types:
            self._reject_upload("content_type_not_allowed", "File content type is not allowed")
        self._normalize_category(payload.category)
        if payload.object_name:
            self._validate_object_name(payload.object_name)

    def _normalize_category(self, category: str) -> str:
        value = str(category or "uploads").strip().strip("/")
        if not value:
            value = "uploads"
        if len(value) > settings.storage.upload_category_max_length:
            self._reject_upload("invalid_category", "Upload category is too long")
        if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9/_-]*", value):
            self._reject_upload("invalid_category", "Upload category is invalid")
        if any(part in {"", ".", ".."} for part in value.split("/")):
            self._reject_upload("invalid_category", "Upload category is invalid")
        return value

    def _validate_object_name(self, object_name: str) -> str:
        normalized = normalize_object_name(object_name)
        if not normalized or is_external_url(normalized):
            self._reject_upload("invalid_object_name", "Object name is invalid")
        if any(part in {"", ".", ".."} for part in normalized.split("/")):
            self._reject_upload("invalid_object_name", "Object name is invalid")
        return normalized

    def _reject_upload(self, reason: str, message: str) -> None:
        record_file_upload_rejected(reason)
        raise BusinessError(message)
