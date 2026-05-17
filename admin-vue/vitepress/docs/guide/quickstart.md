# 快速开始

本指南将带你 5 分钟内启动 Hei Admin Vue 项目。

## 环境要求

| 依赖 | 版本要求 |
|------|---------|
| Node.js | 20 或更高版本 |
| pnpm | 9 或更高版本 |

## 克隆项目

```bash
git clone <项目仓库地址>
cd hei-admin-vue
```

## 安装依赖

```bash
pnpm install
```

## 配置开发代理

编辑项目根目录的 `.env` 文件，配置后端 API 代理地址：

```bash
VITE_PROXY_TARGET=http://localhost:18885
VITE_PROXY_PREFIXES=/api
```

默认代理目标为 `http://localhost:18885`，对应 Hei Gin 后端默认端口。其他后端框架请根据实际情况修改端口：

| 后端框架 | 默认端口 |
|---------|---------|
| Hei Gin | 18885 |
| Hei Boot | 8080 |
| Hei FastAPI | 8000 |

## 启动开发服务

```bash
pnpm dev
```

服务默认在 `http://localhost:3000` 启动，浏览器自动打开。

## 构建生产包

```bash
pnpm build
```

构建命令包含 TypeScript 类型检查（`vue-tsc -b`）和 Vite 打包（`vite build`），构建产物输出到 `dist/` 目录。

## 代码检查

```bash
# ESLint 检查
pnpm lint

# 自动修复
pnpm lint:fix

# 格式检查
pnpm format:check

# 自动格式化
pnpm format
```

## 默认账号

| 端侧 | 账号 | 密码 |
|------|------|------|
| B 端管理 | admin | admin123（需 SM2 加密传输）|

> 登录密码通过 SM2 公钥加密后传输，具体流程请参考 [认证体系](/features/auth)。

## 下一步

- 了解 [项目结构](/guide/structure) 的目录组织方式
- 阅读 [功能概览](/features/) 了解框架能力
- 参考 [模块开发](/modules/development) 开始业务功能开发
