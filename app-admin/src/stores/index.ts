import type { App } from 'vue'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

export * from './TabStore.ts'
export * from './RouterStore.ts'
export * from './TokenStore.ts'
export * from './SiteStore.ts'
export * from './ClientStore.ts'

export function setupPinia(app: App) {
  const pinia = createPinia()
  pinia.use(piniaPluginPersistedstate)
  app.use(pinia)
}
