# HEI FastAPI Portal uni-app

`web/portal-uniapp` 是 HEI FastAPI 的门户端 uni-app 应用，面向移动端或 H5 门户场景。它使用 `PORTAL`
账号体系，主要调用 `/api/v1/portal/*`、公共文件接口和公开空间接口。

## 功能范围

- 首页、消息、用户中心。
- 认证：登录、注册、退出、注销。
- 用户中心：资料、安全设置。
- 消息模块：消息列表、消息详情、待办操作。
- 公开空间：按账号访问门户用户公开空间。
- 字典、Banner、文件访问。

## 技术栈

- uni-app 3 / Vue 3
- Vite / TypeScript
- Pinia
- uview-pro
- UnoCSS / unocss-preset-weapp

## 本地开发

```bash
pnpm install
pnpm dev:h5
```

默认环境变量见 `.env`：

```env
VITE_APP_TITLE="HEI Portal"
VITE_API_URL="http://127.0.0.1:8000"
VITE_PORT=5174
```

开发模式下请求会直接发到 `VITE_API_URL`。后端默认地址是 `http://127.0.0.1:8000`，门户端主要接口
前缀是 `/api/v1/portal/*`。

## 常用命令

```bash
pnpm dev:h5
pnpm dev:mp-weixin
pnpm build:h5
pnpm build:mp-weixin
pnpm type-check
pnpm lint
pnpm lint:fix
pnpm format
pnpm format:check
```

其他小程序平台命令见 `package.json`，例如 `dev:mp-alipay`、`build:mp-alipay`、
`dev:mp-qq`、`build:mp-qq`。

## 生产构建

```bash
pnpm build:h5
```

生产构建读取 `.env.production`。当前生产配置中：

```env
VITE_API_URL=""
```

这表示 H5 产物使用同源 `/api/` 请求，需要由外层网关或 nginx 反向代理到后端。当前目录没有提供
Dockerfile，部署时需要自行托管构建产物或接入现有前端网关。

## 目录结构

```text
src/
  api/          portal API 封装
  components/   通用组件
  config/       字典等配置
  layouts/      门户布局
  pages/        uni-app 页面
  static/       静态资源
  stores/       Pinia 状态
  utils/        请求、会话、字典、安全、树结构等工具
  App.vue       根组件
  main.ts       入口
  manifest.json 应用清单
  pages.json    页面和 tabBar 配置
```

## API 边界

- portal uni-app 不应调用 `/api/v1/admin/*`。
- 登录、注册、注销和用户中心使用 `PORTAL` 账号体系。
- 动态资源来自 `/api/v1/portal/sys/resources/current`。
- 公开空间接口为 `/api/v1/portal/spaces/{account_id}`，由后端 `user.portal` 模块提供。
- 文件访问默认走 `/api/v1/files/*`，也兼容对象存储返回的外部 URL。

## 注意事项

- `web/admin-uniapp` 和 `web/portal-uniapp` 默认都使用 `VITE_PORT=5174`，同时运行时需要修改其中一个端口。
- `manifest.json` 中的应用名、appid 和各小程序平台 appid 仍需要按实际发布目标调整。
- 小程序端请求域名、上传域名和业务域名需要在对应平台后台配置。
