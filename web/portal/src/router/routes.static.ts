/**
 * 静态资源路由表。
 *
 * portal 的基础静态页面在 routes.inner.ts 中声明；这里仅作为 VITE_ROUTE_LOAD_MODE=static
 * 时的动态资源占位，避免重新引入后台管理菜单。
 */
export const staticRoutes: AppRoute.RowRoute[] = []
