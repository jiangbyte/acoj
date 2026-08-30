/** Author: Charlie */

import type { GlobalThemeOverrides } from 'naive-ui'
import { defineStore } from 'pinia'
import { nextTick, ref, watch } from 'vue'
import { useColorMode, useFullscreen, useMediaQuery, usePreferredDark } from '@vueuse/core'
import { colord } from 'colord'
import {
  APPEARANCE_DEFAULTS,
  BORDER_RADIUS_OFF,
  BORDER_RADIUS_ON,
  type FormStyle,
} from './defaults'

// 全局页面级能力使用 documentElement 作为作用对象，例如全屏。
const docEle = ref(document.documentElement)

// 全屏状态由 @vueuse/core 维护，store 只暴露当前状态和切换动作。
const { isFullscreen, toggle } = useFullscreen(docEle)

// 主题模式会同步到本地存储；emitAuto 开启后可以保留 auto 模式，而不是立即解析成 light/dark。
const colorMode = useColorMode({ emitAuto: true })
const prefersDark = usePreferredDark()

// 布局在小屏幕下会切换为移动端交互，例如隐藏桌面侧边栏控制。
const isMobile = useMediaQuery('(max-width: 700px)')

function buildPrimaryPalette(primary: string) {
  const base = colord(primary)
  return {
    primaryColor: primary,
    primaryColorHover: base.lighten(0.08).toHex(),
    primaryColorPressed: base.darken(0.12).toHex(),
    primaryColorSuppl: base.lighten(0.08).toHex(),
  }
}

function applyDocumentAppearance(opts: {
  grayMode: boolean
  borderRadius: string
  primaryColor: string
  bodyColor: string
  cardColor: string
  mutedColor: string
}) {
  const root = document.documentElement
  root.style.setProperty('--border-radius', opts.borderRadius)
  root.style.setProperty('--primary-color', opts.primaryColor)
  root.style.setProperty('--body-color', opts.bodyColor)
  root.style.setProperty('--card-color', opts.cardColor)
  root.style.setProperty('--app-muted-bg', opts.mutedColor)
  root.classList.toggle('gray-mode', opts.grayMode)
}

/**
 * 应用全局状态。
 *
 * 这里集中保存页面刷新开关与外观偏好。
 * 业务数据不要放在这个 store 中，避免和页面模块状态混在一起。
 */
