# ACOJ Portal (React)

Vite + React + TypeScript + Ant Design + UnoCSS 企业级门户脚手架。

## 开发

```bash
pnpm install
pnpm dev
```

默认端口见 `.env` 的 `VITE_PORT`（5174）。若被占用，Vite 会自动换端口。

## 脚本

- `pnpm dev` / `build` / `preview`
- `pnpm lint` / `lint:fix`
- `pnpm format` / `format:check`

## 目录

- `src/api` 接口
- `src/utils/axios` HTTP 解包与拦截器
- `src/utils/dict.ts` 字典缓存（对齐 admin）
- `src/stores` Zustand
- `src/router` 路由与守卫
- `src/layouts` / `src/pages` / `src/components`

旧 Nuxt 实现备份在同级目录 `portal-nuxt`，业务页下一阶段迁移。
