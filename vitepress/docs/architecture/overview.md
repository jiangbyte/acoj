# 架构概述

## 应用初始化流程

Hei FastAPI 的应用启动流程由 `core/app/setup.py` 中的应用工厂（`create_app` 函数）编排，遵循清晰的初始化顺序：

```
Config 加载（.env → Pydantic Settings）
    │
    ▼
FastAPI 应用实例创建
    │
    ▼
中间件注册（顺序决定执行链）
    ├─ CORS 中间件
    ├─ TraceMiddleware（链路追踪）
    └─ AuthMiddleware（路径分流）
    │
    ▼
全局异常处理器注册
    ├─ BusinessException → HTTP 200（body code=400）
    ├─ HTTPException    → 透传状态码（body code=401/403/404）
    ├─ RequestValidationError → HTTP 200（body code=400）
    └─ 通用 Exception   → HTTP 200（body code=500）
    │
    ▼
所有路由器注册（B 端 / C 端 / 公开）
    │
    ▼
Startup 生命周期
    ├─ MySQL 连接验证
    ├─ Redis 连接初始化
    ├─ SM2 国密工具初始化
    ├─ Token 认证工具初始化（B 端 + C 端）
    ├─ 权限接口注册
    ├─ 验证码服务初始化（B 端 + C 端）
    └─ 权限自动扫描 → Redis 缓存
    │
    ▼
Uvicorn 启动 ──── 监听端口，提供服务
```

## 请求生命周期

一个完整的 HTTP 请求在 Hei FastAPI 中的处理流程如下：

```
客户端请求
    │
    ▼
┌──────────────────────────────────────────────┐
│           FastAPI 应用                         │
│                                               │
│  ① CORS 中间件                                │
│     ├─ 处理 OPTIONS 预检请求                   │
│     └─ 添加跨域响应头                          │
│                                               │
│  ② TraceMiddleware                           │
│     ├─ 生成/透传 trace_id                      │
│     └─ 设置到 ContextVar                      │
│                                               │
│  ③ AuthMiddleware（ASGI 原始中间件）           │
│     ├─ 解析请求路径前缀                        │
│     ├─ OPTIONS 请求 → 放行                    │
│     ├─ /api/v{n}/public/b/* → 放行（B 端公开）  │
│     ├─ /api/v{n}/public/c/* → 放行（C 端公开）  │
│     ├─ /api/v{n}/c/* → C 端认证检查            │
│     ├─ /api/v{n}/b/* → B 端认证检查            │
│     └─ 其他 /api/v{n}/* → B 端认证检查（默认）  │
│                                               │
│  ④ 路由匹配                                   │
│     ├─ 匹配对应模块的 Handler                  │
│     └─ 执行依赖注入（get_db, 等）              │
│                                               │
│  ⑤ 装饰器链（从外到内执行）                    │
│     ├─ @HeiCheckLogin → Token 令牌验证          │
│     ├─ @HeiCheckPermission → 权限检查          │
│     ├─ @SysLog → 操作日志录制开始              │
│     └─ @NoRepeat → 防重复提交检查              │
│                                               │
│  ⑥ 业务 Handler                               │
│     ├─ 参数校验（Pydantic 自动）               │
│     ├─ Controller → Service → DAO             │
│     ├─ 业务逻辑处理                            │
│     └─ 统一响应返回                            │
│                                               │
└──────────────────────────────────────────────┘
    │
    ▼
客户端收到响应
```

## 双端认证架构

Hei FastAPI 实现了完全隔离的双端认证体系：

