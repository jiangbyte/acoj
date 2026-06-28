import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import {
  create,
  ProCard,
  ProDataTable,
  ProInput,
  ProModalForm,
  ProPassword,
  ProRadioGroup,
  ProSearchForm,
  ProSelect,
  ProTextarea,
} from 'pro-naive-ui'
import 'virtual:uno.css'
import './style.css'
import App from './App.vue'
import { installI18n } from './i18n'
import { installRouter } from './router'

async function bootstrap() {
  const app = createApp(App)
  const pinia = createPinia()

  pinia.use(piniaPluginPersistedstate)
  app.use(pinia)
  app.use(
    create({
      components: [
        ProCard,
        ProSearchForm,
        ProDataTable,
        ProModalForm,
        ProInput,
        ProRadioGroup,
        ProPassword,
        ProSelect,
        ProTextarea,
      ],
    }),
  )
  installI18n(app)
  await installRouter(app)
  app.mount('#app')
}

void bootstrap()
