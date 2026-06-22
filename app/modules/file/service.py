from datetime import UTC, datetime
from pathlib import PurePosixPath

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config.settings import settings
from app.core.response.pagination import PageData, PageQuery, build_page
from app.core.schema.base import to_schema, to_schema_list
from app.modules.file.repository import FileRepository
from app.modules.file.schema import FileRecordCreate, FileUploadRequest, SysFileSchema
from app.platform.db.transaction import transactional
from app.platform.storage.manager import get_storage


class FileService:
    """文件服务，负责对象存储写入与文件元数据落库的一致性编排。"""

    def __init__(self, db: AsyncSession) -> None:
        self.db = db
        self.repo = FileRepository(db)
        self.storage = get_storage()

    def build_object_name(self, filename: str) -> str:
        """构造对象存储路径，内部保留按日期分片的紧凑命名格式。"""
        safe_name = PurePosixPath(filename).name
        timestamp = datetime.now(UTC).strftime("%Y%m%d")
        return f"uploads/{timestamp}/{safe_name}"

    async def upload(self, payload: FileUploadRequest) -> SysFileSchema:
        """上传文件并创建元数据记录，参数通过对象统一承载。"""
        object_name = self.build_object_name(payload.filename)
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
                    bucket=settings.storage.bucket if settings.storage.provider == "s3" else None,
                    content_type=payload.content_type,
                    size=len(payload.content),
                    url=url,
                )
            )
            return to_schema(SysFileSchema, entity)

    async def delete(self, object_name: str) -> None:
        """删除对象存储文件和文件元数据，删除顺序由服务层显式协调。"""
        entity = await self.repo.get_by_object_name(object_name)
        self.storage.delete_object(object_name)
        if entity:
            await self.repo.delete(entity)

    async def get_url(self, object_name: str) -> str:
        """优先返回已落库的稳定 URL，不存在时退化为存储层实时构造。"""
        entity = await self.repo.get_by_object_name(object_name)
        if entity:
            return str(entity.url)
        return str(self.storage.get_object_url(object_name))

    def get_presigned_url(self, object_name: str) -> str:
        """获取对象的签名访问地址。"""
        return str(self.storage.get_presigned_url(object_name))

    async def list_files(self, pagination: PageQuery) -> PageData[SysFileSchema]:
        """分页列出文件元数据记录。"""
        items = await self.repo.list_files(pagination.offset, pagination.size)
        return build_page(pagination, len(items), to_schema_list(SysFileSchema, items))
