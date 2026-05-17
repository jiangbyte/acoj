---
name: readme-github-actions-vitepress
description: Design for updating README, adding GitHub Actions, and creating VitePress docs site for hei-admin-vue
metadata:
  type: spec
---

# README / GitHub Actions / VitePress 文档站设计

## 目标

对 hei-admin-vue 项目进行三项改进：
1. 根据项目实际情况更新 README 内容
2. 参考 hei-gin 创建 GitHub Actions workflow（VitePress 部署到 GitHub Pages）
3. 创建完整的 VitePress 中文文档站

## 1. README 更新

### 变更点
- 更新技术栈版本号与实际 package.json 一致
- 修正后端框架引用：移除 Hei Boot / Hei Cloud，以 Hei Gin 为主要后端参考（开发代理端口 18885 指向 hei-gin）
- 保留 Hei Boot / Hei Cloud 在相关项目列表中但标注清晰
- 补充实际项目的关键配置说明（开发代理、SM2 加密等）
- 添加 GitHub Actions CI 状态 badge
- 更新在线文档链接为新 VitePress 站点

## 2. GitHub Actions

### Workflow: VitePress 文档部署
- 文件位置：`.github/workflows/deploy.yml`
- 触发条件：push 到 main 分支 + workflow_dispatch 手动触发
- 构建环境：ubuntu-latest, Node.js 24, pnpm 9
- 构建路径：vitepress（working-directory）
- 构建命令：pnpm run docs:build
- 产物上传：vitepress/docs/.vitepress/dist
- 部署方式：actions/deploy-pages@v4

### 与 hei-gin 的差异
- 使用 pnpm 而非 npm（hei-admin-vue 已使用 pnpm）
- 其余结构完全对齐 hei-gin

## 3. VitePress 文档站

### 目录结构
```
vitepress/
├── package.json
└── docs/
    ├── .vitepress/
    │   └── config.mts
    ├── index.md
    ├── overview.md
    ├── guide/
    │   ├── index.md
    │   ├── quickstart.md
    │   └── structure.md
    ├── features/
    │   ├── index.md
    │   ├── auth.md
    │   ├── permission.md
    │   ├── theme-layout.md
    │   ├── components.md
    │   └── http.md
    └── modules/
        ├── index.md
        └── development.md
```

### 站点配置
- 语言：zh-CN
- 标题：Hei Admin Vue
- base URL：/hei-admin-vue/
- 搜索：local 全文搜索
- 代码行号：开启
- 社交链接：GitHub

### 导航栏
| 项 | 链接 |
|---|------|
| 首页 | / |
| 指南 | /guide/ |
| 功能 | /features/ |
| 模块开发 | /modules/ |
| 相关项目（下拉） | Hei Gin / Hei Boot / Hei FastAPI |

### 首页
Hero 区域 + 特性卡片（6 个：Vue3 驱动、Ant Design Vue、TypeScript、UnoCSS、Alova 请求、Pinia 状态管理）

### 页面内容规划

| 页面 | 内容 |
|------|------|
| overview.md | 框架概述、设计理念、适用场景 |
| guide/quickstart.md | 环境要求、安装、启动开发服务、构建 |
| guide/structure.md | 项目目录结构说明 |
| features/auth.md | SM2 加密、JWT 认证、双端认证、登录流程 |
| features/permission.md | RBAC、路由权限、按钮权限、权限指令 |
| features/theme-layout.md | 布局模式、主题切换、响应式、国际化 |
| features/components.md | 表格、表单、弹窗、树形组件等 |
| features/http.md | Alova 封装、请求/响应拦截、错误处理 |
| modules/development.md | 模块开发规范、目录约定 |

## 实施步骤

1. 创建 vitepress/package.json
2. 创建 vitepress/docs/.vitepress/config.mts
3. 创建首页 index.md
4. 创建 guide/ 系列页面
5. 创建 features/ 系列页面
6. 创建 modules/ 系列页面
7. 创建 .github/workflows/deploy.yml
8. 更新 README.md
