# 文件存储

Hei Gin 提供统一的文件存储抽象层，支持多种存储后端的无缝切换。开发者通过 `Engine` 接口操作文件，无需关心底层存储实现。

## 存储接口

**文件**：`sdk/storage/interface.go`

```go
// Engine 定义文件存储操作的标准接口
type Engine interface {
    // Store 将字节数组存储到指定位置
    Store(bucket, fileKey string, data []byte) (string, error)

    // StoreStream 从 Reader 读取数据并存储到指定位置
    StoreStream(bucket, fileKey string, reader io.Reader) (string, error)

    // GetBytes 获取文件内容
    GetBytes(bucket, fileKey string) ([]byte, error)

    // Delete 删除文件（幂等）
    Delete(bucket, fileKey string) error

    // Exists 检查文件是否存在
    Exists(bucket, fileKey string) (bool, error)

    // Copy 复制文件
    Copy(srcBucket, srcKey, dstBucket, dstKey string) error
}
```

## 存储后端实现

### 1. 本地存储（Local）

**文件**：`sdk/storage/local.go`

将文件存储在服务器的本地文件系统中。

**特点**：
- 无需额外服务，开箱即用
- 适合开发和单机部署

**创建方式**：

```go
localStorage := storage.NewLocal("./data/uploads", "")
```

`baseURL` 可配置为静态文件服务器的 HTTP 基础地址（如 `http://localhost:18886/`），为空时返回相对路径 `/uploads/{bucket}/{fileKey}`。

### 2. MinIO 存储

**文件**：`sdk/storage/minio.go`

使用 MinIO 对象存储服务（基于 `minio-go/v7`）。

**创建方式**：

```go
minioStorage := storage.NewMinio(
    "play.min.io",      // endpoint
    "accessKey",         // access key
    "secretKey",         // secret key
    "my-bucket",         // default bucket
    false,               // secure (true for HTTPS)
    "us-east-1",         // region
    "",                  // baseURL（可选，用于自定义域名）
)
```

### 3. AWS S3 存储

**文件**：`sdk/storage/s3.go`

使用 Amazon S3 对象存储服务（基于 `aws-sdk-go-v2`）。

**创建方式**：

```go
s3Storage := storage.NewS3(
    "s3.amazonaws.com",  // endpoint
    "accessKey",         // access key
    "secretKey",         // secret key
    "my-bucket",         // default bucket
    "us-east-1",         // region
    false,               // pathStyle (false for virtual hosted style)
    "",                  // baseURL（可选，用于自定义域名）
)
```

## 工厂模式

SDK 提供工厂函数 `storage.GetStorage()` 根据配置自动创建对应的存储后端实例：

**文件**：`sdk/storage/factory.go`

```go
import "hei-gin/sdk/storage"

// 根据存储类型名称获取 Engine 实例
// 支持 "LOCAL"、"MINIO"、"S3"，未知类型回退到 LOCAL
eng := storage.GetStorage("LOCAL")

// 从 config.yaml 的 storage 配置段加载配置
cfg := storage.GetConfig()
```

### 配置方式

```yaml
# config.yaml
storage:
  default: LOCAL                     # 默认存储类型
  default_base_url: ""               # 文件访问基础 URL（可选）
  local:
    upload_folder: ./uploads
    base_url: ""
  minio:
    endpoint: localhost:9000
    access_key: minioadmin
    secret_key: minioadmin
    bucket: hei-files
    secure: false
    region: us-east-1
    base_url: ""
  s3:
    endpoint: https://s3.amazonaws.com
    access_key: YOUR_S3_ACCESS_KEY
    secret_key: YOUR_S3_SECRET_KEY
    bucket: hei-files
    region: ap-northeast-1
    path_style: false
    base_url: ""
```

## 使用示例

### 直接实例化

```go
import "hei-gin/sdk/storage"

// 选择一种后端并创建实例
store := storage.NewLocal("./uploads", "")
store.Store("my-bucket", "images/avatar.jpg", data)

// 或通过工厂函数
store := storage.GetStorage("LOCAL")
```

### 配置驱动（推荐）

生产环境推荐通过 `config.yaml` 配置存储后端，业务代码通过工厂函数获取：

```go
import "hei-gin/sdk/storage"

store := storage.GetStorage("LOCAL")
store.Store("files", "doc/report.pdf", data)
```

## 选择建议

| 场景 | 推荐方案 |
|------|---------|
| 本地开发 | Local |
| 自建服务器 | MinIO |
| 云原生部署 | S3 |
| 高并发生产环境 | S3 或 MinIO |

## 扩展自定义存储

实现自定义存储后端只需实现 `Engine` 接口的所有方法：

```go
type MyCustomStorage struct{}

func (s *MyCustomStorage) Store(bucket, fileKey string, data []byte) (string, error) {
    return bucket + "/" + fileKey, nil
}

func (s *MyCustomStorage) StoreStream(bucket, fileKey string, reader io.Reader) (string, error) {
    // implementation
}

func (s *MyCustomStorage) GetBytes(bucket, fileKey string) ([]byte, error) {
    // implementation
}

func (s *MyCustomStorage) Delete(bucket, fileKey string) error {
    // implementation
}

func (s *MyCustomStorage) Exists(bucket, fileKey string) (bool, error) {
    // implementation
}

func (s *MyCustomStorage) Copy(srcBucket, srcKey, dstBucket, dstKey string) error {
    // implementation
}
```

## 分片上传

MinIO 和 S3 后端实现了 `ChunkedUploader` 接口（`sdk/storage/chunk.go`），支持大文件分片上传：

```go
type ChunkedUploader interface {
    InitChunkUpload(bucket, fileKey string, totalChunks int) (uploadID string, err error)
    UploadChunk(bucket, fileKey, uploadID string, chunk ChunkInfo) error
    CompleteChunkUpload(bucket, fileKey, uploadID string) (filePath string, err error)
    AbortChunkUpload(bucket, fileKey, uploadID string) error
}
```

分片由 `ChunkInfo` 描述：

```go
type ChunkInfo struct {
    UploadID    string    // Upload session identifier
    ChunkIndex  int       // 0-based chunk index
    TotalChunks int       // Total number of chunks
    Checksum    string    // Optional SHA256 hex checksum
    Data        io.Reader // Chunk data
}
```

使用流程：初始化会话 → 按任意顺序上传分片 → 完成后合并。失败时可中止并清理临时数据。

> 注意：Local 后端不实现 `ChunkedUploader` 接口。
