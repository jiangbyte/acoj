# HEI FastAPI Portal

`web/portal` 是 HEI FastAPI 的门户端前端应用，面向普通门户用户和公开访问场景。它使用 `PORTAL`
账号体系，只调用 `/api/v1/portal/*`、公共文件接口和公开页面相关接口，不复用 admin 用户体系。

## 功能范围

- 门户首页。
- 认证：登录、注册、退出、注销、找回密码、重置密码。
- 用户中心：头像上传、基础资料、安全信息。
- 我的空间和公开空间。
- 动态资源路由和菜单。
- 字典、Banner 展示。
- 消息、通知、待办、实时事件。

## 技术栈

- Vue 3 / Vite / TypeScript
- Naive UI
- Pinia / pinia-plugin-persistedstate
- Vue Router
- axios
- UnoCSS
- Iconify
- vue-advanced-cropper

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

开发模式下请求会直接发到 `VITE_API_URL`。后端默认地址是 `http://127.0.0.1:8000`，门户端主要接口
前缀是 `/api/v1/portal/*`。

## 常用命令

```bash
pnpm dev
pnpm lint
pnpm lint:fix
pnpm build
pnpm preview
pnpm format
pnpm format:check
```

## 生产构建

```bash
pnpm build
```

生产构建读取 `.env.production`。当前生产配置中：

```env
VITE_API_URL=""
```

这表示前端产物使用同源 `/api/` 请求，由 nginx 在容器内反向代理到后端。不要在浏览器端写死后端
内网地址；容器启动时用 `BACKEND_URL` 指定后端。

## Docker

在 `web/portal` 目录内构建：

```bash
docker build -t hei-fastapi-portal .
docker run -d \
  --name hei-fastapi-portal \
  -e BACKEND_URL="http://host.docker.internal:8000" \
  -p 8082:80 \
  hei-fastapi-portal
```

也可以在仓库根目录构建：

```bash
docker build -t hei-fastapi-portal web/portal
```

镜像说明：

- Node 22 alpine 构建 `dist/`。
- nginx 1.27 alpine 托管静态资源。
- 容器内 nginx 监听 `80`。
- `BACKEND_URL` 在容器启动时渲染到 nginx 配置中。
- `CLIENT_MAX_BODY_SIZE` 控制上传体积限制，默认 `10m`。
- `CONTENT_SECURITY_POLICY` 和 `HSTS_HEADER` 可在启动时覆盖。
- nginx 已配置 SPA fallback、`/assets/` 长缓存、`/api/` 反向代理和 SSE 支持。

## 目录结构

```text
src/
  api/          portal API 封装
  components/   通用组件和业务组件
  hooks/        组合式工具
  layouts/      门户布局
  plugins/      图标等插件初始化
  router/       静态路由、动态路由和守卫
  stores/       Pinia 状态
  style.css     全局样式
  typing/       类型声明
  utils/        axios、权限、字典、文件 URL 等工具
  views/        页面
nginx/          生产 nginx 模板
```

## API 边界

portal 前端不应调用 `/api/v1/admin/*`。当前主要接口前缀：

- 认证和用户中心：`/api/v1/portal`
- 动态资源：`/api/v1/portal/sys/resources/current`
- 字典：`/api/v1/portal/sys/dicts/tree`
- Banner：`/api/v1/portal/sys/banners/list`
- 消息：`/api/v1/portal/message/*`
- 实时事件：`/api/v1/portal/message/realtime/events`
- 公开空间：`/api/v1/portal/spaces/{account_id}`
- 文件访问：默认 `/api/v1/files/*`

## 路由和菜单

门户端默认使用动态资源模式：

```env
VITE_ROUTE_LOAD_MODE="dynamic"
```

公开路由由 `VITE_PUBLIC_ROUTE_PATHS` 控制。登录后，前端会结合内置静态路由和
`/api/v1/portal/sys/resources/current` 返回的资源生成导航。用户中心和我的空间属于内置路由，通过头像
下拉菜单进入，不应手动拼入主导航菜单。
