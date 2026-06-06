# 配置文件说明

Hei Gin 使用 `config.yaml` 作为核心配置文件，位于项目根目录。

## 完整配置示例

```yaml
app:
  name: hei-gin
  version: 1.0.0
  debug: true
  host: 127.0.0.1
  port: 18885
  upload_max_size: 52428800
  timeout_keep_alive: 15

db:
  host: localhost
  port: 3306
  user: root
  password: "YOUR_DB_PASSWORD"
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
  password: "YOUR_REDIS_PASSWORD"
  database: 1
  max_connections: 200
  socket_connect_timeout: 10
  socket_timeout: 30
  retry_on_timeout: true
  health_check_interval: 30

token:
  expire_seconds: 2592000
  token_name: Authorization

sm2:
  private_key: YOUR_SM2_PRIVATE_KEY
  public_key: "YOUR_SM2_PUBLIC_KEY"

user:
  reset_password: "123456"

cors:
  allow_origins: ["*"]
  allow_methods: ["*"]
  allow_headers: ["*"]
  allow_credentials: false

snowflake:
  instance: 1

storage:
  default: LOCAL
  default_base_url: ""
  local:
    upload_folder: ./uploads
    base_url: ""
  # minio:
  #   endpoint: localhost:9000
  #   access_key: YOUR_MINIO_ACCESS_KEY
  #   secret_key: YOUR_MINIO_SECRET_KEY
  #   bucket: hei-files
  #   secure: false
  #   region: us-east-1
  #   base_url: ""
  # s3:
  #   endpoint: https://s3.amazonaws.com
  #   access_key: YOUR_S3_ACCESS_KEY
  #   secret_key: YOUR_S3_SECRET_KEY
  #   bucket: hei-files
  #   region: ap-northeast-1
  #   path_style: false
  #   base_url: ""

ws:
  read_buffer_size: 1024
  write_buffer_size: 1024
  heartbeat_interval: 15
  instance_ttl: 60
  stale_clean_interval: 5
  rate_limit_window: 10
  rate_limit_max: 30
  dedup_ttl: 30
  poll_timeout: 2
  pong_timeout: 60
  write_timeout: 10
  online_broadcast_interval: 60
```

## 配置项说明

### app 应用配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `name` | string | `hei-gin` | 应用名称 |
| `version` | string | `1.0.0` | 应用版本号 |
| `debug` | bool | `true` | 调试模式 |
| `host` | string | `127.0.0.1` | 监听地址 |
| `port` | int | `18885` | 监听端口 |
| `upload_max_size` | int64 | `52428800` | 上传文件最大字节数（默认 50MB）|
| `timeout_keep_alive` | int | `15` | Keep-Alive 超时（秒）|

### db 数据库配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `host` | string | `localhost` | MySQL 主机地址 |
| `port` | int | `3306` | MySQL 端口 |
| `user` | string | `root` | 数据库用户名 |
| `password` | string | - | 数据库密码 |
| `database` | string | `hei_data` | 数据库名称 |
| `pool_size` | int | `20` | 连接池初始大小 |
| `max_overflow` | int | `10` | 连接池最大溢出连接数 |
| `pool_recycle` | int | `3600` | 连接回收时间（秒），超过该时间的连接将被回收重建 |
| `pool_pre_ping` | bool | `true` | 获取连接前执行 Ping 检查 |
| `pool_timeout` | int | `30` | 获取连接的超时时间（秒）|
| `connect_timeout` | int | `10` | 建立新连接的超时时间（秒）|
| `echo` | bool | `false` | 打印 SQL 语句到日志 |

### redis 缓存配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `host` | string | `localhost` | Redis 主机地址 |
| `port` | int | `6379` | Redis 端口 |
| `password` | string | - | Redis 密码 |
| `database` | int | `1` | Redis 数据库编号 |
| `max_connections` | int | `200` | 连接池最大连接数 |
| `socket_connect_timeout` | int | `10` | 连接超时（秒）|
| `socket_timeout` | int | `30` | 读写超时（秒）|
| `retry_on_timeout` | bool | `true` | 超时后自动重试 |
| `health_check_interval` | int | `30` | 健康检查间隔（秒）|

### token 认证配置

B 端和 C 端使用相同的 token 配置，通过不同的 Redis 键前缀区分登录类型。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `expire_seconds` | int | `2592000` | Token 有效期（秒，默认 30 天）|
| `token_name` | string | `Authorization` | HTTP 请求头名称，用于传递 Token |

> 注意：Token 是随机 hex 字符串（非 JWT），完全基于 Redis 服务端会话管理。

### sm2 国密配置

| 配置项 | 类型 | 说明 |
|--------|------|------|
| `private_key` | string | SM2 私钥，用于后端解密密码 |
| `public_key` | string | SM2 公钥，用于前端加密密码 |

SM2 密码传输流程：
1. 前端调用 `GET /api/v1/public/b/sm2-public-key` 获取公钥
2. 前端使用公钥对密码进行 SM2 C1C3C2 模式加密
3. 后端使用私钥解密，获取原始密码
4. 后端使用 bcrypt 对密码进行哈希存储

### cors 跨域配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `allow_origins` | []string | `["*"]` | 允许的来源 |
| `allow_methods` | []string | `["*"]` | 允许的方法 |
| `allow_headers` | []string | `["*"]` | 允许的请求头 |
| `allow_credentials` | bool | `false` | 是否允许携带凭证 |

### snowflake 雪花 ID 配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `instance` | int64 | `1` | 实例编号，分布式部署时每个节点必须不同 |

### storage 存储配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `default` | string | `LOCAL` | 默认存储类型（LOCAL/MINIO/S3）|
| `default_base_url` | string | `""` | 文件访问基础 URL |

各存储后端按需配置（见 config.example.yaml 注释示例）。

### ws WebSocket 配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `read_buffer_size` | int | `1024` | WS 读取缓冲区（字节）|
| `write_buffer_size` | int | `1024` | WS 写入缓冲区 |
| `heartbeat_interval` | int | `15` | 心跳发送间隔（秒）|
| `instance_ttl` | int | `60` | 实例心跳 TTL（秒）|
| `stale_clean_interval` | int | `5` | 过期实例清理间隔（分钟）|
| `rate_limit_window` | int | `10` | 限流时间窗口（秒）|
| `rate_limit_max` | int | `30` | 窗口内最大消息数 |
| `dedup_ttl` | int | `30` | 消息去重 TTL（秒）|
| `poll_timeout` | int | `2` | Redis BRPOP 超时（秒）|
| `pong_timeout` | int | `60` | WS Pong 超时（秒）|
| `write_timeout` | int | `10` | WS 写入超时（秒）|
| `online_broadcast_interval` | int | `60` | 在线人数广播间隔（秒）|

## 配置加载顺序

`config.FindAndLoad()`（`sdk/config/config.go`）按以下顺序搜索配置文件：

1. `HEI_CONFIG` 环境变量指定的路径
2. `./config.yaml`（当前目录）
3. `../config.yaml`（父目录）
4. `../../config.yaml`（上两级目录）
5. 从当前目录向上搜索最多 5 层目录
6. 未找到 → 使用默认值（空配置）
