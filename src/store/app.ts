import { defineStore } from 'pinia'
import type { ThemeMode } from '@/utils/themeUtil'

export type LayoutMode = 'vertical'

interface AppState {
  layoutMode: LayoutMode
  collapsed: boolean
  theme: ThemeMode
  colorPrimary: string
  showTabs: boolean
  showFooter: boolean
  showBreadcrumb: boolean
  showBreadcrumbIcon: boolean
  showWatermark: boolean
  showLogo: boolean
  grayMode: boolean
  colorWeak: boolean
  roundedCorners: boolean
  fixedWidth: boolean
  collapseOnOpen: boolean
  showSettings: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    layoutMode: 'vertical',
    collapsed: false,
    theme: 'light',
    colorPrimary: '#1677ff',
    showTabs: true,
    showFooter: false,
    showBreadcrumb: true,
    showBreadcrumbIcon: true,
    showWatermark: false,
    showLogo: true,
    grayMode: false,
    colorWeak: false,
    roundedCorners: false,
    fixedWidth: false,
    collapseOnOpen: true,
    showSettings: false,
  }),
  actions: {
    toggleCollapsed() { this.collapsed = !this.collapsed },
    setLayoutMode(mode: LayoutMode) { this.layoutMode = mode },
    setTheme(theme: ThemeMode) { this.theme = theme },
    setColorPrimary(color: string) { this.colorPrimary = color },
    toggleGrayMode() { this.grayMode = !this.grayMode },
    toggleColorWeak() { this.colorWeak = !this.colorWeak },
    toggleRoundedCorners() { this.roundedCorners = !this.roundedCorners },
    setShowBreadcrumb(v: boolean) { this.showBreadcrumb = v },
    setShowTabs(v: boolean) { this.showTabs = v },
    setShowFooter(v: boolean) { this.showFooter = v },
  },
  persist: true,
})
