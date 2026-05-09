import { defineStore } from 'pinia'

interface TabItem {
  title: string
  path: string
  key: string
  closable: boolean
}

export const useTabStore = defineStore('tab', {
  state: () => ({
    tabs: [{ title: '首页', path: '/dashboard', key: '/dashboard', closable: false }] as TabItem[],
    activeKey: '/dashboard',
  }),
  actions: {
    addTab(tab: TabItem) {
      if (!this.tabs.find(t => t.key === tab.key)) {
        this.tabs.push(tab)
      }
      this.activeKey = tab.key
    },
    removeTab(key: string) {
      const idx = this.tabs.findIndex(t => t.key === key)
      if (idx > -1) {
        this.tabs.splice(idx, 1)
        if (this.activeKey === key) {
          this.activeKey = this.tabs[Math.min(idx, this.tabs.length - 1)]?.key || '/dashboard'
        }
      }
    },
    closeAll() {
      this.tabs = this.tabs.filter(t => !t.closable)
      this.activeKey = '/dashboard'
    },
  },
  persist: true,
})
