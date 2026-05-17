# 项目结构

Hei Gin 项目采用垂直切片（Vertical Slice）架构，按业务领域组织代码，保持了良好的关注点分离。

## 完整目录树

```
hei-gin/
├── main.go                          # 应用入口，初始化并启动服务
├── config.yaml                      # 核心配置文件（数据库、Redis、JWT等）
├── go.mod / go.sum                  # Go 模块依赖定义
│
├── config/                          # 配置加载层
│   └── config.go                    # 配置结构体定义，从 YAML 加载配置
│
├── core/                            # 框架核心（与业务无关的通用能力）
│   ├── app/                         # 应用工厂
│   │   ├── app.go                   # 初始化流程编排（Config→DB→Redis→Auth→Router→Server）
│   │   ├── router.go                # 路由注册总入口，所有模块路由在此汇聚
│   │   └── health.go                # 健康检查 "/" 处理器
│   │
│   ├── auth/                        # 认证与权限系统
│   │   ├── auth_tool.go             # B端（BUSINESS）JWT 认证工具，包级函数
│   │   ├── client_auth_tool.go      # C端（CONSUMER）JWT 认证工具，结构体方法
│   │   ├── permission_interface.go  # 权限查询接口定义
│   │   ├── permission_interface_manager.go  # 权限接口管理器（多实现注册）
│   │   ├── permission_tool.go       # 权限/角色查询门面函数
│   │   ├── permission_matcher.go    # 权限通配符匹配器（* / ** / : / . / /）
│   │   ├── permission_scan.go       # 权限自动扫描与 Redis 缓存
│   │   └── middleware/              # 认证与权限中间件
│   │       ├── check_login.go              # B端登录验证中间件
│   │       ├── client_check_login.go       # C端登录验证中间件
│   │       ├── check_permission.go         # B端权限检查中间件
│   │       ├── client_check_permission.go  # C端权限检查中间件
│   │       ├── check_role.go               # B端角色检查中间件
│   │       ├── client_check_role.go        # C端角色检查中间件
│   │       └── norepeat.go                 # 防重复提交中间件
│   │
│   ├── captcha/                      # 图形验证码
│   │   └── captcha.go               # 验证码生成与验证（Redis 存储，300s TTL）
│   │
│   ├── constants/                    # 常量定义
│   │   ├── constants.go             # 通用常量（SUPER_ADMIN_CODE 等）
│   │   ├── cache_keys.go            # Redis 缓存键前缀定义
│   │   └── base_fields.go           # 数据库基础字段定义（id, created_at 等）
│   │
│   ├── db/                          # 数据库初始化
│   │   ├── ent.go                   # Ent ORM 客户端初始化 + 自动迁移
│   │   └── redis.go                 # Redis 客户端初始化
│   │
│   ├── enums/                       # 枚举定义
│   │   ├── status.go                # 状态枚举（启用/禁用/删除）
│   │   ├── permission.go            # 权限/角色/数据范围枚举
│   │   ├── resource.go              # 资源类型枚举（菜单/按钮/API）
│   │   └── page_data_field.go       # 分页字段枚举
│   │
│   ├── exception/                    # 异常处理
│   │   └── business_error.go        # BusinessException 业务异常定义
│   │
│   ├── log/                         # 操作日志系统
│   │   ├── record.go               # SysLog 中间件（录制请求/响应/签名）
│   │   └── utils.go                 # User-Agent 解析工具
│   │
│   ├── middleware/                   # 全局中间件
│   │   ├── trace.go                 # 链路追踪 - X-Trace-Id 生成与透传
│   │   ├── auth_check.go            # 认证路由分流 - 自动识别 B/C/Public
│   │   ├── recovery.go              # 全局异常恢复 - 捕获 panic 返回友好响应
│   │   └── cors.go                  # CORS 跨域配置
│   │
│   ├── result/                      # 统一响应
│   │   └── result.go               # 标准 JSON 响应格式（code, message, data, success, trace_id）
│   │
│   ├── storage/                     # 文件存储抽象
│   │   ├── interface.go            # 存储接口定义（Store/GetBytes/GetURL/Delete/Exists/Copy）
│   │   ├── local.go                # 本地文件系统实现
│   │   ├── minio.go                # MinIO 对象存储实现
│   │   └── s3.go                   # AWS S3 对象存储实现
│   │
│   └── utils/                       # 工具函数
│       ├── crypto.go               # SM2 加解密 + bcrypt 密码哈希
│       ├── ip.go                   # 客户端 IP 提取 + ip2region 城市查询
│       ├── trace.go                # 链路 ID 生成（UUID）
│       ├── snowflake.go            # 雪花 ID 生成器
│       ├── resolve.go              # 依赖解析工具
│       ├── model.go                # 模型工具函数
│       └── user_agent.go           # UA 解析工具
│
├── ent/                             # Ent ORM 定义与生成代码
│   ├── schema/                      # 实体 Schema 定义（开发者维护）
│   │   ├── sysuser.go               # 系统用户
│   │   ├── sysrole.go               # 系统角色
│   │   ├── sysmodule.go             # 系统模块
│   │   ├── sysresource.go           # 系统资源（菜单/按钮/API）
│   │   ├── sysconfig.go             # 系统配置
│   │   ├── sysdict.go               # 数据字典
│   │   ├── sysbanner.go             # Banner 管理
│   │   ├── sysnotice.go             # 通知公告
│   │   ├── sysorg.go                # 组织架构
│   │   ├── sysgroup.go              # 用户组
│   │   ├── sysposition.go           # 职位
│   │   ├── syslog.go                # 操作日志
│   │   ├── sysfile.go               # 文件记录
│   │   ├── sysquickaction.go        # 快捷操作
│   │   ├── reluserrole.go           # 用户-角色关联
│   │   ├── relrolepermission.go     # 角色-权限关联
│   │   ├── relroleresource.go       # 角色-资源关联
│   │   ├── reluserpermission.go     # 用户-权限直关联
│   │   └── clientuser.go            # C端用户
│   └── gen/                         # Ent 自动生成的代码（不要手动修改）
│
├── modules/                         # 业务模块（垂直切片架构）
│   ├── sys/                         # B端（管理后台）模块
│   │   ├── auth/                    # 认证模块
│   │   │   ├── params.go           # 请求/响应参数结构体
│   │   │   ├── service.go          # 业务逻辑层
│   │   │   └── api/v1/
│   │   │       └── api.go          # 路由注册 + Handler
│   │   ├── banner/                  # Banner 管理
│   │   ├── config/                  # 系统配置管理
│   │   ├── dict/                    # 数据字典管理
│   │   ├── file/                    # 文件上传与管理
│   │   ├── group/                   # 用户组管理
│   │   ├── home/                    # 首页仪表盘 + 快捷操作
│   │   ├── log/                     # 操作日志查询
│   │   ├── notice/                  # 通知公告管理
│   │   ├── org/                     # 组织架构管理
│   │   ├── permission/              # 权限查询
│   │   ├── position/                # 职位管理
│   │   ├── resource/                # 资源管理（菜单/按钮）
│   │   ├── role/                    # 角色管理
│   │   ├── session/                 # 在线会话管理
│   │   ├── user/                    # 用户管理
│   │   └── analyze/                 # 系统分析
│   │
│   ├── client/                      # C端（客户端）模块
│   │   ├── auth/                    # 认证模块
│   │   │   ├── params.go
│   │   │   ├── service.go
│   │   │   └── api/v1/
│   │   │       └── api.go
│   │   ├── session/                 # 会话管理
│   │   └── user/                    # 用户管理
│   │
│   └── biz/                         # 业务扩展模块（自定义业务逻辑，默认为空）
│
├── docs/                            # 开发规划与设计文档（非代码）
├── scripts/                         # 辅助脚本（数据库初始化、部署脚本等）
└── uploads/                         # 本地上传文件存储目录
```

