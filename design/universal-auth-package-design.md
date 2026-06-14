# Micosauth Package Design

## 1. 产品定位

这是一个可通过 `pip` 安装的通用认证鉴权产品包，定位不是“某个项目里的 auth 模块”，而是一个可在不同 Python 项目中复用的基础认证产品。

它解决的不是单一登录接口问题，而是统一解决一类基础设施问题：

- 身份认证
- 权限校验
- 数据范围获取
- 会话管理
- 多认证域隔离
- token 生命周期治理
- 内部 token 反查

它面向的不是某一种项目类型，而是一组重复出现的通用场景：

- Web 应用
- API 服务
- RPC 服务
- WebSocket 服务
- worker / CLI / 定时任务
- 多端用户体系

产品形态上，它是一个独立发布到 PyPI 或私有 Python 仓库的组件。

## 2. 产品目标

这个产品包的目标是：

- 支持多认证域
- 支持登录校验
- 支持角色校验
- 支持权限校验
- 支持基于 Redis 的 token / session 管理
- 支持开箱即用
- 支持配置驱动
- 支持在不同业务场景中接入
- 支持通用 Python 应用接入
- 支持不同 Python Web 框架适配
- 支持内部通过 token 反查认证主体与会话
- 支持临时 token 能力

最终要达到的产品体验是：

> 使用者安装包后，只需要完成少量业务配置和身份接入，就可以获得一套完整、统一、可运营的认证鉴权能力。

同时明确一个前提：

> Redis 是该产品的必选基础依赖，不提供无 Redis 运行模式。

同时明确一个登录模型：

> 登录动作本身只要求传入 `loginId`，额外业务信息放入 token / session。

同时明确一个 token 约定：

> 默认访问 token 风格为 64 位字符串。

## 3. 产品原则

产品原则：

- 框架无关
- Redis 必选
- 配置优先
- 认证与鉴权解耦
- 核心能力稳定，外围接入轻量
- 默认安全，显式扩展

这些原则对应的产品要求是：

- 不能只能在 Web 请求里用
- 不能只能服务“用户名密码登录”
- 不能只能服务单一用户体系
- 不能脱离 Redis 独立运行
- 不能把复杂技术细节暴露给业务侧
- 不能要求用户先理解底层存储模型才能使用

## 4. 目标用户

这个产品包至少有三类直接用户：

### 4.1 业务应用开发者

他们关心的是：

- 怎么快速接入登录能力
- 怎么校验接口是否已登录
- 怎么校验角色和权限
- 怎么拿到数据范围
- 怎么做多端 token 隔离
- 怎么在内部通过 token 反查身份

他们不希望关心：

- Redis key 结构
- token 索引维护细节
- session 清理细节

### 4.2 平台/基础架构开发者

他们关心的是：

- 能否统一集团或团队内多个项目的认证方式
- 能否配置多 realm
- 能否扩展不同登录方式
- 能否统一会话治理和踢人策略
- 能否沉淀成基础能力重复复用

### 4.3 安全与运维相关角色

他们关心的是：

- token 能否撤销
- session 能否查看与管理
- 是否支持按主体强制下线
- 是否支持审计与追踪
- 是否支持过期、禁用、风险控制策略

## 5. 核心价值

这个产品包的核心价值有五个：

### 5.1 统一

统一不同项目的认证鉴权模型，避免每个项目都自行发明一套。

### 5.2 复用

把登录、校验、会话管理、token 反查这些重复能力沉淀成安装即用的产品能力。

### 5.3 隔离

通过 realm 抽象支持多用户体系、多终端、多场景并存。

### 5.4 可运营

不仅能“校验登录”，还能管理 session、撤销 token、查看在线状态、支持治理。

### 5.5 可扩展

业务项目只补最核心的身份和 ACL 逻辑，不必重写整套认证基础设施。

### 5.6 Redis 一致性

认证、session、撤销、在线态、token 反查都基于 Redis 建立统一行为，避免多后端模式导致的能力差异和治理复杂度。

### 5.7 工具分层

产品对外建议提供四类对象：

- `MicosService`
- `MicosSetting`
- `MicosAuthUtil`
- `MicosSessionUtil`

其中：

- `MicosService`
  - 服务入口
  - 负责 realm 注册
  - 负责装配 Redis、AccessProvider、运行时能力
- `MicosSetting`
  - 配置对象
  - 负责 Redis 配置、realm 配置、token/session 策略配置
- `MicosAuthUtil`
  - 主工具
  - 面向认证、鉴权、撤销、反查等核心动作
- `MicosSessionUtil`
  - 辅助工具
  - 面向 session 查询、统计、分析类场景

其中 `MicosSetting` 和 `MicosService` 的定位是：

- 启动阶段接入对象
- 在应用启动时完成初始化与装配
- 初始化完成后，业务侧即可直接使用包能力

### 5.8 Auth / Session 职责边界

职责边界：

- `MicosAuthUtil`
  - 登录
  - 登出
  - 登录状态校验
  - 角色校验
  - 权限校验
  - 数据范围获取
- token 反查
- token 撤销
- session 撤销
- 指定 `loginId` 下线
  - 临时 token 签发
  - 临时 token 校验
- `MicosSessionUtil`
  - session 查询
  - 在线状态查询
  - 在线统计
  - session 统计
  - token 列表查询
  - 会话分析查询
  - 会话图表查询

`MicosSessionUtil` 不负责治理动作，只负责查询和统计。

### 5.9 配置对接边界

产品会维护自己的 Redis 配置模型和认证配置模型。

但如何把这些配置和用户项目本身的配置系统对接，由用户自己决定。

例如用户可以自行对接：

- 环境变量
- 项目 settings
- 配置中心
- `yaml/json/toml`

产品本身不强绑定任何项目配置框架。

## 6. 产品形态目标

这个设计目标明确是一个独立 `pip package`，而不是项目内模块。它应具备：

