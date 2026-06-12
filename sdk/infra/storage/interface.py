"""
Storage interfaces — mirrors hei-gin's ``sdk/storage/interface.go`` and ``chunk.go``.
"""

from abc import ABC, abstractmethod
from typing import Optional, BinaryIO


class FileStorageInterface(ABC):
    """Abstract storage engine.  Mirrors hei-gin's ``Engine`` interface."""

    @abstractmethod
    def get_default_bucket(self) -> str:
        ...

    @abstractmethod
    def store(self, bucket: str, file_key: str, data: bytes) -> str:
        ...

    @abstractmethod
    def store_stream(self, bucket: str, file_key: str, stream: BinaryIO) -> str:
        ...

    @abstractmethod
    def get_bytes(self, bucket: str, file_key: str) -> bytes:
        ...

    @abstractmethod
    def get_url(self, bucket: str, file_key: str) -> str:
        ...

    @abstractmethod
    def get_auth_url(self, bucket: str, file_key: str, timeout_ms: int = 60000) -> str:
        ...

    @abstractmethod
    def delete(self, bucket: str, file_key: str) -> None:
        ...

    @abstractmethod
    def exists(self, bucket: str, file_key: str) -> bool:
        ...

    @abstractmethod
    def copy(self, src_bucket: str, src_key: str, dst_bucket: str, dst_key: str) -> None:
        ...


class ChunkInfo:
    """Describes a single file chunk in a chunked upload session.

    Mirrors hei-gin's ``storage.ChunkInfo``.
    """
    def __init__(
        self,
        upload_id: str,
        chunk_index: int,
        total_chunks: int,
        checksum: str = "",
        data: Optional[BinaryIO] = None,
    ):
        self.upload_id = upload_id
        self.chunk_index = chunk_index
        self.total_chunks = total_chunks
        self.checksum = checksum
        self.data = data


class ChunkedUploader(ABC):
    """Optional interface for storage backends supporting chunked/resumable uploads.

    Mirrors hei-gin's ``storage.ChunkedUploader``.
    """

    @abstractmethod
    async def init_chunk_upload(self, bucket: str, file_key: str,
                                 total_chunks: int) -> str:
        """Initialise a chunked upload session. Returns a unique upload_id."""
        ...

    @abstractmethod
    async def upload_chunk(self, bucket: str, file_key: str,
                            upload_id: str, chunk: ChunkInfo) -> None:
        """Store a single chunk. Chunks can arrive in any order."""
        ...

    @abstractmethod
    async def complete_chunk_upload(self, bucket: str, file_key: str,
                                     upload_id: str) -> str:
        """Finalise the upload, merge all chunks, return the file path."""
        ...

    @abstractmethod
    async def abort_chunk_upload(self, bucket: str, file_key: str,
                                  upload_id: str) -> None:
        """Cancel the upload and clean up temporary data."""
        ...
