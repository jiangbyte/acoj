# hei-gin 插件化重构方案

> 参考 Snowy 的插件式设计哲学，结合 Go 语言特性，将 hei-gin 从"单模块 + 文件夹分模块"改造为"多模块 workspace + SPI 接口驱动"的插件化架构。

---

## 目录

- [1. 现状分析](#1-现状分析)
- [2. 总体架构](#2-总体架构)
- [3. 插件化框架设计（核心）](#3-插件化框架设计核心)
  - [3.1 插件元数据 PluginInfo](#31-插件元数据-plugininfo)
  - [3.2 插件接口 Plugin](#32-插件接口-plugin)
  - [3.3 插件管理器 PluginManager](#33-插件管理器-pluginmanager)
  - [3.4 生命周期管理](#34-生命周期管理)
  - [3.5 依赖解析与拓扑排序](#35-依赖解析与拓扑排序)
  - [3.6 SPI 注册中心 Registry](#36-spi-注册中心-registry)
  - [3.7 事件总线 EventBus](#37-事件总线-eventbus)
  - [3.8 扩展点 Extension](#38-扩展点-extension)
- [4. 插件通信模式](#4-插件通信模式)
- [5. 目录结构](#5-目录结构)
- [6. 各模块职责与依赖关系](#6-各模块职责与依赖关系)
- [7. go.mod 与 go.work 配置](#7-gomod-与-gowork-配置)
- [8. 关键难题：依赖倒置](#8-关键难题依赖倒置)
- [9. API 接口清单](#9-api-接口清单)
- [10. 配置文件规格](#10-配置文件规格)
- [11. 实施步骤](#11-实施步骤)
- [12. 与 Snowy 架构对比](#12-与-snowy-架构对比)

---

## 1. 现状分析

### 当前目录结构

```
hei-gin/
├── go.mod                     # 单模块：所有代码一个 module
├── main.go                    # 入口
├── core/                      # 框架核心（混杂基础设施 + 业务框架）
│   ├── app/                   #   Bootstrap
│   ├── auth/                  #   鉴权引擎（含反向依赖业务表）
│   ├── captcha/               #   验证码
│   ├── db/                    #   数据库
│   ├── middleware/            #   Gin 中间件
│   ├── registry/              #   注册中心
│   ├── module/                #   模块生命周期
│   ├── ws/                    #   WebSocket Hub
│   └── ...                    #   utils, constants, enums...
├── modules/                   # 业务模块（但非独立编译单元）
│   ├── sys/                   #   系统管理（user, role, org...）
│   └── client/                #   C端管理
├── config/                    # 配置定义
└── cmd/                       # CLI 工具
```

### 当前架构问题

| 问题 | 说明 |
|------|------|
| **单模块耦合** | 所有代码一个 `go.mod`，无法独立编译/选择性加载 |
| **依赖倒置缺失** | `core/auth/permission_interface.go` 反向依赖 `modules/sys/user` |
| **硬编码模块列表** | `core/app/modules_gen.go` 通过 blank import 硬编码所有模块 |
| **核心与业务混杂** | `core/` 里既有 db/middleware 等基础设施，又有 auth/captcha 等业务框架 |
| **无 SPI 契约层** | 模块间直接 import，无接口隔离层 |
| **无事件通信** | 模块间无解耦的事件机制 |
| **无扩展点机制** | 无法在某个功能点插入多个实现 |
| **无插件元数据** | 无版本、依赖、作者等描述信息 |

---

## 2. 总体架构

```
┌─────────────────────────────────────────────────────────────────┐
│  app/ 组装层                                                     │
│  通过 go.mod require 选中插件，通过 import 触发插件 init() 注册   │
└──────────────────────┬──────────────────────────────────────────┘
                       │
          ┌────────────┼────────────┬────────────┬────────────┐
          ▼            ▼            ▼            ▼            ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
   │plugin-sys│ │plugin-   │ │plugin-   │ │plugin-im │ │   ...    │
   │          │ │auth      │ │client    │ │          │ │ 更多插件  │
   └─────┬────┘ └─────┬────┘ └─────┬────┘ └─────┬────┘ └──────────┘
         │            │            │            │
         │    ┌───────┴────────────┴────────────┘
         │    │    运行时通过 GetAPI[T]() 互相调用
         ▼    ▼
   ┌─────────────────────────────────────────────────────────────┐
   │  sdk/ 框架层                                                  │
   │  plugin/ (PluginManager, Registry, EventBus, Extension)     │
   │  db/, auth/, middleware/, ws/, scheduler/ ...               │
   └──────────────────────┬──────────────────────────────────────┘
                          │
   ┌──────────────────────▼──────────────────────────────────────┐
   │  api/ 契约层                                                  │
   │  零外部依赖，只定义 interface + VO/DTO                       │
   └─────────────────────────────────────────────────────────────┘
```

---

## 3. 插件化框架设计（核心）

这是本次重构的核心：**`sdk/plugin/`** 包提供完整的插件化基础设施。

### 3.1 插件元数据 PluginInfo

每个插件携带元数据描述自身，类似 Maven 的 `pom.xml` 中的插件信息：

```go
// sdk/plugin/plugin.go

type PluginInfo struct {
    Name         string   `json:"name"`         // 插件唯一标识，如 "plugin-sys"
    Version      string   `json:"version"`      // 语义化版本，如 "1.0.0"
    Description  string   `json:"description"`  // 功能描述
    Dependencies []string `json:"dependencies"` // 依赖的其他插件名称列表，如 ["plugin-sys"]
    Author       string   `json:"author"`       // 作者
    Enabled      bool     `json:"enabled"`      // 是否启用
    Order        int      `json:"order"`        // 初始化优先级（小值优先）
}
```

### 3.2 插件接口 Plugin

```go
// sdk/plugin/plugin.go

// Plugin 是每个插件必须实现的接口
type Plugin interface {
    // Info 返回插件元数据
    Info() PluginInfo

    // Init 插件初始化，此时 DB、Redis 已就绪，HTTP 服务未启动
    // 用于：注册 SPI 实现、初始化自己的资源、建表迁移
    Init() error

    // Start 在 HTTP 服务启动后调用
    // 用于：启动后台协程、订阅事件、定时任务
    Start() error

    // Stop 在服务优雅关闭时调用
    // 用于：释放资源、关闭连接
    Stop() error
}

// DefaultPlugin 提供默认实现，插件可内嵌以减少代码
type DefaultPlugin struct{}

func (DefaultPlugin) Init() error  { return nil }
func (DefaultPlugin) Start() error { return nil }
func (DefaultPlugin) Stop() error  { return nil }
```

### 3.3 插件管理器 PluginManager

```go
// sdk/plugin/manager.go

type PluginManager struct {
    plugins map[string]Plugin          // 所有已注册的插件
    infos   map[string]PluginInfo      // 插件元数据（含从配置合并后的状态）
    order   []string                   // 拓扑排序后的启动顺序
    mu      sync.RWMutex
}

var globalManager = &PluginManager{
    plugins: make(map[string]Plugin),
    infos:   make(map[string]PluginInfo),
}

// Register 由插件的 init() 调用，注册自身到管理器
// 此时只是登记，尚未 Init
func Register(plugin Plugin) { ... }

// InitAll 按拓扑顺序 Init 所有插件
func InitAll() error { ... }

// StartAll 按拓扑顺序 Start 所有插件
func StartAll() { ... }

// StopAll 按拓扑逆序 Stop 所有插件
func StopAll() { ... }

// GetPlugin 按名称获取已注册的插件实例
func GetPlugin(name string) Plugin { ... }

// IsEnabled 判断插件是否启用（结合配置判断）
func IsEnabled(name string) bool { ... }

// SetEnabled 运行时启用/禁用插件
func SetEnabled(name string, enabled bool) { ... }
```

### 3.4 生命周期管理

```
应用启动时序：
  1. 加载配置（config.yaml）
  2. 初始化 DB、Redis
  3. 插件 init() 被触发（通过 blank import）
     → 每个插件的 init() 调用 plugin.Register(self)
  4. PluginManager.InitAll()
     → 依赖拓扑排序
     → 按序调用每个插件的 Init()
     → 插件在 Init() 中注册自己的 SPI 实现
     → 插件在 Init() 中执行数据库迁移
  5. 初始化 WebSocket Hub
  6. 注册 HTTP 路由
  7. PluginManager.StartAll()
     → 按序调用每个插件的 Start()
     → 启动后台任务、订阅事件
  8. HTTP 服务启动

应用关闭时序：
  1. 接收 SIGINT/SIGTERM
  2. HTTP 服务优雅关闭（不再接受新请求）
  3. PluginManager.StopAll()
     → 按逆序调用每个插件的 Stop()
  4. 关闭 DB、Redis 连接
  5. 退出
```

### 3.5 依赖解析与拓扑排序

插件通过 `PluginInfo.Dependencies` 声明依赖，PluginManager 自动解析：

```go
// sdk/plugin/dag.go

// resolveOrder 对已注册的插件进行拓扑排序
// 规则：A 依赖 B，则 B 先 Init，A 后 Init
// 检测循环依赖，返回错误
func (pm *PluginManager) resolveOrder() ([]string, error) {
    // 1. 构建有向无环图
    // 2. Kahn 算法拓扑排序
    // 3. 检测循环依赖
    // 4. 返回有序的插件名称列表
}
```

依赖示例：

```
plugin-auth:
  Dependencies: ["plugin-sys"]
  // auth 需要查询用户，所以依赖 plugin-sys

plugin-client:
  Dependencies: ["plugin-sys"]
  // client 需要访问系统组织架构

plugin-im:
  Dependencies: ["plugin-sys"]
  // im 需要查询用户信息

plugin-sys:
  Dependencies: []  // 无依赖，最先 Init
```

### 3.6 SPI 注册中心 Registry

这是插件间通信的核心机制，类型安全的泛型服务注册表：

```go
// sdk/plugin/registry.go

package plugin

import (
    "fmt"
    "reflect"
    "sync"
)

var registry sync.Map

// RegisterAPI 注册一个 SPI 接口实现
// T 必须是接口类型，impl 必须实现了 T
// 泛型保证编译期类型安全
func RegisterAPI[T any](impl T) {
    var zero T
    key := typeKey(zero)
    registry.Store(key, impl)
}

// GetAPI 获取一个 SPI 接口的实现
// 返回 nil 表示没有插件注册该接口
// 调用方需要判空
func GetAPI[T any]() T {
    var zero T
    key := typeKey(zero)
    if v, ok := registry.Load(key); ok {
        return v.(T)
    }
    return zero
}

// HasAPI 检查某个 SPI 接口是否有注册实现
func HasAPI[T any]() bool {
    var zero T
    key := typeKey(zero)
    _, ok := registry.Load(key)
    return ok
}

// GetAPIs 获取某个 SPI 接口的所有注册实现（用于多实现的扩展点）
func GetAPIs[T any]() []T {
    var zero T
    key := typeKey(zero)
    var result []T
    registry.Range(func(k, v any) bool {
        if k == key {
            result = append(result, v.(T))
        }
        return true
    })
    return result
}

// typeKey 生成接口类型的唯一字符串键
func typeKey(v any) string {
    t := reflect.TypeOf(v)
    // 去掉指针
    for t.Kind() == reflect.Ptr {
        t = t.Elem()
    }
    return fmt.Sprintf("%s.%s", t.PkgPath(), t.Name())
}
```

**使用示例：**

```go
// plugins/plugin-sys/provider/user_provider.go
func init() {
    plugin.RegisterAPI[api.UserAPI](&UserProvider{})
}

// plugins/plugin-auth/username/service.go
func Login(c *gin.Context, p *LoginParam) {
    userAPI := plugin.GetAPI[api.UserAPI]()
    if userAPI == nil {
        panic("plugin-sys not loaded, UserAPI unavailable")
    }
    user := userAPI.Detail(c, uid)
    // ...
}
```

### 3.7 事件总线 EventBus

插件之间通过事件解耦通信，类似 Snowy 的 `CommonDataChangeEventCenter`：

```go
// sdk/plugin/eventbus.go

package plugin

import (
    "log"
    "sync"
)

// Event 事件载体
type Event struct {
    Type    string      // 事件类型，如 "user.created", "role.changed"
    Payload any         // 事件数据
    Sender  string      // 发送者插件名
}

// EventHandler 事件处理函数
type EventHandler func(Event)

// EventBus 事件总线
type EventBus struct {
    handlers map[string][]EventHandler
    mu       sync.RWMutex
    // 可选：异步 channel
    async    chan Event
}

var globalBus = &EventBus{
    handlers: make(map[string][]EventHandler),
    async:    make(chan Event, 100),
}

// Subscribe 订阅某类事件
// handler 会在事件发布时被同步调用
func Subscribe(eventType string, handler EventHandler) {
    globalBus.mu.Lock()
    defer globalBus.mu.Unlock()
    globalBus.handlers[eventType] = append(globalBus.handlers[eventType], handler)
}

// SubscribeAsync 异步订阅事件
// handler 在独立 goroutine 中执行，不会阻塞发布者
func SubscribeAsync(eventType string, handler EventHandler) { ... }

// Publish 发布事件，同步调用所有已订阅的 handler
func Publish(eventType string, payload any) {
    globalBus.mu.RLock()
    handlers := globalBus.handlers[eventType]
    globalBus.mu.RUnlock()

    event := Event{Type: eventType, Payload: payload, Sender: "unknown"}
    for _, h := range handlers {
        func() {
            defer func() {
                if r := recover(); r != nil {
                    log.Printf("[EventBus] handler panic: %v", r)
                }
            }()
            h(event)
        }()
    }
}

// PublishAsync 异步发布事件，不阻塞调用者
func PublishAsync(eventType string, payload any) {
    globalBus.async <- Event{Type: eventType, Payload: payload}
}
```

**Snowy 对比：**

```java
// Snowy 的做法（Java）
CommonDataChangeEventCenter.registerListener(listener);
CommonDataChangeEventCenter.doAddWithData("dict", "add", dictId);

// hei-gin 的做法（Go）
plugin.Subscribe("dict:updated", func(e plugin.Event) {
    dict := e.Payload.(*DictVO)
    cache.ClearDict(dict.Code)
})
plugin.Publish("dict:updated", dictVO)
```

### 3.8 扩展点 Extension

支持一个接口多个实现（如多种登录方式、多种存储驱动）：

```go
// sdk/plugin/extension.go

// ExtensionPoint 扩展点定义
type ExtensionPoint[T any] struct {
    Name    string
    Impls   []T
}

// RegisterExtension 注册一个扩展实现
func RegisterExtension[T any](name string, impl T) { ... }

// GetExtensions 获取某个扩展点的所有实现
func GetExtensions[T any](name string) []T { ... }

// 使用示例：多种登录方式
// plugin.RegisterExtension[api.LoginMethod]("password", &PasswordLogin{})
// plugin.RegisterExtension[api.LoginMethod]("sms", &SMSLogin{})
// plugin.RegisterExtension[api.LoginMethod]("sm2", &SM2Login{})

// func LoginHandler(c *gin.Context) {
//     methods := plugin.GetExtensions[api.LoginMethod]("auth.login")
//     for _, m := range methods {
//         if m.Match(c) {
//             m.Login(c)
//             return
//         }
//     }
// }
```

---

## 4. 插件通信模式

Snowy 的插件通信有三种模式，he-gin 一一对应：

### 模式一：SPI 接口调用（一对一）

```
plugin-auth ──GetAPI[UserAPI]()──→ plugin-sys

适用：一个功能只有一个实现（如用户查询）
```

### 模式二：扩展点（一对多）

```
plugin-auth ──GetExtensions[LoginMethod]──→ [PasswordLogin, SMSLogin, SM2Login...]

适用：一个功能点有多种实现方式
```

### 模式三：事件发布/订阅

```
plugin-sys 发布 "user.created" ──EventBus──→ plugin-auth 订阅处理

适用：解耦通知场景（如创建用户后发通知）
```

### 三种模式的完整示例

```go
// plugins/plugin-sys/provider/user_provider.go
package provider

import (
    "hei-gin/api"
    "hei-gin/sdk/plugin"
)

type UserProvider struct{}

func (p *UserProvider) Detail(id string) *api.UserVO {
    // 查询数据库...
    return &api.UserVO{ID: id, Name: "张三"}
}

func (p *UserProvider) Create(user *api.UserVO) error {
    // 创建用户...
    // 发布事件，通知其他插件
    plugin.Publish("user:created", user)
    return nil
}

func init() {
    // 注册 SPI 实现
    plugin.RegisterAPI[api.UserAPI](&UserProvider{})
}
```

```go
// plugins/plugin-auth/username/service.go
package username

import (
    "hei-gin/api"
    "hei-gin/sdk/plugin"
)

func Login(account, password string) (string, error) {
    // 1. 通过 SPI 调用 plugin-sys
    userAPI := plugin.GetAPI[api.UserAPI]()
    if userAPI == nil {
        return "", errors.New("user service unavailable")
    }
    user := userAPI.DetailByAccount(account)
    // ...验证密码...
    return token, nil
}

func init() {
    // 2. 订阅事件
    plugin.Subscribe("user:created", func(e plugin.Event) {
        user := e.Payload.(*api.UserVO)
        log.Printf("New user created: %s, sending welcome notification...", user.ID)
    })

    // 3. 注册为登录方式扩展
    plugin.RegisterExtension[api.LoginMethod]("auth.login", &PasswordLogin{})
}
```

---

## 5. 目录结构

```
hei-gin/
├── go.work                         # workspace: use 所有子模块
│
├── sdk/                            # 📦 [module: hei-gin/sdk]
│   ├── go.mod                      # 框架所有第三方依赖
│   ├── plugin/                     # ★ 插件框架（本方案第 3 章全部内容）
│   │   ├── plugin.go               #   Plugin interface, PluginInfo, DefaultPlugin
│   │   ├── manager.go              #   PluginManager（注册、生命周期、查询）
│   │   ├── registry.go             #   泛型 SPI 注册中心
│   │   ├── eventbus.go             #   事件总线
│   │   ├── extension.go            #   扩展点（一对多实现）
│   │   └── dag.go                  #   依赖解析 + 拓扑排序
│   ├── app/                        #   应用启动引导
│   ├── db/                         #   GORM + Redis
│   ├── auth/                       #   鉴权引擎（token管理、权限扫描）
│   │   ├── base_auth.go
│   │   ├── permission_interface.go #   ★ 改造后：只留委托，移除业务表查询
│   │   ├── permission_scan.go
│   │   ├── permission_matcher.go
│   │   ├── module.go
│   │   └── middleware/
│   ├── middleware/                 #   Gin 中间件
│   ├── captcha/                    #   验证码引擎
│   ├── storage/                    #   文件存储抽象
│   ├── ws/                         #   WebSocket Hub
│   ├── scheduler/                  #   定时任务引擎
│   ├── log/                        #   日志系统
│   ├── result/                     #   统一返回
│   ├── exception/                  #   业务异常
│   └── utils/                      #   工具 + 常量 + 枚举
│       ├── constants/
│       ├── enums/
│       ├── pojo/
│       └── *.go
│
├── api/                            # 📦 [module: hei-gin/api] ★ SPI 契约层
│   ├── go.mod                      # 零外部依赖
│   ├── auth/                       # AuthAPI
│   ├── sys/                        # UserAPI, RoleAPI, OrgAPI, PermissionAPI...
│   ├── client/                     # ClientUserAPI...
│   └── im/                         # IMAPI...
│
├── plugins/                        # 📦 业务插件
│   ├── plugin-sys/                 # [module: hei-gin/plugins/plugin-sys]
│   │   ├── go.mod
│   │   ├── plugin.go               # Plugin struct + init() 注册
│   │   ├── provider/               # SPI 实现注册
│   │   ├── user/
│   │   ├── role/
│   │   ├── org/
│   │   ├── permission/
│   │   ├── resource/
│   │   ├── menu/
│   │   ├── dict/
│   │   ├── config/
│   │   ├── file/
│   │   ├── group/
│   │   ├── banner/
│   │   ├── notice/
│   │   ├── message/
│   │   ├── log/
│   │   ├── analyze/
│   │   ├── session/
│   │   └── home/
│   │
│   ├── plugin-auth/                # [module: hei-gin/plugins/plugin-auth]
│   │   ├── go.mod
│   │   ├── plugin.go
│   │   ├── provider/
│   │   ├── username/
│   │   ├── captcha/
│   │   ├── sm2/
│   │   └── sso/
│   │
│   ├── plugin-client/              # [module: hei-gin/plugins/plugin-client]
│   │   ├── go.mod
│   │   ├── plugin.go
│   │   ├── provider/
│   │   ├── user/
│   │   ├── auth/
│   │   ├── session/
│   │   └── message/
│   │
│   └── plugin-im/                  # [module: hei-gin/plugins/plugin-im]
│       ├── go.mod
│       ├── plugin.go
│       ├── provider/
│       ├── handler/
│       └── room/
│
├── app/                            # 📦 [module: hei-gin/app] 组装层
│   ├── go.mod
│   ├── main.go                     # import 选中插件 → 触发 init()
│   └── config.yaml
│
├── config/                         # 配置定义
│   └── config.go
├── config.yaml
├── config.example.yaml
└── cmd/codegen/
```

---

## 6. 各模块职责与依赖关系

### 6.1 依赖关系图

```
app (组装点)
  │
  ├── import → plugin-sys ──→ sdk ──→ api
  │              │
  ├── import → plugin-auth ──→ sdk ──→ api
  │              │              │
  ├── import → plugin-client ─→ sdk ──→ api
  │              │
  └── import → plugin-im ────→ sdk ──→ api
                   │
        运行时查询 GetAPI[T]() ── 不通过 go.mod 依赖
```

### 6.2 插件详细划分

#### plugin-sys（系统管理插件）

| 来源 | 提供 SPI | 依赖 | 说明 |
|------|----------|------|------|
| `modules/sys/user` 等 | `UserAPI`, `RoleAPI`, `OrgAPI`, `PermissionAPI`, `MenuAPI`, `DictAPI`, `ConfigAPI`, `FileAPI`, `GroupAPI`, `BannerAPI`, `NoticeAPI`, `MessageAPI`, `LogAPI`, `AnalyzeAPI`, `SessionAPI` | 无其他插件 | 最基础插件 |

**plugin.go：**
```go
package pluginsys

import "hei-gin/sdk/plugin"

type SysPlugin struct{ plugin.DefaultPlugin }

func (p *SysPlugin) Info() plugin.PluginInfo {
    return plugin.PluginInfo{
        Name:        "plugin-sys",
        Version:     "1.0.0",
        Description: "系统管理：用户、角色、组织、权限等基础功能",
        Dependencies: []string{},
        Order:       10,
    }
}

func (p *SysPlugin) Init() error {
    // 注册所有 SPI 实现
    plugin.RegisterAPI[api.UserAPI](&provider.UserProvider{})
    plugin.RegisterAPI[api.RoleAPI](&provider.RoleProvider{})
    plugin.RegisterAPI[api.PermissionAPI](&provider.PermissionProvider{})
    // 数据库迁移
    // ...
    return nil
}

func init() { plugin.Register(&SysPlugin{}) }
```

#### plugin-auth（认证鉴权插件）

| 来源 | 提供 SPI | 调用其他插件 SPI | 依赖声明 |
|------|----------|-----------------|---------|
| `modules/sys/auth/*` + `modules/client/auth/*` | `AuthAPI` | `UserAPI`, `PermissionAPI` | `["plugin-sys"]` |

```go
package pluginauth

type AuthPlugin struct{ plugin.DefaultPlugin }

func (p *AuthPlugin) Info() plugin.PluginInfo {
    return plugin.PluginInfo{
        Name:         "plugin-auth",
        Version:      "1.0.0",
        Description:  "认证鉴权：登录、注册、SSO",
        Dependencies: []string{"plugin-sys"},  // 运行时需要 UserAPI
        Order:        20,
    }
}

func (p *AuthPlugin) Init() error {
    plugin.RegisterAPI[api.AuthAPI](&provider.AuthProvider{})
    plugin.RegisterExtension[api.LoginMethod]("auth.login", &username.PasswordLogin{})
    plugin.RegisterExtension[api.LoginMethod]("auth.login", &captcha.CaptchaLogin{})
    return nil
}
```

#### plugin-client（C端管理插件）

| 来源 | 提供 SPI | 调用其他插件 SPI | 依赖声明 |
|------|----------|-----------------|---------|
| `modules/client/*`（不含 auth/ws） | `ClientUserAPI` | `UserAPI` | `["plugin-sys"]` |

#### plugin-im（即时通讯插件）

| 来源 | 提供 SPI | 调用其他插件 SPI | 依赖声明 |
|------|----------|-----------------|---------|
| `modules/sys/ws` + `modules/client/ws` | `IMAPI` | `UserAPI` | `["plugin-sys"]` |

---

## 7. go.mod 与 go.work 配置

### `sdk/go.mod`

```
module hei-gin/sdk
go 1.25.10
require (
    github.com/gin-gonic/gin v1.12.0
    github.com/redis/go-redis/v9 v9.19.0
    github.com/gin-contrib/cors v1.7.7
    github.com/robfig/cron/v3 v3.0.1
    github.com/gorilla/websocket v1.5.3
    github.com/bwmarrin/snowflake v0.3.0
    github.com/google/uuid v1.6.0
    github.com/minio/minio-go/v7 v7.1.0
    github.com/aws/aws-sdk-go-v2 ...
    golang.org/x/crypto ...
    golang.org/x/image ...
    gopkg.in/yaml.v3 v3.0.1
    gorm.io/gorm ...
    gorm.io/driver/mysql ...
)
```

### `api/go.mod`

```
module hei-gin/api
go 1.25.10
// 零外部依赖，只定义 interface 和 VO/DTO
```

### `plugins/plugin-sys/go.mod`

```
module hei-gin/plugins/plugin-sys
go 1.25.10
require (
    hei-gin/sdk v0.0.0
    hei-gin/api v0.0.0
    gorm.io/gorm ...
)
replace (
    hei-gin/sdk => ../../sdk
    hei-gin/api => ../../api
)
```

### `plugins/plugin-auth/go.mod`

```
module hei-gin/plugins/plugin-auth
go 1.25.10
require (
    hei-gin/sdk v0.0.0
    hei-gin/api v0.0.0
)
// ★ 不 require plugin-sys！只通过 api 接口运行时调用
replace (
    hei-gin/sdk => ../../sdk
    hei-gin/api => ../../api
)
```

### `app/go.mod`

```
module hei-gin/app
go 1.25.10
require (
    hei-gin/sdk v0.0.0
    hei-gin/plugins/plugin-sys v0.0.0
    hei-gin/plugins/plugin-auth v0.0.0
    hei-gin/plugins/plugin-client v0.0.0
    hei-gin/plugins/plugin-im v0.0.0
)
replace (
    hei-gin/sdk => ../sdk
    hei-gin/api => ../api
    hei-gin/plugins/plugin-sys => ../plugins/plugin-sys
    hei-gin/plugins/plugin-auth => ../plugins/plugin-auth
    hei-gin/plugins/plugin-client => ../plugins/plugin-client
    hei-gin/plugins/plugin-im => ../plugins/plugin-im
)
```

### `go.work`

```
go 1.25.10

use (
    ./sdk
    ./api
    ./plugins/plugin-sys
    ./plugins/plugin-auth
    ./plugins/plugin-client
    ./plugins/plugin-im
    ./app
)
```

---

## 8. 关键难题：依赖倒置

### 8.1 问题描述

当前最大的架构异味：

```go
// core/auth/permission_interface.go
package auth

import (
    userModel "hei-gin/modules/sys/user"  // ← 框架层反向依赖业务层
    roleModel "hei-gin/modules/sys/role"   // ← 框架层反向依赖业务层
)

type HeiPermissionInterfaceImpl struct{}

func (p *HeiPermissionInterfaceImpl) GetPermissionList(loginID, loginType string) {
    // 直接查询 sys_user, sys_role, sys_role_permission 等业务表
    var entities []userModel.RelUserRole
    db.DB.Where("user_id = ?", loginID).Find(&entities)
}
```

### 8.2 解决方案：接口抽取 + SPI 注册

```
┌────────────────────────────────────────────────────────────┐
│ api/sys/permission.go (接口定义，零依赖)                     │
│   PermissionAPI interface { GetPermissionList(...) }       │
└──────────────────────────┬─────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           ▼                               ▼
┌──────────────────────┐   ┌──────────────────────────────────┐
│ sdk/auth/ (委托调用)   │   │ plugin-sys/provider/ (实现)      │
│ HasPermission()  →    │   │ PermissionProvider               │
│  plugin.GetAPI        │   │ GetPermissionList() {            │
│    [PermissionAPI]()  │   │   // 直接查询 user/role 表       │
│  .GetPermissionList() │   │ }                                │
└──────────────────────┘   └──────────────────────────────────┘
```

**第一步：在 `api/sys/permission.go` 定义接口**

```go
package api

type PermissionAPI interface {
    GetPermissionList(loginID string, loginType string) ([]string, error)
    GetRoleList(loginID string, loginType string) ([]string, error)
    GetPermissionScopeMap(loginID string, loginType string) (map[string]ScopeInfo, error)
}

type ScopeInfo struct {
    GroupScope     string   `json:"group_scope"`
    OrgScope       string   `json:"org_scope"`
    CustomGroupIDs []string `json:"custom_group_ids"`
    CustomOrgIDs   []string `json:"custom_org_ids"`
}
```

**第二步：`sdk/auth/permission_interface.go` 改为委托**

```go
package auth

import (
    "hei-gin/api"
    "hei-gin/sdk/plugin"
)

// HasPermission 委托给注册的 PermissionAPI 实现
func HasPermission(loginID string, loginType string, codes []string) bool {
    permAPI := plugin.GetAPI[api.PermissionAPI]()
    if permAPI == nil {
        return false
    }
    perms, _ := permAPI.GetPermissionList(loginID, loginType)
    return permission_matcher.MatchAny(perms, codes)
}
```

**第三步：`plugin-sys/provider/permission_provider.go` 提供实现**

```go
// plugins/plugin-sys/provider/permission_provider.go
package provider

import (
    "hei-gin/api"
    "hei-gin/sdk/plugin"
    userModel "hei-gin/plugins/plugin-sys/user"
    roleModel "hei-gin/plugins/plugin-sys/role"
)

type PermissionProvider struct{}

func (p *PermissionProvider) GetPermissionList(loginID, loginType string) ([]string, error) {
    // 原来 HeiPermissionInterfaceImpl.GetPermissionList 的完整逻辑
    // 直接使用本模块内的 userModel, roleModel，不依赖任何其他插件
}

func init() {
    plugin.RegisterAPI[api.PermissionAPI](&PermissionProvider{})
}
```

### 8.3 `log` 的反向依赖处理

类似问题：`core/log/syslog.go` 和 `core/log/record.go` 引用了 `modules/sys/log`。

**解决方案：**
- `sdk/log/` 只定义日志记录接口和工具函数，不依赖业务表
- 日志记录的实际存储逻辑在 `plugin-sys/log/` 中
- 通过事件总线：`sdk/log.Record(event)` → EventBus → `plugin-sys` 订阅处理

```go
// sdk/log/record.go
package log

import "hei-gin/sdk/plugin"

type LogEntry struct {
    Type    string
    Content string
    UserID  string
    // ...
}

// Record 记录日志，通过事件总线通知业务插件持久化
func Record(entry LogEntry) {
    plugin.PublishAsync("log:record", entry)
}

// plugins/plugin-sys/log/handler.go
func init() {
    plugin.Subscribe("log:record", func(e plugin.Event) {
        entry := e.Payload.(log.LogEntry)
        // 写入 sys_log 表
    })
}
```

---

## 9. API 接口清单

### `api/auth/auth.go`

```go
package api

type AuthAPI interface {
    Login(account, password, validCode, validCodeReqNo string) (token string, err error)
    Logout(token string) error
    GetLoginUser(token string) (*LoginUserVO, error)
    GetCaptchaOpen() bool
}

type LoginUserVO struct {
    ID       string   `json:"id"`
    Username string   `json:"username"`
    Avatar   string   `json:"avatar"`
    Roles    []string `json:"roles"`
    Perms    []string `json:"perms"`
}

// LoginMethod 扩展点：多种登录方式
type LoginMethod interface {
    Type() string                    // "password", "sms", "sm2"
    Match(c *gin.Context) bool       // 是否匹配当前请求
    Login(c *gin.Context) (string, error)  // 执行登录
}
```

### `api/sys/user.go`

```go
package api

type UserAPI interface {
    Detail(ctx context.Context, id string) (*UserVO, error)
    DetailByAccount(ctx context.Context, account string) (*UserVO, error)
    Page(ctx context.Context, param *UserPageParam) (*PageResult, error)
    Create(ctx context.Context, user *UserVO, operatorID string) error
    Update(ctx context.Context, user *UserVO, operatorID string) error
    Delete(ctx context.Context, ids []string) error
    UpdatePassword(ctx context.Context, id, oldPwd, newPwd string) error
    // 权限相关
    GetRoleIDs(ctx context.Context, userID string) ([]string, error)
    GetPermissionCodes(ctx context.Context, userID string) ([]string, error)
}
```

### `api/sys/permission.go`

```go
package api

type PermissionAPI interface {
    GetPermissionList(loginID string, loginType string) ([]string, error)
    GetRoleList(loginID string, loginType string) ([]string, error)
    GetPermissionScopeMap(loginID string, loginType string) (map[string]ScopeInfo, error)
}
```

### `api/sys/role.go`

```go
package api

type RoleAPI interface {
    List(ctx context.Context) ([]*RoleVO, error)
    Detail(ctx context.Context, id string) (*RoleVO, error)
    Create(ctx context.Context, role *RoleVO) error
    Update(ctx context.Context, role *RoleVO) error
    Delete(ctx context.Context, ids []string) error
}
```

### `api/sys/org.go`

```go
package api

type OrgAPI interface {
    Tree(ctx context.Context) ([]*OrgVO, error)
    Detail(ctx context.Context, id string) (*OrgVO, error)
    Create(ctx context.Context, org *OrgVO) error
    Update(ctx context.Context, org *OrgVO) error
    Delete(ctx context.Context, ids []string) error
}
```

### `api/client/user.go`

```go
package api

type ClientUserAPI interface {
    Current(ctx context.Context) (*ClientUserVO, error)
    UpdateProfile(ctx context.Context, vo *ClientUserVO) error
    Register(ctx context.Context, vo *RegisterVO) error
}
```

### `api/im/api.go`

```go
package api

type IMAPI interface {
    SendMessage(ctx context.Context, from, to, content string) error
    Broadcast(ctx context.Context, room string, msg *MessageVO) error
    JoinRoom(ctx context.Context, userID, room string) error
    LeaveRoom(ctx context.Context, userID, room string) error
    GetRoomMembers(ctx context.Context, room string) ([]string, error)
}

type MessageVO struct {
    ID        string    `json:"id"`
    From      string    `json:"from"`
    To        string    `json:"to"`
    Content   string    `json:"content"`
    Room      string    `json:"room"`
    Timestamp time.Time `json:"timestamp"`
}
```

---

## 10. 配置文件规格

```yaml
# app/config.yaml
app:
  host: "0.0.0.0"
  port: 8080

db:
  host: "127.0.0.1"
  port: 3306
  database: "hei-gin"
  # ...

redis:
  addr: "127.0.0.1:6379"
  # ...

token:
  expire_seconds: 86400
  token_name: "Authorization"

ws:
  heartbeat_interval: 15
  pong_timeout: 10
  instance_ttl: 60

# ★ 插件配置
plugins:
  # 启用/禁用控制
  enabled:
    - plugin-sys
    - plugin-auth
    - plugin-client
    - plugin-im

  # 每个插件的独立配置
  plugin-sys:
    enabled: true
    # 插件自定义配置...

  plugin-auth:
    enabled: true
    captcha:
      enabled: true
    sso:
      enabled: false

  plugin-im:
    enabled: true
    max_room_size: 100

  plugin-client:
    enabled: true
```

---

## 11. 实施步骤

### Phase A：框架层重构（不动业务代码）

#### A1：创建 `sdk/` + `api/` 目录结构和 go.mod

- `mkdir -p sdk/{app,db,auth/middleware,...,plugin/api,utils/{constants,enums,pojo}}`
- `mkdir -p api/{auth,sys,client,im}`
- 创建 `sdk/go.mod`、`api/go.mod`

#### A2：实现 `sdk/plugin/` 插件框架核心

按以下顺序创建 5 个文件：

1. `sdk/plugin/plugin.go` — `PluginInfo`, `Plugin` interface, `DefaultPlugin`
2. `sdk/plugin/registry.go` — 泛型 `RegisterAPI[T]`, `GetAPI[T]`, `GetAPIs[T]`
3. `sdk/plugin/extension.go` — `RegisterExtension[T]`, `GetExtensions[T]`
4. `sdk/plugin/eventbus.go` — `Subscribe`, `Publish`, async channel
5. `sdk/plugin/dag.go` — 拓扑排序
6. `sdk/plugin/manager.go` — `PluginManager`, `Register`, `InitAll`, `StartAll`, `StopAll`

#### A3：复制 core/ → sdk/ + 替换 import 路径

- `cp -r core/* sdk/`
- 递归替换 `hei-gin/core` → `hei-gin/sdk`
- 递归替换 `hei-gin/modules` → `hei-gin/plugins`（暂存引用，后续指向各插件）

#### A4：改造 `sdk/auth/permission_interface.go`（核心解耦）

- 剥离 `HeiPermissionInterfaceImpl` 实现到暂存文件
- 改为 `plugin.GetAPI[api.PermissionAPI]()` 委托模式
- 保留 `HasPermission`, `HasRole`, `PermissionDelegate` 等对外函数

#### A5：改造 `sdk/log/` 解耦

- `sdk/log/` 只保留工具函数和类型定义
- 日志持久化通过 EventBus 异步通知

#### A6：改造 `sdk/app/` 使用 PluginManager

- `app.go` 不再直接 blank import `core/auth`, `core/captcha` 等
- 改为调用 `plugin.InitAll()` → `plugin.StartAll()` → `plugin.StopAll()`

#### A7：移除 `core/` 目录，`go build ./sdk/...` 验证

---

### Phase B：插件化迁移（业务代码搬家 + 适配）

#### B1：`plugins/plugin-sys/`

- 创建 `go.mod`、`plugin.go`
- 从 `modules/sys/` 搬迁所有子包
- 创建 `provider/user_provider.go` 等 SPI 提供者
- 将原 `HeiPermissionInterfaceImpl` 搬入 `provider/permission_provider.go`
- 路由注册改为通过 Provider 注册，而非直接往 registry 写

#### B2：`plugins/plugin-auth/`

- 创建 `go.mod`、`plugin.go`
- 从 `modules/sys/auth/` + `modules/client/auth/` 合并搬迁
- 改为通过 `plugin.GetAPI[api.UserAPI]()` 调用用户查询

#### B3：`plugins/plugin-client/`

- 创建 `go.mod`、`plugin.go`
- 从 `modules/client/`（不含 auth 和 ws）搬迁

#### B4：`plugins/plugin-im/`

- 创建 `go.mod`、`plugin.go`
- 合并 `modules/sys/ws` + `modules/client/ws`
- 消息转发通过事件总线实现

#### B5：移除 `modules/` 目录

---

### Phase C：组装与验证

#### C1：创建 `app/`

- `app/go.mod` require 所有选中的 plugin
- `app/main.go` blank import 选中插件
- 配置加载改为 `app/config.yaml`

#### C2：创建 `go.work`

```bash
go work init ./sdk ./api ./plugins/plugin-sys ./plugins/plugin-auth \
    ./plugins/plugin-client ./plugins/plugin-im ./app
```

#### C3：编译验证

```bash
for dir in sdk api plugins/plugin-* app; do
    (cd $dir && go mod tidy && go build ./...)
done
```

---

## 12. 与 Snowy 架构对比

### 12.1 设计思想对比

| 维度 | Snowy（Java/Spring） | hei-gin（Go） |
|------|----------------------|---------------|
| **模块管理** | Maven parent pom + 子模块 | `go.work` workspace + 独立 `go.mod` |
| **SPI 接口层** | `snowy-plugin-api`（独立 Maven module） | `api/`（独立 Go module，零依赖） |
| **接口注入** | `@Resource` Spring DI 自动注入 | `plugin.RegisterAPI[T]()` + `plugin.GetAPI[T]()` 显式注册查询 |
| **实现层** | `snowy-plugin-*` 子模块 | `plugins/plugin-*` 独立 Go module |
| **框架层** | `snowy-common` + Spring Boot | `sdk/` |
| **组装点** | `snowy-web-app`（pom.xml 选依赖） | `app/`（main.go 选 import） |
| **事件通信** | `CommonDataChangeEventCenter` | `plugin.EventBus`（channel 驱动） |
| **条件加载** | Maven profile / `@ConditionalOnProperty` | 改 `app/main.go` 的 import + `config.yaml` plugins.enabled |
| **扩展点** | `@Autowired` 多实现 + `@Qualifier` | `plugin.RegisterExtension[T]()` + `plugin.GetExtensions[T]()` |
| **依赖管理** | Maven dependency（编译期 + 运行时） | `PluginInfo.Dependencies`（运行时拓扑排序） |
| **插件元数据** | pom.xml 中的 artifactId/version | `PluginInfo{Name, Version, Dependencies}` |

### 12.2 Go 特色设计（不照搬 Java）

| Java 做法 | Go 做法 | 原因 |
|-----------|---------|------|
| `@Autowired` 自动注入 | `plugin.GetAPI[T]()` 显式查询 | Go 无运行时代理，显式调用更透明 |
| `@ComponentScan` 自动发现 | `init()` + `RegisterAPI()` | Go 无注解扫描，init() 是标准自注册方式 |
| Maven dependency 传递 | `go.mod` + `replace` 本地路径 | Go 原生依赖管理 |
| `EventCenter` 同步通知 | EventBus + `chan` 异步 | Go channel 是语言级特性 |
| Spring AOP 拦截 | Gin Middleware 中间件链 | Gin 已有，无需额外抽象 |
| `@ConditionalOnProperty` | `app/main.go` import 选择 + config 控制 | Go 无条件 Bean，显式 import 更直接 |
| `ClassLoader` 隔离 | Go 各 module 独立编译，自带隔离 | Go 的 module 系统本身就是隔离的 |

### 12.3 插件化带来的能力

| 能力 | 实现方式 |
|------|----------|
| **选择性加载** | `app/main.go` 选择 import 哪些插件 |
| **热插拔**（下次启动生效） | 改配置 + 改 import，重启即可 |
| **独立开发** | 每个插件独立 `go.mod`，可独立 tag、独立 CI |
| **并行构建** | workspace 模式下，各 module 可并行 `go build` |
| **版本管理** | 发布时各插件独立 tag，`go.mod` 指向版本号而非 replace |
| **第三方插件** | 外部开发者只需依赖 `hei-gin/api` 和 `hei-gin/sdk` 即可开发插件 |

---

> 计划版本：v2.0（完整插件化框架设计）