- 可独立版本化
- 可独立发布
- 可通过 `pip install` 接入
- Redis 作为基础依赖随主包安装
- 可通过 `extras` 安装框架或扩展能力
- 可通过插件机制扩展
- 可在不同项目中复用

产品命名明确为：

- pip 包名：`micosauth`
- Python 导入名：`micosauth`

## 7. 非目标

以下内容不作为第一阶段必须能力：

- OAuth2 / OIDC 服务端完整实现
- SAML
- 多因子认证完整产品化
- UI 登录页
- 完整 IAM 平台能力
- 审批流式授权模型
- 数据库行级权限编译器
- 脱离 Redis 的轻量运行模式
- 基于内存的正式运行模式

这些能力后续可以通过扩展模块追加，不放入核心内核。

## 8. 产品能力范围

本包提供四层能力：

1. 认证内核
2. 鉴权内核
3. 会话与 token 管理
4. 框架适配与接入层

对业务侧的承诺是：

> 业务只需要通过 `pip` 安装包、准备 Redis、配置 realm、注册访问规则，并自行完成项目配置到产品配置的对接，即可获得完整认证鉴权能力。

## 8.1 首期必须提供的产品能力

首期必须明确提供的能力：

- 登录
- 登出
- 登录状态校验
- 角色校验
- 权限校验
- 数据范围获取
- 多 realm 隔离
- token 签发
- token 校验
- token 撤销
- 临时 token 签发与校验
- session 管理
- 根据 token 反查 subject / claims / session
- 按 subject 查询在线 session
- 按 subject 强制下线
- session 分页查询
- token 列表查询
- 会话分析统计
- 会话图表数据

## 8.2 首期不必提供但要预留的能力

- 刷新 token
- MFA
- 风险控制
- 单设备策略
- OAuth2 对接
- 数据权限执行器
- 可视化后台

## 8.3 pip 包产品约束

作为一个独立 pip 包，需要额外满足：

- 最小核心依赖
- 可选依赖分层
- 不把框架依赖强行塞给所有用户
- Redis 作为核心依赖直接纳入主包
- 支持 `pyproject.toml` 安装
- 支持语义化版本管理
- 支持清晰的 API 稳定性承诺

## 9. 典型使用场景

### 9.1 单系统后台

- 一个管理端用户体系
- 登录后访问受保护接口
- 校验角色和权限

### 9.2 多端用户体系

- 管理端一套用户
- 客户端一套用户
- 各自 token 隔离
- 各自 ACL 规则独立

### 9.3 多租户系统

- 不同租户共享认证包
- token 中携带 tenant 信息
- ACL 加载按 tenant 隔离

### 9.4 API 网关后业务服务

- 网关负责透传 token
- 业务服务只做 claims 校验和权限判断

### 9.5 非 HTTP 场景扩展

- WebSocket
- RPC
- 定时任务代入身份执行
- CLI / worker
- 事件消费程序

第一阶段主接入面可以先覆盖 HTTP，但核心设计必须是通用 Python 运行时可用，而不是 Web-only。

即使不在 Web 场景，仍然共享同一套 Redis token / session 基础设施。

## 10. 功能地图

从产品视角，这个包的功能可以分为 6 组。

### 10.1 Realm 管理能力

- 定义多个认证域
- 注册多个 realm
- 为不同认证域配置不同 token 规则
- 为不同认证域配置如何根据 `loginId` 获取角色、权限、数据范围
- 控制不同认证域的隔离边界

### 10.2 认证能力

- 基于 `loginId` 登录
- 登出
- 登录状态判断
- token 校验
- token 过期判断
- token 撤销
- 临时 token 签发
- 临时 token 校验
- token 刷新能力预留

### 10.3 鉴权能力

- 登录校验
- 角色校验
- 权限校验
- 数据范围获取
- 组合校验规则

### 10.4 Session 运营能力

- 查询主体在线 session
- 查询单 token 对应 session
- 统计在线会话
- 分页查询在线 session
- 查询某 `loginId` 的 token 列表
- 会话分析统计
- 会话图表数据

这些能力全部建立在 Redis 在线会话模型之上。

### 10.5 Token 反查能力

- 给定 token 判断是否有效
- 给定 token 反查 claims
- 给定 token 反查 subject
- 给定 token 反查 realm
- 给定 token 反查 session
- 给定临时 token 判断是否有效

### 10.6 接入能力

- 普通 Python 代码直接调用
- Web 框架 middleware / decorator / dependency 接入
- RPC / WebSocket / worker 场景接入

## 11. 核心产品流程

产品上必须先定义清楚流程，再谈技术结构。首期至少要确定下面 7 条主流程。

### 11.1 初始化流程

目标：

- 安装包后完成初始化
- 建立 Redis 连接
- 初始化 `MicosSetting`
- 初始化 `MicosService`
- 注册 realm
- 注册 realm 的权限、角色、数据范围获取逻辑
- 配置工具自身 Redis 连接信息
- 完成项目配置到工具配置的映射

结果：

- 应用启动后即可使用认证能力
- Redis 不可用则初始化不应通过
- 启动完成后，运行阶段直接使用 `MicosAuthUtil`、`MicosSessionUtil` 和装饰器能力

### 11.2 登录流程

目标：

- 让一个主体完成认证并拿到 token

关键步骤：

1. 选择 realm
2. 输入 `loginId`
3. 创建 session
4. 签发 token
5. 将额外信息写入 token / session
6. 返回登录结果

流程输出：

- token
- session 基础信息
- `loginId`
- 过期信息

默认 token 生成规则：

- 默认生成 64 位字符串 token

### 11.3 访问校验流程

目标：

- 在运行时判断某次访问是否已经认证

关键步骤：

1. 获取 token
2. 解析 realm
3. 校验 token 有效性
4. 加载主体信息
5. 构造认证上下文
6. 返回是否通过

输出结果：

