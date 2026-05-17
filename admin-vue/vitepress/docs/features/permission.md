# 权限管理

框架采用 RBAC（基于角色的访问控制）权限模型，支持路由级和按钮级权限控制。

## 权限模型

```
用户 ──→ 角色 ──→ 权限
用户 ──→ 直接权限（直授）
```

## 权限获取流程

用户登录成功后，框架自动并行获取菜单和权限数据：

```typescript
async loadMenusAndPermissions() {
  const routeStore = useRouteStore()
  const [menusRes, permsRes] = await Promise.all([
    fetchUserMenus(),
    fetchUserPermissions(),
  ])
  if (menusRes.data) {
    routeStore.setMenus(menusRes.data)
    await routeStore.initAuthRoute()
  }
  if (permsRes.data) this.permissions = permsRes.data
}
```

## 路由级权限

框架根据后端返回的菜单配置，通过 `menusToRoutes` 函数动态生成 Vue Router 路由：

```typescript
// src/router/dynamic.ts
const modules = import.meta.glob('/src/views/**/*.vue')

export function menusToRoutes(menus: any[]): any[] {
  const sorted = [...menus].sort((a, b) => (a.sort_code ?? 0) - (b.sort_code ?? 0))

  return sorted
    .filter((m: any) => m.type !== 'BUTTON')   // 按钮类型不生成路由
    .map((m: any) => {
      const meta = {
        title: m.name,
        icon: m.icon,
        type: m.type,
        cache: m.is_cache === 'YES',
        affix: m.is_affix === 'YES',
        breadcrumb: m.is_breadcrumb !== 'NO',
        visible: m.is_visible !== 'NO',
      }

      // 外链类型：新窗口打开，无组件
      if (m.type === 'EXTERNAL_LINK' && m.external_url) {
        meta.href = m.external_url
        return { path: m.route_path, name: m.code, meta }
      }

      const route = {
        path: m.route_path,
        name: m.code,
        redirect: m.redirect_path || undefined,
        meta,
      }

      if (m.component_path) {
        const fullPath = `/src/views/${m.component_path}.vue`
        route.component = modules[fullPath] || (() => import('@/views/error/404.vue'))
      }

      if (m.children?.length && m.is_visible !== 'NO') {
        route.children = menusToRoutes(m.children)
      }

      return route
    })
}
```

### 菜单类型

| 类型 | 说明 | 是否生成路由 |
|------|------|------------|
| MENU | 菜单目录/页面 | 是 |
| EXTERNAL_LINK | 外链（新窗口打开） | 是（无组件，`meta.href`） |
| BUTTON | 按钮（权限标识） | 否 |

### 动态路由初始化

生成的动态路由通过 `router.addRoute('root', route)` 添加到根路由下：

```typescript
async initAuthRoute() {
  // 先清除之前添加的动态路由
  this.authRouteNames.forEach(name => router.removeRoute(name))
  this.authRouteNames = []

  const routes = menusToRoutes(this.menus)

  // 收集缓存和白名单
  const cacheNames: string[] = []
  function collectCache(routes: any[]) { /* 递归收集 meta.cache 的路由名 */ }
  this.cacheRoutes = cacheNames

  // 注册路由
  routes.forEach(route => router.addRoute('root', route))
  this.isInitAuthRoute = true
}
```

## 按钮级权限

`auth` store 中的 `permissions` 数组存储了当前用户所有的权限标识码：

```typescript
const authStore = useAuthStore()

// 模板中直接使用
authStore.hasPermission('sys:user:create')

// 或通过 hasPermission 函数解构使用
const hasPermission = authStore.hasPermission

// 按钮级控制
<a-button v-if="hasPermission('sys:user:create')" type="primary">新增用户</a-button>
```

## 路由守卫权限控制

`src/router/guard.ts` 中的路由守卫统一处理认证检查和动态路由初始化：

1. 外链直接打开新窗口（`meta.href`）
2. 未登录跳转登录页（携带 redirect 参数，登录后回到目标页）
3. 已登录访问登录页 → 跳转首页
4. Token 登录后加载动态路由和菜单
5. 动态路由首次初始化时显示全局 loading 遮罩
6. 路由初始化完成后自动重定向到目标路径
7. 处理 `not-found` 重试（解决动态路由注册时序问题）
