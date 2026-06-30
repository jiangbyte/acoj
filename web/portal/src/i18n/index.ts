import type { App as VueApp } from 'vue'
import { createI18n } from 'vue-i18n'
import { messages } from './messages'

const langs: App.Lang[] = ['zhCN', 'enUS']
const defaultLang = (import.meta.env.VITE_DEFAULT_LANG || 'zhCN') as App.Lang

function getInitialLang(): App.Lang {
  const storedLang = localStorage.getItem('lang') as App.Lang | null
  return storedLang && langs.includes(storedLang) ? storedLang : defaultLang
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: getInitialLang(),
  fallbackLocale: defaultLang,
  messages,
  fallbackWarn: false,
})

export function installI18n(app: VueApp) {
  app.use(i18n)
}