- 通过
- 不通过
- 不通过原因

### 11.4 鉴权流程

目标：

- 在已认证前提下判断是否有角色/权限

关键步骤：

1. 确认主体已认证
2. 按 `realmId + loginId` 获取角色、权限、数据范围
3. 匹配角色或权限
4. 返回放行或拒绝

输出结果：

- 放行
- 拒绝
- 拒绝原因

### 11.5 token 反查流程

目标：

- 不依赖请求对象，直接通过 token 反查认证信息

适用场景：

- RPC
- WebSocket
- 内部服务
- CLI
- worker

关键步骤：

1. 输入 token
2. 定位 realm
3. 校验 token
4. 读取 claims
5. 读取 session
6. 读取 `loginId`
7. 返回完整结果

输出结果：

- token 是否有效
- 对应 realm
- 对应 subject
- 对应 session
- 对应 claims

### 11.5.1 临时 token 流程

目标：

- 为短时、一次性或特殊场景提供独立 token 能力

临时 token 输入参数：

- `tempId`
- `type`
- `time`

适用场景：

- 短时下载
- 短时授权
- 回调确认
- 一次性跳转
- 临时凭证交换

产品要求：

- 临时 token 由 `MicosAuthUtil` 负责签发与校验
- 临时 token 与普通登录 token 区分管理
- 临时 token 支持独立过期时间
- 临时 token 默认不进入普通在线 session 统计

### 11.6 登出/撤销流程

目标：

- 使 token 或 session 立即失效

支持方式：

- 当前 token 登出
- 单 token 撤销
- 单 session 撤销
- 某主体全部 session 撤销

### 11.7 Session 管理流程

目标：

- 对登录状态进行查看和治理

支持动作：

- 查询在线 session
- 查看某 session 基本信息
- 查看某 token 所属 session
- 强制下线
- 刷新权限快照能力预留

## 12. 产品体验要求

### 12.1 接入体验

接入一个新项目时，用户应只回答几个关键问题：

- 有几个 realm
- Redis 如何接入
- 登录时如何传入 `loginId`
- 如何根据 `realmId + loginId` 获取权限、角色、数据范围
- 需要哪些额外 session/token 信息

接入动作应主要发生在启动阶段：

- 构造 `MicosSetting`
- 初始化 `MicosService`
- 完成 realm 注册与能力装配

启动完成后，应进入“直接使用能力”的状态，而不是每次使用前重复初始化。

不应要求用户一开始就处理：

- Redis key 设计
- session 索引结构
- token 内部持久化细节

### 12.2 使用体验

业务开发者使用时，应有三种自然入口：

- 登录/登出能力
- 运行时校验能力
- token 反查能力

### 12.3 运维体验

平台或运维应能完成：

- 查询主体在线状态
- 查询 token 是否有效
- 撤销 token
- 强制下线主体

### 12.4 演进体验

后续新增：

- 新 realm
- 新登录方式
- 新 ACL 来源
- 新适配层

不应破坏已有接入项目。

## 13. MVP 产品范围

第一阶段产品必须聚焦，避免一次性做成完整 IAM。

### 13.1 必做

- 多 realm
- 登录/登出
- token 校验
- 角色校验
- 权限校验
- 数据范围获取
- Redis session/token 管理
- token 反查
- subject 在线会话管理
- 通用 Python 接入
- 一个主流 Web adapter

### 13.2 可延后

- refresh token
- MFA
- 风险识别
- 可视化后台
- 复杂审批授权
- 外部身份协议

## 14. 核心设计理念

### 14.1 Realm 是顶层隔离单位

一个认证域 `Realm` 代表一套独立的认证上下文，至少包括：

- 用户身份来源
- token 命名空间
- ACL 加载方式
- 登录策略
- 路由匹配规则

一个应用可配置多个 realm，例如：

- `admin`
- `user`
- `partner`
- `device`

### 14.2 LoginId 是认证主体主键

产品层面统一使用 `loginId` 作为认证主体主键。

它可以代表：

- 用户 ID
- 员工 ID
- 服务账号 ID
- 设备 ID
- 第三方调用方 ID

产品不要求内置用户模型，只要求能够围绕 `realmId + loginId` 建立认证上下文。

### 14.3 ACL 是鉴权快照，不等于数据源

权限数据实际来源可能是：

- 数据库
- RPC
- 配置中心
- 第三方 IAM

包内部只依赖标准 ACL 模型，不绑定具体来源。

角色、权限、数据范围都允许返回空。

空返回的语义必须明确：

- 角色为空：无角色
- 权限为空：无权限
- 数据范围为空：无数据范围

不能把空值解释为异常。

### 14.4 认证与鉴权分层

认证只回答：

- 你是谁
- 你是否已登录
- 当前 token 是否有效

鉴权只回答：

- 你是否拥有某角色
- 你是否拥有某权限
- 你是否具备某作用域

### 14.5 核心层不依赖具体 Web 框架

核心内核不应依赖任何具体 Web 框架，也不应把“请求”作为唯一入口。它只依赖通用上下文抽象和 token / subject 查询服务，不直接依赖：

- FastAPI
- Flask
- Django
- Starlette

这些都放在 adapter 层处理。

### 14.6 token 既用于认证，也用于内部反查

这个包不能只支持“从请求里取 token 再认证”，还必须支持业务内部主动调用：

- 给定 token，反查 subject
- 给定 token，反查 claims
- 给定 token，反查 session
- 给定 token，判断是否有效
- 给定 token，刷新 TTL
- 给定 token，执行撤销

这意味着 token 相关服务必须是核心公共能力，而不是仅作为 middleware 内部细节存在。

### 14.7 中间件不负责定义认证规则

产品只提供认证服务与上下文能力，中间件如何接入、如何从请求中提取 token、如何执行认证动作，可以由使用者自行完成。

官方可以提供适配器，但产品边界上不强制把认证流程绑定在某个框架 middleware 上。

