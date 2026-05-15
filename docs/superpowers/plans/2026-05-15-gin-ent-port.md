# Hei-Gin (Gin + Ent) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the entire hei-fastapi Python application to Go using Gin framework and Ent ORM, with identical API routes, business logic, and modular architecture.

**Architecture:** Layered modular architecture mirroring the FastAPI project — config layer, core framework (auth, middleware, DB, result, errors, utils), and business modules (sys backend + client frontend). Each module follows a consistent handler/service/params/dao pattern. Database access via Ent's generated type-safe ORM. Auth via JWT + Redis session management.

**Tech Stack:** Go 1.22+, Gin Web Framework, Ent ORM, go-redis, golang-jwt, MySQL, Redis, excelize (Excel import/export)

---

## File Structure

```
hei-gin/
├── main.go                     # Entry point
├── go.mod / go.sum
├── config.yaml                 # Default configuration
├── config/
│   └── config.go               # Configuration struct + loader
├── core/
│   ├── app.go                  # Gin app initialization
│   ├── router.go               # Route registration
│   ├── health.go               # Health check handler
│   ├── db/
│   │   ├── ent.go              # Ent client init
│   │   └── redis.go            # Redis client init
│   ├── auth/
│   │   ├── auth_tool.go        # JWT + Redis auth (B端)
│   │   ├── client_auth_tool.go # JWT + Redis auth (C端)
│   │   ├── permission_matcher.go   # Wildcard permission matching
│   │   ├── permission_tool.go      # Permission checking
│   │   ├── permission_interface.go # Permission DB interface
│   │   └── permission_scan.go      # Auto-discover permissions
│   ├── middleware/
│   │   ├── auth.go             # Auth middleware (ASGI-like path matching)
│   │   ├── cors.go             # CORS middleware
│   │   ├── recovery.go         # Exception recovery
│   │   └── trace.go            # Trace ID middleware
│   ├── captcha/
│   │   └── captcha.go          # Image captcha service
│   ├── constants/
│   │   └── constants.go        # Redis key prefixes, system fields
│   ├── enums/
│   │   └── enums.go            # All enum types
│   ├── errors/
│   │   └── business_error.go   # BusinessException equivalent
│   ├── result/
│   │   └── result.go           # success/failure/page_data response
│   └── utils/
│       ├── crypto.go           # SM2 encrypt/decrypt, bcrypt hash
│       ├── ip.go               # Client IP extraction
│       ├── model.go            # strip_system_fields, apply_update
│       ├── snowflake.go        # Snowflake ID generator
│       └── excel.go            # Excel export/import utilities (excelize)
│   ├── log/
│   │   └── decorator.go        # SysLog decorator equivalent (Gin middleware)
│   ├── norepeat/
│   │   └── norepeat.go         # NoRepeat duplicate submission prevention
├── ent/
│   ├── schema/                 # Ent schema definitions (one per table)
│   │   ├── sys_user.go
│   │   ├── sys_banner.go
│   │   ├── sys_config.go
│   │   ├── sys_dict.go
│   │   ├── sys_dict_data.go
│   │   ├── sys_file.go
│   │   ├── sys_group.go
│   │   ├── sys_home.go
│   │   ├── sys_log.go
│   │   ├── sys_notice.go
│   │   ├── sys_org.go
│   │   ├── sys_permission.go
│   │   ├── sys_position.go
│   │   ├── sys_resource.go
│   │   ├── sys_role.go
│   │   ├── rel_user_role.go
│   │   ├── rel_user_permission.go
│   │   ├── rel_role_permission.go
│   │   ├── rel_role_resource.go
│   │   ├── rel_org_role.go
│   │   ├── client_user.go
│   │   └── generate.go
│   └── (generated code)
├── modules/
│   ├── sys/
│   │   ├── auth/
│   │   │   ├── captcha.go      # GET /api/v1/public/b/captcha
│   │   │   ├── sm2.go          # GET /api/v1/public/b/sm2/public-key
│   │   │   ├── username.go     # POST login/register/logout
│   │   │   └── params.go       # Request/response types
│   │   ├── banner/
│   │   │   ├── api.go          # CRUD routes
│   │   │   ├── service.go      # Business logic
│   │   │   └── params.go       # VO, page params
│   │   ├── user/
│   │   │   ├── api.go          # User routes
│   │   │   ├── service.go      # User business logic
│   │   │   ├── dao.go          # User-specific queries
│   │   │   └── params.go       # VOs, params
│   │   ├── role/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── org/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── group/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── position/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── resource/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── permission/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── config/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── dict/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── file/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── log/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── notice/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   ├── dao.go
│   │   │   └── params.go
│   │   ├── session/
│   │   │   ├── api.go
│   │   │   ├── service.go
│   │   │   └── params.go
│   │   └── home/
│   │       ├── api.go
│   │       ├── service.go
│   │       ├── dao.go
│   │       └── params.go
│   └── client/
│       ├── auth/
│       │   ├── captcha.go      # GET /api/v1/public/c/captcha
│       │   ├── username.go     # POST login
│       │   └── params.go
│       ├── session/
│       │   ├── api.go
│       │   ├── service.go
│       │   └── params.go
│       └── user/
│           ├── api.go
│           ├── service.go
│           ├── dao.go
│           └── params.go
└── docs/
    └── superpowers/plans/
```

---

## Phase 1: Project Scaffold & Core Framework

### Task 1.1: Initialize Go module and install dependencies

**Files:**
- Create: `hei-gin/go.mod`
- Create: `hei-gin/config.yaml`

Initialize the Go module and install all required dependencies:
- `github.com/gin-gonic/gin` - Web framework
- `entgo.io/ent` - ORM
- `github.com/go-sql-driver/mysql` - MySQL driver
- `github.com/redis/go-redis/v9` - Redis client
- `github.com/golang-jwt/jwt/v5` - JWT
- `github.com/golang-jwt/jwt/v5` - JWT
- `github.com/google/uuid` - UUID generation
- `github.com/bwmarrin/snowflake` - Snowflake ID
- `golang.org/x/crypto` - bcrypt
- `gopkg.in/yaml.v3` - YAML config
- `github.com/xuri/excelize/v2` - Excel import/export
- `github.com/gin-contrib/cors` - CORS middleware
- `github.com/nfnt/resize` - Image processing (captcha) or `github.com/golang/freetype`

### Task 1.2: Create configuration

