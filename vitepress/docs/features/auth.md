# 认证体系

Hei FastAPI 实现了双端认证体系，B 端（管理后台）和 C 端（客户端）使用独立的认证工具和独立的 Redis 键空间，共享同一个 Token 配置。

## 双端认证设计

### 设计理念

B 端和 C 端是两种完全不同的用户群体，有着不同的安全要求和业务逻辑：

- **B 端用户**：系统管理员、运营人员，数量少、权限大，需要严格的权限管控
- **C 端用户**：平台普通用户，数量多、权限小，需要高并发支持

因此 Hei FastAPI 将两端的认证逻辑完全隔离，互不干扰。

### 认证工具对比

| 维度 | B 端（BUSINESS） | C 端（CONSUMER） |
|------|------------------|-----------------|
| 实现方式 | 类方法（`HeiAuthTool`） | 独立实例（`HeiClientAuthTool`） |
| 文件 | `sdk/auth/auth/hei_auth_tool.py` | `sdk/auth/auth/hei_client_auth_tool.py` |

| Redis 键前缀 | `hei:auth:BUSINESS:` | `hei:auth:CONSUMER:` |
| 认证装饰器 | `@HeiCheckLogin` | `@HeiClientCheckLogin` |
| 权限装饰器 | `@HeiCheckPermission` | `@HeiClientCheckPermission` |
| 登录类型 | `BUSINESS` | `CONSUMER` |

> **注意**：B 端和 C 端使用同一份 Token 配置（`.env` 中的 `Token__*`），包括 `expire_seconds`、`token_name`。通过不同的 Redis Key 前缀区分登录类型。

## Token 会话管理

### Token 设计

每个登录会话生成一个 **随机 Token**

### 会话存储

Token 会话信息存储在 Redis 中，数据结构如下：

```
Redis Key: hei:auth:{BUSINESS|CONSUMER}:token:{random_token}
Redis Value (JSON):
{
  "user_id": "snowflake-id",
  "type": "BUSINESS",
  "created_at": "2026-01-01 10:00:00",
  "extra": { ... }
}
```

同一用户的令牌通过 Redis Set 管理，用于批量踢下线：

```
Redis Key: hei:auth:{BUSINESS|CONSUMER}:session:{user_id}
Redis Value: 多个 random_token（Redis Set）
Redis TTL: 等于 token 过期时间
```

### 令牌禁用

当用户主动登出或被踢下线时：
1. 从 `session:{user_id}` 的 Set 中移除该 token
2. 删除 `token:{random_token}` 键值对
3. Token 立即失效（下次请求时无法从 Redis 获取数据）

## 核心 API

### B 端认证 API（HeiAuthTool 类方法）

```python
from sdk.auth.auth.hei_auth_tool import HeiAuthTool

# 登录：通过用户 ID 签发 Token，存储会话到 Redis
token = HeiAuthTool.login(request, user_id, extra={"role": "admin"})

# 登出：销毁当前请求的 Token
HeiAuthTool.logout(request)

# 踢下线：删除指定用户的全部会话
HeiAuthTool.kickout(user_id)

# 检查当前请求是否已登录
is_login = HeiAuthTool.isLogin(request)

# 获取当前请求对应的登录用户 ID
user_id = HeiAuthTool.getLoginId(request)

# 从指定 Token 中解析用户 ID
user_id = HeiAuthTool.getLoginIdByToken(token_string)

# 获取 Token 值（从请求头中提取）
token = HeiAuthTool.getTokenValue(request)

# 刷新 Token 过期时间
HeiAuthTool.renewTimeout(request)
HeiAuthTool.renewTimeout(request, 7200)

# 禁用 / 检查 / 解除禁用
HeiAuthTool.disable(user_id, 300)      # 禁用 300 秒
ok = HeiAuthTool.isDisable(user_id)    # 是否被禁用
HeiAuthTool.untieDisable(user_id)      # 解除禁用

# 获取登录类型标识
login_type = HeiAuthTool.getLoginType()  # 返回 "BUSINESS"
```

### C 端认证 API（HeiClientAuthTool 实例方法）

```python
from sdk.auth.auth.hei_client_auth_tool import HeiClientAuthTool

auth_tool = HeiClientAuthTool()

# 登录
token = auth_tool.login(request, user_id, extra={"nickname": "Tom"})

# 登出
auth_tool.logout(request)

# 踢下线
auth_tool.kickout(user_id)

# 检查是否已登录
is_login = auth_tool.isLogin(request)

# 获取当前登录用户 ID
user_id = auth_tool.getLoginId(request)

# 获取 Token 值
token = auth_tool.getTokenValue(request)

# 刷新过期时间
auth_tool.renewTimeout(request)

# 禁用 / 检查
auth_tool.disable(user_id, 300)
ok = auth_tool.isDisable(user_id)

# 获取登录类型标识
login_type = auth_tool.getLoginType()  # 返回 "CONSUMER"
```

