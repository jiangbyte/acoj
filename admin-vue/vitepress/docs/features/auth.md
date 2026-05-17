# 认证体系

框架提供完整的认证体系，采用 SM2 国密加密传输密码，JWT 令牌管理。

## 登录流程

```
用户输入账号密码
       │
       ▼
获取 SM2 公钥 ← ─ ─ ─ ─ ─ ─ 后端 /api/v1/public/b/sm2/public-key
       │
       ▼
SM2 加密密码（C1C3C2 模式）
       │
       ▼
发送登录请求 ─ ─ ─ ─ ─ ─ ─ ─ → 后端验证
       │                           │
       ◁ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
       │
       ▼
存储 Token → 获取用户信息 → 加载菜单权限 → 进入首页
```

## 密码加密

登录密码使用 `sm-crypto` 库的 SM2 公钥加密：

```typescript
import { sm2 } from 'sm-crypto'

// 从后端获取公钥后加密密码
const encrypted = sm2.doEncrypt(password, publicKey, 1)  // 1 = C1C3C2 模式
```

## 令牌管理

Token 存储在 Pinia 的 `auth` store 中，通过 `pinia-plugin-persistedstate` 持久化到 `localStorage`：

```typescript
// src/store/auth.ts
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    userInfo: null,
    permissions: [],
    sm2PublicKey: '',
  }),
  persist: true,
})
```

HTTP 请求通过 Alova 的 `onAuthRequired` 钩子自动携带 Token：

```typescript
const { onAuthRequired } = createServerTokenAuthentication({
  assignToken: method => {
    const authStore = useAuthStore()
    if (authStore.token) {
      method.config.headers.Authorization = `${authStore.token}`
    }
  },
})
```

## 权限轮询

框架支持权限定时刷新，默认每 5 分钟轮询一次：

```typescript
startPermissionPolling(intervalMs = 5 * 60 * 1000)
```

## 退出登录

```typescript
async logout() {
  await fetchLogout().catch(() => {})
  this.token = ''
  this.userInfo = null
  this.permissions = []
  this.stopPermissionPolling()
  useRouteStore().reset()
}
```

## 自动登出

当 Token 过期（HTTP 401 或业务 code 401）时，框架自动清除登录态并跳转到登录页：

```typescript
function forceLogout() {
  const authStore = useAuthStore()
  if (!authStore.isLogin) return

  authStore.token = ''
  authStore.userInfo = null
  authStore.permissions = []
  authStore.stopPermissionPolling()
  useRouteStore().reset()

  import('@/router').then(({ router }) => {
    const name = router.currentRoute.value.name
    if (name && name !== 'login') {
      router.push({
        name: 'login',
        query: { redirect: router.currentRoute.value.fullPath },
      })
    }
  })
}
```