## 15. 总体架构

建议分为 6 层：

### 6.1 Core

纯领域核心：

- `Realm`
- `LoginContext`
- `SessionClaims`
- `ACL`
- `PermissionMatcher`
- `AuthManager`
- `AuthorizationService`

不依赖 Web 框架，但产品能力默认建立在 Redis 基础设施之上。

### 6.2 SPI

标准扩展接口：

- `TokenStore`
- `SessionStore`
- `AccessProvider`
- `EventPublisher`
- `Clock`
- `RequestContextResolver`

SPI 只定义协议，不提供业务实现。

### 6.3 Infrastructure

内建基础设施实现：

- `RedisTokenStore`
- `RedisSessionStore`
- `JWTSigner`
- `OpaqueTokenGenerator`
- `PasswordHasher`

### 6.4 Application

统一编排服务：

- 登录流程
- 登出流程
- token 刷新
- token 反查
- 权限快照刷新
- session 查询
- 强制下线

### 6.5 Adapter

不同接入层适配：

- `fastapi`
- `starlette`
- `flask`
- `django`
- `aiohttp`

### 6.6 Developer Experience

开发者友好层：

- 配置模型
- 自动注册
- 装饰器
- 中间件
- 文档生成
- 诊断工具

## 6.7 Distribution

面向 pip 包发布，还应额外考虑一层“分发结构”：

- 核心包
- JWT 扩展
- FastAPI 扩展
- Flask 扩展
- Django 扩展

可以有两种方案：

### 方案 A：单包 + extras

包名：

- `micosauth`

安装方式：

```bash
pip install micosauth
pip install "micosauth[fastapi]"
pip install "micosauth[fastapi,jwt]"
```

优点：

- 用户心智简单
- 版本管理最容易
- 文档统一

缺点：

- 单包会越来越重

### 方案 B：主包 + 多扩展子包

包名示例：

- `micosauth-core`
- `micosauth-fastapi`
- `micosauth-flask`
- `micosauth-jwt`

优点：

- 依赖隔离干净
- 子能力可独立演进

缺点：

- 安装和版本协调更复杂

推荐第一阶段采用：

- 单包 + extras

原因是：

- 更适合前期推广
- 文档更简单
- 使用者更容易上手

这里的前提是：

- Redis 依赖属于主包
- extras 只用于框架适配或扩展能力
- 后续如有必要再拆分子包

## 7. 核心对象模型

### 7.1 RealmDefinition

定义一个 realm 的静态配置：

- `id`
- `name`
- `token_namespace`
- `token_transport`
- `public_paths`
- `route_matchers`
- `default_guard_mode`
- `session_policy`
- `acl_policy`
- `subject_type`
- `extra_claim_fields`

### 7.2 LoginContext

认证主体上下文模型：

- `login_id`
- `realm_id`
- `tenant_id`
- `extra`

### 7.3 SessionClaims

建议标准结构：

- `session_id`
- `login_id`
- `realm_id`
- `issued_at`
- `expires_at`
- `not_before`
- `tenant_id`
- `extra`
- `acl_version`
- `session_version`

### 7.4 ACL

建议标准结构：

- `roles: list[str]`
- `permissions: list[str]`
- `scopes: dict[str, ScopeSet]`
- `attributes: dict[str, Any]`

`scopes` 用于未来数据权限扩展。

### 7.5 Session

建议把 session 设计成一等对象，而不是 token payload 的附属概念：

- `session_id`
- `realm_id`
- `login_id`
- `created_at`
- `expires_at`
- `last_access_at`
- `device_id`
- `device_type`
- `client_ip`
- `user_agent`
- `status`
- `extra`

### 7.6 TokenRecord

建议把 token 也设计成显式对象，便于内部服务反查：

- `token_id`
- `session_id`
- `realm_id`
- `login_id`
- `issued_at`
- `expires_at`
- `token_type`
- `status`
- `fingerprint`
- `extra`

`token_value` 本身不建议在领域对象中长期裸传递，查询时可以输入 token 原文，但存储和日志层应支持脱敏或 hash。

### 7.7 AuthContext

请求期上下文：

- `realm`
- `subject`
- `claims`
- `acl`
- `token`
- `authenticated`
- `request_id`

这个对象是所有 adapter 和 guard 的统一载体。

### 7.8 TokenLookupResult

为了支持内部通过 token 反查，建议提供标准结果对象：

- `valid: bool`
- `reason: str | None`
- `realm`
- `login_id`
- `claims`
- `session`
- `token_record`
- `acl`

这个对象既可用于 Web adapter，也可用于内部服务调用。

## 8. 配置模型

## 8.1 顶层配置

建议提供统一配置入口：

```python
auth_config = AuthConfig(
    redis=...,
    realms=[...],
    defaults=...,
    adapters=...,
)
```

### 8.2 全局默认配置

建议全局默认项：

- Redis 连接配置
- token 格式
- 默认过期时间
- 刷新策略
- 是否允许多端并存
- 默认 header 名称
- 默认异常映射
- 缓存策略
- 时间源
- 安全策略

### 8.3 realm 配置

每个 realm 至少配置：

- `id`
- `token_name`
- `token_transport`
- `token_ttl`
- `refresh_ttl`
- `max_sessions_per_subject`
- `allow_concurrent_sessions`
- `access_provider`

### 8.4 配置方式

支持三种：

1. 纯代码配置
2. `yaml/json/toml` 配置
3. 环境变量映射

推荐代码配置为主，文件配置为辅。

### 8.5 pip 包接入方式

作为独立包，推荐暴露统一入口：

```python
from micosauth import MicosService, MicosSetting, MicosAuthUtil, MicosSessionUtil
```

典型初始化方式：

```python
micos_setting = MicosSetting(
    redis=...,
    realms=[...],
    defaults=...,
)

micos_service = MicosService(micos_setting)
micos_auth = MicosAuthUtil(micos_service)
micos_session = MicosSessionUtil(micos_service)
```

