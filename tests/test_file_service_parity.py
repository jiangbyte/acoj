from __future__ import annotations

import io

import pytest

from plugins.plugin_sys.file.params import ChunkUploadInitParam, FilePageParam
from plugins.plugin_sys.file.service import CHUNK_SIZE, FileService
from sdk.web.exception import BusinessException


class _Repo:
    def __init__(self) -> None:
        self.db = self
        self.page_param = None

    def page(self, param):
        self.page_param = param
        return [], 0


class _UploadFile:
    def __init__(self, filename: str, data: bytes):
        self.filename = filename
        self._data = data

    async def read(self) -> bytes:
        return self._data


def _service() -> FileService:
    service = FileService.__new__(FileService)
    service.repository = _Repo()
    service.db = service.repository.db
    return service


def test_file_page_caps_page_size() -> None:
    service = _service()

    service.page(FilePageParam(current=1, size=100))

    assert service.repository.page_param.current == 1
    assert service.repository.page_param.size == 100


@pytest.mark.asyncio
async def test_upload_rejects_file_over_limit(monkeypatch) -> None:
    service = _service()
    monkeypatch.setattr(service, "max_upload_size", lambda: 4)

    with pytest.raises(BusinessException, match="文件大小超过限制"):
        await service.upload(_UploadFile("a.txt", b"12345"), "u1")


def test_init_chunk_upload_validates_total_chunks() -> None:
    service = _service()

    with pytest.raises(BusinessException, match="total_chunks 与文件大小不匹配"):
        service.init_chunk_upload(
            ChunkUploadInitParam(
                file_name="a.txt",
                file_size=CHUNK_SIZE + 1,
                total_chunks=1,
                engine="LOCAL",
                bucket="DEFAULT",
            )
        )
