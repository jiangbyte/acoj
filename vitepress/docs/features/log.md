# 操作日志

Hei Gin 提供了完整的操作日志系统，能够自动录制用户的操作行为，为审计和安全追溯提供数据基础。

## SysLog 中间件

**文件**：`sdk/log/syslog.go`

`SysLog` 是操作日志录制的核心中间件，通过在路由注册时挂载，自动捕获和记录 API 请求信息。

```go
func SysLog(name string) gin.HandlerFunc
```

### 注册方式

```go
import "hei-gin/sdk/log"

r.POST("/user/save",
    authMiddleware.HeiCheckLogin(),
    authMiddleware.HeiCheckPermission([]string{"sys:user:create"}),
    log.SysLog("创建用户"),
    handler.UserSave,
)
```

### 录制内容

| 字段 | 来源 | 说明 |
|------|------|------|
| `category` | 中间件判断 | `"OPERATE"`（成功）或 `"EXCEPTION"`（异常）|
| `name` | `SysLog` 参数 | 操作名称 |
| `exe_status` | 中间件判断 | `"SUCCESS"` 或 `"FAIL"` |
| `exe_message` | 中间件捕获 | BusinessError 详情或 panic 信息 |
| `op_ip` | `utils.GetClientIP()` | 客户端 IP（X-Forwarded-For → X-Real-IP → ClientIP）|
| `op_address` | `utils.GetCityInfo()` | IP 地理位置（当前返回 "-"）|
| `op_browser` | `log.ParseUserAgent()` | 浏览器名称 |
| `op_os` | `log.ParseUserAgent()` | 操作系统 |
| `req_method` | `c.Request.Method` | HTTP 方法 |
| `req_url` | `c.Request.URL.String()` | 请求 URL |
| `param_json` | `log.ExtractParamsJson()` | 请求参数 JSON（仅 POST/PUT/PATCH）|
| `op_time` | `time.Now()` | 操作时间 |
| `trace_id` | `utils.GetTraceID()` | 跟踪 ID |
| `op_user` | `gin.Context("loginUser")` | 操作人用户名 |
| `sign_data` | `log.GenerateLogSignature()` | SM3 签名 |

### 异常处理

`SysLog` 中间件通过 `defer` 捕获 handler 中的 panic：

- `*exception.BusinessError` → 记录异常日志后重新 panic（由 Recovery 处理）
- 其他 panic → 记录异常日志后重新 panic
- `exeMessage` 截断至 2000 字符

## LogPersistence 持久化

日志持久化采用函数变量模式，由插件提供具体实现：

```go
// sdk/log/syslog.go
var LogPersistence LogPersistenceFunc

type LogPersistenceFunc func(ctx interface{}, category, name, exeStatus, exeMessage, opIP, opAddress, opBrowser, opOS, opUser, traceID, signData, method, url, params string, opTime interface{})
```

`plugin-sys` 插件在 `Init()` 中设置 `log.LogPersistence`，将日志写入 `sys_log` 表。如果未设置持久化，日志会通过 `log.Printf` 输出到控制台。

## 工具函数

### ExtractParamsJson

**文件**：`sdk/log/utils.go`

提取 POST/PUT/PATCH 请求体参数为 JSON 字符串：

- 仅对 POST/PUT/PATCH 方法生效；GET/DELETE 返回 `""`
- 自动排除基础设施参数：`request`、`db`、`file`
- 排除值为 `nil` 的参数
- 读取 Body 后自动恢复，不影响下游 `ShouldBindJSON` 等操作

### ParseUserAgent

```go
func ParseUserAgent(ua string) (browser, os string)
```

从 User-Agent 字符串中提取浏览器和操作系统名称。

### GetResultJson

```go
func GetResultJson(result any) string
```

将结果值序列化为 JSON 字符串。结果为 nil 或序列化失败时返回 `""`。

### GenerateLogSignature

```go
func GenerateLogSignature(opData map[string]any) string
```

使用 SM3 算法和盐值 `"hei-log-sign"` 对日志数据进行哈希签名。`opData` map 序列化为 JSON 后计算签名。当前由 SysLog 中间件自动调用。

