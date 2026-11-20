import 'uno.css'
import '@/assets/main.css'
import '@/plugins/offline-icons'
import App from '@/App.vue'
import { setupPinia } from '@/stores'
import { setupRouter } from '@/router'

async function setupApp() {
  // Implementation note.
  const app = createApp(App)

  // Implementation note.
  await setupPinia(app)

  // Implementation note.
  await setupRouter(app)

  // Implementation note.
  app.mount('#app')
}

setupApp()