## 目录设计说明

### core/ 框架核心

`core/` 目录包含与具体业务无关的通用框架能力，是整个项目的基础设施层。每个子包职责单一、高内聚低耦合：

- **app/**：应用生命周期管理，统一的初始化编排
- **auth/**：完整的认证授权体系，包含 B/C 双端
- **middleware/**：HTTP 中间件，请求预处理和后处理
- **storage/**：文件存储抽象层，支持多种后端

### modules/ 业务模块

`modules/` 采用垂直切片架构，每个模块独立包含其参数、服务和 API 层。这种结构的好处：

- **高内聚**：一个功能的所有代码在同一个目录下
- **低耦合**：模块之间通过接口交互，不直接依赖
- **易于裁剪**：不需要的模块可以直接移除
- **便于协作**：不同团队维护不同模块

### ent/ 数据模型

`ent/schema/` 由开发者维护，定义数据库实体的字段、索引和关系。`ent/gen/` 由 `ent generate` 命令自动生成，提供类型安全的 CRUD 操作。

### 模块内部结构约定

每个业务模块遵循统一的结构约定：

```
modules/<domain>/<module>/
├── params.go              # 请求参数和响应结构体
├── service.go             # 业务逻辑（调用 Ent DAO）
└── api/v1/
    └── api.go             # 路由注册 + HTTP Handler
```

这种约定使得任何开发者都能快速定位和理解模块代码。
