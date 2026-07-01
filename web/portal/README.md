# HEI FastAPI Portal

`web/portal` 是 HEI FastAPI 脚手架的门户端前端应用，面向普通门户用户和公开访问场景。它使用 portal 用户体系和 `/api/v1/portal/*` 接口，不再复用 admin 用户体系。

## 功能范围

- 门户首页。
- 登录、注册、退出、注销。
- 用户中心：门户用户个人资料、密码、手机号、邮箱维护。
- 我的空间：当前用户公开主页。
- 公开空间：`/space/:accountId` 面向其他用户展示公开资料。
- 动态资源路由和菜单。
- 字典、Banner 展示。
- 消息、通知、待办、实时事件。

## 技术栈

- Vue 3
- Vite
- TypeScript
- Naive UI
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
VITE_PORT=5163
VITE_HOME_PATH="/home"
VITE_PUBLIC_ROUTE_PATHS="/home,/auth/login,/auth/register,/auth/forgot-password,/auth/reset-password,/not-found"
VITE_ROUTE_LOAD_MODE="dynamic"
VITE_API_URL="http://127.0.0.1:8000"
```

后端默认运行在 `http://127.0.0.1:8000`，portal 前端请求 `/api/v1/portal/*`。

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
docker build -t hei-fastapi-portal .
docker run -e BACKEND_URL="http://host.docker.internal:8000" -p 8082:80 hei-fastapi-portal
```

镜像说明：

- Node 22 alpine 构建 `dist/`。
- nginx 1.27 alpine 托管静态资源。
- 容器内 nginx 监听 `80`。
- `BACKEND_URL` 在容器启动时渲染到 nginx 配置中。
- nginx 已配置 SPA fallback、`/assets/` 长缓存、`/api/` 反向代理和 SSE 支持。

## 目录结构

```text
src/
  api/          portal API 封装
  components/   通用组件
  hooks/        组合式工具
  i18n/         国际化装配
  layouts/      门户布局
  router/       路由与守卫
  stores/       Pinia 状态
  utils/        axios、权限、字典等工具
  views/        页面
locales/        国际化文案
nginx/          生产 nginx 模板
```

## API 边界

portal 前端不应调用 `/api/v1/admin/*`。当前主要接口前缀：

- 认证和用户中心：`/api/v1/portal`
- 动态资源：`/api/v1/portal/sys/resources/current`
- 字典：`/api/v1/portal/sys/dicts/tree`
- Banner：`/api/v1/portal/sys/banners/list`
- 消息：`/api/v1/portal/message/*`
- 公开空间：`/api/v1/portal/spaces/{account_id}`

## 路由和菜单

portal 的导航菜单由首页静态入口和后端动态资源组成。用户中心和我的空间属于内置路由，通过头像下拉菜单进入，不应手动拼入主导航菜单。
