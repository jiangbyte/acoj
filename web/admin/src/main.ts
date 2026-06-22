import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import 'virtual:uno.css'
import './style.css'
import App from './App.vue'
import { router } from '@/router'
import { setupRouterGuards } from '@/router/guards'

async function setup() {
  const app = createApp(App)
  const pinia = createPinia()

  pinia.use(piniaPluginPersistedstate)
  app.use(pinia)
  setupRouterGuards(router)
  app.use(router)
  await router.isReady()
  app.mount('#app')
}

setup()
