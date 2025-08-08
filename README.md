# Astro Code OJ 系统文档

## 项目简介

Astro Code OJ 是一个基于 Java 的在线评测系统，支持题目管理、用户管理、题集管理、代码提交与评测、AI 聊天辅助等功能。系统采用 Spring Boot + MyBatis Plus 构建后端服务，前端使用 Vue 3 + TypeScript + Vite 构建管理界面。

系统支持以下核心模块：

- **题目管理**：支持题目增删改查、代码提交与评测。
- **题集管理**：支持题集创建、进度跟踪、提交与评测。
- **用户管理**：支持用户注册、登录、权限管理。
- **系统管理**：支持公告、横幅、配置、字典、角色等系统级管理。
- **AI 聊天**：集成 DashScope AI 接口，提供题目辅助解答。
- **文件管理**：支持文件上传、下载、预览。
- **服务监控**：集成 Nacos 服务发现，支持服务健康状态检查。
- **消息队列**：使用 RabbitMQ 实现异步评测任务处理。

## 技术栈

### 后端
- Java 17
- Spring Boot
- MyBatis Plus
- Redis
- RabbitMQ
- Nacos
- Swagger
- Maven / Spring Cloud

### 前端
- Vue 3
- TypeScript
- Vite
- Naive UI
- Vue Router
- Pinia

## 功能模块

### 用户模块
- 用户注册、登录、退出
- 用户信息管理
- 用户组、角色、权限管理
- 用户行为记录（如题目浏览）

### 题目模块
- 题目增删改查
- 题目分类管理
- 题目标签管理
- 代码提交与评测
- 提交记录查看
- 代码相似度检测

### 题集模块
- 题集创建、编辑、删除
- 题集进度跟踪
- 题集提交与评测
- 题集代码相似度检测

### 系统模块
- 系统公告管理
- 系统横幅管理
- 系统配置管理（如文件上传限制、允许扩展名等）
- 字典管理（数据字典维护）
- AI 聊天接口集成

### 文件模块
- 文件上传、下载、预览
- 文件删除
- 支持本地存储

### 监控模块
- 服务健康状态检查
- Nacos 服务注册与发现集成

### 消息队列模块
- 使用 RabbitMQ 实现异步评测任务处理
- 支持题目评测、题集评测、代码相似度检测等任务队列

## 后端结构说明

### 模块划分

- `modular/problem`：题目相关逻辑，包括题目、提交、解决、相似度检测等。
- `modular/set`：题集相关逻辑，包括题集、提交、解决、进度、相似度检测等。
- `modular/sys`：系统管理模块，包括用户、角色、权限、公告、横幅、配置、字典等。
- `modular/auth`：用户认证模块，包括登录、注册、验证码、用户信息等。
- `modular/chat`：AI 聊天模块，集成 DashScope API。
- `modular/file`：文件上传与管理模块。
- `modular/monitor`：服务健康监控模块。
- `mq`：消息队列配置，包括 RabbitMQ 的 Exchange、Queue、Binding 配置。

### 核心类说明

- `CoreApplication.java`：Spring Boot 启动类。
- `RabbitConfig.java`：RabbitMQ 配置类。
- `AiConfig.java`：AI 聊天配置类。
- `FileStorageConfig.java`：文件存储配置类。
- `MybatisPlusRedisCache.java`：MyBatis Plus 与 Redis 集成的二级缓存实现。
- `RedisConfig.java`：Redis 缓存配置类。
- `HealthController.java`：服务健康状态检查接口。

## 前端结构说明

### 技术栈
- Vue 3 + TypeScript + Vite
- Naive UI 作为组件库
- Vue Router + Pinia 状态管理
- Alova 网络请求封装
- UnoCSS 实现原子化样式

### 页面结构