**Files:**
- Create: `hei-gin/config/config.go`
- Modify: `hei-gin/config.yaml`

```go
package config

import (
    "os"
    "gopkg.in/yaml.v3"
)

type Config struct {
    App      AppConfig      `yaml:"app"`
    DB       DatabaseConfig `yaml:"db"`
    Redis    RedisConfig    `yaml:"redis"`
    JWT      JWTConfig      `yaml:"jwt"`
    SM2      SM2Config      `yaml:"sm2"`
    CORS     CORSConfig     `yaml:"cors"`
    Snowflake SnowflakeConfig `yaml:"snowflake"`
}

type AppConfig struct {
    Name                string `yaml:"name"`
    Version             string `yaml:"version"`
    Debug               bool   `yaml:"debug"`
    Host                string `yaml:"host"`
    Port                int    `yaml:"port"`
    UploadMaxSize       int64  `yaml:"upload_max_size"`
    ImportMaxFileSizeMB int    `yaml:"import_max_file_size_mb"`
    TimeoutKeepAlive    int    `yaml:"timeout_keep_alive"`
}

type DatabaseConfig struct {
    Host          string `yaml:"host"`
    Port          int    `yaml:"port"`
    User          string `yaml:"user"`
    Password      string `yaml:"password"`
    Database      string `yaml:"database"`
    PoolSize      int    `yaml:"pool_size"`
    MaxOverflow   int    `yaml:"max_overflow"`
    PoolRecycle   int    `yaml:"pool_recycle"`
    PoolPrePing   bool   `yaml:"pool_pre_ping"`
    PoolTimeout   int    `yaml:"pool_timeout"`
    ConnectTimeout int   `yaml:"connect_timeout"`
    Echo          bool   `yaml:"echo"`
}

type RedisConfig struct {
    Host                 string `yaml:"host"`
    Port                 int    `yaml:"port"`
    Password             string `yaml:"password"`
    Database             int    `yaml:"database"`
    MaxConnections       int    `yaml:"max_connections"`
    SocketConnectTimeout int    `yaml:"socket_connect_timeout"`
    SocketTimeout        int    `yaml:"socket_timeout"`
    RetryOnTimeout       bool   `yaml:"retry_on_timeout"`
    HealthCheckInterval  int    `yaml:"health_check_interval"`
}

type JWTConfig struct {
    SecretKey     string `yaml:"secret_key"`
    Algorithm     string `yaml:"algorithm"`
    ExpireSeconds int    `yaml:"expire_seconds"`
    TokenName     string `yaml:"token_name"`
}

type SM2Config struct {
    PrivateKey string `yaml:"private_key"`
    PublicKey  string `yaml:"public_key"`
}

type CORSConfig struct {
    AllowOrigins     []string `yaml:"allow_origins"`
    AllowMethods     []string `yaml:"allow_methods"`
    AllowHeaders     []string `yaml:"allow_headers"`
    AllowCredentials bool     `yaml:"allow_credentials"`
}

type SnowflakeConfig struct {
    Instance int64 `yaml:"instance"`
}

var Global *Config

func Load(path string) error {
    data, err := os.ReadFile(path)
    if err != nil {
        return err
    }
    Global = &Config{}
    return yaml.Unmarshal(data, Global)
}

func init() {
    // Default config path
    _ = Load("config.yaml")
}
```

### Task 1.3: Database connection (Ent client + Redis)

**Files:**
- Create: `hei-gin/core/db/ent.go`
- Create: `hei-gin/core/db/redis.go`

```go
// core/db/ent.go
package db

import (
    "database/sql"
    "fmt"
    "time"
    
    "entgo.io/ent/dialect"
    entsql "entgo.io/ent/dialect/sql"
    "github.com/go-sql-driver/mysql"
    
    "hei-gin/config"
    "hei-gin/ent"
)

var Client *ent.Client

func InitEnt() error {
    cfg := config.Global.DB
    dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&parseTime=True&loc=Local",
        cfg.User, cfg.Password, cfg.Host, cfg.Port, cfg.Database)
    
    db, err := sql.Open("mysql", dsn)
    if err != nil {
        return fmt.Errorf("failed to open database: %w", err)
    }
    
    db.SetMaxOpenConns(cfg.PoolSize + cfg.MaxOverflow)
    db.SetMaxIdleConns(cfg.PoolSize)
    db.SetConnMaxLifetime(time.Duration(cfg.PoolRecycle) * time.Second)
    db.SetConnMaxIdleTime(time.Duration(cfg.PoolRecycle) * time.Second)
    
    drv := entsql.OpenDB(dialect.MySQL, db)
    Client = ent.NewClient(ent.Driver(drv))
    return nil
}

func Close() {
    if Client != nil {
        Client.Close()
    }
}
```

Redis implementation mirrors the FastAPI async redis setup but using go-redis v9:

```go
// core/db/redis.go
package db

import (
    "context"
    "fmt"
    "time"
    
    "github.com/redis/go-redis/v9"
    
    "hei-gin/config"
)

var Redis *redis.Client

func InitRedis() error {
    cfg := config.Global.Redis
    addr := fmt.Sprintf("%s:%d", cfg.Host, cfg.Port)
    
    Redis = redis.NewClient(&redis.Options{
        Addr:         addr,
        Password:     cfg.Password,
        DB:           cfg.Database,
        PoolSize:     cfg.MaxConnections,
        DialTimeout:  time.Duration(cfg.SocketConnectTimeout) * time.Second,
        ReadTimeout:  time.Duration(cfg.SocketTimeout) * time.Second,
        WriteTimeout: time.Duration(cfg.SocketTimeout) * time.Second,
    })
    
    ctx := context.Background()
    if err := Redis.Ping(ctx).Err(); err != nil {
        return fmt.Errorf("redis ping failed: %w", err)
    }
    return nil
}

func CloseRedis() {
    if Redis != nil {
        Redis.Close()
    }
}
```

### Task 1.4: Result package (standard API response — EXACTLY matching Python)

**Files:**
- Create: `hei-gin/core/result/result.go`

The response format must be **100% identical** to hei-fastapi:

Python response dict:
```python
def success(data=None):
    return {"code": 200, "message": "请求成功", "data": data, "success": True, "trace_id": trace_id}

def failure(message="请求参数格式错误", code=400, data=None):
    return {"code": code, "message": message, "data": data, "success": False, "trace_id": trace_id}
```

