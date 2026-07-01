from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql.elements import ColumnElement

from app.modules.sys.file.model import SysFile
from app.modules.sys.file.schema import FileRecordCreate


class FileRepository:
    """文件仓储，负责对象存储元数据的持久化和查询。"""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payload: FileRecordCreate) -> SysFile:
        """创建文件元数据记录，避免仓储继续接收长平铺参数列表。"""
        entity = SysFile(**payload.model_dump())
        self.db.add(entity)
        await self.db.flush()
        return entity

    async def get_by_object_name(self, object_name: str) -> SysFile | None:
        """按对象名查询文件元数据，用于 URL 查询和删除逻辑。"""
        stmt = select(SysFile).where(SysFile.object_name == object_name)
        return (await self.db.execute(stmt)).scalar_one_or_none()

    async def get_by_id(self, file_id: str) -> SysFile | None:
        """按文件 ID 获取文件元数据。"""
        return await self.db.get(SysFile, file_id)

    async def list_files(
        self,
        offset: int,
        limit: int,
        data_scope_filter: ColumnElement[bool] | None = None,
    ) -> tuple[list[SysFile], int]:
        """分页查询文件元数据列表。"""
        stmt = select(SysFile)
        count_stmt = select(func.count(SysFile.id))
        if data_scope_filter is not None:
            stmt = stmt.where(data_scope_filter)
            count_stmt = count_stmt.where(data_scope_filter)
        stmt = stmt.order_by(SysFile.created_at.desc()).offset(offset).limit(limit)
        items = list((await self.db.execute(stmt)).scalars().all())
        total = (await self.db.execute(count_stmt)).scalar_one()
        return items, total

    async def delete(self, entity: SysFile) -> None:
        """删除文件元数据记录，物理文件删除由服务层协调。"""
        await self.db.delete(entity)
