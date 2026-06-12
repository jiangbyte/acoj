# 中间件体系

Hei FastAPI 的中间件系统分为三个层次：全局中间件、装饰器中间件和内置中间件。以下逐一介绍每个中间件的功能和使用方式。

## 全局中间件

### 1. CORS 中间件

**文件**：`sdk/middleware/cors.py`

CORS 中间件处理跨域请求，配置项来源于 `.env` 中的 `CORS__*` 配置段。

**功能**：
- 处理 OPTIONS 预检请求
- 配置允许的来源、方法、头
- 支持 Allow Credentials 配置

**注册方式**：

```python
from sdk.middleware.cors import setup_cors

app = setup_cors(app)
```

### 2. TraceMiddleware

**文件**：`sdk/middleware/trace.py`

TraceMiddleware 负责全链路追踪，为每个请求生成或透传唯一的 trace_id。

**功能**：
- 检查请求头中是否包含 `trace_id`
- 如果存在，透传该 Trace ID
- 如果不存在，调用 `generate_trace_id()` 生成新的 UUID
- 将 Trace ID 设置到 ContextVar 中，在整个请求生命周期内可访问

**响应示例**：

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {},
  "success": true,
  "trace_id": "a1b2c3d4e5f67890abcdef1234567890"
}
```

**注册方式**：

```python
from sdk.middleware.trace import TraceMiddleware

