# 架构概述

## 应用初始化流程

Hei Gin 的应用启动流程由 `core/app/app.go` 中的应用工厂（App Factory）编排，遵循清晰的初始化顺序：

```
Config 加载
    │
    ▼
Logger 初始化
    │
    ▼
DB (Ent Client) 初始化 ────────── MySQL 连接 + 自动迁移
    │
    ▼
Redis 初始化 ───────────────────── Redis 连接
    │
    ▼
SM2 初始化 ─────────────────────── 国密加密工具加载
    │
    ▼
Auth 初始化 ────────────────────── JWT 认证工具初始化（B端 + C端）
    │
    ▼
Permission 接口注册 ────────────── 权限查询接口注册到管理器
    │
    ▼
Captcha 初始化 ─────────────────── 图形验证码服务
    │
    ▼
Gin Engine 创建 ────────────────── 路由引擎
    │
    ▼
Middleware 注册 ────────────────── 全局中间件链
    │
    ▼
Router 注册 ────────────────────── 业务路由挂载
    │
    ▼
Permission 扫描 ────────────────── 扫描路由注册的权限并缓存到 Redis
    │
    ▼
HTTP Server 启动 ───────────────── 监听端口，提供服务
```

## 请求生命周期

一个完整的 HTTP 请求在 Hei Gin 中的处理流程如下：

```
客户端请求
    │
    ▼
┌─────────────────────────────────────────────┐
│              Gin Engine                      │
│                                             │
│  ① Trace 中间件                             │
│     ├─ 生成/透传 X-Trace-Id                  │
│     └─ 设置到 Context                        │
│                                             │
│  ② AuthCheck 中间件                          │
│     ├─ 解析请求路径前缀                       │
│     ├─ /api/v1/public/b/* → B端公开          │
│     ├─ /api/v1/public/c/* → C端公开          │
│     ├─ /api/v1/b/*        → B端认证          │
│     ├─ /api/v1/c/*        → C端认证          │
│     └─ 设置 auth 上下文                      │
│                                             │
│  ③ Recovery 中间件                          │
│     ├─ 捕获 panic                           │
│     ├─ BusinessException → 业务错误响应       │
│     └─ 其他 panic → 500 内部错误              │
│                                             │
│  ④ CORS 中间件                              │
│     ├─ 处理跨域请求                          │
│     └─ OPTIONS 预检请求直接返回               │
│                                             │
│  ⑤ 路由匹配 → Handler                       │
│     ├─ 路径 /api/v1/b/xxx                   │
│     └─ 匹配到对应的模块 handler               │
│                                             │
│  ⑥ 路由组中间件链                            │
│     ├─ HeiCheckLogin    → JWT 令牌验证       │
│     ├─ HeiCheckPermission → 权限检查          │
│     ├─ SysLog           → 操作日志录制        │
│     └─ NoRepeat         → 防重复提交          │
│                                             │
│  ⑦ 业务 Handler                             │
│     ├─ 参数校验                              │
│     ├─ 业务逻辑处理                           │
│     └─ 统一响应返回                           │
│                                             │
└─────────────────────────────────────────────┘
    │
    ▼
客户端收到响应
```

## 双端认证架构

Hei Gin 实现了完全隔离的双端认证体系：

```
                      ┌──────────────────────┐
                      │    客户端请求          │
                      └──────────┬───────────┘
                                 │
                                 ▼
                      ┌──────────────────────┐
                      │   AuthCheck 中间件     │
                      │   基于路径自动分流      │
                      └──────┬──────────┬─────┘
                             │          │
              ┌──────────────┘          └──────────────┐
              ▼                                         ▼
┌─────────────────────────┐         ┌─────────────────────────┐
│     B 端 (BUSINESS)      │         │     C 端 (CONSUMER)      │
│                         │         │                         │
│  auth_tool.go (包级函数)  │         │  client_auth_tool.go    │
│                         │         │  (结构体方法)             │
│  ◆ Login()              │         │  ◆ Login()               │
│  ◆ Logout()             │         │  ◆ Logout()              │
│  ◆ Kickout()            │         │  ◆ Kickout()             │
│  ◆ IsLogin()            │         │  ◆ IsLogin()             │
│  ◆ GetLoginID()         │         │  ◆ GetLoginID()          │
│                         │         │                         │
│  check_login.go          │         │  client_check_login.go   │
│  check_permission.go     │         │  client_check_permission.│
│  check_role.go           │         │  go                     │
│                         │         │  client_check_role.go    │
│  /api/v1/public/b/*      │         │                         │
│  /api/v1/b/*             │         │  /api/v1/public/c/*      │
│  /api/v1/sys/*           │         │  /api/v1/c/*             │
│                         │         │  /api/v1/client/*        │
│  → 后台管理员             │         │                         │
└─────────────────────────┘         │  → 普通用户               │
                                    └─────────────────────────┘
```

