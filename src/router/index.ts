import { createRouter, createWebHashHistory } from 'vue-router'
import { setupRouterGuard } from './guard'
import { staticRoutes } from './routes'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/base-layout/index.vue'),
      name: 'layout',
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页', icon: 'DashboardOutlined' } },
      ],
    },
    ...staticRoutes,
  ],
})

setupRouterGuard(router)

export { router }
export default router