app.add_middleware(TraceMiddleware)
```

### 3. AuthMiddleware

**文件**：`sdk/middleware/auth.py`

AuthMiddleware 是一个原始 ASGI 中间件（非 `BaseHTTPMiddleware`），根据请求路径自动识别认证类型，对无需认证的路径放行，对需要认证的路径进行登录检查。

**路由分流规则**：

```
静态路径 (/favicon.ico, /docs, /redoc, /openapi.json)  -> 放行
OPTIONS 方法                                              -> 放行
/api/v{n}/public/b/* 或 /api/v{n}/public/c/*             -> 放行（无需认证）
/api/v{n}/c/*                                            -> 要求 C 端登录认证
/api/v{n}/b/*                                            -> 要求 B 端登录认证
其他 /api/v{n}/*（如 /sys/* /client/*）                   -> 要求 B 端登录认证（默认规则）
```

未通过认证的请求会收到 HTTP 200 的 JSON 响应（body 中 code=401, success=false）。

**为什么使用原始 ASGI 中间件**：
- `BaseHTTPMiddleware` 在处理流式响应和文件上传时存在 body streaming 问题
- 原始 ASGI 中间件能直接操作 ASGI scope，对请求/响应有完全控制权

**注册方式**：

```python
from sdk.middleware.auth import AuthMiddleware

app.add_middleware(AuthMiddleware)
```

### 4. 全局异常处理器

**文件**：`sdk/middleware/exception.py`

全局异常处理器捕获所有未处理的异常，转换为友好的 JSON 错误响应。

**支持的异常类型**：

| 异常类型 | HTTP 状态码 | Body code | 说明 |
|---------|------------|-----------|------|
| `BusinessException` | 200 | 400 | 业务异常，如用户名或密码错误 |
| `HTTPException` (401) | 401 | 401 | 未认证 |
| `HTTPException` (403) | 403 | 403 | 无权限 |
| `HTTPException` (404) | 404 | 404 | 资源不存在 |
| `RequestValidationError` | 200 | 400 | 参数校验失败 |
| 通用 `Exception` | 200 | 500 | 服务器内部错误 |

**注册方式**：

```python
from sdk.middleware.exception import setup_exception_handlers

setup_exception_handlers(app)
```

**BusinessException 使用方式**：

```python
from sdk.exception import BusinessException

# 在 Service 层抛出业务异常
raise BusinessException("用户名或密码错误", 400)
```

**对应响应**：

```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null,
  "success": false,
  "trace_id": "a1b2c3d4..."
}
```

## 装饰器中间件

装饰器通过函数包装方式实现，按需应用到具体的路由上。

### 5. @HeiCheckLogin（B 端登录验证）

**文件**：`sdk/auth/decorator/hei_check_login.py`

验证请求携带的 Token Token 是否有效。

**函数签名**：

```python
def HeiCheckLogin(func=None, *, login_type: str = LoginTypeEnum.BUSINESS)
```

**功能**：
- 从 `Authorization` 头中提取 Bearer Token
- 解析 Token 的有效性
- 验证 Token 是否在禁用列表中
- 从 Redis 中读取会话信息

**使用方式**：

```python
from sdk.auth.decorator import HeiCheckLogin

@router.get("/api/v1/b/user/info")
@HeiCheckLogin
async def get_user_info(request: Request):
    ...
```

### 6. @HeiClientCheckLogin（C 端登录验证）

**文件**：`sdk/auth/decorator/hei_client_check_login.py`

C 端的专用登录验证装饰器，内部委托给 `HeiCheckLogin(func, login_type=LoginTypeEnum.CONSUMER)`。

**使用方式**：

```python
from sdk.auth.decorator import HeiClientCheckLogin

@router.get("/api/v1/c/user/profile")
@HeiClientCheckLogin
async def get_profile(request: Request):
    ...
```

### 7. @HeiCheckPermission（B 端权限检查）

**文件**：`sdk/auth/decorator/hei_check_permission.py`

检查当前登录用户是否拥有指定的权限。同时具有**权限自动注册**功能。

**函数签名**：

```python
def HeiCheckPermission(
    permission: Union[str, List[str]],
    mode: str = CheckModeEnum.AND,
    login_type: str = LoginTypeEnum.BUSINESS
)
```

- `permission`：单个权限代码字符串或字符串列表
- `mode`：匹配模式，默认为 `"AND"`（需要全部权限），传入 `"OR"` 则满足任意一个即可
- `login_type`：登录类型，默认为 `"BUSINESS"`

**功能**：
- 自动注册权限代码到权限扫描系统
- 检查用户是否为 SUPER_ADMIN，是则直接放行
- 从 Redis/DB 中获取用户权限集合
- 使用权限匹配器进行通配符匹配
- 匹配成功则放行，失败则返回 403

**使用方式**：

```python
from sdk.auth.decorator import HeiCheckPermission

# 单个权限（AND 模式）
@router.get("/api/v1/sys/user/page")
@HeiCheckPermission("sys:user:page")
async def page(...):
    ...

# 多个权限（AND 模式：需要全部）
@router.post("/api/v1/sys/user/save")
@HeiCheckPermission(["sys:user:create", "sys:user:modify"])
async def save(...):
    ...

# OR 模式：任意一个匹配即可
@router.post("/api/v1/sys/user/save")
@HeiCheckPermission(["sys:user:create", "sys:user:update"], mode="OR")
async def save(...):
    ...
```

### 8. @HeiClientCheckPermission（C 端权限检查）

**文件**：`sdk/auth/decorator/hei_client_check_permission.py`

C 端的权限检查装饰器。内部委托给 `HeiCheckPermission(permission, mode, login_type=LoginTypeEnum.CONSUMER)`。

```python
from sdk.auth.decorator import HeiClientCheckPermission

@router.get("/api/v1/client-user/page")
@HeiClientCheckPermission("client:user:page")
async def page(...):
    ...
```

### 9. @HeiCheckRole（B 端角色检查）

**文件**：`sdk/auth/decorator/hei_check_role.py`

检查当前用户是否拥有指定的角色。

```python
from sdk.auth.decorator import HeiCheckRole

# 单个角色
@router.delete("/api/v1/sys/user/remove")
@HeiCheckRole("admin")
async def remove(...):
    ...

# 多个角色（OR 模式）
@router.post("/api/v1/sys/config/save")
@HeiCheckRole(["admin", "operator"], mode="OR")
async def config_save(...):
    ...
```

### 10. @HeiClientCheckRole（C 端角色检查）

**文件**：`sdk/auth/decorator/hei_client_check_role.py`

C 端的角色检查装饰器，内部委托给 `HeiCheckRole(role, mode, login_type=LoginTypeEnum.CONSUMER)`。

### 11. @SysLog（操作日志录制）

**文件**：`sdk/log/decorator.py`

自动录制用户的操作日志，在 handler 执行完成后自动写入 `sys_log` 表。

```python
from sdk.log import SysLog

# 记录操作日志
@router.post("/api/v1/sys/user/create")
@SysLog("新增用户")  # 操作名称
@HeiCheckPermission("sys:user:create")
async def create(...):
    ...
```

**录制内容**：操作名称、分类（OPERATE/EXCEPTION）、执行状态（SUCCESS/FAIL）、IP 地址、地理位置、浏览器、操作系统、请求方法、请求 URL、请求参数、操作时间、Trace ID、操作人等。

### 12. @NoRepeat（防重复提交）

**文件**：`sdk/auth/decorator/norepeat.py`

防止用户在指定时间间隔内重复提交相同的请求。

```python
from sdk.auth.decorator import NoRepeat

# 5 秒内禁止重复提交（默认值）
@router.post("/api/v1/sys/user/create")
@NoRepeat
@HeiCheckPermission("sys:user:create")
async def create(...):
    ...

# 自定义时间间隔（3 秒）
@router.post("/api/v1/sys/user/create")
@NoRepeat(interval=3000)
@HeiCheckPermission("sys:user:create")
async def create(...):
    ...
```

**参数**：
- `interval`：防重复时间间隔，单位**毫秒**（ms），默认 5000

**原理**：
- 基于请求的路径 + 参数 + 用户 ID + IP 生成缓存键
- 对请求参数计算哈希值
- 在 Redis 中记录该哈希值和时间戳（TTL 为 3600 秒）
- 同一请求在指定间隔内重复提交会被拒绝（返回 `BusinessException`）

## 装饰器执行顺序

装饰器在 Python 中是从下到上应用、从上到下执行的：

```python
@router.post("/api/v1/sys/user/create")    # ① 路由匹配
@SysLog("新增用户")                         # ② 开始日志录制
@HeiCheckPermission("sys:user:create")     # ③ 权限检查
@NoRepeat(interval=3000)                   # ④ 防重复检查
async def create(...):                     # ⑤ 业务逻辑
    ...
```

执行顺序：

```
请求到达
    │
    ▼
① 路由匹配（FastAPI 路由）
    │
    ▼
② @SysLog 开始录制
    │
    ▼
③ @HeiCheckPermission 权限检查
    │
    ▼
④ @NoRepeat 防重复检查
    │
    ▼
⑤ 业务 Handler 执行
    │
    ▼
⑥ @NoRepeat 设置防重复标记（后处理）
    │
    ▼
⑦ @SysLog 完成录制（后处理）
    │
    ▼
返回响应
```

> 注意：`@HeiCheckLogin` 由全局 `AuthMiddleware` 统一处理，无需在路由上单独添加。权限装饰器 `@HeiCheckPermission` 必须位于 `@router.*` 下方（紧贴函数），这是 FastAPI 装饰器的约束。
