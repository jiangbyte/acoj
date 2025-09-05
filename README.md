# Astro Code OJ 系统文档

## 项目简介

Astro Code OJ 是一个基于 Java 的在线评测系统，支持题目管理、用户管理、题集管理、代码提交与评测、AI 聊天辅助等功能。系统采用 Spring Boot + MyBatis Plus 构建后端服务，前端使用 Vue 3 + TypeScript + Vite 构建管理界面。

- **题目管理**：支持题目增删改查、代码提交与评测。
- **题集管理**：支持题集创建、进度跟踪、提交与评测。
- **用户管理**：支持用户注册、登录、用户管理。
- **系统管理**：支持公告、横幅、配置、字典、角色等系统级管理。
- **AI 聊天**：集成 DashScope AI 接口，提供题目解析辅助。
- **文件管理**：支持文件上传、预览。
- **消息队列**：使用 RabbitMQ 实现异步评测任务处理。

## 技术栈

### 后端
- Java 21
- Spring Boot
- MyBatis Plus
- Redis
- RabbitMQ
- Nacos
- Maven
- Spring Cloud

### 前端
- Vue 3
- TypeScript
- Vite
- Naive UI
- Vue Router
- Pinia

## 系统架构

![](doc/images/e892e8bf.png)

## 预览

**首页**

![](doc/images/a82b3f81.png)

**题库**

![](doc/images/e8530edb.png)

**题集**

![](doc/images/dc073a89.png)

**排行榜**

![](doc/images/033125c8.png)

**提交状态**

![](doc/images/3ac037d0.png)

**管理后台**

![](doc/images/d76986a8.png)

![](doc/images/69d964a5.png)

**题目管理**

![](doc/images/049a9c78.png)

![](doc/images/196642b3.png)

**字典管理**

![](doc/images/ff4b5b99.png)

**提交页**

代码提交

![](doc/images/56cab4a0.png)

测评结果

![](doc/images/07dd0fcc.png)

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