如果接入 FastAPI：

```python
from micosauth.adapters.fastapi import install_fastapi_auth

install_fastapi_auth(app, micos_service)
```

包的配置设计必须满足：

- 自带 Redis 配置模型
- 可在应用启动时一次初始化
- 可在测试中轻量构造
- 可在多应用中重复实例化
- 不依赖全局单例
- 不强绑定任何项目配置系统

## 9. 核心扩展接口设计

### 9.1 AccessProvider

负责根据 `realmId + loginId` 返回授权快照：

- 角色
- 权限
- 数据范围
- 属性约束

空返回语义：

- 返回空角色列表：该主体无角色
- 返回空权限列表：该主体无权限
- 返回空数据范围：该主体无数据范围

这不应视为异常。

### 9.2 TokenStore

负责 token 生命周期：

- 保存 token
- 删除 token
- 读取 claims
- 按 token 读取 token record
- 按 token 读取 session_id / login_id / realm_id
- 校验 token 是否存在
- 更新 TTL
- 判断 token 状态

`TokenStore` 必须把“按 token 反查”作为一等能力，而不是附带能力。

### 9.3 SessionStore

负责主体会话集合：

- 列出某 `loginId` 全部 session
- 根据 session_id 读取 session
- 统计在线会话
- 强制下线
- 踢单 token
- 维护会话索引

### 9.4 EventPublisher

发布认证相关事件：

- 登录成功
- 登录失败
- 登出
- token 刷新
- ACL 刷新
- session 被踢

便于接入审计、监控、通知。

## 10. token 与 session 设计

### 10.1 token 类型

设计上应同时支持两类：

1. Opaque Token
2. JWT / JWS

推荐第一阶段默认使用 Opaque Token + Redis 存储，因为：

- 服务端可控性更强
- 强制下线容易
- ACL 刷新容易
- 撤销简单

JWT 支持作为可选模式，不作为默认唯一模式。

第一阶段默认访问 token 约定：

- 使用 opaque token
- token 风格为 64 位字符串

### 10.1.1 临时 token

除了普通访问 token，还应支持临时 token。

临时 token 的核心属性：

- 由 `tempId` 标识业务对象或临时主体
- 由 `type` 标识临时 token 类型
- 由 `time` 指定有效时长或过期时间

临时 token 与普通 token 的差异：

- 生命周期更短
- 不默认纳入普通 session 管理
- 使用场景更明确
- 允许不绑定完整登录 session

### 10.2 Session 与 Token 区分

建议明确区分：

- `session`
  某个主体的一次登录会话
- `token`
  某个会话下的一种访问凭证

这样可以自然支持：

- 一个主体多个 session
- 一个 session 多个 token
- access token + refresh token
- 通过 token 反查所属 session
- 通过 token 反查所属 subject

### 10.3 标准字段

建议 token 关联字段：

- `token_id`
- `session_id`
- `subject_id`
- `realm_id`
- `issued_at`
- `expires_at`
- `device_id`
- `device_type`
- `client_ip`
- `user_agent`

普通 token 默认生成约束：

- 默认生成 64 位字符串

临时 token 建议额外字段：

- `temp_id`
- `temp_type`
- `temp_expire_at`

### 10.4 Redis 数据模型

建议按职责拆 key，而不是全部塞进单一 payload：

- token -> claims
- token -> token metadata
- session -> session metadata
- subject -> session ids
- session -> token ids
- session index
- token expiry index
- disable / blacklist / revocation set

### 10.5 建议 key 命名

例如：

- `micosauth:{realm}:token:{token_id}`
- `micosauth:{realm}:token_meta:{token_id}`
- `micosauth:{realm}:session:{session_id}`
- `micosauth:{realm}:subject_sessions:{subject_id}`
- `micosauth:{realm}:session_tokens:{session_id}`
- `micosauth:{realm}:token_expiry`
- `micosauth:{realm}:session_expiry`
- `micosauth:{realm}:revoked_tokens`

如果使用 opaque token，建议增加一层 token hash：

- `micosauth:{realm}:token_hash:{sha256(token_value)} -> token_id`

这样可支持：

- 内部通过 token 原文反查 token_id
- 存储层避免直接把明文 token 作为主键
- 更方便做日志脱敏和安全治理

其中 `micosauth` 表示本产品的统一 Redis 前缀。

### 10.6 TTL 策略

建议分离：

- token TTL
- session TTL
- refresh token TTL

不要默认把 session TTL 和 token TTL 强绑定。

### 10.7 刷新策略

至少支持：

- 固定过期
- 滑动过期
- refresh token 刷新 access token
- 禁止刷新

## 11. ACL 设计

### 11.1 ACL 读取策略

应支持三种模式：

1. 每请求实时读取 ACL
2. 登录时快照 ACL，按版本刷新
3. 混合模式

推荐默认：

- token 只存最小 claims
- ACL 放请求期缓存
- 可选启用 ACL 快照缓存

原因是通用包不应强推“权限变更必须刷新 token”这一实现方式。

### 11.2 ACL 缓存

可选增加 ACL cache 层：

- key 按 `realm + subject_id + acl_version`
- 可设置短 TTL
- provider 可返回版本号

### 11.3 角色与权限模型

建议内核只处理标准集合：

- `roles: set[str]`
- `permissions: set[str]`

不在核心层绑定：

- 菜单
- 路由树
- 按钮 UI

这些应属于上层业务模型。

### 11.4 Permission Matcher

支持：

- 精确匹配
- 单段通配 `*`
- 多段通配 `**`
- 可配置分隔符

同时允许业务自定义 matcher。

## 12. 认证流程设计

### 12.1 登录流程

建议统一登录编排：

1. 选择 realm
2. 输入 `loginId`
3. 创建 session
4. 生成 token
5. 写入存储
6. 写入额外信息到 token / session
7. 发布登录事件
8. 返回登录结果