- `login.vue`：登录页面
- `dashboard/index.vue`：仪表盘页面，包含统计图表
- `modular/problem/problem/index.vue`：题目管理页面
- `modular/problem/submit/index.vue`：提交记录页面
- `modular/problem/solved/index.vue`：已解决题目页面
- `modular/set/set/index.vue`：题集管理页面
- `modular/set/submit/index.vue`：题集提交页面
- `modular/set/solved/index.vue`：题集解决记录页面
- `modular/sys/user/index.vue`：用户管理页面
- `modular/sys/role/index.vue`：角色管理页面
- `modular/sys/banner/index.vue`：横幅管理页面
- `modular/sys/config/index.vue`：系统配置页面
- `modular/sys/dict/index.vue`：字典管理页面
- `modular/sys/article/index.vue`：系统文章管理页面
- `modular/sys/notice/index.vue`：公告管理页面

## 安装与部署

### 后端部署

1. **依赖安装**
   - JDK 17+
   - Maven / Gradle
   - Redis
   - RabbitMQ
   - MySQL / PostgreSQL
   - Nacos（用于服务注册与发现）

2. **配置文件**
   - 修改 `application.yaml` 中的数据库、Redis、RabbitMQ、Nacos 等配置。

3. **启动服务**
   ```bash
   mvn spring-boot:run
   # 或使用构建命令
   mvn package
   java -jar target/app-core.jar
   ```

4. **访问接口文档**
   - 启动后访问：`http://localhost:8080/swagger-ui.html` 查看 API 接口文档。

### 前端部署

1. **依赖安装**
   - Node.js 18+
   - pnpm（推荐）

2. **安装依赖**
   ```bash
   pnpm install
   ```

3. **启动开发服务器**
   ```bash
   pnpm dev
   ```

4. **构建生产环境**
   ```bash
   pnpm build
   ```

5. **部署**
   - 构建完成后，将 `dist` 目录部署到 Nginx 或静态服务器即可。

## 使用说明

### 用户管理
- 用户可注册、登录、退出。
- 管理员可管理用户、角色、权限、用户组等。

### 题目管理
- 管理员可添加、编辑、删除题目。
- 用户可查看题目、提交代码、查看提交结果。
- 支持代码相似度检测。

### 题集管理
- 管理员可创建题集、添加题目。
- 用户可查看题集、提交题集题目、查看进度。
- 支持题集代码相似度检测。

### AI 聊天
- 用户可通过 `/chat/stream` 接口与 AI 交互。
- 支持通过题目 ID 获取题目描述、标题、测试用例等信息。

### 文件管理
- 支持文件上传、下载、预览、删除。
- 支持配置上传目录、最大文件大小、允许扩展名等。

### 服务监控
- 通过 `/sys/monitor/health/status` 接口查看服务健康状态。
- 支持服务实例、集群、节点级别的健康检查。

### 消息队列
- 所有评测任务通过 RabbitMQ 异步处理。
- 支持题目评测、题集评测、代码相似度检测等任务队列。

## API 接口文档

详见 Swagger 接口文档：
- `http://localhost:29093/doc.html`

## 预览

**首页**

![img.png](/doc/img_0.png)

**题库**

![img.png](/doc/img_1.png)

**题集**

![img.png](/doc/img_7.png)

**排行榜**

![img_2.png](/doc/img_2.png)

**提交状态**

![img_3.png](/doc/img_3.png)

**管理后台**

![img_4.png](/doc/img_4.png)

**题目编辑**

![img_5.png](/doc/img_5.png)

**字典管理**

![img_6.png](/doc/img_6.png)

**提交页**

代码提交

![img.png](/doc/img_8.png)

测评结果

![img.png](/doc/img_9.png)

## 贡献指南

欢迎贡献代码，请遵循以下流程：

1. Fork 本项目
2. 创建新分支（feature/xxx）
3. 提交 Pull Request
4. 等待审核与合并

## 许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT)，欢迎自由使用与修改。

## 联系方式

如有问题，请联系项目维护者或提交 Issue。