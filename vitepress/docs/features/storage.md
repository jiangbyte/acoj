# 文件存储

Hei Gin 提供统一的文件存储抽象层，支持多种存储后端的无缝切换。开发者通过接口操作文件，无需关心底层存储实现。

## 存储接口

**文件**：`core/storage/interface.go`

```go
// FileStorage 定义文件存储操作的标准接口
type FileStorage interface {
    // GetDefaultBucket 返回默认存储桶/命名空间名称
    GetDefaultBucket() string

    // Store 将字节数组存储到指定位置
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    // data: 文件字节数据
    // returns: 对象标识（如 bucket/fileKey）, 错误
    Store(bucket, fileKey string, data []byte) (string, error)

    // StoreStream 从 Reader 读取数据并存储到指定位置
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    // reader: 文件内容读取流
    // returns: 对象标识（如 bucket/fileKey）, 错误
    StoreStream(bucket, fileKey string, reader io.Reader) (string, error)

    // GetBytes 获取文件内容
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    // returns: 文件字节数据, 错误
    GetBytes(bucket, fileKey string) ([]byte, error)

    // GetURL 获取文件访问 URL
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    // returns: 可直接访问的 URL
    GetURL(bucket, fileKey string) string

    // GetAuthURL 获取带鉴权的文件访问 URL
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    // timeoutMs: URL 有效期（毫秒）
    // returns: 带时效性的访问 URL, 错误
    GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error)

    // Delete 删除文件
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    Delete(bucket, fileKey string) error

    // Exists 检查文件是否存在
    // bucket: 存储桶名称
    // fileKey: 文件键值（路径）
    // returns: true=存在, false=不存在
    Exists(bucket, fileKey string) (bool, error)

    // Copy 复制文件
    // srcBucket: 源存储桶
    // srcKey: 源文件键值
    // dstBucket: 目标存储桶
    // dstKey: 目标文件键值
    Copy(srcBucket, srcKey, dstBucket, dstKey string) error
}
```

## 存储后端实现

### 1. 本地存储（Local）

**文件**：`core/storage/local.go`

将文件存储在服务器的本地文件系统中。

**特点**：
- 无需额外服务，开箱即用
- 适合开发和单机部署
- 默认 Bucket 为 `"local"`

**创建方式**：

```go
localStorage := storage.NewLocalStorage("./data/uploads")
```

所有文件将存储在指定的 `uploadFolder` 目录下，以 `{uploadFolder}/{bucket}/{fileKey}` 的路径结构组织。

**注意**：`GetURL` 返回的是本地文件系统路径，而非 HTTP URL。`GetAuthURL` 与 `GetURL` 返回相同路径（本地文件系统无鉴权机制）。`Delete` 会忽略文件不存在的错误以实现幂等删除。

### 2. MinIO 存储

**文件**：`core/storage/minio.go`

使用 MinIO 对象存储服务（基于 `minio-go/v7`）。

**特点**：
- 高性能对象存储
- 兼容 S3 API
- 支持私有和公开 Bucket
- 支持自动创建 Bucket
- 适合自建存储服务

**创建方式**：

```go
minioStorage := storage.NewMinioStorage(
    "play.min.io",      // endpoint
    "accessKey",         // access key
    "secretKey",         // secret key
    "my-bucket",         // default bucket
    false,               // secure (true for HTTPS)
    "us-east-1",         // region
)
```

**使用前提**：需要部署 MinIO 服务并提供访问密钥。

**行为细节**：
- `Store` / `StoreStream` 会自动检测并创建不存在的 Bucket
- `Store` / `StoreStream` 返回 `"{bucket}/{fileKey}"` 格式的对象标识
- `GetURL` 返回 `"{endpoint}/{bucket}/{fileKey}"` 格式的端点 URL
- `GetAuthURL` 使用 `PresignedGetObject` 生成临时的预签名 URL
- `Delete` 会忽略 `minio.ErrorResponse` 类型的服务端错误以实现幂等删除
- `Exists` 通过 `StatObject` 检测对象是否存在

### 3. AWS S3 存储

**文件**：`core/storage/s3.go`

使用 Amazon S3 对象存储服务（基于 `aws-sdk-go-v2`）。

**特点**：
- 高可用、高持久性
- 全球 CDN 加速
- 按量付费
- 适合生产环境

**创建方式**：

```go
s3Storage := storage.NewS3Storage(
    "s3.amazonaws.com",  // endpoint
    "accessKey",         // access key
    "secretKey",         // secret key
    "my-bucket",         // default bucket
    "us-east-1",         // region
    false,               // pathStyle (false for virtual hosted style)
)
```

**使用前提**：需要 AWS 账号并创建 S3 Bucket。

**行为细节**：
- `Store` / `StoreStream` 会自动检测并创建不存在的 Bucket
- `Store` / `StoreStream` 返回 `"{bucket}/{fileKey}"` 格式的对象标识
- `GetURL` 返回 `"{endpoint}/{bucket}/{fileKey}"` 格式的端点 URL
- `GetAuthURL` 使用 `s3.NewPresignClient` 和 `PresignGetObject` 生成预签名 URL
- `Exists` 通过 `HeadObject` 检测对象是否存在
- `Copy` 通过 `CopyObject` 实现服务端拷贝

## 使用方式

存储包仅定义了接口和具体实现，不包含全局管理器。使用时应直接实例化具体实现，或通过依赖注入（DI）传递 `FileStorage` 接口实例。

### 直接实例化

```go
import "hei-gin/core/storage"

// 选择一种后端并创建实例
localStorage := storage.NewLocalStorage("./data/uploads")
minioStorage := storage.NewMinioStorage("play.min.io", "ak", "sk", "bucket", false, "us-east-1")
s3Storage := storage.NewS3Storage("s3.amazonaws.com", "ak", "sk", "bucket", "us-east-1", false)
```

### 依赖注入

在应用启动时，将存储实例注册到依赖注入容器中，业务代码通过接口类型使用：

```go
type FileService struct {
    storage storage.FileStorage
}

func NewFileService(s storage.FileStorage) *FileService {
    return &FileService{storage: s}
}

func (s *FileService) Upload(fileKey string, data []byte) (string, error) {
    return s.storage.Store(s.storage.GetDefaultBucket(), fileKey, data)
}
```

## 使用示例

### 文件上传

```go
import (
    "io"
    "hei-gin/core/storage"
)

// 使用本地存储
store := storage.NewLocalStorage("./data/uploads")

func uploadFile(bucket, fileKey string, data []byte) (string, error) {
    return store.Store(bucket, fileKey, data)
}

// 或使用流式上传
func uploadStream(bucket, fileKey string, reader io.Reader) (string, error) {
    return store.StoreStream(bucket, fileKey, reader)
}
```

### 文件删除

```go
func deleteFile(bucket, fileKey string) error {
    return store.Delete(bucket, fileKey)
}
```

### 文件访问

```go
// 获取公开访问 URL
url := store.GetURL("public", "images/avatar.jpg")

// 获取带鉴权的临时访问 URL（有效期为 30 秒 = 30000 毫秒）
authURL, err := store.GetAuthURL("private", "documents/report.pdf", 30000)
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

实现自定义存储后端只需要实现 `FileStorage` 接口的所有方法：

```go
type MyCustomStorage struct{}

func (s *MyCustomStorage) GetDefaultBucket() string {
    return "my-bucket"
}

func (s *MyCustomStorage) Store(bucket, fileKey string, data []byte) (string, error) {
    // 实现自定义存储逻辑
    return bucket + "/" + fileKey, nil
}

// 实现其他接口方法...
```