## 登录流程

完整的登录流程涉及多个步骤：

```
前端                          后端
 │                             │
 ├── 1. 获取验证码 ──────────► │  GET /api/v1/public/b/captcha
 │◄── 返回验证码图片 ──────────┤
 │                             │
 ├── 2. 获取 SM2 公钥 ───────► │  GET /api/v1/public/b/sm2/public-key
 │◄── 返回公钥 ────────────────┤
 │                             │
 ├── 3. 加密密码 ──────────────┤  前端使用公钥 SM2 加密密码（C1C3C2）
 │                             │
 ├── 4. 提交登录 ────────────► │  POST /api/v1/public/b/login
 │   {                        │  {
 │     captcha_id,            │    验证码校验
 │     captcha_value,         │    SM2 私钥解密密码
 │     username,              │    bcrypt 比对密码
 │     password(加密后)        │    生成随机 Token（单一 Token）
 │   }                        │    存储 Redis 会话
 │◄── 返回 Token ─────────────┤    返回随机 Token
 │                             │
 ├── 5. 携带 Token 请求 API ─► │  Authorization: Bearer <token>
 │                             │  AuthMiddleware 自动验证
 │                             │
```

## 认证路由分流

`sdk/middleware/auth.py` 中的 `AuthMiddleware` 是一个原始 ASGI 中间件，根据请求路径的前缀自动识别认证上下文。使用正则 `^/api/v\d+/([^/]+)/` 提取路径第一个分段：

```
路由分流逻辑：
  /favicon.ico, /docs, /redoc, /openapi.json → 无需认证
  OPTIONS 请求 → 无需认证
  /api/v{n}/public/b/* 或 /api/v{n}/public/c/* → 无需认证
  /api/v{n}/c/* → 使用 HeiClientAuthTool 检查（C 端）
  /api/v{n}/b/* → 使用 HeiAuthTool 检查（B 端）
  其他 /api/v{n}/* → 使用 HeiAuthTool 检查（B 端，默认规则）
```

未通过认证的请求会收到 HTTP 200 的 JSON 响应（body 中 code=401, success=false）。

## 会话管理

系统管理员可以通过会话管理模块查看和操作在线用户：

- **查看在线用户**：通过 Redis 扫描 `hei:auth:BUSINESS:session:*` 或 `hei:auth:CONSUMER:session:*` 列出活跃 session
- **强制下线**：`HeiAuthTool.kickout(login_id)` 或 `HeiClientAuthTool.kickout(login_id)` 踢掉指定用户的所有会话
- **指定 Token 下线**：通过 `kickout_token` 方法删除指定 Token
- **会话分析**：查看在线用户数、今日登录数等统计

### 会话管理 API

```http
# B 端会话管理
GET    /api/v1/sys/session/analysis          # 会话分析统计
GET    /api/v1/sys/session/page              # 在线用户列表
POST   /api/v1/sys/session/exit              # 强制下线用户
GET    /api/v1/sys/session/tokens            # 用户 Token 列表
POST   /api/v1/sys/session/exit-token        # 指定 Token 下线
GET    /api/v1/sys/session/chart-data        # 会话图表数据

# C 端会话管理
GET    /api/v1/client/session/analysis       # 会话分析统计
GET    /api/v1/client/session/page           # 在线用户列表
POST   /api/v1/client/session/exit           # 强制下线用户
GET    /api/v1/client/session/tokens         # 用户 Token 列表
POST   /api/v1/client/session/exit-token     # 指定 Token 下线
GET    /api/v1/client/session/chart-data     # 会话图表数据
```

## 安全特性

1. **密码传输加密**：使用 SM2 国密算法（C1C3C2 模式）加密密码传输
2. **密码存储哈希**：使用 bcrypt 加盐哈希存储密码
3. **随机 Token**：纯 Redis 服务端会话管理
4. **Redis 会话**：服务端会话管理，可主动失效
5. **Token 禁用**：登出/踢下线后 Token 立即从 Redis 删除
6. **Disable 机制**：支持按 login_id 临时禁止登录（防暴力破解）
7. **验证码保护**：登录需要图形验证码，防止暴力破解
