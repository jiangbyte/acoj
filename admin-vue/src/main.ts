import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import router from './router'
import Antd from 'ant-design-vue'
import App from './App.vue'
import 'ant-design-vue/dist/reset.css'
import './styles/global.scss'
import 'virtual:uno.css'
import { dictLabel, dictColor, getDictItems } from './utils'

const app = createApp(App)

// Register dict tool globally for template usage: {{ $dict.label('sex', record.sex) }}
app.config.globalProperties.$dict = {
  label: dictLabel,
  color: dictColor,
  items: getDictItems,
}

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

app.use(router)
app.use(Antd)
app.mount('#app')
