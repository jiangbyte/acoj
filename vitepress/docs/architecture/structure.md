# 项目结构

Hei FastAPI 项目采用垂直切片（Vertical Slice）架构，按业务领域组织代码，保持了良好的关注点分离。

## 完整目录树

```
hei-fastapi/
├── main.py                          # 应用入口，Uvicorn 启动
├── .env                             # 环境配置（数据库、Redis、JWT 等）
├── pyproject.toml                   # 项目元数据与依赖定义
├── requirements.txt                 # 锁定依赖版本
│
├── config/                          # 配置层
│   └── settings.py                  # Pydantic-Settings 嵌套模型配置
│
├── core/                            # 框架核心（与业务无关的通用能力）
│   ├── app/                         # 应用工厂
│   │   ├── setup.py                 # create_app() - FastAPI 应用工厂，初始化编排
│   │   ├── router.py                # 路由注册总入口（所有模块路由在此汇聚）
│   │   ├── lifespan.py              # 应用生命周期（DB/Redis/SM2/JWT/权限/验证码初始化）
│   │   └── health.py                # 健康检查 "GET /" 处理器
│   │
│   ├── auth/                        # 认证与权限系统
│   │   ├── auth/
│   │   │   ├── hei_auth_tool.py            # B端（BUSINESS）JWT 认证工具
│   │   │   └── hei_client_auth_tool.py     # C端（CONSUMER）JWT 认证工具
│   │   ├── decorator/                      # 认证与权限装饰器
│   │   │   ├── hei_check_login.py          # B端登录验证装饰器
│   │   │   ├── hei_check_permission.py     # B端权限检查装饰器
│   │   │   ├── hei_check_role.py           # B端角色检查装饰器
│   │   │   ├── hei_client_check_login.py   # C端登录验证装饰器
│   │   │   ├── hei_client_check_permission.py # C端权限检查装饰器
│   │   │   ├── hei_client_check_role.py    # C端角色检查装饰器
│   │   │   └── norepeat.py                 # 防重复提交装饰器
│   │   ├── permission/
│   │   │   ├── hei_permission_interface.py         # 权限查询接口定义
│   │   │   ├── hei_permission_interface_manager.py # 权限接口管理器
│   │   │   ├── hei_permission_matcher.py           # 权限通配符匹配器
│   │   │   └── hei_permission_tool.py              # 权限查询门面
│   │   ├── permission_scan.py             # 权限自动扫描与 Redis 缓存
│   │   └── pojo/                          # 登录用户信息 POJO
│   │       ├── login_user_info.py         # B端登录用户信息
│   │       └── login_client_user_info.py  # C端登录用户信息
│   │
│   ├── captcha/                      # 图形验证码
│   │   └── captcha.py               # 验证码生成与验证（Pillow 生成，Redis 存储）
│   │
│   ├── constants/                    # 常量定义
│   │   ├── base_fields.py            # 基础模型字段（id, created_at 等）
│   │   ├── cache_keys.py             # Redis 缓存键前缀
│   │   └── constants.py              # 通用常量
│   │
│   ├── db/                           # 数据库层
│   │   ├── mysql.py                  # SQLAlchemy 引擎 + SessionLocal + get_db()
│   │   ├── redis.py                  # Redis 异步客户端
│   │   ├── base_dao.py               # BaseDAO 通用 CRUD 基类（仅 .pyc）
│   │   ├── base_service.py           # BaseService 通用业务逻辑基类（已移除）
│   │   ├── query_wrapper.py          # QueryWrapper 链式查询构建器（已移除）
│   │   └── meta_object_handler.py    # 系统字段自动填充（已移除）
│   │
│   ├── enums/                        # 枚举定义
│   │   ├── permission_enum.py        # 权限/登录类型/校验模式/数据范围枚举
│   │   ├── resource_enum.py          # 资源类型枚举
│   │   ├── status_enum.py            # 状态枚举
│   │   └── page_data_field_enum.py   # 分页字段枚举
│   │
│   ├── exception/                    # 异常定义
│   │   └── business_exception.py     # BusinessException
│   │
│   ├── log/                          # 操作日志系统
│   │   ├── decorator.py             # @SysLog 装饰器 + save_exception_log()
│   │   └── utils.py                  # UA 解析、参数提取、签名生成
│   │
│   ├── middleware/                    # 全局中间件
│   │   ├── auth.py                   # AuthMiddleware - 认证路由分流（原始 ASGI）
│   │   ├── cors.py                   # CORS 跨域配置
│   │   ├── exception.py              # 全局异常处理器
│   │   └── trace.py                  # TraceMiddleware - 链路追踪
│   │
│   ├── pojo/                         # 公共数据对象
│   │   ├── datetime_mixin.py         # 日期时间序列化混入
│   │   └── id_params.py              # IdParam / IdsParam
│   │
│   ├── result/                       # 统一响应
│   │   └── result.py                 # success() / failure() / page_data()
│   │
│   ├── storage/                      # 文件存储抽象
│   │   ├── interface.py              # FileStorageInterface 抽象接口
│   │   ├── local_storage.py          # 本地文件系统实现
│   │   ├── minio_storage.py          # MinIO 对象存储实现
│   │   └── s3_storage.py             # AWS S3 兼容实现
│   │
│   └── utils/                        # 工具函数
│       ├── sm2_crypto_util.py        # SM2 加解密（C1C3C2/C1C2C3 格式转换）
│       ├── excel_utils.py            # Excel 导出导入
│       ├── ip_utils.py               # 客户端 IP 提取 + ip2region
│       ├── snowflake_utils.py        # 雪花 ID 生成器
│       ├── model_utils.py            # 模型工具函数
│       ├── trace_utils.py            # Trace ID 上下文管理
│       ├── resolve_utils.py          # 依赖解析工具
│       └── user_agent_utils.py       # UA 解析
│
├── modules/                          # 业务模块（垂直切片架构）
│   ├── sys/                          # B端（后台管理）
│   │   ├── auth/                     # 认证模块
│   │   │   ├── captcha/              # 图形验证码
│   │   │   ├── sm2/                  # SM2 公钥获取
│   │   │   └── username/             # 登录/注册/登出
│   │   ├── banner/                   # Banner 管理
│   │   ├── config/                   # 系统配置管理
│   │   ├── dict/                     # 数据字典（树形结构）
│   │   ├── file/                     # 文件上传与管理
│   │   ├── group/                    # 用户组管理
│   │   ├── home/                     # 首页仪表盘 + 快捷操作
│   │   ├── log/                      # 操作日志查询
│   │   ├── notice/                   # 通知公告管理
│   │   ├── org/                      # 组织架构（树形结构）
│   │   ├── permission/               # 权限查询
│   │   ├── position/                 # 职位管理
│   │   ├── resource/                 # 资源管理（菜单/按钮）
│   │   ├── role/                     # 角色管理
│   │   ├── session/                  # 在线会话管理
│   │   ├── analyze/                  # 系统分析
│   │   └── user/                     # 用户管理
│   │
│   ├── client/                       # C端（客户端）
│   │   ├── auth/                     # 认证模块
│   │   │   ├── captcha/              # 图形验证码
│   │   │   └── username/             # 登录/注册
│   │   ├── session/                  # 在线会话管理
│   │   └── user/                     # C端用户管理
│   │
│   └── biz/                          # 业务扩展模块（默认为空）
│
├── scripts/                          # 辅助脚本
│   └── sqls/
│       └── hei_ddl.sql               # 数据库建表 DDL（含示例数据）
│
├── docs/                             # 开发文档
│   └── Command.md                    # 命令参考
│
└── LICENSE                           # MIT 许可证
```