### 12.2 无状态认证流程

对每个请求：

1. 解析 realm
2. 提取 token
3. 校验 token
4. 读取 claims
5. 获取 `loginId`
6. 按 `realmId + loginId` 加载 ACL
7. 构造 `AuthContext`
8. 注入请求上下文

### 12.2.1 内部 token 反查流程

对非 Web 场景或内部服务调用，建议支持：

```python
result = auth.inspect_token(token_value, realm=None)
```

标准流程：

1. 解析或定位 realm
2. 用 token 原文查 token hash / token id
3. 读取 token record
4. 校验 token 状态和 TTL
5. 读取 claims
6. 读取 session
7. 获取 `loginId`
8. 按策略加载 ACL
9. 返回 `TokenLookupResult`

这条链路不能依赖 HTTP request。

### 12.3 登出流程

支持：

- 当前 token 登出
- 当前 session 登出
- 全部 session 登出

### 12.4 强制下线流程

支持：

- 踢单 token
- 踢单 session
- 踢某主体全部 session
- 踢某 realm 下所有 session

## 13. 鉴权流程设计

### 13.1 登录校验

`require_login()` 只做：

- 已认证判断
- 可选主体状态判断

### 13.2 角色校验

`require_roles(...)`

支持：

- AND
- OR
- NOT

### 13.3 权限校验

`require_permissions(...)`

支持：

- AND
- OR
- matcher 自定义

### 13.4 Scope 校验

预留：

- `require_scope(...)`

用于以后做数据范围能力。

## 14. realm 解析策略

realm 解析必须显式设计，不应只依赖 URL path。

建议支持多种解析器链：

1. 显式装饰器参数
2. 路由级绑定
3. Host 绑定
4. Path 前缀绑定
5. Header 指定
6. 默认 realm
7. token record 中携带的 realm
8. 内部调用显式传入 realm

解析器接口：

```python
realm = realm_resolver.resolve(request_context)
```

采用责任链模式，先命中先返回。

## 15. 请求上下文模型

### 15.1 Framework-neutral RequestContext

设计一个中立抽象：

- `method`
- `path`
- `headers`
- `cookies`
- `query`
- `client_ip`
- `user_agent`
- `transport`
- `framework_request`

核心层只依赖这个抽象。

### 15.1.1 非请求场景上下文

除了 `RequestContext`，还应支持通用执行上下文：

- `ExecutionContext`

可包含：

- `source`
- `request_id`
- `trace_id`
- `metadata`

供以下场景使用：

- worker
- CLI
- 定时任务
- 内部服务调用
- token inspect / revoke / refresh

### 15.2 AuthContext Injection

adapter 负责将 `AuthContext` 注入框架请求对象。

例如 FastAPI 可注入：

- `request.state.auth`
- `request.state.subject`

但核心层不依赖这些字段名。

## 16. 框架适配设计

### 16.1 FastAPI / Starlette

需要提供：

- middleware
- dependency
- decorators
- exception handlers

### 16.2 Flask

需要提供：

- `before_request`
- `g.auth`
- decorators

### 16.3 Django

需要提供：

- middleware
- request attribute injection
- decorators / mixins

### 16.4 适配原则

adapter 做三件事：

1. 把框架请求转换为 `RequestContext`
2. 调用 core 服务
3. 把 `AuthContext` 写回框架上下文

adapter 不能承载业务规则。

### 16.4.1 非 Web 接入

除了 Web adapter，还建议提供轻量 runtime API，供普通 Python 代码直接调用：

```python
mico_auth.inspect_token(token)
mico_auth.get_login_id_by_token(token)
mico_auth.get_session_by_token(token)
mico_auth.is_token_valid(token)
mico_auth.revoke_token(token)
```

这部分不属于 adapter，而属于核心应用服务。

### 16.5 pip extras 与 adapter 对应关系

建议可选依赖设计：

- `micosauth[jwt]`
- `micosauth[fastapi]`
- `micosauth[flask]`
- `micosauth[django]`
- `micosauth[full]`

建议映射关系：

- `fastapi`
  - `fastapi`
  - `starlette`
- `flask`
  - `flask`
- `django`
  - `django`
- `jwt`
  - `pyjwt` 或等价库
- `full`
  - 聚合所有稳定扩展

这样使用者只安装所需框架能力；Redis 作为基础依赖默认安装。

## 17. 装饰器与依赖注入 API 设计

应该同时提供两种使用方式：

### 17.1 声明式 guard

例如：

```python
@require_login()
@require_permissions("sys:user:create")
```

### 17.2 依赖式 guard

例如：

```python
Depends(auth.require_login_dep())
```

原因是不同团队对声明方式偏好不同。

### 17.3 显式 realm

所有 guard 都必须支持：

- `realm="admin"`

### 17.4 推断 realm

如果不显式指定，则通过 `realm_resolver` 决定。

## 18. 异常与错误模型

必须有统一错误语义：

- `AuthenticationRequired`
- `InvalidToken`
- `TokenExpired`
- `SessionRevoked`
- `SubjectDisabled`
- `PermissionDenied`
- `RoleDenied`
- `RealmNotFound`
- `RealmResolutionError`
- `ACLLoadError`
- `TokenNotFound`
- `TokenRevoked`
- `SessionNotFound`

每个 adapter 再把这些异常映射为具体 HTTP 响应。

## 19. 审计与可观测性

建议内置事件和指标埋点：

- 登录成功次数
- 登录失败次数
- token 校验失败次数
- 权限拒绝次数
- 在线 session 数
- ACL 加载耗时
- Redis 操作耗时

同时记录标准审计事件：

- who
- realm
- action
- target
- result
- timestamp
- request metadata

## 20. 安全设计要求

### 20.1 token 安全

- 使用高熵随机 token
- 支持 token hashing 存储
- 支持定期轮换签名密钥
- 支持 refresh token 分离

