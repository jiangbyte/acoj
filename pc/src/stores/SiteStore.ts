import themeJson from './theme.json'

interface SiteStore {
  authModal: boolean
  themeOverrides: object
  collapsed: boolean
}

export const useSiteStore = defineStore('websiteStore', {
  state: (): SiteStore => {
    return {
      authModal: true,
      themeOverrides: themeJson,
      collapsed: true,
    }
  },
  getters: {},
  actions: {
    toggleCollapse() {
      this.collapsed = !this.collapsed
    },
  },
  persist: {
    storage: sessionStorage,
  },
})
