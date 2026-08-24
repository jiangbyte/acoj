# Astro Code OJ (AC OJ)

![JDK](https://img.shields.io/badge/JDK-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Supported-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Supported-DC382D?logo=redis&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/version-1.0.0-orange)

**Astro Code OJ（AC OJ）** 是一套面向编程教学与算法训练的在线评测系统：Java / Spring Cloud 负责业务与管理 API，Go（go-zero）负责判题与代码相似度服务，Vue 3 提供管理端与用户端。支持多语言评测、题集与竞赛、异步判题调度，并可接入 AI 辅助能力。

> 当前版本：`1.0.0` · 协议：[MIT License](LICENSE) · 仓库：[jiangbyte/acoj](https://github.com/jiangbyte/acoj)

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [工程结构](#工程结构)
- [快速开始](#快速开始)
- [性能测试](#性能测试)
- [License](#license)

## 功能特性

| 模块 | 说明 |
| --- | --- |
| 题目体系 | 题目增删改查、分类标签、样例 / 测试点、难度分级 |
| 多语言评测 | C / C++、Java、Python、Go 等；由 Go 判题服务执行 |
| 用户与权限 | 角色权限、排行榜、学习进度与提交统计 |
| 题集 / 竞赛 | 自定义题集、进度追踪、竞赛相关数据模型 |
| 异步判题 | RabbitMQ 任务投递，支持高并发提交 |
| 相似度检测 | 独立 similarity-service，辅助查重分析 |
| AI 辅助 | 可对接大模型接口，提供题目解析与代码建议（按部署配置启用） |
| 双端前端 | `admin` 管理端 + `pc` 用户端（Vue 3 / Naive UI / Monaco） |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 业务后端 | JDK 21 · Spring Boot 3.2 · Spring Cloud · Maven 多模块 |
| 持久化 / 缓存 | MySQL · MyBatis-Plus · Redis / Redisson · Sa-Token |
| 中间件 | Nacos（配置与发现）· RabbitMQ · MinIO（对象存储） |
| 判题 / 查重 | Go 1.24 · go-zero · ANTLR 语法资源（`antlrv4/`） |
| 前端 | Vue 3 · TypeScript · Vite · Naive UI · Pinia · Monaco Editor |
| 文档 | Knife4j |

## 工程结构

```text
astro-code-oj/
├── pom.xml                 # Maven 聚合根（revision 1.0.0）
├── galaxy-dependencies/    # 依赖 BOM
├── galaxy-common/          # 公共框架（base-framework）
├── galaxy-oj/              # 主业务服务（默认端口 89）
├── judge-service/          # Go 判题服务
├── similarity-service/     # Go 相似度服务
├── admin/                  # Vue 3 管理端
├── pc/                     # Vue 3 用户端
├── sql/                    # 数据库脚本
├── antlrv4/                # 多语言 ANTLR 文法
├── test/                   # 压测与用户数据生成链路
│   ├── User Generate/      # 生成 / 注册 / Token 填充
│   └── ojtest/             # k6 压测与汇总绘图
└── depoloy/                # 部署相关（本地私有，默认 gitignore）
```

## 快速开始

### 环境要求

- JDK **21**、Maven **3.8+**
- MySQL **8+**、Redis、RabbitMQ、Nacos
- Go **1.24+**（判题 / 相似度服务）
- Node.js **18+**、pnpm（前端）

### 1. 初始化数据库

导入 `sql/` 下脚本（按你的环境选择最新可用 SQL，例如 `sql/astro_code_05.sql`）：

```bash
mysql -u root -p -e "CREATE DATABASE astro_code DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p astro_code < sql/astro_code_05.sql
```

### 2. 配置与启动业务后端

`galaxy-oj` 通过 Nacos 拉取 `astro-code-common.yaml` 与 `galaxy-oj.yaml`（见 `galaxy-oj/src/main/resources/application.yaml`）。本地默认 Maven profile 为 `dev`，Nacos 地址默认为 `localhost:8848`。

```bash
# 在仓库根目录
mvn -pl galaxy-oj -am clean package -DskipTests
mvn -pl galaxy-oj spring-boot:run
```

| 项 | 默认 |
| --- | --- |
| API | http://127.0.0.1:89 |
| Profile | `dev`（见根 `pom.xml`） |

### 3. 启动判题 / 相似度服务

```bash
# 判题
cd judge-service
go run main.go -f etc/judge.yaml -nacos

# 相似度（另开终端）
cd similarity-service
go run main.go -f etc/similar.yaml
```

配置见各服务 `etc/*.yaml`（Nacos 地址、命名空间等按环境修改）。

### 4. 启动前端

```bash
# 管理端
cd admin
pnpm install
pnpm dev          # 默认读取 .env.dev 中的 VITE_GATEWAY

# 用户端
cd pc
pnpm install
pnpm dev
```

将 `admin/.env.dev` / `pc/.env.dev` 中的 `VITE_GATEWAY` 指向本机网关或 `galaxy-oj` 地址（例如 `http://localhost:89`）。

## 性能测试

仓库内保留了一套 k6 压测链路，位于 `test/`：

| 路径 | 用途 |
| --- | --- |
| `test/User Generate/user_gen.py` | 生成 `测试用户数据_1000个.csv` |
| `test/User Generate/register.py` | 批量注册 |
| `test/User Generate/login.py` | 登录并将 Token 写回 CSV |
| `test/ojtest/{100..250}/` | 各并发档 k6 脚本与原始结果 |
| `test/ojtest/analyze_k6_batch.py` | 汇总 CSV 与趋势图 |

在对应并发目录执行 `run-k6-tests.ps1`（脚本通过 `$PSScriptRoot` 定位同目录 `optimized-oj-test.js`），汇总时在 `test/ojtest` 下运行分析脚本。

## License

本项目基于 [MIT License](LICENSE) 开源。
