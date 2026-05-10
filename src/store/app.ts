import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ThemeMode } from '@/utils'

export type LayoutMode = 'vertical'

// 响应式移动端检测
const isMobileRef = ref(window.matchMedia('(max-width: 700px)').matches)
if (typeof window !== 'undefined') {
  const mql = window.matchMedia('(max-width: 700px)')
  mql.addEventListener('change', e => {
    isMobileRef.value = e.matches
  })
}

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
  /** 全局加载中（登录后、路由初始化等过渡场景） */
  loading: boolean
  /** 页面刷新计数器 */
  reloadCounter: number
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
    loading: false,
    reloadCounter: 0,
  }),
  getters: {
    isMobile(): boolean {
      return isMobileRef.value
    },
  },
  actions: {
    toggleCollapsed() {
      this.collapsed = !this.collapsed
    },
    setLayoutMode(mode: LayoutMode) {
      this.layoutMode = mode
    },
    setTheme(theme: ThemeMode) {
      this.theme = theme
    },
    setColorPrimary(color: string) {
      this.colorPrimary = color
    },
    toggleGrayMode() {
      this.grayMode = !this.grayMode
    },
    toggleColorWeak() {
      this.colorWeak = !this.colorWeak
    },
    toggleRoundedCorners() {
      this.roundedCorners = !this.roundedCorners
    },
    setShowBreadcrumb(v: boolean) {
      this.showBreadcrumb = v
    },
    setShowTabs(v: boolean) {
      this.showTabs = v
    },
    setShowFooter(v: boolean) {
      this.showFooter = v
    },
    setLoading(v: boolean) {
      this.loading = v
    },
    reloadPage() {
      this.reloadCounter++
    },
  },
  persist: true,
})
