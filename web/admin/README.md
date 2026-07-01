# HEI FastAPI Admin

`web/admin` 是 HEI FastAPI 脚手架的管理端前端应用，面向系统管理员和运营人员。它使用 admin 用户体系和 `/api/v1/admin/*` 接口，不与 portal 用户体系混用。

## 功能范围

- 登录、注册、退出、注销。
- 动态资源路由和菜单。
- IAM/RBAC：账号、部门、用户组、角色、资源、岗位、权限授权。
- 系统模块：字典、Banner、文件相关能力。
- 消息模块：通知、站内消息、待办、实时事件。
- 用户中心：管理端个人资料和安全设置。

## 技术栈

- Vue 3
- Vite
- TypeScript
- Naive UI / Pro Naive UI
- Pinia
- UnoCSS
- axios
- vue-i18n
- vue-router

## 本地开发

```bash
pnpm install
pnpm dev
```

默认环境变量见 `.env`：

```env
VITE_PORT=5173
VITE_HOME_PATH="/dashboard"
VITE_ROUTE_LOAD_MODE="dynamic"
VITE_API_URL="http://127.0.0.1:8000"
```

后端默认运行在 `http://127.0.0.1:8000`，admin 前端请求 `/api/v1/admin/*`。

## 常用命令

```bash
pnpm dev
pnpm lint
pnpm build
pnpm preview
pnpm format
pnpm format:check
```

## 生产构建

```bash
pnpm build
```

生产构建读取 `.env.production`。当前配置中：

```env
VITE_API_URL=""
```

表示前端生产产物使用同源 `/api/` 请求，由 nginx 反向代理到后端。

## Docker

```bash
docker build -t hei-fastapi-admin .
docker run -e BACKEND_URL="http://host.docker.internal:8000" -p 8081:81 hei-fastapi-admin
```

镜像说明：

- Node 22 alpine 构建 `dist/`。
- nginx 1.27 alpine 托管静态资源。
- 容器内 nginx 监听 `81`。
- `BACKEND_URL` 在容器启动时渲染到 nginx 配置中。
- nginx 已配置 SPA fallback 和 `/api/` 反向代理。

## 目录结构

```text
src/
  api/          admin API 封装
  components/   通用组件和消息组件
  hooks/        组合式工具
  i18n/         国际化装配
  layouts/      管理端布局
  router/       路由与守卫
  stores/       Pinia 状态
  utils/        axios、权限、字典等工具
  views/        页面
locales/        国际化文案
nginx/          生产 nginx 模板
```

## 路由和菜单

admin 使用动态资源模式时，会调用 `/api/v1/admin/sys/resources/current` 获取当前用户资源，再由前端生成动态路由和菜单。静态内置路由主要包括认证页、错误页和用户中心等基础页面。
