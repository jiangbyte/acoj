type LocaleMessage = Record<string, unknown>

const langs: App.Lang[] = ['zhCN', 'enUS']
const localeModules = import.meta.glob<LocaleMessage>('../../locales/**/*.json', {
  eager: true,
  import: 'default',
})

export const messages = Object.fromEntries(langs.map((lang) => [lang, {}])) as Record<
  App.Lang,
  Record<string, LocaleMessage>
>

Object.entries(localeModules).forEach(([path, message]) => {
  const match = path.match(/locales\/([^/]+)\/(.+)\.json$/)
  if (!match) {
    return
  }

  const [, lang, modulePath] = match
  if (!isAppLang(lang) || !isRecord(message)) {
    return
  }

  setMessageByPath(messages[lang], modulePath.split('/'), message)
})

function isAppLang(lang: string): lang is App.Lang {
  return langs.includes(lang as App.Lang)
}

function isRecord(value: unknown): value is LocaleMessage {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function setMessageByPath(target: LocaleMessage, paths: string[], message: LocaleMessage) {
  const [moduleName, ...children] = paths
  if (!moduleName) {
    return
  }

  if (!children.length) {
    target[moduleName] = mergeMessage(target[moduleName], message)
    return
  }

  const current = target[moduleName]
  if (!isRecord(current)) {
    target[moduleName] = {}
  }

  setMessageByPath(target[moduleName] as LocaleMessage, children, message)
}

function mergeMessage(current: unknown, message: LocaleMessage) {
  if (!isRecord(current)) {
    return message
  }

  return deepMerge(current, message)
}

function deepMerge(target: LocaleMessage, source: LocaleMessage): LocaleMessage {
  const result: LocaleMessage = { ...target }

  Object.entries(source).forEach(([key, value]) => {
    const current = result[key]
    result[key] = isRecord(current) && isRecord(value) ? deepMerge(current, value) : value
  })

  return result
}