export const useAppStore = defineStore('app-store', {
  state: () => ({
    // 侧边栏是否折叠。
    collapsed: false,

    // 页面内容渲染开关。reloadPage 会短暂关闭再打开，用于触发当前路由视图重载。
    loadFlag: true,

    primaryColor: APPEARANCE_DEFAULTS.primaryColor as string,
    roundedStyle: APPEARANCE_DEFAULTS.roundedStyle as boolean,
    formStyle: APPEARANCE_DEFAULTS.formStyle as FormStyle,
    showWatermark: APPEARANCE_DEFAULTS.showWatermark as boolean,
    showBreadcrumb: APPEARANCE_DEFAULTS.showBreadcrumb as boolean,
    showTabbar: APPEARANCE_DEFAULTS.showTabbar as boolean,
    accordionMenu: APPEARANCE_DEFAULTS.accordionMenu as boolean,
    grayMode: APPEARANCE_DEFAULTS.grayMode as boolean,
  }),
  getters: {
    // 用户选择的主题模式，可能是 light、dark 或 auto。
    storeColorMode: () => colorMode.value,

    // Naive UI 只需要 light/dark；auto 在这里按当前结果映射给组件库。
    naiveTheme: () =>
      colorMode.value === 'dark' || (colorMode.value === 'auto' && prefersDark.value)
        ? 'dark'
        : 'light',

    // 当前是否处于全屏状态。
    fullScreen: () => isFullscreen.value,

    // 当前是否命中移动端断点。
    isMobile: () => isMobile.value,

    borderRadius(): string {
      return this.roundedStyle ? BORDER_RADIUS_ON : BORDER_RADIUS_OFF
    },

    /** 布局层背景：内容区浅底 / 顶栏与卡片白面 */
    surfaceColors(): { body: string; card: string; muted: string } {
      if (this.naiveTheme === 'dark') {
        return {
          body: '#101014',
          card: '#18181c',
          muted: '#1c1c22',
        }
      }
      return {
        body: '#f0f2f5',
        card: '#ffffff',
        muted: '#fafafa',
      }
    },

    themeOverrides(): GlobalThemeOverrides {
      const radius = this.borderRadius
      const { body, card, muted } = this.surfaceColors
      return {
        common: {
          ...buildPrimaryPalette(this.primaryColor),
          borderRadius: radius,
          borderRadiusSmall: radius,
          bodyColor: body,
          cardColor: card,
          modalColor: card,
          popoverColor: card,
          tableColor: card,
          tableHeaderColor: muted,
          actionColor: muted,
          tagColor: muted,
          inputColor: card,
        },
      }
    },

    /** ProLayout：内容区用 body，顶栏用 card */
    layoutThemeOverrides(): { color: string; layoutColor: string } {
      const { body, card } = this.surfaceColors
      return {
        color: body,
        layoutColor: card,
      }
    },
  },
  actions: {
    /**
     * 设置颜色模式。
     *
     * light/dark 表示强制指定主题；auto 表示跟随系统主题。
     */
    setColorMode(mode: 'light' | 'dark' | 'auto') {
      colorMode.value = mode
    },

    setPrimaryColor(color: string) {
      this.primaryColor = color
      this.syncDocumentAppearance()
    },

    setFormStyle(style: FormStyle) {
      this.formStyle = style
    },

    setRoundedStyle(enabled: boolean) {
      this.roundedStyle = enabled
      this.syncDocumentAppearance()
    },

    syncDocumentAppearance() {
      const { body, card, muted } = this.surfaceColors
      applyDocumentAppearance({
        grayMode: this.grayMode,
        borderRadius: this.borderRadius,
        primaryColor: this.primaryColor,
        bodyColor: body,
        cardColor: card,
        mutedColor: muted,
      })
    },

    resetAppearance() {
      this.primaryColor = APPEARANCE_DEFAULTS.primaryColor
      this.roundedStyle = APPEARANCE_DEFAULTS.roundedStyle
      this.formStyle = APPEARANCE_DEFAULTS.formStyle
      this.showWatermark = APPEARANCE_DEFAULTS.showWatermark
      this.showBreadcrumb = APPEARANCE_DEFAULTS.showBreadcrumb
      this.showTabbar = APPEARANCE_DEFAULTS.showTabbar
      this.accordionMenu = APPEARANCE_DEFAULTS.accordionMenu
      this.grayMode = APPEARANCE_DEFAULTS.grayMode
      this.syncDocumentAppearance()
    },

    /**
     * 切换侧边栏折叠状态。
     */
    toggleCollapse() {
      this.collapsed = !this.collapsed
    },

    /**
     * 切换浏览器全屏状态。
     */
    toggleFullScreen() {
      toggle()
    },

    /**
     * 重载当前页面内容区域。
     *
     * 通过短暂关闭 loadFlag 让视图组件卸载，再在下一轮渲染后恢复。
     * delay 为恢复渲染前的等待时间；传 0 可以立即恢复。
     */
    async reloadPage(delay = 600) {
      this.loadFlag = false
      await nextTick()
      if (delay) {
        window.setTimeout(() => {
          this.loadFlag = true
        }, delay)
      } else {
        this.loadFlag = true
      }
    },
  },
  persist: {
    // 应用偏好使用 localStorage 持久化，刷新页面后继续沿用用户设置。
    storage: localStorage,
  },
})

/** 在 store 水合后同步 document 外观（供 Main 调用一次即可） */
export function watchAppAppearance(store: ReturnType<typeof useAppStore>) {
  store.syncDocumentAppearance()
  watch(
    () =>
      [store.grayMode, store.roundedStyle, store.primaryColor, store.naiveTheme] as const,
    () => store.syncDocumentAppearance(),
  )
}
