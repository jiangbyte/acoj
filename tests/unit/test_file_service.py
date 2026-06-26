from sqlalchemy import select

from app.core.config.settings import settings
from app.core.datetime import format_utc_iso8601
from app.modules.file.model import SysFile
from app.modules.file.schema import FileUploadRequest
from app.modules.file.service import FileService


async def test_file_service_upload_and_url(tmp_path, db_session):
    old_provider = settings.storage.provider
    old_root = settings.storage.local_root
    settings.storage.provider = "local"
    settings.storage.local_root = str(tmp_path)
    try:
        service = FileService(db_session)
        entity = await service.upload(
            FileUploadRequest(filename="avatar.png", content=b"hello", content_type="image/png")
        )
        await db_session.commit()
        assert entity.object_name.endswith("/avatar.png")
        assert "avatar.png" in entity.url
        assert format_utc_iso8601(entity.created_at).endswith("Z")
        assert await service.get_url(entity.object_name) == entity.url
        stored = (
            await db_session.execute(select(SysFile).where(SysFile.id == entity.id))
        ).scalar_one()
        assert stored.original_name == "avatar.png"
        await service.delete(entity.object_name)
    finally:
        settings.storage.provider = old_provider
        settings.storage.local_root = old_root