### 20.2 防重放

可选支持：

- nonce
- 单设备绑定
- token version

### 20.3 会话控制

支持：

- 单点登录
- 限制并发会话数
- 限制设备数
- 新登录挤掉旧会话

### 20.4 风险控制扩展点

预留：

- 风险评分器
- 登录策略决策器
- 验证码接入点
- 二次验证接入点

## 21. 包结构建议

建议独立包结构如下：

```text
micosauth/
  core/
  spi/
  infra/
    redis/
    jwt/
    memory/
  application/
  adapters/
    fastapi/
    flask/
    django/
  config/
  exceptions/
  events/
  utils/
```

建议仓库根结构：

```text
micosauth/
  pyproject.toml
  README.md
  LICENSE
  docs/
  tests/
  micosauth/
```

### 21.1 导入路径建议

推荐导入入口：

```python
from micosauth import MicosService, MicosSetting, MicosAuthUtil, MicosSessionUtil
from micosauth.adapters.fastapi import AuthMiddleware
from micosauth.guards import require_login, require_permissions
```

### 21.2 公开 API 控制

作为 pip 包，必须严格区分：

- public API
- internal API

建议规则：

- `micosauth.*` 顶层只暴露稳定 API
- `micosauth._internal.*` 放内部实现
- 所有未文档化的模块默认不承诺稳定性

否则包一旦被第三方依赖，后续演进成本会非常高。

## 22. 推荐公共 API

建议对业务开放的 API 足够少：

### 22.0 使用模型

产品使用时分为两个工具：

- `MicosAuthUtil`
- `MicosSessionUtil`

并配套两个基础对象：

- `MicosService`
- `MicosSetting`

它们的职责不是业务运行期高频调用，而是启动阶段完成一次性接入和装配。

职责划分：

- `MicosAuthUtil`
  - 主工具
  - 登录
  - 登出
  - 登录状态校验
  - 角色校验
  - 权限校验
  - 数据范围获取
  - token 反查
  - token 撤销
  - session 撤销
  - 指定 `loginId` 下线
- `MicosSessionUtil`
  - 辅助工具
  - 查询 session
  - 查询在线状态
  - 查询统计
  - 在线统计
  - token 列表查询
  - 会话分析查询
  - 会话图表查询

### 22.1 初始化

```python
micos_setting = MicosSetting(...)
micos_service = MicosService(micos_setting)
micos_auth = MicosAuthUtil(micos_service)
micos_session = MicosSessionUtil(micos_service)
```

产品语义上：

- `MicosSetting` 用于启动配置
- `MicosService` 用于启动装配
- 启动完成后，业务主要直接使用 `MicosAuthUtil`、`MicosSessionUtil`、装饰器和运行时能力

### 22.2 登录登出

```python
micos_auth.login(...)
micos_auth.logout_current(...)
micos_auth.logout_session(...)
micos_auth.logout_login_id(...)
micos_auth.revoke_token(...)
micos_auth.revoke_session(...)
micos_auth.kickout_login_id(...)
micos_auth.create_temp_token(temp_id=..., type=..., time=...)
micos_auth.verify_temp_token(...)
```

### 22.3 会话管理

```python
micos_session.list_sessions(...)
micos_session.get_session(...)
micos_session.list_tokens(...)
micos_session.count_online_sessions(...)
micos_session.count_login_id_sessions(...)
micos_session.get_analysis(...)
micos_session.get_chart_data(...)
```

### 22.3.1 token 反查 API

这部分是本包必须暴露的公共能力：

```python
micos_auth.inspect_token(token, realm=None)
micos_auth.get_claims_by_token(token, realm=None)
micos_auth.get_login_id_by_token(token, realm=None)
micos_auth.get_session_by_token(token, realm=None)
micos_auth.get_realm_by_token(token)
micos_auth.is_token_valid(token, realm=None)
micos_auth.touch_token(token, realm=None)
micos_auth.inspect_temp_token(token, type=None)
```

这些接口适用于：

- 内部服务
- worker
- CLI
- WebSocket 握手后续处理
- RPC token 透传校验

### 22.4 guard

```python
micos_auth.require_login(...)
micos_auth.require_roles(...)
micos_auth.require_permissions(...)
```

### 22.5 adapter

```python
app.add_middleware(auth.fastapi.middleware())
```

### 22.6 pip 包友好的公共 API 设计原则

公共 API 需要满足：

- 名称稳定
- 参数显式
- 不依赖项目内部对象
- 尽量不暴露底层存储细节
- 尽量不要求用户继承复杂基类

推荐尽量以：

- 配置对象
- 协议接口
- 工厂函数

作为对外边界，而不是大量要求用户操作内部类。

## 23. 开箱即用能力

为了做到“只要引入，并配置就可以使用”，需要内置默认实现：

- Redis token store
- Redis session store
- 默认 Redis 配置模型
- 默认 permission matcher
- 默认异常映射
- 默认 realm resolver
- 默认 FastAPI adapter
- 默认 `loginId` 登录流程编排
- 默认 64 位字符串 token 生成策略
- 默认 token inspect 服务
- 默认临时 token 服务

但以下内容必须由业务提供：

- 项目配置到产品配置的映射
- `AccessProvider`

这是通用包与业务系统的天然边界。

### 23.1 pip 包文档必须覆盖的开箱路径

为了让这个包真正具备“安装即可接入”的体验，文档必须至少包含：

1. 5 分钟快速开始
2. FastAPI 最小示例
3. Redis 初始化示例
4. 多 realm 示例
5. 自定义 ACLProvider 示例
6. 常见异常处理示例

没有这些，包即使设计得对，也很难在真实项目里推广。

## 24. 推荐接入方式

### 24.1 最小接入

业务只提供：

- `IdentityProvider`
- `CredentialVerifier`
- `ACLProvider`
- realm 配置