**Critical behavior:** ALL responses return HTTP 200 with the business status in `code` field. Even auth failures (401), validation errors (400), and server errors (500) return HTTP 200 with `{"code": 401, "success": false, ...}`. The HTTP status code is ALWAYS 200.

```go
package result

import (
    "net/http"
    
    "github.com/gin-gonic/gin"
)

type Response struct {
    Code    int         `json:"code"`
    Message string      `json:"message"`
    Data    interface{} `json:"data"`
    Success bool        `json:"success"`
    TraceID string      `json:"trace_id"`
}

type PageData struct {
    Records interface{} `json:"records"`
    Total   int64       `json:"total"`
    Page    int         `json:"page"`
    Size    int         `json:"size"`
    Pages   int         `json:"pages"`
}
```

### Task 1.5: Business error + enums

**Files:**
- Create: `hei-gin/core/errors/business_error.go`
- Create: `hei-gin/core/enums/enums.go`

```go
// core/errors/business_error.go
package errors

type BusinessError struct {
    Message string
    Code    int
}

func (e *BusinessError) Error() string {
    return e.Message
}

func NewBusinessError(message string, code ...int) *BusinessError {
    c := 400
    if len(code) > 0 {
        c = code[0]
    }
    return &BusinessError{Message: message, Code: c}
}
```

Enums file consolidates all Python enums:

```go
package enums

type LoginType string
const (
    LoginTypeBusiness LoginType = "BUSINESS"
    LoginTypeConsumer LoginType = "CONSUMER"
)

type Status string
const (
    StatusYes     Status = "YES"
    StatusNo      Status = "NO"
    StatusActive  Status = "ACTIVE"
    StatusEnabled Status = "ENABLED"
    StatusDisabled Status = "DISABLED"
)

type UserStatus string
const (
    UserStatusActive   UserStatus = "ACTIVE"
    UserStatusInactive UserStatus = "INACTIVE"
    UserStatusLocked   UserStatus = "LOCKED"
)

type ResourceType string
const (
    ResourceTypeDirectory    ResourceType = "DIRECTORY"
    ResourceTypeMenu         ResourceType = "MENU"
    ResourceTypeButton       ResourceType = "BUTTON"
    ResourceTypeInternalLink ResourceType = "INTERNAL_LINK"
    ResourceTypeExternalLink ResourceType = "EXTERNAL_LINK"
)

type ResourceCategory string
const (
    ResourceCategoryBackendMenu ResourceCategory = "BACKEND_MENU"
    ResourceCategoryFrontendMenu ResourceCategory = "FRONTEND_MENU"
    ResourceCategoryBackendButton ResourceCategory = "BACKEND_BUTTON"
    ResourceCategoryFrontendButton ResourceCategory = "FRONTEND_BUTTON"
)

type DataScope string
const (
    DataScopeAll          DataScope = "ALL"
    DataScopeSelf         DataScope = "SELF"
    DataScopeOrg          DataScope = "ORG"
    DataScopeOrgAndBelow  DataScope = "ORG_AND_BELOW"
    DataScopeCustomOrg    DataScope = "CUSTOM_ORG"
    DataScopeGroup        DataScope = "GROUP"
    DataScopeGroupAndBelow DataScope = "GROUP_AND_BELOW"
    DataScopeCustomGroup  DataScope = "CUSTOM_GROUP"
)

type PermissionCategory string
const (
    PermissionCategoryBackend  PermissionCategory = "BACKEND"
    PermissionCategoryFrontend PermissionCategory = "FRONTEND"
)

type PermissionPath string
const (
    PermissionPathDirect   PermissionPath = "P0"
    PermissionPathUserRole PermissionPath = "P1"
    PermissionPathOrgRole  PermissionPath = "P2"
)

type CheckMode string
const (
    CheckModeAND CheckMode = "AND"
    CheckModeOR  CheckMode = "OR"
)

type ExportType string
const (
    ExportTypeCurrent  ExportType = "current"
    ExportTypeSelected ExportType = "selected"
    ExportTypeAll      ExportType = "all"
)
```

### Task 1.6: Middleware (Trace, CORS, Recovery, Auth)

**Files:**
- Create: `hei-gin/core/middleware/trace.go`
- Create: `hei-gin/core/middleware/cors.go`
- Create: `hei-gin/core/middleware/recovery.go`
- Create: `hei-gin/core/middleware/auth.go`

**Trace middleware** - implements the same trace ID logic as FastAPI:
- Look for `X-Trace-Id` header (or constant equivalent), generate UUID if absent
- Store in gin context and make available via `result.GetTraceID()`

**CORS middleware** - Wraps gin's CORS handler with config values:

```go
package middleware

import (
    "time"
    
    "github.com/gin-contrib/cors"
    "github.com/gin-gonic/gin"
    
    "hei-gin/config"
)

func SetupCORS() gin.HandlerFunc {
    cfg := config.Global.CORS
    return cors.New(cors.Config{
        AllowOrigins:     cfg.AllowOrigins,
        AllowMethods:     cfg.AllowMethods,
        AllowHeaders:     cfg.AllowHeaders,
        AllowCredentials: cfg.AllowCredentials,
        MaxAge:           12 * time.Hour,
    })
}
```

**Recovery middleware** - Handles BusinessError with code/message, and panics with 500.
ALL responses return HTTP 200 (matching Python behavior), using the `code` field for business status:

```go
package middleware

import (
    "log"
    
    "github.com/gin-gonic/gin"
    
    bizerr "hei-gin/core/errors"
    "hei-gin/core/result"
)

func Recovery() gin.HandlerFunc {
    return func(c *gin.Context) {
        defer func() {
            if err := recover(); err != nil {
                if be, ok := err.(*bizerr.BusinessError); ok {
                    // Business errors → HTTP 200 with business code (e.g., 400)
                    c.JSON(200, result.Response{
                        Code:    be.Code,
                        Message: be.Message,
                        Data:    nil,
                        Success: false,
                        TraceID: result.GetTraceID(c),
                    })
                } else {
                    log.Printf("[PANIC] %v", err)
                    // Server errors → HTTP 200 with code 500
                    c.JSON(200, result.Response{
                        Code:    500,
                        Message: "服务器内部错误",
                        Data:    nil,
                        Success: false,
                        TraceID: result.GetTraceID(c),
                    })
                }
                c.Abort()
            }
        }()
        c.Next()
    }
}
```

