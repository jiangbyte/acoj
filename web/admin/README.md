# hei-fastapi Admin

`web/admin` 是 hei-fastapi 的管理端前端，基于 Vue 3、TypeScript、Vite、Pinia、Vue Router、Ant Design Vue、UnoCSS 和 Alova 构建。

## 快速开始

```bash
npm install
npm run dev
```

常用命令：

```bash
npm run lint
npm run build
npm run preview
```

## 环境变量

复制 `.env.example` 后按本地环境调整：

```bash
VITE_DEFAULT_HOME_PATH=/dashboard/workplace
VITE_API_BASE_URL=http://127.0.0.1:8000
VITE_APP_TITLE=黑Fastapi
```

当前阶段接口由 `src/apis` 固定返回代码 mock 数据。页面和状态管理仍只从 `src/apis` 调用，后续接真实后端时按领域替换 API 实现即可。

## 目录约定

- `src/apis`：领域接口入口。业务代码只从这里获取数据，不直接依赖 `mock/`。
- `src/views`：页面级组件，按业务域分组。
- `src/stores`：Pinia 状态，存放登录、用户、路由、应用布局状态。
- `src/router`：静态路由、动态资源路由构建和路由守卫。
- `src/components`：可复用组件，`pro` 偏管理端业务组件，`common` 偏通用展示组件。
- `src/utils`：HTTP、格式化、图标等通用工具。
- `mock`：本地开发数据与模拟接口，只应被 API 层引用。

## 新增页面/API 约定

1. 在 `src/apis/<domain>` 添加接口函数，必要时在 `mock/modules/<domain>.ts` 补 mock 实现。
2. 页面和 store 只导入 `@/apis/<domain>`，不要直接导入 `@mock/modules/*`。
3. 动态菜单资源通过后端资源数据驱动，页面组件路径需与 `src/views` 下的实际文件对应。
4. 复杂逻辑保留少量中文注释，简单状态和模板渲染不额外堆注释。
