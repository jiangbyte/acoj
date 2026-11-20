import type { App } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'
import { routes } from './fiexd'

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

export async function setupRouter(app: App) {
  setupRouterGuard(router)
  app.use(router)
  await router.isReady()
}
