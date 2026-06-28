import { defineStore } from 'pinia'
import { shallowRef } from 'vue'
import { dictApi } from '@/api'

export const dictTreeState = shallowRef<any[]>([])

export const useDictStore = defineStore('dict-store', {
  state: () => ({
    tree: [] as any[],
    loaded: false,
    loading: false,
    lastLoadedAt: null as any,
  }),
  actions: {
    syncDictTree() {
      dictTreeState.value = this.tree
    },

    async refreshDict() {
      if (this.loading) {
        return
      }

      this.loading = true
      try {
        const response = await dictApi.tree()
        this.tree = response.data ?? []
        dictTreeState.value = this.tree
        this.loaded = true
        this.lastLoadedAt = Date.now()
      } finally {
        this.loading = false
      }
    },

    clearDict() {
      this.tree = []
      dictTreeState.value = []
      this.loaded = false
      this.loading = false
      this.lastLoadedAt = null
    },
  },
  persist: {
    storage: localStorage,
  },
})
