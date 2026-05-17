# 文件存储

Hei FastAPI 提供统一的文件存储抽象层，支持多种存储后端的无缝切换。开发者通过接口操作文件，无需关心底层存储实现。

## 存储接口

**文件**：`core/storage/interface.py`

```python
from abc import ABC, abstractmethod
from typing import Optional, BinaryIO

class FileStorageInterface(ABC):
    """文件存储操作的标准接口"""
    
    @abstractmethod
    def get_default_bucket(self) -> str:
        """返回默认存储桶/命名空间名称"""
        ...
    
    @abstractmethod
    def store(self, bucket: str, file_key: str, data: bytes) -> str:
        """将字节数组存储到指定位置"""
        ...
    
    @abstractmethod
    def store_stream(self, bucket: str, file_key: str, stream: BinaryIO) -> str:
        """将二进制流存储到指定位置"""
        ...
    
    @abstractmethod
    def get_bytes(self, bucket: str, file_key: str) -> bytes:
        """获取文件内容"""
        ...
    
    @abstractmethod
    def get_url(self, bucket: str, file_key: str) -> str:
        """获取文件访问 URL"""
        ...
    
    @abstractmethod
    def get_auth_url(self, bucket: str, file_key: str, timeout_ms: int = 60000) -> str:
        """获取带鉴权的文件访问 URL（预签名 URL）"""
        ...
    
    @abstractmethod
    def delete(self, bucket: str, file_key: str) -> None:
        """删除文件"""
        ...
    
    @abstractmethod
    def exists(self, bucket: str, file_key: str) -> bool:
        """检查文件是否存在"""
        ...
    
    @abstractmethod
    def copy(self, src_bucket: str, src_key: str, dst_bucket: str, dst_key: str) -> None:
        """复制文件"""
        ...
```

## 存储后端实现

### 1. 本地存储（LocalStorage）

**文件**：`core/storage/local_storage.py`

将文件存储在服务器的本地文件系统中。

**特点**：
- 无需额外服务，开箱即用
- 适合开发和单机部署

**创建方式**：

```python
from core.storage.local_storage import LocalStorage

storage = LocalStorage(upload_dir="./uploads")
```

所有文件将存储在指定的 `upload_dir` 目录下，以 `{upload_dir}/{bucket}/{file_key}` 的路径结构组织。

**行为细节**：
- `store` 自动创建不存在的目录
- `get_url` 返回本地文件系统路径
- `get_auth_url` 与 `get_url` 返回相同路径（本地文件系统无鉴权机制）
- `delete` 忽略文件不存在的错误（幂等删除）

### 2. MinIO 存储（MinioStorage）

**文件**：`core/storage/minio_storage.py`

使用 MinIO 对象存储服务。

**特点**：
- 高性能对象存储
- 兼容 S3 API
- 支持私有和公开 Bucket
- 适合自建存储服务

**创建方式**：

```python
from core.storage.minio_storage import MinioStorage

storage = MinioStorage(
    endpoint="play.min.io",    # MinIO 服务地址
    access_key="minioadmin",   # 访问密钥
    secret_key="minioadmin",   # 秘密密钥
    default_bucket="hei-data", # 默认存储桶
    secure=False,              # 是否使用 HTTPS
    region="us-east-1",        # 区域
)
```

**行为细节**：
- `store` 会自动检测并创建不存在的 Bucket
- `get_url` 返回 `{endpoint}/{bucket}/{file_key}` 格式的端点 URL
- `get_auth_url` 使用预签名 URL（Presigned URL）
- `delete` 忽略不存在的文件错误（幂等删除）

### 3. S3 存储（S3Storage）

**文件**：`core/storage/s3_storage.py`

使用 Amazon S3 兼容对象存储服务。

**特点**：
- 高可用、高持久性
- 全球 CDN 加速
- 适合生产环境

**创建方式**：

```python
from core.storage.s3_storage import S3Storage

storage = S3Storage(
    endpoint="s3.amazonaws.com",  # S3 端点
    access_key="your-access-key",
    secret_key="your-secret-key",
    default_bucket="hei-data",    # 默认存储桶
    region="us-east-1",           # 区域
    path_style=True,              # 使用 path-style 寻址（默认 True）
)
```

**行为细节**：
- `store` 会自动检测并创建不存在的 Bucket
- `get_url` 返回 API 风格的 URL（path-style 或 virtual-hosted）
- `get_auth_url` 使用 AWS Signature V4 生成预签名 URL
- `copy` 通过 `copy_object` 实现服务端拷贝

## 使用方式

### 文件上传（在 File 模块中）

```python
from core.storage.local_storage import LocalStorage

# 创建存储实例
storage = LocalStorage(upload_dir="./uploads")

# 上传文件
file_key = storage.store("hei-data", "images/avatar.jpg", file_bytes)

# 获取文件 URL
url = storage.get_url("hei-data", "images/avatar.jpg")

# 下载文件
data = storage.get_bytes("hei-data", "images/avatar.jpg")

# 删除文件
storage.delete("hei-data", "images/avatar.jpg")
```

### 文件管理 API

```http
POST /api/v1/sys/file/upload              # 上传文件
GET  /api/v1/sys/file/download             # 下载文件
GET  /api/v1/sys/file/page                 # 文件列表分页查询
GET  /api/v1/sys/file/detail               # 文件详情
POST /api/v1/sys/file/remove               # 删除文件记录
POST /api/v1/sys/file/remove-absolute      # 物理删除文件
```

## 选择建议

| 场景 | 推荐方案 |
|------|---------|
| 本地开发 | LocalStorage |
| 自建服务器 | MinioStorage |
| 云原生部署 | S3Storage |
| 高并发生产环境 | S3Storage 或 MinioStorage |
| 内网环境 | MinioStorage |

## 扩展自定义存储

实现自定义存储后端只需要继承 `FileStorageInterface` 并实现所有抽象方法：

```python
from core.storage.interface import FileStorageInterface

class MyCustomStorage(FileStorageInterface):
    def get_default_bucket(self) -> str:
        return "my-bucket"
    
    def store(self, bucket: str, file_key: str, data: bytes) -> str:
        # 实现自定义存储逻辑
        return f"{bucket}/{file_key}"
    
    # 实现其他接口方法...
```