## 日志查询 API

日志模块（`plugins/plugin-sys/log/`）提供以下接口，均注册在 `/api/v1/sys/log/` 路径下。

### 分页列表

```http
GET /api/v1/sys/log/page?current=1&size=10&category=OPERATE&name=创建用户&exe_status=SUCCESS&op_user=admin
```

列表查询排除大字段（`exe_message`、`param_json`、`result_json`、`sign_data`），按操作时间降序排列。

### 日志详情

```http
GET /api/v1/sys/log/detail?id={logId}
```

### 创建 / 修改 / 删除

```http
POST /api/v1/sys/log/create
POST /api/v1/sys/log/modify
POST /api/v1/sys/log/remove       # ID 列表批量删除
POST /api/v1/sys/log/delete-by-category  # 按分类删除
```

### 统计图表 API

```http
GET /api/v1/sys/log/vis/line-chart-data     # 最近 7 天每日 LOGIN/LOGOUT 数量
GET /api/v1/sys/log/vis/pie-chart-data      # LOGIN/LOGOUT 总量饼图
GET /api/v1/sys/log/op/bar-chart-data       # 最近 7 天每日 OPERATE/EXCEPTION 数量
GET /api/v1/sys/log/op/pie-chart-data       # OPERATE/EXCEPTION 总量饼图
```

## 数据模型

操作日志的 GORM 模型定义在 `plugins/plugin-sys/log/model.go`，映射数据库表 `sys_log`：

| 字段名 | 数据库类型 | 说明 |
|--------|----------|------|
| `id` | varchar(32) | 主键，雪花 ID |
| `category` | varchar(255) | 日志分类 |
| `name` | varchar(255) | 日志名称 |
| `exe_status` | varchar(255) | 执行状态 |
| `exe_message` | text | 具体消息 |
| `op_ip` | varchar(255) | 操作 IP |
| `op_address` | varchar(255) | 操作地址 |
| `op_browser` | varchar(255) | 浏览器 |
| `op_os` | varchar(255) | 操作系统 |
| `req_method` | varchar(255) | 请求方式 |
| `req_url` | text | 请求地址 |
| `param_json` | text | 请求参数 |
| `result_json` | text | 返回结果 |
| `op_time` | datetime | 操作时间 |
| `trace_id` | varchar(64) | 跟踪 ID |
| `op_user` | varchar(255) | 操作人 |
| `sign_data` | text | SM3 签名数据 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

## 最佳实践

### 选择合适的记录方式

```go
// SysLog 中间件 - 自动记录，适合标准 API 路由
r.POST("/api/v1/sys/user", log.SysLog("创建用户"), handler.UserSave)

// 持久化函数 - 程序化记录，适合登录/登出等场景
// 通过 log.LogPersistence 直接调用（由 plugin-sys 提供实现）
```

### 避免敏感信记录

`ExtractParamsJson` 自动排除 `request`、`db`、`file` 参数。登录密码在传输前已 SM2 加密。

### 日志清理

通过 API 批量删除或按分类删除过期日志：

```http
POST /api/v1/sys/log/delete-by-category
{"category": "OPERATE"}
```

### RecordAuthLog

**文件**：`sdk/log/record.go`

程序化记录认证相关日志（如登录/登出），不依赖于认证 Token 上下文。

```go
func RecordAuthLog(c *gin.Context, name, category, exeStatus, exeMessage, opUser string)
```

| 参数 | 说明 |
|------|------|
| `name` | 日志名称，如"用户登录" |
| `category` | 日志分类，如"LOGIN"、"LOGOUT" |
| `exeStatus` | 执行状态，"SUCCESS" / "FAIL" |
| `exeMessage` | 执行消息 |
| `opUser` | 操作人用户名 |

与 SysLog 中间件不同，它不依赖 Context 中的 `loginUser`，适用于尚无认证 Token 的登录事件。

```go
log.RecordAuthLog(c, "用户登录", "LOGIN", "SUCCESS", "", username)
```
