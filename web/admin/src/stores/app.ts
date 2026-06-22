import { defineStore } from 'pinia'
import type { LocationQuery, RouteLocationNormalizedLoaded } from 'vue-router'

import { DEFAULT_HOME_PATH } from '@/config/app'

export type ThemeMode = 'light' | 'dark' | 'auto'

export interface VisitedTab {
  title: string
  full_path: string
  path: string
  query: LocationQuery
  resource_id?: string
  resource_code?: string
  icon?: string
  href?: string
  active_menu?: string
  affix: boolean
  closable: boolean
}

function getSystemDark() {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

export const useAppStore = defineStore('app', {
  state: () => ({
    collapsed: false,
    mobileMenuOpen: false,
    systemDark: getSystemDark(),
    themeMode: 'auto' as ThemeMode,
    pin_tabs: [] as VisitedTab[],
    tabs: [] as VisitedTab[],
    current_tab_path: '',
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
    all_tabs: (state) => [...state.pin_tabs, ...state.tabs],
  },
  actions: {
    toggleCollapsed() {
      this.collapsed = !this.collapsed
    },
    setMobileMenuOpen(open: boolean) {
      this.mobileMenuOpen = open
    },
    setThemeMode(mode: ThemeMode) {
      this.themeMode = mode
    },
    setSystemDark(dark: boolean) {
      this.systemDark = dark
    },
    createVisitedTab(route: RouteLocationNormalizedLoaded): VisitedTab {
      return {
        title: String(route.meta.title || route.name || route.path),
        full_path: route.fullPath,
        path: route.path,
        query: route.query,
        resource_id: route.meta.resourceId ? String(route.meta.resourceId) : undefined,
        resource_code: route.meta.resourceCode ? String(route.meta.resourceCode) : undefined,
        icon: route.meta.icon ? String(route.meta.icon) : undefined,
        href: route.meta.href ? String(route.meta.href) : undefined,
        active_menu: route.meta.activeMenu ? String(route.meta.activeMenu) : undefined,
        affix: Boolean(route.meta.affixTab),
        closable: !route.meta.affixTab,
      }
    },
    addVisitedTab(route: RouteLocationNormalizedLoaded) {
      if (!route.meta?.requiresAuth || route.meta.withoutTab || route.meta.href) {
        return
      }
      if (this.hasVisitedTab(route.fullPath)) {
        return
      }
      const tab = this.createVisitedTab(route)
      if (tab.affix) {
        this.pin_tabs.push(tab)
        return
      }
      this.tabs.push(tab)
    },
    hasVisitedTab(fullPath: string) {
      return this.all_tabs.some((item) => item.full_path === fullPath)
    },
    setCurrentTab(fullPath: string) {
      this.current_tab_path = fullPath
    },
    getFallbackTabPath(fullPath?: string) {
      const allTabs = this.all_tabs
      if (!allTabs.length) {
        return DEFAULT_HOME_PATH
      }

      if (!fullPath) {
        return allTabs[0]?.full_path || DEFAULT_HOME_PATH
      }

      const tabIndex = this.tabs.findIndex((item) => item.full_path === fullPath)
      const right = tabIndex >= 0 ? this.tabs[tabIndex + 1] : undefined
      const left = tabIndex > 0 ? this.tabs[tabIndex - 1] : undefined
      const affixed = this.pin_tabs[0]
      return (
        right?.full_path ||
        left?.full_path ||
        affixed?.full_path ||
        allTabs[0]?.full_path ||
        DEFAULT_HOME_PATH
      )
    },
    removeVisitedTab(fullPath: string) {
      const target = this.tabs.find((item) => item.full_path === fullPath)
      if (!target || !target.closable) {
        return
      }
      this.tabs = this.tabs.filter((item) => item.full_path !== fullPath)
    },
    closeLeftTabs(fullPath: string) {
      const index = this.tabs.findIndex((item) => item.full_path === fullPath)
      if (index < 0) {
        return
      }
      this.tabs = this.tabs.filter((_, itemIndex) => itemIndex >= index)
    },
    closeRightTabs(fullPath: string) {
      const index = this.tabs.findIndex((item) => item.full_path === fullPath)
      if (index < 0) {
        return
      }
      this.tabs = this.tabs.filter((_, itemIndex) => itemIndex <= index)
    },
    closeOtherTabs(fullPath: string) {
      this.tabs = this.tabs.filter((item) => item.full_path === fullPath)
    },
    closeAllTabs() {
      this.tabs = []
    },
    clearAllTabs() {
      this.pin_tabs = []
      this.tabs = []
      this.current_tab_path = ''
    },
  },
  persist: {
    key: 'hei-admin-app',
    pick: ['collapsed', 'themeMode'],
  },
})