```
                      ┌──────────────────────┐
                      │    客户端请求          │
                      └──────────┬───────────┘
                                 │
                                 ▼
                      ┌──────────────────────┐
                      │  AuthMiddleware       │
                      │  基于路径自动分流      │
                      └──────┬──────────┬─────┘
                             │          │
              ┌──────────────┘          └──────────────┐
              ▼                                         ▼
┌─────────────────────────┐         ┌─────────────────────────┐
│     B 端 (BUSINESS)      │         │     C 端 (CONSUMER)      │
│                         │         │                         │
│  HeiAuthTool (类方法)    │         │  HeiClientAuthTool      │
│                         │         │  (独立实例)              │
│  ◆ login()              │         │  ◆ login()              │
│  ◆ logout()             │         │  ◆ logout()             │
│  ◆ kickout()            │         │  ◆ kickout()            │
│  ◆ isLogin()            │         │  ◆ isLogin()            │
│  ◆ getLoginId()         │         │  ◆ getLoginId()         │
│                         │         │                         │
│  @HeiCheckLogin         │         │  @HeiClientCheckLogin   │
│  @HeiCheckPermission    │         │  @HeiClientCheckPerm    │
│  @HeiCheckRole          │         │  @HeiClientCheckRole    │
│                         │         │                         │
│  /api/v1/public/b/*     │         │  /api/v1/public/c/*     │
│  /api/v1/b/*            │         │  /api/v1/c/*            │
│  /api/v1/sys/* 等       │         │                       │
│                         │         │                         │
│  → 后台管理员            │         │  → 普通用户             │
└─────────────────────────┘         └─────────────────────────┘
```

### B 端认证（BUSINESS）

通过 `HeiAuthTool` 类方法提供全局访问点：

- 用户：系统管理员、运营人员
- 认证方式：Token（随机字符串，Redis 会话）
- 会话存储：Redis，键前缀 `hei:auth:BUSINESS:`
- 权限控制：RBAC + 数据权限
- 公开接口：验证码、SM2 公钥、登录、注册

### C 端认证（CONSUMER）

通过 `HeiClientAuthTool` 独立实例提供：

- 用户：前端普通用户
- 认证方式：Token（单一 Token）
- 会话存储：Redis，键前缀 `hei:auth:CONSUMER:`
- 权限控制：基础权限校验
- 公开接口：验证码、SM2 公钥、登录、注册

## Redis 键结构

认证和权限系统使用规范的 Redis 键结构：

```
hei:auth:{BUSINESS|CONSUMER}:token:{token_value}            → 会话数据
hei:auth:{BUSINESS|CONSUMER}:session:{user_id}              → 用户 Token 列表（Set）
hei:auth:{BUSINESS|CONSUMER}:disable:{user_id}              → 禁用标记
hei:permission:keys                                         → 权限定义缓存
BUSINESS:captcha:{captcha_id}                               → B 端验证码
CONSUMER:captcha:{captcha_id}                               → C 端验证码
norepeat:{ip}:{user_id}:{path}                              → 防重复提交
```

## 权限系统数据流

```
路由注册时（启动阶段）
    │
    ▼
@HeiCheckPermission 装饰器捕获路由信息
    │
    ▼
权限自动扫描 ──────────→ Redis 缓存权限定义列表
    │
    ▼
运行时请求到达
    │
    ▼
@HeiCheckPermission 装饰器
    ├─ 从 Redis 获取用户权限
    ├─ 权限匹配器匹配（支持 * 和 ** 通配符）
    ├─ 匹配成功 → 放行
    └─ 匹配失败 → 403 拒绝
```

## 异常处理机制

Hei FastAPI 使用全局异常处理器进行异常管理：

```
业务层
    │
    ├─ 正常逻辑 → 返回 success() 响应
    │
    └─ 异常逻辑 → raise BusinessException(code, message)
                      │
                      ▼
                全局异常处理器捕获
                      │
                      ├─ BusinessException → HTTP 200（body code=400）
                      ├─ HTTPException    → 透传状态码（body code=401/403/404）
                      ├─ RequestValidationError → HTTP 200（body code=400）
                      └─ 通用 Exception   → HTTP 200（body code=500）
                      │
                      ▼
                返回标准错误响应
                {code: 4xx/5xx, message: "错误信息", success: false, trace_id: "..."}
```
