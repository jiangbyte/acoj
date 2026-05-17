# 配置文件说明

Hei Gin 使用 `config.yaml` 作为核心配置文件，位于项目根目录。以下为完整配置项说明。

## 配置文件示例

```yaml
app:
  name: hei-gin
  version: 1.0.0
  debug: true
  host: 127.0.0.1
  port: 18885
  upload_max_size: 52428800
  import_max_file_size_mb: 10
  timeout_keep_alive: 15

db:
  host: localhost
  port: 3306
  user: root
  password: "123456"
  database: hei_data
  pool_size: 20
  max_overflow: 10
  pool_recycle: 3600
  pool_pre_ping: true
  pool_timeout: 30
  connect_timeout: 10
  echo: false

redis:
  host: localhost
  port: 6379
  password: "123456"
  database: 1
  max_connections: 200
  socket_connect_timeout: 10
  socket_timeout: 30
  retry_on_timeout: true
  health_check_interval: 30

jwt:
  secret_key: hei-fastapi-jwt-secret-key-2026-please-change-in-production
  algorithm: HS256
  expire_seconds: 2592000
  token_name: Authorization

sm2:
  private_key: "SM2私钥"
  public_key: "SM2公钥"

cors:
  allow_origins: ["*"]
  allow_methods: ["*"]
  allow_headers: ["*"]
  allow_credentials: false

snowflake:
  instance: 1
```

## 配置项说明

### app 应用配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `name` | string | `hei-gin` | 应用名称，用于日志和标识 |
| `version` | string | `1.0.0` | 应用版本号 |
| `debug` | bool | `true` | 是否开启调试模式，影响日志输出详细程度 |
| `host` | string | `127.0.0.1` | HTTP 服务监听地址 |
| `port` | int | `18885` | HTTP 服务监听端口 |
| `upload_max_size` | int64 | `52428800` | 上传文件最大字节数（默认 50MB） |
| `import_max_file_size_mb` | int | `10` | 导入文件最大大小，单位 MB |
| `timeout_keep_alive` | int | `15` | HTTP Keep-Alive 超时时间（秒） |

### db 数据库配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `host` | string | `localhost` | MySQL 主机地址 |
| `port` | int | `3306` | MySQL 端口 |
| `user` | string | `root` | 数据库用户名 |
| `password` | string | `123456` | 数据库密码 |
| `database` | string | `hei_data` | 数据库名称 |
| `pool_size` | int | `20` | 连接池初始大小 |
| `max_overflow` | int | `10` | 连接池最大溢出连接数（超过 pool_size 后可额外创建的最大连接数）|
| `pool_recycle` | int | `3600` | 连接回收时间（秒），超过该时间的连接将被回收重建 |
| `pool_pre_ping` | bool | `true` | 是否在获取连接前执行 Ping 检查连接有效性 |
| `pool_timeout` | int | `30` | 从连接池获取连接的超时时间（秒） |
| `connect_timeout` | int | `10` | 建立新数据库连接的超时时间（秒） |
| `echo` | bool | `false` | 是否打印 SQL 语句到日志（调试用） |

`pool_pre_ping: true` 可以避免使用已断开的连接，建议生产环境开启。`pool_recycle` 建议小于数据库端的连接超时时间。

### redis 缓存配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `host` | string | `localhost` | Redis 主机地址 |
| `port` | int | `6379` | Redis 端口 |
| `password` | string | `123456` | Redis 密码（无密码留空）|
| `database` | int | `1` | Redis 数据库编号 |
| `max_connections` | int | `200` | 连接池最大连接数 |
| `socket_connect_timeout` | int | `10` | 连接 Redis 超时时间（秒） |
| `socket_timeout` | int | `30` | Redis 读写超时时间（秒） |
| `retry_on_timeout` | bool | `true` | 超时后是否自动重试 |
| `health_check_interval` | int | `30` | 健康检查间隔时间（秒） |

### jwt 认证配置

JWT 配置为**单一共享配置**，管理端（B 端）和客户端（C 端）使用相同的配置字段，通过不同的 Redis Key 前缀区分登录类型。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `secret_key` | string | - | JWT 签名密钥，生产环境务必修改为足够长的随机字符串 |
| `algorithm` | string | `HS256` | JWT 签名算法，目前支持 HMAC 系算法 |
| `expire_seconds` | int | `2592000` | Token 有效期（秒），默认 30 天 |
| `token_name` | string | `Authorization` | HTTP 请求头名称，用于传递 Token |

> 安全提示：生产环境中 `secret_key` 应使用足够长的随机字符串，切勿使用默认值。

### sm2 国密配置

| 配置项 | 类型 | 说明 |
|--------|------|------|
| `private_key` | string | SM2 私钥，用于后端解密密码 |
| `public_key` | string | SM2 公钥，用于前端加密密码 |

SM2 是中国国家密码管理局公布的椭圆曲线公钥密码算法。Hei Gin 使用 SM2 对登录密码进行加密传输，防止密码在传输过程中被窃取。

前后端交互流程：
1. 前端调用 `GET /api/v1/public/b/sm2-public-key` 获取公钥
2. 前端使用公钥对密码进行 SM2 加密（C1C2C3 模式）
3. 后端使用私钥解密，获取原始密码
4. 后端使用 bcrypt 对密码进行哈希存储

### cors 跨域配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `allow_origins` | []string | `["*"]` | 允许的请求来源列表，`["*"]` 表示允许所有来源 |
| `allow_methods` | []string | `["*"]` | 允许的 HTTP 方法列表 |
| `allow_headers` | []string | `["*"]` | 允许的请求头列表 |
| `allow_credentials` | bool | `false` | 是否允许跨域请求携带凭证（Cookie、Authorization 头等）|

开发环境可使用 `["*"]` 允许所有来源。生产环境应明确指定 `allow_origins` 为具体的前端域名列表。当 `allow_origins` 设置为具体域名时，`allow_credentials` 可设为 `true`。

### snowflake 雪花 ID 配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `instance` | int64 | `1` | 实例编号，分布式部署时每个节点应使用不同的编号 |

Snowflake ID 生成 64 位唯一 ID，结构为：`1位符号位 + 41位时间戳 + 10位工作节点ID + 12位序列号`。在多节点部署时，确保每个节点的 `instance` 值不同以避免 ID 冲突。
