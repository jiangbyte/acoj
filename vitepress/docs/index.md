---
layout: home

hero:
  name: "Hei Gin"
  text: "Go 快速开发框架"
  tagline: 基于 Gin + GORM 构建，开箱即用的后台管理系统解决方案
  image:
    src: /logo.svg
    alt: Hei Gin Logo
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/quickstart
    - theme: alt
      text: 了解架构
      link: /architecture/overview
    - theme: secondary
      text: GitHub
      link: https://github.com/jiangbyte/hei-gin

features:
  - icon: ⚡
    title: 高性能
    details: 基于 Gin 框架，极致性能表现，毫秒级 API 响应
  - icon: 🔐
    title: 双端认证
    details: B端（管理端）和 C端（客户端）独立的 Token 认证和权限体系
  - icon: 🏗️
    title: 插件化架构
    details: Go Workspace 多模块架构，业务以插件自注册，零侵入 SDK
  - icon: 🗄️
    title: RBAC + 数据权限
    details: 完备的 RBAC 权限模型，支持行级数据范围控制
  - icon: 📝
    title: 操作日志
    details: 全量操作记录，SM3 签名防篡改，可审计追溯
  - icon: 📁
    title: 文件存储
    details: 本地/MinIO/S3 多种存储后端，支持分片上传
---
