import { defineStore } from 'pinia'

import { DEFAULT_LOCALE, normalizeLocale, setI18nLocale, type SupportedLocale } from '@/i18n'

export type ThemeMode = 'light' | 'dark' | 'auto'

function getSystemDark() {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

export const useAppStore = defineStore('app', {
  state: () => ({
    mobileMenuOpen: false,
    systemDark: getSystemDark(),
    themeMode: 'auto' as ThemeMode,
    locale: DEFAULT_LOCALE as SupportedLocale,
  }),
  getters: {
    isDark: (state) => {
      if (state.themeMode === 'dark') {
        return true
      }
      if (state.themeMode === 'light') {
        return false
      }
      return state.systemDark
    },
  },
  actions: {
    setMobileMenuOpen(open: boolean) {
      this.mobileMenuOpen = open
    },
    setThemeMode(mode: ThemeMode) {
      this.themeMode = mode
    },
    setLocale(locale: SupportedLocale) {
      this.locale = normalizeLocale(locale)
      setI18nLocale(this.locale)
    },
    setSystemDark(dark: boolean) {
      this.systemDark = dark
    },
    toggleThemeMode() {
      this.themeMode = this.isDark ? 'light' : 'dark'
    },
  },
  persist: {
    key: 'hei-portal-app',
    pick: ['themeMode', 'locale'],
    afterHydrate: ({ store }) => {
      store.setLocale(normalizeLocale(store.locale))
    },
  },
})
