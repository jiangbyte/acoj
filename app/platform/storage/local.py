from pathlib import Path
from urllib.parse import quote

from app.core.config.settings import settings


class LocalStorage:
    def __init__(self, root: str = "./storage") -> None:
        self.root = Path(root)
        self.root.mkdir(parents=True, exist_ok=True)

    def upload_bytes(self, object_name: str, content: bytes, content_type: str = "application/octet-stream") -> str:
        _ = content_type
        target = self.root / object_name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
        return self.get_object_url(object_name)

    def delete_object(self, object_name: str) -> None:
        target = self.root / object_name
        if target.exists():
            target.unlink()

    def get_object_url(self, object_name: str) -> str:
        if settings.storage.base_url:
            return f"{settings.storage.base_url.rstrip('/')}/{quote(object_name)}"
        return str((self.root / object_name).resolve())

    def get_presigned_url(self, object_name: str) -> str:
        return self.get_object_url(object_name)