### B 端认证（BUSINESS）

使用包级函数实现，通过 `core/auth/auth_tool.go` 提供全局访问点：

- 用户：系统管理员、运营人员
- 认证方式：JWT 单 Token
- 会话存储：Redis，键前缀 `hei:auth:BUSINESS:token:`、`hei:auth:BUSINESS:session:`
- 权限控制：RBAC + 数据权限
- 公开接口：验证码、SM2 公钥、登录、注册

### C 端认证（CONSUMER）

使用结构体方法实现，通过 `core/auth/client_auth_tool.go` 的 `HeiClientAuthTool` 实例提供：

- 用户：前端普通用户
- 认证方式：JWT 单 Token
- 会话存储：Redis，键前缀 `hei:auth:CONSUMER:token:`、`hei:auth:CONSUMER:session:`
- 权限控制：基础权限校验
- 公开接口：验证码、SM2 公钥、登录、注册

## Redis 键结构

认证和权限系统使用规范的 Redis 键结构：

```
hei:auth:{BUSINESS|CONSUMER}:token:{token}       → JWT Token 数据
hei:auth:{BUSINESS|CONSUMER}:session:{userID}    → 用户会话集合（存多个 token）
hei:auth:{BUSINESS|CONSUMER}:disable:{loginID}   → 账号禁用标记
hei:permission:keys                              → 系统全部权限定义（JSON）
hei:dict:tree / hei:dict:fulltree                → 数据字典缓存
{BUSINESS|CONSUMER}:captcha:{captchaId}           → 验证码（300s TTL）
norepeat:{ip}:{userId}:{path}                    → 防重复提交指纹
```

## 中间件体系

Hei Gin 的中间件分为三个层次：

### 全局中间件（注册在 Gin Engine 上）

按注册顺序执行：

1. **Trace** - 链路追踪，生成/透传 X-Trace-Id
2. **AuthCheck** - 认证路由分流，根据路径前缀自动识别 B/C/Public
3. **Recovery** - 全局异常恢复，捕获 panic 返回友好错误
4. **CORS** - 跨域配置

### 业务中间件（注册在路由组上）

按需组合到各个路由组：

- **HeiCheckLogin** / **HeiClientCheckLogin** - JWT 登录验证
- **HeiCheckPermission** / **HeiClientCheckPermission** - 权限检查（同时自动注册权限）
- **HeiCheckRole** / **HeiClientCheckRole** - 角色检查
- **SysLog** - 操作日志录制
- **NoRepeat** - 防重复提交

### 内置中间件

Gin 框架自带的中间件：

- **Logger** - 请求日志
- **Recovery** - Gin 内置恢复（Hei Gin 使用自定义 Recovery 替代）

## 权限系统数据流

```
路由注册时（启动阶段）
    │
    ▼
Permission 中间件捕获路由信息
    │
    ▼
Permission 自动扫描 ──────────→ Redis 缓存权限列表
    │
    ▼
运行时请求到达
    │
    ▼
HeiCheckPermission 中间件
    ├─ 从 Redis 获取用户权限
    ├─ 权限匹配器匹配（支持 * 和 ** 通配符）
    ├─ 匹配成功 → 放行
    └─ 匹配失败 → 403 拒绝
```

## 异常处理机制

Hei Gin 使用 panic-based 的异常处理模式：

```
业务层
    │
    ├─ 正常逻辑 → 返回正常响应
    │
    └─ 异常逻辑 → panic(exception.NewBusinessError("错误描述", 400))
                      │
                      ▼
                Recovery 中间件捕获
                      │
                      ▼
                解析 BusinessError
                      │
                      ▼
                返回标准错误响应
                {code: 400, message: "错误描述", success: false}
                （HTTP 状态码始终为 200，错误码在 body 的 code 字段）
```

这种模式使得业务代码不需要层层 `if err != nil` 判断错误处理，异常流程统一由 Recovery 中间件管理。
