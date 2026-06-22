import { createI18n } from 'vue-i18n'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import enUS from 'ant-design-vue/es/locale/en_US'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import 'dayjs/locale/en'

import enUSMessages from './locales/en-US'
import zhCNMessages from './locales/zh-CN'

export const SUPPORTED_LOCALES = ['zh-CN', 'en-US'] as const
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]
export const DEFAULT_LOCALE: SupportedLocale = 'zh-CN'

export const localeOptions = [
  { key: 'zh-CN', labelKey: 'locale.zh-CN' },
  { key: 'en-US', labelKey: 'locale.en-US' },
] as const

const messages = {
  'zh-CN': zhCNMessages,
  'en-US': enUSMessages,
}

export const antdLocales = {
  'zh-CN': zhCN,
  'en-US': enUS,
}

export function isSupportedLocale(locale: string): locale is SupportedLocale {
  return SUPPORTED_LOCALES.includes(locale as SupportedLocale)
}

export function normalizeLocale(locale?: string | null): SupportedLocale {
  if (locale && isSupportedLocale(locale)) {
    return locale
  }
  return DEFAULT_LOCALE
}

export const i18n = createI18n({
  legacy: false,
  locale: DEFAULT_LOCALE,
  fallbackLocale: DEFAULT_LOCALE,
  messages,
})

export function setI18nLocale(locale: SupportedLocale) {
  i18n.global.locale.value = locale
  document.documentElement.lang = locale
  dayjs.locale(locale === 'zh-CN' ? 'zh-cn' : 'en')
}

export function t(key: string, values?: Record<string, unknown>) {
  return i18n.global.t(key, values ?? {})
}
