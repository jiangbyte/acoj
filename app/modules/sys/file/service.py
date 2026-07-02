from datetime import UTC, datetime
from pathlib import PurePosixPath
from uuid import uuid4

from fastapi.responses import FileResponse, RedirectResponse, Response
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.settings import settings
from app.core.exceptions.business import NotFoundError
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import to_schema, to_schema_list
from app.core.security.data_scope import build_data_scope_filter
from app.core.security.session import SessionPayload
from app.modules.sys.file.model import SysFile
from app.modules.sys.file.repository import FileRepository
from app.modules.sys.file.schema import FileRecordCreate, FileUploadRequest, SysFileSchema
from app.platform.db.transaction import transactional
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
        return (
            f"{category.strip('/')}/{now:%Y}/{now:%m}/{now:%d}/"
            f"{stem}-{uuid4().hex}{suffix}"
        )

    async def upload(self, payload: FileUploadRequest) -> SysFileSchema:
        """上传文件并创建元数据记录，参数通过对象统一承载。"""
        object_name = payload.object_name or self.build_object_name(payload.filename, payload.category)
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
                    bucket=settings.storage.bucket if settings.storage.provider != "local" else None,
                    content_type=payload.content_type,
                    size=len(payload.content),
                    url=resolve_file_url(object_name) or url,
                )
            )
            return self._with_resolved_url(to_schema(SysFileSchema, entity))

    async def delete(self, object_name: str) -> None:
        """删除对象存储文件和文件元数据，删除顺序由服务层显式协调。"""
        entity = await self.repo.get_by_object_name(object_name)
        self.storage.delete_object(object_name)
        if entity:
            await self.repo.delete(entity)

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
            )
        return RedirectResponse(url=self.storage.get_object_url(normalized))

    async def page(
        self,
        pagination: PageQuery,
        session: SessionPayload | None = None,
    ) -> PageData[SysFileSchema]:
        """分页列出文件元数据记录。"""
        data_scope_filter = None
        if session is not None:
            data_scope_filter = await build_data_scope_filter(
                self.db,
                session,
                "sys:file:page",
                owner_column=SysFile.created_by,
            )
        items, total = await self.repo.list_files(
            pagination.offset,
            pagination.size,
            data_scope_filter,
        )
        schemas = [self._with_resolved_url(schema) for schema in to_schema_list(SysFileSchema, items)]
        return build_page(pagination, total, schemas)

    def _with_resolved_url(self, schema: SysFileSchema) -> SysFileSchema:
        schema.url = resolve_file_url(schema.object_name) or schema.url
        return schema
