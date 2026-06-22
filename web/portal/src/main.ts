import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import 'virtual:uno.css'
import './style.css'
import App from './App.vue'
import { i18n } from '@/i18n'
import { router } from '@/router'

async function setup() {
  const app = createApp(App)
  const pinia = createPinia()

  pinia.use(piniaPluginPersistedstate)

  app.use(pinia)
  app.use(i18n)
  app.use(router)
  await router.isReady()
  app.mount('#app')
}

setup()
