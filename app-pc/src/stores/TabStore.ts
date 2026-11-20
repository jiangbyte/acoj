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
  },
  persist: {
    storage: sessionStorage,
  },
})
