# Hei FastAPI 框架介绍

Hei FastAPI 是 HEI 快速开发框架的 Python 单体应用版本，基于 **FastAPI** + **SQLAlchemy 2.0** 构建，提供开箱即用的快速开发解决方案。

## 适用场景

- 后台管理系统（Admin Panel）
- API 服务
- 微服务单体原型
- 中小型业务系统

## 技术栈

| 类型 | 技术 |
|------|------|
| 核心框架 | Python 3.10+ / FastAPI 0.136+ / Uvicorn |
| ORM | SQLAlchemy 2.0 (Mapped + mapped_column) |
| 数据验证 | Pydantic v2 + Pydantic-Settings |
| 数据库 | MySQL 8.0+ (PyMySQL) |
| 缓存 | Redis 6.0+ (redis-py async) |
| 认证 | JWT (HS256) / SM2 国密 + bcrypt |
| 文件存储 | 本地文件系统 / MinIO / AWS S3 |
| 分布式 ID | Snowflake |
| IP 地理定位 | ip2region |
| Excel 处理 | OpenPyXL |
| 测试 | pytest + pytest-asyncio + httpx |

## 核心特性

- **双端认证体系**：B 端（管理端）和 C 端（客户端）独立的 JWT 认证与权限装饰器
- **SM2 国密加密**：登录密码传输使用国密 SM2 C1C3C2 模式加密
- **bcrypt 密码哈希**：存储密码使用 bcrypt 加盐哈希
- **RBAC 权限控制**：用户 → 角色 → 权限 + 用户直授权限，双层模型
- **权限自动发现**：启动时自动扫描 `@HeiCheckPermission` 装饰器并缓存到 Redis
- **权限匹配器**：支持 `*` 单级和 `**` 多级通配符匹配
- **数据权限**：支持组织/组维度的行级数据范围控制，8 种粒度，最严策略合并
- **操作日志**：`@SysLog` 装饰器自动录制操作，支持请求参数和返回结果
- **防重复提交**：基于 Redis 的 `@NoRepeat` 接口防重复调用
- **链路追踪**：基于 ContextVar 的 trace_id 全链路追踪
- **图形验证码**：B 端/C 端独立的 Pillow 验证码服务
- **统一响应**：标准 JSON 响应格式 `{code, message, data, success, trace_id}`
- **异常处理**：全局捕获 BusinessException 返回业务错误码
- **文件存储**：本地、MinIO、S3 三种后端可切换
- **雪花 ID**：分布式唯一 ID 生成器
- **IP 定位**：基于 ip2region 的客户端 IP 地理定位
