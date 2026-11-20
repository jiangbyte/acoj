import 'uno.css'
import '@/assets/main.css'
import '@/plugins/offline-icons'
import App from '@/App.vue'
import { setupPinia } from '@/stores'
import { setupRouter } from '@/router'

async function setupApp() {
  // Vue instance
  const app = createApp(App)

  // Install Pinia
  await setupPinia(app)

  // Install the router
  await setupRouter(app)

  // Mount Vue
  app.mount('#app')
}

setupApp()
