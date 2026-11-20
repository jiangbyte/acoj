import 'uno.css'
import '@/assets/main.css'
import App from '@/App.vue'
import { setupPinia } from '@/stores'
import { setupRouter } from '@/router'

async function setupApp() {
  // Vue实例
  const app = createApp(App)

  // 引入Pinia
  await setupPinia(app)

  // 引入路由
  await setupRouter(app)

  // 挂载Vue
  app.mount('#app')
}

setupApp()
