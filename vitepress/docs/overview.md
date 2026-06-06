# Hei Gin 框架介绍

Hei Gin 是 HEI 快速开发框架的 Go 单体应用版本，基于 **Gin** + **GORM** 构建，提供开箱即用的快速开发解决方案。

## 项目结构

采用 **Go Workspace 多模块架构**：

```
hei-gin/          (根模块 + go.work)
├── sdk/          (框架 SDK — 配置/认证/权限/中间件/存储/日志/定时等)
├── api/          (接口定义层 — Plugin/EventBus/LogPersistenceAPI)
├── plugins/
│   ├── plugin-sys/     (系统管理插件 — RBAC/用户/角色/组织/日志查询等)
│   ├── plugin-client/  (客户端插件 — C 端认证/用户/会话)
│   └── plugin-im/      (IM 插件 — WebSocket/好友/群组/消息/广播)
├── app/          (应用组装层 — 导入所有插件并启动)
└── cmd/          (命令行工具 — migrate/codegen)
```

## 适用场景

- 后台管理系统（Admin Panel）
- API 服务
- 微服务单体原型
- 中小型业务系统

## 技术栈

| 类型 | 技术 |
|------|------|
| 核心框架 | Go 1.25+ / Gin 1.12+ |
| ORM | GORM (gorm.io/gorm) |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.0+ (go-redis) |
| 认证 | Token（随机 hex 字符串，非 JWT）/ SM2 国密 / bcrypt |
| 文件存储 | 本地 / MinIO / AWS S3 |
| 分布式 ID | Snowflake (bwmarrin/snowflake) |
| 定时任务 | cron (robfig/cron/v3) |
| WebSocket | gorilla/websocket |
| IP 定位 | ip2region |

## 核心特性

- **插件化架构**：业务模块以插件形式自注册，零侵入 SDK
- **双端认证**：B 端（管理端）和 C 端（客户端）独立的 Token 认证，基于 Redis 服务端会话
- **SM2 国密**：登录密码传输使用国密 SM2 C1C3C2 模式加密
- **RBAC 权限**：用户→角色→权限 + 用户直授权限，双层模型
- **数据权限**：8 级数据范围控制，最严格限制优先策略
- **权限发现**：启动时自动扫描注册的权限并缓存到 Redis
- **通配符匹配**：支持 `*` 单级和 `**` 多级通配符匹配
- **操作日志**：全量录制，SM3 签名防篡改
- **防重复提交**：基于 Redis 请求哈希去重
- **链路追踪**：基于 trace_id 的全链路追踪
- **统一响应**：标准 JSON 格式 `{code, message, data, success, trace_id}`
- **文件存储**：统一的 Engine 接口，本地/MinIO/S3 可切换
- **分片上传**：大文件分片上传（MinIO/S3 原生 multipart）
- **定时调度**：cron 表达式 + 固定间隔，优雅关闭
- **事件总线**：内存事件总线，模块间解耦
- **通用 CRUD**：分页/详情/删除等标准操作函数
- **WebSocket IM**：跨实例消息投递、在线状态感知、消息去重、限流