## 目录设计说明

### core/ 框架核心

`core/` 目录包含与具体业务无关的通用框架能力，是整个项目的基础设施层。每个子包职责单一、高内聚低耦合：

- **app/**：应用生命周期管理，统一的初始化编排
- **auth/**：完整的认证授权体系，包含 B/C 双端认证、权限装饰器、权限匹配器
- **middleware/**：HTTP 中间件，请求预处理和后处理（原始 ASGI 实现）
- **storage/**：文件存储抽象层，支持本地/MinIO/S3 多种后端
- **db/**：数据库访问层，包含 MySQL 连接、Redis 客户端

### modules/ 业务模块

`modules/` 采用垂直切片架构，每个模块独立包含其模型、参数、服务、数据访问和 API 层。这种结构的好处：

- **高内聚**：一个功能的所有代码在同一个目录下
- **低耦合**：模块之间通过接口交互，不直接依赖
- **易于裁剪**：不需要的模块可以直接移除
- **便于协作**：不同团队维护不同模块

### 模块内部结构约定

每个业务模块遵循统一的结构约定：

```
modules/<domain>/<module>/
├── models.py        # SQLAlchemy ORM 模型（Mapped + mapped_column）
├── params.py        # Pydantic v2 请求参数和响应模型
├── dao.py           # 数据访问层
├── service.py       # 业务逻辑层
└── api/v1/api.py    # FastAPI 路由定义 + Controller
```

这种约定使得任何开发者都能快速定位和理解模块代码。

### 关于 ASGI 原始中间件

Hei FastAPI 使用原始 ASGI 中间件（而非 `BaseHTTPMiddleware`）实现 Auth 和 Trace 功能，原因如下：

- `BaseHTTPMiddleware` 在处理流式响应和文件上传时存在 body streaming 问题
- 原始 ASGI 中间件能直接操作 ASGI scope，对请求/响应有完全控制权
- 更高效，无额外的中间件包装开销
