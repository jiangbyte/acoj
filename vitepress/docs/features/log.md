# 操作日志

Hei Gin 提供了完整的操作日志系统，能够自动录制用户的操作行为，为审计和安全追溯提供数据基础。

## SysLog 中间件

**文件**：`core/log/syslog.go`

SysLog 是操作日志录制的核心中间件，通过在路由注册时挂载，自动捕获和记录 API 请求信息。它接受一个操作名称并返回一个 `gin.HandlerFunc`。

```go
func SysLog(name string) gin.HandlerFunc
```

没有额外的录制选项参数。请求参数（Body）的录制行为由中间件内部自动处理（仅 POST/PUT/PATCH 方法），响应结果默认不录制。

### 注册方式

```go
import "hei-gin/core/log"

// 记录操作日志
r.POST("/user/save",
    middleware.HeiCheckLogin(),
    middleware.HeiCheckPermission("sys:user:create"),
    log.SysLog("创建用户"),
    handler.UserSave,
)
```

### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `name` | string | 操作名称，如"创建用户"、"修改配置" |

## RecordAuthLog 函数

**文件**：`core/log/record.go`

用于程序化记录登录/登出等认证相关日志。与 SysLog 中间件不同，它不需要从上下文中获取操作人，而是直接接受操作人名称参数——这对尚无可用的认证 Token 的登录事件至关重要。

```go
func RecordAuthLog(c *gin.Context, name, category, exeStatus, exeMessage, opUser string)
```

该函数当前为**控制台日志输出**（`log.Printf`），尚未接入数据库持久化，待日志模块完成后接入。

### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `name` | string | 日志名称 |
| `category` | string | 日志分类（如 "LOGIN"、"LOGOUT"） |
| `exeStatus` | string | 执行状态（"SUCCESS" / "FAIL"） |
| `exeMessage` | string | 执行消息 |
| `opUser` | string | 操作人用户名 |

## 录制内容

中间件内部通过 `saveLog` 函数将日志写入数据库，每条记录包含以下字段：

| 字段 | 类型 | 说明 | 中间件是否填充 |
|------|------|------|:----------:|
| `id` | string | 雪花 ID（分布式唯一） | 是 |
| `category` | string | 日志分类：`"OPERATE"`（成功）或 `"EXCEPTION"`（异常） | 是 |
| `name` | string | 操作名称（如"创建用户"） | 是 |
| `exe_status` | string | 执行状态：`"SUCCESS"` 或 `"FAIL"` | 是 |
| `exe_message` | string | 具体消息（BusinessError 详情或 panic 信息，成功时为空） | 是 |
| `op_ip` | string | 客户端 IP 地址 | 是 |
| `op_address` | string | IP 地理位置（城市，由 ip2region 解析） | 是 |
| `op_browser` | string | 浏览器名称（由 User-Agent 解析） | 是 |
| `op_os` | string | 操作系统名称（由 User-Agent 解析） | 是 |
| `class_name` | string | 类名称 | 否 |
| `method_name` | string | 方法名称 | 否 |
| `req_method` | string | HTTP 方法（GET/POST/PUT/DELETE） | 是 |
| `req_url` | string | 请求 URL | 是 |
| `param_json` | string | 请求参数 JSON（仅 POST/PUT/PATCH 方法，排除 request/db/file 参数） | 是 |
| `result_json` | string | 返回结果 JSON | 否* |
| `op_time` | time | 操作时间 | 是 |
| `trace_id` | string | 跟踪 ID | 是 |
| `op_user` | string | 操作人用户名（从 gin.Context 的 "loginUser" 键获取） | 是 |
| `sign_data` | string | SM3 签名数据 | 否* |
| `created_at` | time | 创建时间 | 是 |
| `created_by` | string | 创建用户 | 否 |
| `updated_at` | time | 更新时间 | 是 |
| `updated_by` | string | 更新用户 | 否 |

> *`result_json` 和 `sign_data` 字段存在于数据库 schema 中，提供 `GetResultJson` 和 `GenerateLogSignature` 工具函数，但当前 SysLog 中间件**不会**自动填充这两个字段。如需要，可在业务 handler 中手动调用并设置。

## 请求参数提取机制

`ExtractParamsJson` 函数（`core/log/utils.go`）负责提取请求参数：

- 仅对 **POST / PUT / PATCH** 方法生效；GET / DELETE 返回空字符串
- 自动排除基础设施参数：`request`、`db`、`file`
- 排除值为 `nil` 的参数
- 读取 Body 后会自动恢复，不影响下游 handler 的 `ShouldBindJSON` 等操作

## SM3 签名工具

`GenerateLogSignature` 函数（`core/log/utils.go`）可用于对日志数据进行防篡改签名：

```go
func GenerateLogSignature(opData map[string]any) string
```

- 使用 SM3 算法和盐值 `"hei-log-sign"` 进行哈希
- 将 `opData` map 序列化为 JSON 后计算签名
- 当前为可选功能，中间件**不会**自动调用此函数；需在业务代码中手动使用

## 日志查询 API

日志模块（`modules/sys/log/`）提供以下接口，均注册在 `/api/v1/sys/log/` 路径下。

### 日志列表

```http
GET /api/v1/sys/log/page
```

请求参数（Query）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `current` | int | 页码，默认 1 |
| `size` | int | 每页条数，默认 10 |
| `keyword` | string | 按操作名称模糊搜索（可选） |
| `category` | string | 按分类筛选：`OPERATE` / `EXCEPTION`（可选） |
| `exe_status` | string | 按执行状态筛选：`SUCCESS` / `FAIL`（可选） |

