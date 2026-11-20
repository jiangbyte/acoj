import { router } from '@/router'
import type { RouteLocationNormalized } from 'vue-router'

interface TabState {
  pinedTabs: RouteLocationNormalized[]
  tabs: RouteLocationNormalized[]
  currentPath: string
}

export const useTabStore = defineStore('tab-store', {
  state: (): TabState => {
    return {
      pinedTabs: [],
      tabs: [],
      currentPath: '',
    }
  },
  getters: {
    allTabs: state => [...state.pinedTabs, ...state.tabs],
  },
  actions: {
    addTab(route: RouteLocationNormalized) {
      if (route.meta.withoutTab) {
        return
      }

      if (this.exist(route.path)) {
        return
      }

      if (route.meta.pined) {
        this.pinedTabs.push(route)
      }
      else {
        this.tabs.push(route)
      }
    },
    exist(path: string) {
      return this.allTabs.some(tab => tab.path === path)
    },
    setCurrent(path: string) {
      this.currentPath = path
    },
    closeTab(path: string) {
      const tabsLength = this.tabs.length
      if (this.tabs.length > 1) {
        const index = this.tabs.findIndex((item) => {
          return item.path === path
        })
        const isLast = index + 1 === tabsLength
        if (this.currentPath === path && !isLast) {
          router.push(this.tabs[index + 1].path as string)
        }
        else if (this.currentPath === path && isLast) {
          router.push(this.tabs[index - 1].path as string)
        }
      }
      this.tabs = this.tabs.filter((item) => {
        return item.path !== path
      })
      if (tabsLength - 1 === 0) {
        router.push('/')
      }
    },
    // 关闭全部非固定的标签
    closeAllUnpinnedTabs() {
      // 如果当前标签是非固定标签且会被关闭，需要跳转到固定标签或首页
      const currentTabWillBeClosed = this.tabs.some(tab => tab.path === this.currentPath)

      if (currentTabWillBeClosed) {
        // 优先跳转到固定标签
        if (this.pinedTabs.length > 0) {
          router.push(this.pinedTabs[0].path as string)
        }
        else {
          router.push('/')
        }
      }

      // 清除非固定标签
      this.tabs = []
    },
    // 关闭除了当前页外的所有标签
    closeOtherTabs() {
      if (!this.currentPath) {
        return
      }

      // 保留固定标签和当前标签
      const currentTab = this.allTabs.find(tab => tab.path === this.currentPath)

      // 清除非固定标签
      this.tabs = []

      // 如果当前标签是固定标签，则已经保留在 pinedTabs 中
      // 如果当前标签是非固定标签，需要重新添加
      if (currentTab && !currentTab.meta?.pined) {
        this.tabs.push(currentTab)
      }

      // 如果当前标签被关闭了（理论上不会发生），跳转到首页
      if (!this.exist(this.currentPath)) {
        router.push('/')
      }
    },
  },
  persist: {
    storage: sessionStorage,
  },
})
