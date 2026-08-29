# pc — User Frontend

Web client for contestants / learners: practice problems, submit code, view results, problem sets, contests, and more. Built with Vue 3 + TypeScript + Vite + Naive UI. Dev server default port **80**.

## Prerequisites

- Node.js **18+**
- Package manager: **pnpm** (recommended)
- Business backend `oj` running (default `http://localhost:89`)

## Environment Variables

| File | Purpose |
| --- | --- |
| `.env` | Shared variables (e.g. version) |
| `.env.dev` | Used by `pnpm dev` (`--mode dev`) |
| `.env.production` | Production build / `dev:production` |

Key variables:

| Variable | Description |
| --- | --- |
| `VITE_GATEWAY` | Backend gateway / API base URL; locally usually `http://localhost:89` |
| `VITE_MAIN_SERVICE_CONTEXT` | Backend context path; may be an empty string |

Example `.env.dev`:

```env
VITE_GATEWAY=http://localhost:89
VITE_MAIN_SERVICE_CONTEXT=""
```

## Install & Start

```bash
cd pc
pnpm install
pnpm dev
```

Open http://localhost:80

Other commands:

```bash
# Dev server with production mode env
pnpm dev:production

# Production build
pnpm build

# Preview build output
pnpm preview
```

## Working With the Backend

1. Start MySQL / Redis / RabbitMQ / Nacos and `oj`
2. Start `judge-service` if you need online judging
3. Point `VITE_GATEWAY` at a reachable API
4. Register / log in, pick a problem, and submit

## Troubleshooting

1. **UI loads but APIs fail**  
   Check `VITE_GATEWAY`, whether the backend listens on 89, and browser CORS / mixed-content limits.

2. **Port 80 in use**  
   Change `server.port` in `vite.config.ts`, or free the port.

3. **Captcha login failures**  
   Test environments may use a fixed captcha (see backend / load-test scripts); production follows the real captcha policy.

## Related Docs

- Overview: [../README.md](../README.md)
- Business backend: [../oj/README.md](../oj/README.md)
- Admin: [../admin/README.md](../admin/README.md)
