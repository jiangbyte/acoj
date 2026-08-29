# admin — Admin Frontend

Management console for operators / instructors: users & roles, problems & test cases, problem sets / contests, notices, similarity tasks, and more. Built with Vue 3 + TypeScript + Vite + Naive UI. Dev server default port **81**.

## Prerequisites

- Node.js **18+**
- Package manager: **pnpm** (recommended)
- Business backend `oj` running (default `http://localhost:89`)

## Environment Variables

| File | Purpose |
| --- | --- |
| `.env` | Shared variables |
| `.env.dev` | Used by `pnpm dev` (`--mode dev`) |
| `.env.production` | Production build / `dev:production` |

Key variables:

| Variable | Description |
| --- | --- |
| `VITE_GATEWAY` | Backend API base URL; locally usually `http://localhost:89` |
| `VITE_MAIN_SERVICE_CONTEXT` | Backend context path; may be empty |

Example `.env.dev`:

```env
VITE_GATEWAY=http://localhost:89
VITE_MAIN_SERVICE_CONTEXT=""
```

> Note: Root `.env` may contain leftover port examples. Prefer `.env.dev` / `.env.production` and your real backend URL.

## Install & Start

```bash
cd admin
pnpm install
pnpm dev
```

Open http://localhost:81

Other commands:

```bash
pnpm dev:production
pnpm build
pnpm preview
```

## Working With the Backend

1. Start infrastructure and `oj`
2. Sign in with an account that has admin privileges
3. Maintain problems, test cases, users, contests, etc.
4. The user app `pc` can then practice and compete against that data

## Troubleshooting

1. **No menus after login, or 401/403**  
   Check role permissions and that tokens are sent to the correct `VITE_GATEWAY`.

2. **Port 81 in use**  
   Change `server.port` in `vite.config.ts`.

3. **Upload / file operations fail**  
   Ensure MinIO (or your object store) is running per Nacos config and reachable from the backend.

## Related Docs

- Overview: [../README.md](../README.md)
- Business backend: [../oj/README.md](../oj/README.md)
- User app: [../pc/README.md](../pc/README.md)
