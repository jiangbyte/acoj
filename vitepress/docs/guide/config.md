# 配置文件说明

Hei FastAPI 使用 `.env` 文件作为核心配置文件，位于项目根目录。通过 Pydantic-Settings 的嵌套模型解析，环境变量使用 `__` 作为嵌套分隔符。

## 配置文件示例

```env
# 应用配置
APP__NAME=hei-fastapi
APP__VERSION=1.0.0
APP__DEBUG=true
APP__HOST=127.0.0.1
APP__PORT=18885
APP__UPLOAD_MAX_SIZE=52428800
APP__TIMEOUT_KEEP_ALIVE=15

# 数据库配置
DB__HOST=localhost
DB__PORT=3306
DB__USER=root
DB__PASSWORD=123456
DB__DATABASE=hei_data
DB__POOL_SIZE=20
DB__MAX_OVERFLOW=10

# Redis 配置
REDIS__HOST=localhost
REDIS__PORT=6379
REDIS__PASSWORD=123456
REDIS__DATABASE=1
REDIS__MAX_CONNECTIONS=200

# Token 配置
TOKEN__EXPIRE_SECONDS=2592000
TOKEN__TOKEN_NAME=Authorization

# SM2 国密配置
SM2__PRIVATE_KEY=your-sm2-private-key
SM2__PUBLIC_KEY=your-sm2-public-key

# CORS 配置
CORS__ALLOW_ORIGINS=["*"]
CORS__ALLOW_METHODS=["*"]
CORS__ALLOW_HEADERS=["*"]
CORS__ALLOW_CREDENTIALS=false

# 雪花ID 配置
SNOWFLAKE__INSTANCE=1
```

## 配置项说明

### APP 应用配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `APP__NAME` | str | `hei-fastapi` | 应用名称 |
| `APP__VERSION` | str | `1.0.0` | 应用版本号 |
| `APP__DEBUG` | bool | `true` | 是否开启调试模式 |
| `APP__HOST` | str | `127.0.0.1` | 监听地址 |
| `APP__PORT` | int | `18885` | 监听端口 |
| `APP__UPLOAD_MAX_SIZE` | int | `52428800` | 上传文件最大字节数（50MB） |
| `APP__TIMEOUT_KEEP_ALIVE` | int | `15` | HTTP Keep-Alive 超时（秒） |

### DB 数据库配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `DB__HOST` | str | `localhost` | MySQL 主机地址 |
| `DB__PORT` | int | `3306` | MySQL 端口 |
| `DB__USER` | str | `root` | 数据库用户名 |
| `DB__PASSWORD` | str | `123456` | 数据库密码 |
| `DB__DATABASE` | str | `hei_data` | 数据库名称 |
| `DB__POOL_SIZE` | int | `20` | 连接池大小 |
| `DB__MAX_OVERFLOW` | int | `10` | 连接池最大溢出数 |

`DB__URL` 由程序自动计算：`mysql+pymysql://{user}:{password}@{host}:{port}/{database}?charset=utf8mb4`

### REDIS 缓存配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `REDIS__HOST` | str | `localhost` | Redis 主机地址 |
| `REDIS__PORT` | int | `6379` | Redis 端口 |
| `REDIS__PASSWORD` | str | `123456` | Redis 密码（无密码留空） |
| `REDIS__DATABASE` | int | `1` | Redis 数据库编号 |
| `REDIS__MAX_CONNECTIONS` | int | `200` | 连接池最大连接数 |

`REDIS__URL` 由程序自动计算：`redis://{password}@{host}:{port}/{database}`（密码为空时省略）

### Token 认证配置

TOKEN 配置为**单一共享配置**，管理端（B 端）和客户端（C 端）使用相同的配置字段，通过不同的 Redis Key 前缀区分登录类型。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `TOKEN__TOKEN_NAME` | str | `Authorization` | HTTP 请求头名称 |
| `TOKEN__EXPIRE_SECONDS` | int | `2592000` | Token 有效期（秒，默认 30 天） |


### SM2 国密配置

| 配置项 | 类型 | 说明 |
|--------|------|------|
| `SM2__PRIVATE_KEY` | str | SM2 私钥，用于后端解密密码 |
| `SM2__PUBLIC_KEY` | str | SM2 公钥，用于前端加密密码 |

SM2 是中国国家密码管理局公布的椭圆曲线公钥密码算法。Hei FastAPI 使用 SM2 对登录密码进行加密传输（C1C3C2 模式），防止密码在传输过程中被窃取。

前后端交互流程：
1. 前端调用 `GET /api/v1/public/b/sm2/public-key` 获取公钥
2. 前端使用公钥对密码进行 SM2 加密（C1C3C2 模式）
3. 后端使用私钥解密，获取原始密码
4. 后端使用 bcrypt 对密码进行哈希存储

### CORS 跨域配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `CORS__ALLOW_ORIGINS` | list | `["*"]` | 允许的请求来源列表 |
| `CORS__ALLOW_METHODS` | list | `["*"]` | 允许的 HTTP 方法列表 |
| `CORS__ALLOW_HEADERS` | list | `["*"]` | 允许的请求头列表 |
| `CORS__ALLOW_CREDENTIALS` | bool | `false` | 是否允许携带凭证 |

开发环境可使用 `["*"]` 允许所有来源。生产环境应明确指定 `allow_origins` 为具体的前端域名列表。

### SNOWFLAKE 雪花 ID 配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `SNOWFLAKE__INSTANCE` | int | `1` | 实例编号，分布式部署时每个节点应使用不同的编号 |

Snowflake ID 生成 64 位唯一 ID，结构为：`1位符号位 + 41位时间戳 + 10位工作节点ID + 12位序列号`。在多节点部署时，确保每个节点的 `instance` 值不同以避免 ID 冲突。

## 存储后端配置

框架支持三种文件存储后端，通过环境变量切换（当前需在代码中配置）：

### 本地存储（默认）

```env
STORAGE__ACTIVE=local
STORAGE__LOCAL__PATH=./uploads
```

### MinIO 对象存储

```env
STORAGE__ACTIVE=minio
STORAGE__MINIO__ENDPOINT=http://localhost:9000
STORAGE__MINIO__ACCESS_KEY=minioadmin
STORAGE__MINIO__SECRET_KEY=minioadmin
STORAGE__MINIO__BUCKET=hei-data
```

### S3 兼容对象存储

```env
STORAGE__ACTIVE=s3
STORAGE__S3__ENDPOINT=s3.amazonaws.com
STORAGE__S3__ACCESS_KEY=your-access-key
STORAGE__S3__SECRET_KEY=your-secret-key
STORAGE__S3__BUCKET=hei-data
STORAGE__S3__REGION=us-east-1
```

## 配置加载机制

配置使用 Pydantic-Settings 的 `BaseSettings` 类加载，支持：

1. **`.env` 文件**：项目根目录下的 `.env` 文件
2. **环境变量**：系统环境变量（优先级高于 `.env` 文件）
3. **默认值**：代码中定义的默认值

嵌套配置通过 `__` 分隔符实现，例如 `DB__HOST` 对应 `settings.DB.HOST`。