非 Web 项目的最小接入也应成立，不要求必须安装任何 Web adapter。
但 Redis 仍然是最小接入前提。

### 24.2 标准接入

再补充：

- 事件发布器
- 审计器
- 指标上报
- 自定义异常格式

### 24.3 企业接入

再扩展：

- 多种登录方式
- 多级 realm resolver
- JWT + opaque 双模式
- 多租户支持
- 风险控制策略

## 25. 兼容性要求

### 25.1 Python 版本

建议：

- Python 3.11+

### 25.2 Web 框架

首批：

- FastAPI / Starlette
- Flask

第二批：

- Django
- AioHTTP

### 25.3 Redis

建议支持：

- 单机 Redis
- Sentinel
- Cluster

通过业务侧注入客户端或工厂函数来兼容。

### 25.4 pip 发布兼容性

发布层面建议：

- 使用 `pyproject.toml`
- 使用 PEP 621 metadata
- wheel 与 sdist 同时发布
- 明确 Python 版本 `requires-python`
- extras 在元数据中声明

建议：

- 默认发布到 PyPI
- 同时支持组织私有源镜像

## 26. 关键设计选择

### 26.1 选择 realm 作为一级隔离

原因：

- 多用户体系场景很常见
- 比单一 `Auth` 抽象更通用
- 能自然隔离 token 命名空间和 ACL 策略

### 26.2 选择 SPI 而不是写死 ORM

原因：

- 通用包不能耦合 SQLAlchemy
- 不能假设用户表结构
- 不能假设角色权限模型

### 26.3 默认使用 Opaque Token + Redis

原因：

- 更容易撤销
- 更容易做 session 管控
- 更适合企业后台

这里不是“默认但可替换”，而是首期产品明确绑定这一基础方案。

并且普通访问 token 的默认表现形式明确为：

- 64 位字符串 token

### 26.4 核心层不绑定 HTTP

原因：

- 便于 WebSocket / RPC 扩展
- 便于单元测试
- 便于多框架适配

### 26.5 token 反查必须进入公共 API

原因：

- 很多系统不仅要“拦请求”，还要“内部拿 token 查身份”
- WebSocket、MQ、RPC、后台任务都常见这种需求
- 如果只把它做成 middleware 内部逻辑，包就不通用

## 27. 风险点与设计约束

### 27.1 不可能完全零配置

通用认证包不可能知道你的用户数据在哪里，因此：

- 身份加载
- 凭证校验
- ACL 加载

这三件事必须由业务注入。

### 27.2 ACL 设计不能过重

如果一开始把菜单、资源树、数据权限 SQL 都塞进核心，会导致包不可复用。

### 27.3 framework adapter 容易膨胀

要严格限制 adapter 的责任，只做适配，不做业务编排。

### 27.4 Redis 模型要考虑高并发

需要避免：

- 巨大 session set
- 无界索引
- 清理策略缺失

后续实现阶段要重点压测。

## 28. 第一阶段实现边界

建议 MVP 只做：

1. 多 realm
2. Opaque Token
3. Redis token/session 管理
4. ACLProvider SPI
5. 登录 / 登出 / 强制下线
6. 登录 / 角色 / 权限 guard
7. FastAPI adapter
8. token inspect / reverse lookup API
9. 临时 token 能力

先不做：

- JWT 双模
- refresh token
- 多因子认证
- Django / Flask 全量适配
- 风险控制

### 28.1 第一阶段发布形态

MVP 阶段就应以 pip 包形态交付，而不是先写成项目内模块再拆。

第一阶段交付物应包括：

- `pyproject.toml`
- 核心包代码
- FastAPI adapter
- README 快速开始
- 基础 API 文档
- 单元测试
- 集成测试
- 可发布 wheel

## 29. 第二阶段扩展

- JWT mode
- refresh token
- 更多 adapter
- 审计事件总线
- ACL 缓存版本化
- device/session policy
- tenant-aware realm policy

同时可考虑：

- Flask extras
- Django extras
- 插件 entry points
- 官方扩展包拆分

## 30. 建议的落地顺序

### 30.1 Phase 1

- 定义 core 模型
- 定义 SPI
- 完成 Redis store
- 完成 AuthManager
- 完成 FastAPI adapter

### 30.2 Phase 2

- 完成 guard API
- 完成 session 管理 API
- 完成事件模型
- 完成指标埋点

### 30.3 Phase 3

- 完成 JWT 模式
- 完成 Flask adapter
- 完成更细粒度 session policy

### 30.4 Phase 4

- 稳定 public API
- 发布 `1.0.0`
- 建立迁移说明
- 建立 deprecation policy

## 30.5 版本策略

作为 pip 包，必须明确版本策略：

- `0.x`
  - 快速演进阶段
  - API 可能变化
- `1.x`
  - public API 稳定
  - 遵循语义化版本

建议：

- 在 `1.0.0` 前，不承诺全部内部 API 稳定
- 从 `1.0.0` 开始，只对文档中声明的 public API 做稳定承诺

## 30.6 插件机制

为了增强 pip 包生态能力，建议支持插件发现机制。

可选方式：

1. Python entry points
2. 显式注册

例如允许第三方包注册：

- 自定义 token transport
- 自定义 adapter
- 自定义 matcher
- 自定义 event sink

推荐第一阶段只支持显式注册，第二阶段再引入 entry points。

## 31. 方案总结

目标中的“通用”不是指这个包替业务决定一切，而是指：

- 统一抽象
- 稳定边界
- 明确 SPI
- 默认实现完整
- 接入成本低

最终形态应当是：

> 一个可通过 `pip` 安装、以 realm 为隔离单位、以 Redis 为必选会话后端、既支持多种 Python 运行场景接入、也支持通过 token 反查 subject/session/claims 的通用认证鉴权包。

业务侧只需补齐三类能力：

- 身份加载
- 凭证校验
- ACL 加载

其余认证鉴权基础设施都由包统一提供。