列表查询**排除**大字段（`exe_message`、`param_json`、`result_json`、`sign_data`），按操作时间降序排列。

### 日志详情

```http
GET /api/v1/sys/log/detail?id={logId}
```

返回单条日志的完整信息，包含所有字段。

### 创建日志

```http
POST /api/v1/sys/log/create
```

手动创建操作日志记录。

### 修改日志

```http
POST /api/v1/sys/log/modify
```

编辑现有操作日志记录。

### 删除日志

```http
POST /api/v1/sys/log/remove
```

按 ID 列表批量删除日志。

### 按分类删除

```http
POST /api/v1/sys/log/delete-by-category
```

请求参数（JSON Body）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | string | 日志分类，如 `OPERATE` 或 `EXCEPTION` |

### 访问量折线图

```http
GET /api/v1/sys/log/vis/line-chart-data
```

返回最近 7 天每日的 LOGIN / LOGOUT 数量。

### 访问量饼图

```http
GET /api/v1/sys/log/vis/pie-chart-data
```

返回 LOGIN / LOGOUT 总量。

### 操作量柱状图

```http
GET /api/v1/sys/log/op/bar-chart-data
```

返回最近 7 天每日的 OPERATE / EXCEPTION 数量。

### 操作量饼图

```http
GET /api/v1/sys/log/op/pie-chart-data
```

返回 OPERATE / EXCEPTION 总量。

## 数据模型

操作日志的数据模型定义在 `ent/schema/syslog.go`，映射数据库表 `sys_log`：

| 字段名 | 数据库类型 | Go 类型 | 说明 |
|--------|----------|---------|------|
| `id` | varchar(32) | string | 主键，雪花 ID |
| `category` | varchar(255) | *string | 日志分类（OPERATE / EXCEPTION） |
| `name` | varchar(255) | *string | 日志名称 |
| `exe_status` | varchar(255) | *string | 执行状态（SUCCESS / FAIL） |
| `exe_message` | text | *string | 具体消息 |
| `op_ip` | varchar(255) | *string | 操作 IP |
| `op_address` | varchar(255) | *string | 操作地址 |
| `op_browser` | varchar(255) | *string | 操作浏览器 |
| `op_os` | varchar(255) | *string | 操作系统 |
| `class_name` | varchar(255) | *string | 类名称 |
| `method_name` | varchar(255) | *string | 方法名称 |
| `req_method` | varchar(255) | *string | 请求方式 |
| `req_url` | text | *string | 请求地址 |
| `param_json` | text | *string | 请求参数 |
| `result_json` | text | *string | 返回结果 |
| `op_time` | datetime | *time.Time | 操作时间 |
| `trace_id` | varchar(64) | *string | 跟踪 ID |
| `op_user` | varchar(255) | *string | 操作人姓名 |
| `sign_data` | text | *string | 签名数据 |
| `created_at` | datetime | *time.Time | 创建时间 |
| `created_by` | varchar(32) | *string | 创建用户 |
| `updated_at` | datetime | *time.Time | 更新时间 |
| `updated_by` | varchar(32) | *string | 更新用户 |

## 最佳实践

### 1. 选择合适的记录方式

```go
// SysLog 中间件 - 自动记录，适合标准 API 路由
r.POST("/api/v1/sys/user", log.SysLog("创建用户"), handler.UserSave)

// RecordAuthLog - 程序化记录，适合登录/登出等特殊场景
log.RecordAuthLog(c, "用户登录", "LOGIN", "SUCCESS", "", username)
```

### 2. 手动录制响应结果或签名

当前中间件不会自动记录 `result_json` 和 `sign_data`。若需要，可在 handler 中手动调用工具函数：

```go
import "hei-gin/core/log"

func MyHandler(c *gin.Context) {
    data := doBusiness(c)
    
    // 手动获取响应结果 JSON
    resultJSON := log.GetResultJson(data)
    
    // 手动生成签名
    sign := log.GenerateLogSignature(map[string]any{
        "result": data,
        "user":   "xxx",
    })
    
    // 将 data 存入日志或通过其他方式持久化
    _ = resultJSON
    _ = sign
}
```

### 3. 避免记录敏感信息

`ExtractParamsJson` 会自动排除 `request`、`db`、`file` 等基础设施参数，但仍需注意：

- 登录接口的密码字段虽然已 SM2 加密，但仍建议不在请求体中包含明文敏感信息
- 涉及个人隐私的数据（身份证、手机号）建议在传输前脱敏
- 大文件上传接口的请求体不会被记录（文件参数被排除）

### 4. 日志清理策略

操作日志数据量会随时间增长，可通过 API 定期清理：

```http
POST /api/v1/sys/log/delete-by-category
Content-Type: application/json

{"category": "OPERATE"}
```

或通过系统配置模块设置自动清理策略。

## 相关文件

| 文件 | 说明 |
|------|------|
| `core/log/syslog.go` | SysLog 中间件实现（自动录制 + 数据库写入） |
| `core/log/record.go` | RecordAuthLog 函数（程序化记录，当前为控制台日志） |
| `core/log/utils.go` | 工具函数：User-Agent 解析、参数提取、结果序列化、签名生成 |
| `ent/schema/syslog.go` | 数据库模型定义 |
| `modules/sys/log/` | 日志查询管理模块（API + Service + 参数） |
