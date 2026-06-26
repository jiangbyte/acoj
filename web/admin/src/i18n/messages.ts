type LocaleMessage = Record<string, unknown>

const langs: App.Lang[] = ['zhCN', 'enUS']
const localeModules = import.meta.glob<LocaleMessage>('../../locales/*/*.json', {
  eager: true,
  import: 'default',
})

export const messages = Object.fromEntries(langs.map((lang) => [lang, {}])) as Record<
  App.Lang,
  Record<string, LocaleMessage>
>

Object.entries(localeModules).forEach(([path, message]) => {
  const match = path.match(/locales\/([^/]+)\/([^/]+)\.json$/)
  if (!match) {
    return
  }

  const [, lang, moduleName] = match
  if (!isAppLang(lang) || !isRecord(message)) {
    return
  }

  messages[lang][moduleName] = message
})

function isAppLang(lang: string): lang is App.Lang {
  return langs.includes(lang as App.Lang)
}

function isRecord(value: unknown): value is LocaleMessage {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
