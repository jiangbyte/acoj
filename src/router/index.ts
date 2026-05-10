import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'
import { staticRoutes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'root',
      component: () => import('@/layouts/base-layout/index.vue'),
      children: [],
    },
    ...staticRoutes,
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/error/404.vue') },
  ],
})

setupRouterGuard(router)

export { router }
export default router
