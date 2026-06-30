import type { NDateLocale, NLocale } from 'naive-ui'
import { dateZhCN, zhCN } from 'naive-ui'
import { i18n } from '@/i18n'

export function setLocale(locale: App.Lang) {
  i18n.global.locale.value = locale
}

type Translate = (key: string, params?: Record<string, unknown>) => string

export const $t: Translate = (key, params) => i18n.global.t(key, params ?? {})

export function translateLocale(locale_key?: string | null, fallback?: string | null) {
  const locale = i18n.global.locale.value
  const key = String(locale_key ?? '').trim()
  if (key && i18n.global.te(key, locale)) {
    return i18n.global.t(key)
  }
  return fallback ?? key
}

export const naiveI18nOptions: Record<
  App.Lang,
  { locale: NLocale | null; dateLocale: NDateLocale | null }
> = {
  zhCN: {
    locale: zhCN,
    dateLocale: dateZhCN,
  },
  enUS: {
    locale: null,
    dateLocale: null,
  },
}
