import { createRouter, createWebHistory } from 'vue-router'

import { innerRoutes } from './inner-routes'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: innerRoutes,
  scrollBehavior: () => ({ left: 0, top: 0 }),
})