**Auth middleware** - ASGI-level path matching (mirrors Python AuthMiddleware exactly).

**IMPORTANT:** All auth failures return HTTP 200 with `code=401` — NEVER return actual HTTP 401. This matches Python's `failure(message="Unauthorized", code=401)` returning 200.

Auth routing rules (priority order):
1. **Static paths** (`/favicon.ico`, `/docs`, `/swagger`) → skip auth
2. **OPTIONS** requests → skip auth
3. **Public paths** (`/api/v*/public/b/*` or `/api/v*/public/c/*`) → skip auth
4. **C端 paths** (`/api/v*/c/*`) → require C端 (consumer) JWT token verification
5. **DEFAULT (everything else)**: includes `/api/v*/b/*`, `/api/v*/sys/*`, `/api/v*/...` → require B端 (business) JWT token verification. This catch-all matches anything that is not `/api/v*/c/*` or public.
6. On failure: return HTTP 200 with `{"code": 401, "message": "Unauthorized", "success": false}`

### Task 1.7: App initialization + router setup

**Files:**
- Create: `hei-gin/core/app.go`
- Create: `hei-gin/core/router.go`
- Create: `hei-gin/core/health.go`

App initialization creates the Gin engine and registers global middleware (trace, cors, recovery, auth).

**Route registration rule (CRITICAL):**
Each module registers its own routes with **COMPLETE FULL PATHS** — no root prefix grouping. The router.go ONLY imports and registers module-level Gin router groups. Each module's api.go defines the full path:

```go
// ✅ CORRECT — modules/sys/banner/api.go
func RegisterRoutes(r *gin.RouterGroup) {
    r.GET("/api/v1/sys/banner/page", ...)
    r.POST("/api/v1/sys/banner/create", ...)
    // ...
}

// ❌ WRONG — do NOT do root-level prefix grouping
// r.Group("/api/v1/sys")  ← NEVER
```

This mirrors the FastAPI pattern where each `api.py` defines complete paths:
```python
@router.get("/api/v1/sys/banner/page", ...)
@router.post("/api/v1/sys/banner/create", ...)
```

The router.go imports all module register functions and calls them with `router.Group("/")` (root-level group).

**Full route registration example (module api.go):**

Each module's api.go registers routes with **all decorators as middleware chain**, mirroring the Python decorator stack order:

```go
// === modules/sys/banner/api.go ===
package banner

import (
    "github.com/gin-gonic/gin"
    "hei-gin/core/auth"       // CheckPermission middleware
    "hei-gin/core/log"        // SysLog middleware
    "hei-gin/core/norepeat"   // NoRepeat middleware
)

func RegisterRoutes(r *gin.RouterGroup) {
    // Python equivalent:
    // @router.get("/api/v1/sys/banner/page", summary="获取Banner分页")
    // @HeiCheckPermission("sys:banner:page")
    // async def page(...)
    r.GET("/api/v1/sys/banner/page",
        auth.CheckPermission("sys:banner:page"),
        handler.Page,
    )

    // Python equivalent:
    // @router.post("/api/v1/sys/banner/create")
    // @SysLog("添加Banner")
    // @HeiCheckPermission("sys:banner:create")
    // @NoRepeat(interval=3000)
    // async def create(...)
    r.POST("/api/v1/sys/banner/create",
        log.SysLog("添加Banner"),
        auth.CheckPermission("sys:banner:create"),
        norepeat.NoRepeat(3000),
        handler.Create,
    )

    // POST modify — SysLog + Permission
    r.POST("/api/v1/sys/banner/modify",
        log.SysLog("编辑Banner"),
        auth.CheckPermission("sys:banner:modify"),
        handler.Modify,
    )

    // POST remove — SysLog + Permission
    r.POST("/api/v1/sys/banner/remove",
        log.SysLog("删除Banner"),
        auth.CheckPermission("sys:banner:remove"),
        handler.Remove,
    )

    // GET detail — Permission only
    r.GET("/api/v1/sys/banner/detail",
        auth.CheckPermission("sys:banner:detail"),
        handler.Detail,
    )

    // GET export — SysLog + Permission
    r.GET("/api/v1/sys/banner/export",
        log.SysLog("导出Banner数据"),
        auth.CheckPermission("sys:banner:export"),
        handler.Export,
    )

    // GET template — Permission only
    r.GET("/api/v1/sys/banner/template",
        auth.CheckPermission("sys:banner:template"),
        handler.Template,
    )

    // POST import — SysLog + Permission + NoRepeat
    r.POST("/api/v1/sys/banner/import",
        log.SysLog("导入Banner数据"),
        auth.CheckPermission("sys:banner:import"),
        norepeat.NoRepeat(5000),
        handler.Import,
    )
}
```

**Decorator-to-Middleware Mapping:**

| Python Decorator | Go Middleware | Purpose |
|---|---|---|
| `@HeiCheckPermission("x:y:z")` | `auth.CheckPermission("x:y:z")` | Permission verification |
| `@HeiCheckLogin` | Global auth middleware (path-based) | Login verification |
| `@SysLog("名称")` | `log.SysLog("名称")` | Operation audit log |
| `@NoRepeat(3000)` | `norepeat.NoRepeat(3000)` | Duplicate prevention |

Note: `@HeiCheckLogin` is handled by the **global auth middleware** (Task 1.6), not per-route. The global middleware checks `/api/v*/c/*` for C端 token and everything else for B端 token. Per-route `CheckPermission` additionally checks the specific permission code against the user's RBAC permissions.

### Task 1.8: Auth tool (JWT + Redis)

**Files:**
- Create: `hei-gin/core/auth/auth_tool.go`
- Create: `hei-gin/core/auth/client_auth_tool.go`

Implement `HeiAuthTool` with all methods from `hei_auth_tool.py`:
- `Login(id, extra) -> token` - Create JWT, store in Redis (token→user_id, user_id→[tokens])
- `Logout()`, `Kickout(loginId)`, `KickoutToken(loginId, token)`
- `IsLogin(request) -> bool`, `GetLoginId(request) -> string`
- `GetTokenValue(request) -> string`, `GetTokenInfo(request) -> map`
- `GetExtra(key)` - Extract extra claims from JWT
- `RenewTimeout(timeout)` - Extend TTL
- `Disable(loginId, time)`, `IsDisable(loginId)`, `CheckDisable(loginId)`
- `GetTokenValuesByLoginId(loginId) -> []string`

