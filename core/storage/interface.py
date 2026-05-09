from abc import ABC, abstractmethod
from typing import Optional, BinaryIO


class FileStorageInterface(ABC):

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
