import type { NDateLocale, NLocale } from 'naive-ui'
import { dateZhCN, zhCN } from 'naive-ui'
import { i18n } from '@/i18n'

export function setLocale(locale: App.Lang) {
  i18n.global.locale.value = locale
}

export const $t = i18n.global.t

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
