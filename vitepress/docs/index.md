---
layout: home

hero:
  name: "Hei FastAPI"
  text: "Python 快速开发框架"
  tagline: 基于 FastAPI + SQLAlchemy 2.0 构建，开箱即用的后台管理系统解决方案
  image:
    src: /logo.svg
    alt: Hei FastAPI Logo
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/quickstart
    - theme: alt
      text: 了解架构
      link: /architecture/overview
    - theme: secondary
      text: GitHub
      link: https://github.com/jiangbyte/hei-fastapi

features:
  - icon: ⚡
    title: 高性能
    details: 基于 FastAPI 异步框架，Python 类型安全，自动 OpenAPI 文档生成
  - icon: 🔐
    title: 双端认证
    details: B端（管理端）和 C端（客户端）独立的 JWT 认证和权限体系
  - icon: 🗄️
    title: SQLAlchemy 2.0
    details: 现代化 Mapped 映射，类型安全，Pydantic v2 深度集成
  - icon: 🏗️
    title: RBAC + 数据权限
    details: 完备的 RBAC 权限模型，支持行级数据范围控制，最严策略合并
  - icon: 📝
    title: 操作日志
    details: "@SysLog 装饰器自动录制，支持参数/结果录制，异常日志自动捕获"
  - icon: 📁
    title: 文件存储
    details: 本地/MinIO/S3 多种存储后端，统一接口一键切换
---