Key Redis key patterns (mirroring Python constants):
```
hei:auth:BUSINESS:token:{token}  →  JSON token data
hei:auth:BUSINESS:session:{userId}  →  Set of tokens
hei:auth:BUSINESS:disable:{userId}  →  "1" (with TTL)
```

### Task 1.9: Permission system (middleware + scanning)

**Files:**
- Create: `hei-gin/core/auth/permission_matcher.go`
- Create: `hei-gin/core/auth/permission_tool.go`
- Create: `hei-gin/core/auth/permission_interface.go`
- Create: `hei-gin/core/auth/permission_scan.go`
- Create: `hei-gin/core/auth/middleware.go`  ← CheckPermission middleware

**CheckPermission Middleware** (`core/auth/middleware.go`):

This is the Go equivalent of Python's `@HeiCheckPermission("sys:banner:page")` decorator:

```go
package auth

import (
    "github.com/gin-gonic/gin"
    "hei-gin/core/result"
)

// permissionRouteRegistry stores all registered permission codes for auto-discovery.
// Key: "GET:/api/v1/sys/banner/page", Value: "sys:banner:page"
var permissionRouteRegistry = map[string]string{}

// CheckPermission returns a Gin middleware that verifies the user has the required permission.
// This is the Go equivalent of Python's @HeiCheckPermission("x:y:z") decorator.
func CheckPermission(code string) gin.HandlerFunc {
    return func(c *gin.Context) {
        fullKey := c.Request.Method + ":" + c.FullPath()
        permissionRouteRegistry[fullKey] = code
        
        loginType := DetectLoginType(c) // "BUSINESS" or "CONSUMER"
        if !PermissionTool.HasPermission(c, code, loginType) {
            result.Failure(c, "缺少权限: "+code, 403)
            c.Abort()
            return
        }
        c.Next()
    }
}

// DetectLoginType determines whether this route uses BUSINESS or CONSUMER auth
// by checking if the path starts with /api/v1/c/.
func DetectLoginType(c *gin.Context) string {
    // If path matches /api/v*/c/* → CONSUMER, else BUSINESS
    // ...
}
```

**PermissionMatcher** (`permission_matcher.go`):
- Wildcard matching (`*` = single level, `**` = multi level)
- Supports `:` / `.` / `/` separators (auto-detect)
- Methods: `Match(pattern, permission)`, `HasPermission(required, list)`, `HasPermissionAnd`, `HasPermissionOr`

**PermissionTool** (`permission_tool.go`):
- `GetPermissionList(c, loginType)` → loads user permissions from DB (via PermissionInterface)
- `GetRoleList(c, loginType)` → loads user roles
- `HasPermission(c, code, loginType)` → delegates to PermissionMatcher
- `HasPermissionAnd`, `HasPermissionOr`, `HasRole`, etc.
- SUPER_ADMIN gets ALL permissions

