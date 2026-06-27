import { defineStore } from 'pinia'
import {
  clearStoredDictTree,
  dictList,
  dictTypeColor,
  dictTypeData,
  refreshDict,
  translateDictTree,
} from '@/utils/dict'

export const useDictStore = defineStore('dict-store', {
  state: () => ({
    tree: [] as any[],
    loaded: false,
    loading: false,
    lastLoadedAt: null as any,
  }),
  getters: {
    dictList: () => dictList,
    dictTypeData: () => dictTypeData,
    dictTypeColor: () => dictTypeColor,
    translateDictTree: () => translateDictTree,
  },
  actions: {
    async refreshDict() {
      if (this.loading) {
        return
      }

      this.loading = true
      try {
        const tree = await refreshDict()
        this.tree = tree
        this.loaded = true
        this.lastLoadedAt = Date.now()
      } finally {
        this.loading = false
      }
    },

    clearDict() {
      this.tree = []
      this.loaded = false
      this.loading = false
      this.lastLoadedAt = null
      clearStoredDictTree()
    },
  },
  persist: {
    storage: localStorage,
  },
})
