import { defineStore } from 'pinia'

interface AppState {
  layoutMode: 'vertical' | 'horizontal' | 'mixed'
  collapsed: boolean
  showTabs: boolean
  showFooter: boolean
  theme: 'light' | 'dark'
  colorPrimary: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    layoutMode: 'vertical',
    collapsed: false,
    showTabs: true,
    showFooter: false,
    theme: 'light',
    colorPrimary: '#1677ff',
  }),
  actions: {
    toggleCollapsed() { this.collapsed = !this.collapsed },
    setLayoutMode(mode: AppState['layoutMode']) { this.layoutMode = mode },
    setTheme(theme: AppState['theme']) { this.theme = theme },
  },
  persist: true,
})
