# 操作日志

Hei FastAPI 提供了完整的操作日志系统，能够自动录制用户的操作行为，为审计和安全追溯提供数据基础。

## @SysLog 装饰器

**文件**：`core/log/decorator.py`

`@SysLog` 是操作日志录制的核心装饰器，通过在路由上挂载，自动捕获和记录 API 请求信息。

```python
def SysLog(name: str):
```

### 注册方式

```python
from core.log import SysLog

@router.post("/api/v1/sys/user/create")
@SysLog("创建用户")  # 自动记录操作人、时间、IP、请求参数、返回结果
@HeiCheckPermission("sys:user:create")
async def create(...):
    ...
```

### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `name` | str | 操作名称，如"创建用户"、"修改配置" |

## 异常日志记录

`save_exception_log` 函数用于在发生异常时自动记录错误日志：

```python
from core.log import save_exception_log

# 在异常处理器中自动调用
save_exception_log(request, exc, name=None)  # name 可选，默认取"请求方法 路径"
```

该函数由全局异常处理器自动调用，无需手动介入。

## 录制内容

每条日志记录包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | str | 雪花 ID（分布式唯一） |
| `category` | str | 日志分类：`OPERATE`（操作）或 `EXCEPTION`（异常） |
| `name` | str | 操作名称（如"创建用户"） |
| `exe_status` | str | 执行状态：`SUCCESS` 或 `FAIL` |
| `exe_message` | str | 执行消息（异常时的错误信息） |
| `op_ip` | str | 客户端 IP 地址 |
| `op_address` | str | IP 地理位置（由 ip2region 解析） |
| `op_browser` | str | 浏览器名称 |
| `op_os` | str | 操作系统名称 |
| `class_name` | str | 类名称（函数所属模块路径） |
| `method_name` | str | 方法名称（函数完整限定名） |
| `req_method` | str | HTTP 方法（GET/POST/PUT/DELETE） |
| `req_url` | str | 请求 URL |
| `param_json` | text | 请求参数 JSON |
| `result_json` | text | 返回结果 JSON |
| `op_time` | datetime | 操作时间 |
| `trace_id` | str | 跟踪 ID |
| `op_user` | str | 操作人用户名 |
| `sign_data` | text | 签名数据 |
| `created_at` | datetime | 创建时间 |
| `created_by` | str | 创建人 |
| `updated_at` | datetime | 更新时间 |
| `updated_by` | str | 更新人 |

## 日志查询 API

日志模块提供以下接口，均注册在 `/api/v1/sys/log/` 路径下。

### 日志列表

```http
GET /api/v1/sys/log/page
```

请求参数（Query）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `current` | int | 页码，默认 1 |
| `size` | int | 每页条数，默认 10 |
| `keyword` | str | 按操作名称模糊搜索（可选） |
| `category` | str | 按分类筛选：`OPERATE` / `EXCEPTION`（可选） |
| `exe_status` | str | 按执行状态筛选：`SUCCESS` / `FAIL`（可选） |

### 日志详情

```http
GET /api/v1/sys/log/detail?id={log_id}
```

返回单条日志的完整信息，包含所有字段。

### 其他日志接口

```http
POST /api/v1/sys/log/create                     # 手动创建日志
POST /api/v1/sys/log/modify                     # 编辑日志
POST /api/v1/sys/log/remove                     # 删除日志（按 ID 列表）
POST /api/v1/sys/log/delete-by-category         # 按分类批量删除
```

### 日志图表数据

```http
GET /api/v1/sys/log/vis/line-chart-data         # 登录/登出趋势（近 7 天）
GET /api/v1/sys/log/vis/pie-chart-data          # 登录/登出占比
GET /api/v1/sys/log/op/bar-chart-data           # 操作/异常趋势（近 7 天）
GET /api/v1/sys/log/op/pie-chart-data           # 操作/异常占比
```

## 数据模型

操作日志映射数据库表 `sys_log`：

| 字段名 | 数据库类型 | Python 类型 | 说明 |
|--------|----------|------------|------|
| `id` | varchar(32) | str | 主键，雪花 ID |
| `category` | varchar(255) | str | 日志分类 |
| `name` | varchar(255) | str | 日志名称 |
| `exe_status` | varchar(255) | str | 执行状态 |
| `exe_message` | text | str | 具体消息 |
| `op_ip` | varchar(255) | str | 操作 IP |
| `op_address` | varchar(255) | str | 操作地址 |
| `op_browser` | varchar(255) | str | 操作浏览器 |
| `op_os` | varchar(255) | str | 操作系统 |
| `class_name` | varchar(255) | str | 类名称（函数模块路径） |
| `method_name` | varchar(255) | str | 方法名称（函数限定名） |
| `req_method` | varchar(255) | str | 请求方式 |
| `req_url` | text | str | 请求地址 |
| `param_json` | text | str | 请求参数 |
| `result_json` | text | str | 返回结果 |
| `op_time` | datetime | datetime | 操作时间 |
| `trace_id` | varchar(64) | str | 跟踪 ID |
| `op_user` | varchar(255) | str | 操作人 |
| `sign_data` | text | str | 签名数据 |

## 最佳实践

### 1. 选择合适的记录方式

```python
# @SysLog 装饰器 — 自动记录，适合标准 API 路由
@router.post("/api/v1/sys/user/create")
@SysLog("创建用户")
@HeiCheckPermission("sys:user:create")
async def create(...):
    ...

# save_exception_log — 异常时自动记录（由全局异常处理器自动调用）
# 无需手动调用
```

### 2. 避免记录敏感信息

- 登录接口的密码字段已 SM2 加密，日志中记录的是加密后的密文
- 涉及个人隐私的数据（身份证、手机号）建议在传输前脱敏
- 文件上传接口的请求体不会完整记录

### 3. 日志清理策略

操作日志数据量会随时间增长，可通过 API 定期清理：

```http
POST /api/v1/sys/log/delete-by-category
Content-Type: application/json

{"category": "OPERATE"}
```

### 4. 日志签名机制

日志系统支持 SM3 签名机制，可用于防篡改验证：

```python
from core.log.utils import generate_log_signature

# 生成日志签名
signature = generate_log_signature({"key": "value"})
```

## 相关文件

| 文件 | 说明 |
|------|------|
| `core/log/decorator.py` | @SysLog 装饰器 + save_exception_log 函数 |
| `core/log/utils.py` | User-Agent 解析、参数序列化、签名生成 |
| `modules/sys/log/` | 日志查询管理模块（API + Service + DAO） |