**PermissionInterface** (`permission_interface.go`):
- Runtime permission loader — queries DB per request
- Resolves 2 permission paths (matching Python's implementation exactly):
  - P0 (DIRECT): User → RelUserPermission
  - P1 (USER_ROLE): User → Role → RelRolePermission
- Scope resolution with priority merging (lower P value = higher priority)
- SUPER_ADMIN: returns all permission codes from Redis cache

**PermissionScan** (`permission_scan.go`):
- Called at app startup after all routes are registered
- Scans Gin's route table (`r.Routes()`) to discover all registered routes
- For each route, looks up the permission code from `permissionRouteRegistry`
- Groups by module (e.g., "sys:banner" from "sys:banner:page")
- Caches the grouped permission tree in Redis under `hei:permission:keys`
- Used by the Permission module UI to show/assign permissions

### Task 1.10: Constants

**Files:**
- Create: `hei-gin/core/constants/constants.go`

Constants mirror Python's `base_fields.py` and `cache_keys.py`:
- `BASE_SYSTEM_FIELDS` - `{"id", "created_at", "created_by", "updated_at", "updated_by"}`
- Redis key prefixes matching Python exactly

### Task 1.11: Excel Utility (import/export for all modules)

**Files:**
- Create: `hei-gin/core/utils/excel.go`

Implement Excel export and import using excelize, matching Python's `excel_utils.py`:
- `ExportExcel(data []map[string]interface{}, sheetName, title string) ([]byte, error)` - Generate Excel file bytes from data list
- `ParseExcel(fileBytes []byte, sheetName string) ([]map[string]string, error)` - Parse uploaded Excel to structured data
- `HandleImport(file, service, voType, importParamType, db, request)` - Generic import handler (reads Excel → validates → calls service.import_data)

Every CRUD module must include these 3 routes:
- `GET /api/v1/sys/{module}/export` - Export data as Excel download
- `GET /api/v1/sys/{module}/template` - Download import template Excel
- `POST /api/v1/sys/{module}/import` - Import data from uploaded Excel file

The export flow: query records → convert to VO → write Excel → return as file download (Content-Disposition: attachment).
The import flow: receive uploaded file → parse Excel → validate → batch insert.

### Task 1.12: NoRepeat (Duplicate Submission Prevention)

**Files:**
- Create: `hei-gin/core/norepeat/norepeat.go`

Gin middleware equivalent of Python's `@NoRepeat(interval=3000)` decorator:
- Key: `norepeat:{ip}:{userId}:{path}`
- Value: JSON `{hash, time}` of serialized request params
- If same hash within interval (ms), return BusinessError("请求过于频繁")
- Exclude `file` params from hash (multipart uploads)
- TTL: 3600s

Usage as Gin handler:
```go
router.POST("/api/v1/sys/banner/create", NoRepeat(3000), handler)
```

### Task 1.13: SysLog (Operation Logging)

**Files:**
- Create: `hei-gin/core/log/decorator.go`

Gin middleware equivalent of Python's `@SysLog("操作名称")` decorator:
- Intercepts request before handler
- Records start time, extracts request params
- After handler (or on panic), saves log entry to `sys_log` table
- Log fields: category(OPERATE/EXCEPTION), name, exe_status, exe_message, trace_id, op_ip, op_address, op_browser, op_os, class_name, method_name, req_method, req_url, param_json, result_json, op_time, op_user, sign_data
- Sign data is a HMAC signature of key log fields for tamper detection

Usage as Gin handler (wraps handler that returns result.Response):
```go
router.POST("/api/v1/sys/banner/create", SysLog("添加Banner"), NoRepeat(3000), handler)
```

### Task 1.14: Snowflake + Crypto + IP + Model utils

### Task 1.15: Main entry point

**Files:**
- Create: `hei-gin/main.go`

```go
package main

import (
    "fmt"
    "log"

    "hei-gin/config"
    "hei-gin/core"
    "hei-gin/core/db"
)

func main() {
    // Load config
    if err := config.Load("config.yaml"); err != nil {
        log.Fatalf("Failed to load config: %v", err)
    }

    // Init database
    if err := db.InitEnt(); err != nil {
        log.Fatalf("Failed to init database: %v", err)
    }
    defer db.Close()

    // Init Redis
    if err := db.InitRedis(); err != nil {
        log.Fatalf("Failed to init Redis: %v", err)
    }
    defer db.CloseRedis()

    // Create and start app
    app := core.CreateApp()
    addr := fmt.Sprintf("%s:%d", config.Global.App.Host, config.Global.App.Port)
    log.Printf("Server starting on %s", addr)
    app.Run(addr)
}
```

---

## Phase 2: Ent Schemas (All Database Tables)

All schemas use Snowflake IDs (string), UTC timestamps, and utf8mb4 character set — matching the Python models exactly.

### Task 2.1: Basic Ent schemas (sys_banner, sys_config, sys_dict, etc.)

Each schema follows the exact same field definitions as the SQLAlchemy models. Example:

```go
// ent/schema/sys_banner.go
package schema

import (
    "entgo.io/ent"
    "entgo.io/ent/schema/field"
    "entgo.io/ent/schema/index"
)

type SysBanner struct {
    ent.Schema
}

func (SysBanner) Fields() []ent.Field {
    return []ent.Field{
        field.String("id").MaxLen(32).Comment("主键"),
        field.String("title").MaxLen(255).Comment("轮播标题"),
        field.String("image").MaxLen(500).Comment("轮播图片"),
        field.String("category").MaxLen(32).Comment("轮播类别"),
        field.String("type").MaxLen(32).Comment("轮播类型"),
        field.String("position").MaxLen(32).Comment("展示位置"),
        field.String("url").MaxLen(500).Optional().Comment("跳转地址"),
        field.String("link_type").MaxLen(16).Optional().Default("URL").Comment("链接类型"),
        field.String("summary").MaxLen(500).Optional().Comment("轮播摘要"),
        field.Text("description").Optional().Comment("轮播描述"),
        field.Int("sort_code").Optional().Default(0).Comment("排序"),
        field.Int("view_count").Optional().Default(0).Comment("浏览次数"),
        field.Int("click_count").Optional().Default(0).Comment("点击次数"),
        field.Time("created_at").Optional().Comment("创建时间"),
        field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
        field.Time("updated_at").Optional().Comment("更新时间"),
        field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
    }
}

func (SysBanner) Edges() []ent.Edge {
    return nil
}
```

**All schemas to create (matching Python models exactly):**

| Schema | Table | Notes |
|--------|-------|-------|
| SysUser | sys_user | Account, password(bcrypt), profile fields, org/group/position refs |
| RelUserRole | rel_user_role | user_id, role_id, scope, custom_scope_group_ids |
| RelUserPermission | rel_user_permission | user_id, permission_code, scope, custom_scope_group/org_ids |
| SysBanner | sys_banner | Standard CRUD fields |
| SysConfig | sys_config | config_key, config_value, category, etc. |
| SysDict | sys_dict | dict type definition |
| SysDictData | sys_dict_data | dict key-value entries |
| SysFile | sys_file | File storage records |
| SysGroup | sys_group | User group with parent hierarchy |
| SysHome | sys_home | Home/dashboard config |
| SysLog | sys_log | Operation log records |
| SysNotice | sys_notice | Announcement |
| SysOrg | sys_org | Organization with parent hierarchy |
| SysPermission | sys_permission | Permission definitions |
| SysPosition | sys_position | Job position |
| SysResource | sys_resource | Menu/resources with parent hierarchy |
| SysRole | sys_role | Role definitions |
| RelRolePermission | rel_role_permission | role_id, permission_code, scope |
| RelRoleResource | rel_role_resource | role_id, resource_id |
| RelOrgRole | rel_org_role | org_id, role_id, scope |
| ClientUser | client_user | Client-side user model |

### Task 2.2: Generate Ent code

Run `go generate ./ent` to generate all Ent client code.

---

## Phase 3: Sys Auth Module

### Task 3.1: Captcha API

**Files:**
- Create: `hei-gin/modules/sys/auth/captcha.go`

Route: `GET /api/v1/public/b/captcha`
- Generate 4-char captcha image (PNG base64)
- Store code in Redis with key `BUSINESS:captcha:{uuid}`, TTL 5min
- Return `{captcha_base64, captcha_id, captcha_code(debug only)}`

### Task 3.2: SM2 Public Key API

**Files:**
- Create: `hei-gin/modules/sys/auth/sm2.go`

Routes:
- `GET /api/v1/public/b/sm2/public-key`
- `GET /api/v1/public/c/sm2/public-key`

Return the SM2 public key from config.

### Task 3.3: Username Auth (login/register/logout)

**Files:**
- Create: `hei-gin/modules/sys/auth/username.go`
- Create: `hei-gin/modules/sys/auth/params.go`

Routes:
- `POST /api/v1/public/b/login` - Username/password login with captcha validation
  - Decrypt password (SM2), verify bcrypt hash, check user status
  - Create JWT token via HeiAuthTool, record login info, log auth event
- `POST /api/v1/public/b/register` - User self-registration
  - Validate captcha, check duplicate username, create user with bcrypt password
- `POST /api/v1/b/logout` - User logout (requires login)
  - Clear token from Redis, log auth event

---

## Phase 4: Simple CRUD Modules

Each module follows the same pattern as the FastAPI version:
- **api.go** - Register routes, bind params, call service
- **service.go** - Business logic, extend BaseCrudService pattern
- **params.go** - Request/response structs (VO, PageParam, ExportParam, ImportParam)

Every CRUD module MUST include these 8 standard routes:
| Method | Path | Middleware Chain | Description |
|--------|------|-----------------|-------------|
| GET | `/api/v1/sys/{module}/page` | `CheckPermission("sys:{module}:page")` | Paginated list |
| POST | `/api/v1/sys/{module}/create` | `SysLog("xxx")` + `CheckPermission("sys:{module}:create")` + `NoRepeat(3000)` | Create |
| POST | `/api/v1/sys/{module}/modify` | `SysLog("xxx")` + `CheckPermission("sys:{module}:modify")` | Update |
| POST | `/api/v1/sys/{module}/remove` | `SysLog("xxx")` + `CheckPermission("sys:{module}:remove")` | Delete |
| GET | `/api/v1/sys/{module}/detail` | `CheckPermission("sys:{module}:detail")` | Get by ID |
| GET | `/api/v1/sys/{module}/export` | `SysLog("xxx")` + `CheckPermission("sys:{module}:export")` | Export Excel |
| GET | `/api/v1/sys/{module}/template` | `CheckPermission("sys:{module}:template")` | Download import template |
| POST | `/api/v1/sys/{module}/import` | `SysLog("xxx")` + `CheckPermission("sys:{module}:import")` + `NoRepeat(5000)` | Import from Excel |

The export/template/import routes follow this exact pattern:
- **export**: Accepts `ExportParam` (export_type, current, size, selected_ids), queries data accordingly, returns Excel file as download
- **template**: Generates Excel with column headers (excluding system fields), returns as download
- **import**: Receives uploaded Excel file, parses via `HandleImport`, validates, batch inserts

### Task 4.1: Banner Module

**Files:**
- Create: `hei-gin/modules/sys/banner/api.go`
- Create: `hei-gin/modules/sys/banner/service.go`
- Create: `hei-gin/modules/sys/banner/params.go`

Routes: All 8 standard CRUD routes listed above.
Uses Ent's generated CRUD directly in service (no separate DAO needed for simple modules).

### Task 4.2: Config Module

**Files:**
- Create: `hei-gin/modules/sys/config/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes
- Config-specific:
  - `GET /api/v1/sys/config/query-by-category` - Query by category (no pagination)
  - `GET /api/v1/sys/config/query-by-key` - Query by config key (single value)
- DAO needed for QueryWrapper-style dynamic queries (category, key filters)

### Task 4.3: Dict Module

**Files:**
- Create: `hei-gin/modules/sys/dict/api.go`, `service.go`, `dao.go`, `params.go`
- Ent schemas: SysDict, SysDictData
- Routes: All 8 standard CRUD routes, plus:
  - `GET /api/v1/sys/dict/treeselect` - Tree structure of dict categories
  - `GET /api/v1/sys/dict/dict-detail` - Get dict data items by dict type
- Implements tree structure building from flat list (parent-based hierarchy)
- Uses DAO for dict-data specific queries (by type/category)

### Task 4.4: Notice Module

**Files:**
- Create: `hei-gin/modules/sys/notice/api.go`, `service.go`, `params.go`
- Routes: All 8 standard CRUD routes

### Task 4.5: Group Module

**Files:**
- Create: `hei-gin/modules/sys/group/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes + `GET /api/v1/sys/group/treeselect`
- Tree structure with parent_id hierarchy

### Task 4.6: Position Module

**Files:**
- Create: `hei-gin/modules/sys/position/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes

### Task 4.7: Home Module

**Files:**
- Create: `hei-gin/modules/sys/home/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes

---

## Phase 5: Complex Modules

### Task 5.1: Org Module

**Files:**
- Create: `hei-gin/modules/sys/org/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes + tree structure
- Additional routes:
  - `GET /api/v1/sys/org/treeselect` - Tree structure
  - `GET /api/v1/sys/org/own-roles` - Get org's role IDs
  - `POST /api/v1/sys/org/grant-role` - Assign roles to org

### Task 5.2: Role Module

**Files:**
- Create: `hei-gin/modules/sys/role/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes
- Additional routes:
  - `GET /api/v1/sys/role/own-resources` - Get role's resource IDs
  - `POST /api/v1/sys/role/grant-resource` - Assign resources to role
  - `GET /api/v1/sys/role/own-permissions` - Get role's permission codes
  - `POST /api/v1/sys/role/grant-permission` - Assign permissions to role
  - `GET /api/v1/sys/role/own-users` - Users assigned this role

### Task 5.3: Resource Module

**Files:**
- Create: `hei-gin/modules/sys/resource/api.go`, `service.go`, `dao.go`, `params.go`
- Routes: All 8 standard CRUD routes + tree structure
- Additional routes:
  - `GET /api/v1/sys/resource/treeselect` - Tree structure
  - `GET /api/v1/sys/resource/build-bootstrap-menus` - Build menu tree for frontend

### Task 5.4: Permission Module

**Files:**
- Create: `hei-gin/modules/sys/permission/api.go`, `service.go`, `dao.go`, `params.go`
- Routes:
  - `GET /api/v1/sys/permission/get-modules` - Get distinct module prefixes from Redis cache
  - `GET /api/v1/sys/permission/get-permissions-by-module` - Get permissions under a module
  - `POST /api/v1/sys/permission/refresh-cache` - Re-scan route permissions
  - `GET /api/v1/sys/permission/detail` - Get permission detail
  - `POST /api/v1/sys/permission/grant` - Grant permission to role/user
  - `POST /api/v1/sys/permission/modify-scope` - Modify data scope

### Task 5.5: User Module (Most Complex)

This is by far the most complex module with 18 routes total. Must handle cross-table queries, RBAC filtering, password hashing, and menu tree building.

**Files:**
- Create: `hei-gin/modules/sys/user/api.go`, `service.go`, `dao.go`, `params.go`

**Standard routes (all 8 CRUD):**
- `GET /api/v1/sys/user/page` - Paginated with keyword/status search + batch enrich org/group/position names
- `POST /api/v1/sys/user/create` - Create with role/group assignments
- `POST /api/v1/sys/user/modify` - Update with role/group sync
- `POST /api/v1/sys/user/remove` - Cascade delete user roles/permissions
- `GET /api/v1/sys/user/detail` - Detail with org/group/position names + role IDs
- `GET /api/v1/sys/user/export` - Export Excel
- `GET /api/v1/sys/user/template` - Download import template
- `POST /api/v1/sys/user/import` - Import from Excel

**Specific routes:**
- `POST /api/v1/sys/user/grant-role` - Assign roles to user
- `POST /api/v1/sys/user/grant-permission` - Direct permission grant
- `GET /api/v1/sys/user/own-permission-detail` - User's permission details with scope
- `GET /api/v1/sys/user/own-roles` - User's role IDs
- `GET /api/v1/sys/user/current` - Current user profile
- `GET /api/v1/sys/user/menus` - Current user's menu tree (RBAC filtered)
- `GET /api/v1/sys/user/permissions` - Current user's permission codes
- `POST /api/v1/sys/user/update-profile` - Update current user profile
- `POST /api/v1/sys/user/update-avatar` - Update avatar (base64)
- `POST /api/v1/sys/user/update-password` - Change password

The user service must handle:
  - N+1 prevention: batch resolve org/group/position names for page queries
  - Role assignment cascade (delete old RelUserRole, insert new)
  - SUPER_ADMIN gets all menus/permissions
  - Menu tree building from flat resource list
  - Cross-table queries: user → role → resource, user → org → role → resource

---

## Phase 6: Remaining Sys Modules

### Task 6.1: Log Module

**Files:**
- Create: `hei-gin/modules/sys/log/api.go`, `service.go`, `dao.go`, `params.go`

Routes:
- `GET /api/v1/sys/log/page` - Paginated log query (with keyword filters)
- `GET /api/v1/sys/log/detail` - Log detail
- `POST /api/v1/sys/log/remove` - Log delete
- `POST /api/v1/sys/log/clean` - Clean all logs
- `GET /api/v1/sys/log/export` - Export Excel
- `GET /api/v1/sys/log/template` - Download import template (optional)
- `POST /api/v1/sys/log/import` - Import from Excel (optional)

### Task 6.2: Session Module

**Files:**
- Create: `hei-gin/modules/sys/session/api.go`, `service.go`, `params.go`

Routes:
- `GET /api/v1/sys/session/page` - Paginated active sessions (from Redis)
- `POST /api/v1/sys/session/kickout` - Force logout user
- No import/export needed (session data is ephemeral from Redis)

### Task 6.3: File Module

**Files:**
- Create: `hei-gin/modules/sys/file/api.go`, `service.go`, `dao.go`, `params.go`

Routes:
- `POST /api/v1/sys/file/upload` - File upload (stores metadata + saves file)
- `GET /api/v1/sys/file/page` - File list (paginated)
- `POST /api/v1/sys/file/remove` - File delete
- No import/export needed (files are uploaded individually)

### Task 6.4: Analyze Module

**Files:**
- Create: `hei-gin/modules/sys/analyze/api.go`, `service.go`, `dao.go`, `params.go`

Routes:
- `GET /api/v1/sys/analyze/dashboard` - Dashboard statistics (counts of users, roles, orgs, etc.)

---

## Phase 7: Client (C端) Modules

### Task 7.1: Client Auth

**Files:**
- Create: `hei-gin/modules/client/auth/captcha.go`
- Create: `hei-gin/modules/client/auth/username.go`
- Create: `hei-gin/modules/client/auth/params.go`

Routes:
- `GET /api/v1/public/c/captcha` - Captcha (using c_captcha prefix)
- `POST /api/v1/public/c/login` - Client user login (username/password + captcha)
- `POST /api/v1/c/logout` - Client logout

### Task 7.2: Client Session

**Files:**
- Create: `hei-gin/modules/client/session/api.go`, `service.go`, `params.go`

Routes:
- `GET /api/v1/c/session/page` - Active client sessions
- `POST /api/v1/c/session/kickout` - Force logout client user

### Task 7.3: Client User

**Files:**
- Create: `hei-gin/modules/client/user/api.go`, `service.go`, `dao.go`, `params.go`
- Ent schema: ClientUser

Routes:
- `GET /api/v1/c/user/page` - Paginated (with export support)
- `POST /api/v1/c/user/create` - Create
- `POST /api/v1/c/user/modify` - Update
- `POST /api/v1/c/user/remove` - Delete
- `GET /api/v1/c/user/detail` - Detail
- `GET /api/v1/c/user/export` - Export Excel
- `GET /api/v1/c/user/template` - Download import template
- `POST /api/v1/c/user/import` - Import from Excel
- `POST /api/v1/c/user/update-profile` - Update profile
- `POST /api/v1/c/user/update-avatar` - Update avatar
- `POST /api/v1/c/user/update-password` - Change password

---

## Route Summary (All APIs)

| Method | Path | Auth | Module |
|--------|------|------|--------|
| GET | / | None | Health |
| GET | /api/v1/public/b/captcha | None | Sys Auth |
| GET | /api/v1/public/b/sm2/public-key | None | Sys Auth |
| POST | /api/v1/public/b/login | None | Sys Auth |
| POST | /api/v1/public/b/register | None | Sys Auth |
| POST | /api/v1/b/logout | B端 | Sys Auth |
| GET | /api/v1/public/c/captcha | None | Client Auth |
| GET | /api/v1/public/c/sm2/public-key | None | Client Auth |
| POST | /api/v1/public/c/login | None | Client Auth |
| POST | /api/v1/c/logout | C端 | Client Auth |
| GET/POST | /api/v1/sys/banner/* | Permission | Sys Banner |
| GET/POST | /api/v1/sys/config/* | Permission | Sys Config |
| GET/POST | /api/v1/sys/dict/* | Permission | Sys Dict |
| GET/POST | /api/v1/sys/file/* | Permission | Sys File |
| GET/POST | /api/v1/sys/group/* | Permission | Sys Group |
| GET/POST | /api/v1/sys/home/* | Permission | Sys Home |
| GET/POST | /api/v1/sys/log/* | Permission | Sys Log |
| GET/POST | /api/v1/sys/notice/* | Permission | Sys Notice |
| GET/POST | /api/v1/sys/org/* | Permission | Sys Org |
| GET/POST | /api/v1/sys/permission/* | Permission | Sys Permission |
| GET/POST | /api/v1/sys/position/* | Permission | Sys Position |
| GET/POST | /api/v1/sys/resource/* | Permission | Sys Resource |
| GET/POST | /api/v1/sys/role/* | Permission | Sys Role |
| GET/POST | /api/v1/sys/session/* | Permission | Sys Session |
| GET/POST | /api/v1/sys/user/* | Permission | Sys User |
| GET/POST | /api/v1/sys/analyze/* | Permission | Sys Analyze |
| GET/POST | /api/v1/c/session/* | C端 | Client Session |
| GET/POST | /api/v1/c/user/* | Permission | Client User |
