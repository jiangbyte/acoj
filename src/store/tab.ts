import { defineStore } from 'pinia'

interface TabItem {
  title: string
  path: string
  key: string
  closable: boolean
  icon?: string
  affix?: boolean
}

const HOME_PATH = import.meta.env.VITE_HOME_PATH as string || '/dashboard'

export const useTabStore = defineStore('tab', {
  state: () => {
    return {
      tabs: [{ title: '首页', path: HOME_PATH, key: HOME_PATH, closable: false, affix: true }] as TabItem[],
      activeKey: HOME_PATH,
    }
  },
  actions: {
    alignHomePath() {
      const first = this.tabs[0]
      if (first && first.key !== HOME_PATH) {
        first.path = HOME_PATH
        first.key = HOME_PATH
      }
      if (this.activeKey !== HOME_PATH && this.tabs.every(t => t.key !== this.activeKey)) {
        this.activeKey = HOME_PATH
      }
    },
    addTab(tab: TabItem) {
      const existing = this.tabs.find(t => t.key === tab.key)
      if (existing) {
        if (tab.icon) existing.icon = tab.icon
      } else {
        this.tabs.push(tab)
      }
      this.activeKey = tab.key
    },
    removeTab(key: string) {
      const idx = this.tabs.findIndex(t => t.key === key)
      if (idx === -1 || this.tabs[idx].affix) return
      this.tabs.splice(idx, 1)
      if (this.activeKey === key) {
        this.activeKey = this.tabs[Math.min(idx, this.tabs.length - 1)]?.key || HOME_PATH
      }
    },
    closeAll() {
      this.tabs = this.tabs.filter(t => t.affix)
      this.activeKey = HOME_PATH
    },
  },
  persist: true,
})
