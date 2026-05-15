# Hei Admin Vue

<img width="120" src="https://jiangbyte.github.io/hei-docs/logo.svg">

**Hei Admin Vue** 是 HEI 快速开发框架的 Vue3 前端管理后台解决方案，基于 Vue 3 + Vite + TypeScript + Ant Design Vue 构建。

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.5+-brightgreen.svg)
![Vite](https://img.shields.io/badge/Vite-8.x-orange.svg)
![Ant Design Vue](https://img.shields.io/badge/Ant%20Design%20Vue-4.x-blue)

## 简介

Hei Admin Vue 与 Hei Boot 和 Hei Cloud 后端框架配套使用，提供完整的前后端分离架构解决方案。项目采用现代化的前端技术栈，包含完善的权限管理、丰富的 UI 组件、灵活的布局配置。

**在线文档**: [https://jiangbyte.github.io/hei-docs/hei-admin-vue/](https://jiangbyte.github.io/hei-docs/hei-admin-vue/)

## 技术栈

| 类型 | 技术 |
|------|------|
| 核心框架 | Vue 3.5+、Vite 8.x、TypeScript 6.x |
| UI 组件 | Ant Design Vue 4.x、@ant-design/icons-vue |
| 状态管理 | Pinia（pinia-plugin-persistedstate 持久化） |
| 路由 | Vue Router 4.x |
| HTTP 请求 | Alova 3.x（Fetch / XHR 双适配器） |
| 样式方案 | UnoCSS + Sass |
| 图表 | ECharts 6.x |
| 工具库 | @vueuse/core、Day.js |
| 图片裁剪 | Cropper.js、vue-advanced-cropper |
| 加密 | sm-crypto（国密 SM2/SM3/SM4） |

## 功能模块

| 模块 | 说明 |
|------|------|
| 仪表盘 | 统计卡片、趋势图表、系统信息、公告 |
| 用户管理 | 系统用户 CRUD、角色授权、部门授权、权限授权 |
| 角色管理 | 角色 CRUD、资源授权、权限授权 |
| 资源管理 | 菜单/按钮资源管理 |
| 组织架构 | 部门管理、用户组管理、岗位管理 |
| 客户端用户 | C 端用户管理 |
| 字典管理 | 业务字典维护 |
| 系统配置 | 基础配置、业务配置、存储配置、安全配置 |
| 公告管理 | 系统公告发布与维护 |
| 横幅管理 | 首页横幅管理 |
| 文件管理 | 文件上传、预览、下载 |
| 日志管理 | 操作日志、访问日志 |
| 在线用户 | Token 管理、会话监控 |
| 个人中心 | 个人信息查看与编辑 |

## 项目结构

```
hei-admin-vue/
├── build/               # 构建配置（代理等）
├── public/              # 静态资源
├── src/
│   ├── api/            # API 接口层（Alova）
│   ├── components/     # 公共组件
│   │   ├── form/      # 表单组件（搜索面板、抽屉表单、字典选择）
│   │   ├── layout/    # 布局组件（分割面板、树形面板）
│   │   ├── modal/     # 弹窗组件（导入、导出）
│   │   ├── table/     # 表格组件（CRUD 表格、树形表格）
│   │   ├── result/    # 结果组件（403 页面）
│   │   └── user/      # 用户组件
│   ├── composables/   # 组合式函数
│   ├── hooks/         # 自定义 Hooks（useCrud、useImportExport、useMobile）
│   ├── layouts/       # 布局组件（基础布局、空白布局）
│   ├── router/        # 路由配置（动态路由、路由守卫）
│   ├── store/         # 状态管理（应用、认证、字典、路由、标签页）
│   ├── styles/        # 全局样式
│   ├── types/         # TypeScript 类型定义
│   ├── utils/         # 工具函数（HTTP 封装、主题、图标、字典、下载）
│   │   └── http/     # Alova 实例封装（请求/响应拦截、令牌刷新、错误处理）
│   ├── views/         # 页面组件
│   │   ├── auth/     # 登录/注册/在线监控
│   │   ├── client/   # 客户端用户
│   │   ├── dashboard/# 仪表盘
│   │   ├── error/    # 403/404
│   │   ├── home/     # 首页
│   │   └── sys/      # 系统管理（用户、角色、资源、组织、字典、配置、公告、横幅、文件、日志）
│   ├── App.vue        # 根组件
│   └── main.ts        # 入口文件
├── package.json       # 项目配置
├── vite.config.ts     # Vite 配置
└── tsconfig.json      # TypeScript 配置
```

## 快速开始

```bash
# 安装依赖
pnpm install

# 启动开发服务
pnpm dev

# 构建生产包
pnpm build

# 代码检查
pnpm lint

# 代码格式化
pnpm format
```

## 相关项目

| 项目 | 说明 |
|------|------|
| [Hei Boot](https://github.com/jiangbyte/hei-boot) | Java 单体应用（Spring Boot） |
| [Hei Cloud](https://github.com/jiangbyte/hei-cloud) | Java 微服务架构（Spring Cloud） |
| [Hei FastAPI](https://github.com/jiangbyte/hei-fastapi) | Python 后端（FastAPI） |
| [Hei Gin](https://github.com/jiangbyte/hei-gin) | Go 后端（Gin） |
| [Hei Fastify](https://github.com/jiangbyte/hei-fastify) | Node.js 后端（Fastify） |

## 参与贡献

我们非常欢迎社区贡献！

1. Fork 本仓库
2. 新建 `Feat_xxx` 分支
3. 提交代码
4. 创建 Pull Request

感谢所有为 HEI 项目做出贡献的开发者！

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议

## 联系方式

- [Gitee](https://gitee.com/jiangbyte/hei-admin-vue)
- [GitHub](https://github.com/jiangbyte/hei-admin-vue)
- [掘金](https://juejin.cn/user/1968540037686224)

---

如果这个项目对你有帮助，请给一个 Star 支持！
