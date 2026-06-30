import { defineStore } from 'pinia'
import { shallowRef } from 'vue'
import { dictApi } from '@/api'

export const dictTreeState = shallowRef<any[]>([])

let refreshDictPromise: Promise<void> | null = null

function syncDictTreeState(tree: any[]) {
  dictTreeState.value = Array.isArray(tree) ? tree : []
}

export const useDictStore = defineStore('dict-store', {
  state: () => ({
    tree: [] as any[],
    loaded: false,
    loading: false,
    lastLoadedAt: null as any,
  }),
  actions: {
    syncDictTree() {
      syncDictTreeState(this.tree)
    },

    async refreshDict() {
      if (refreshDictPromise) {
        return refreshDictPromise
      }

      refreshDictPromise = (async () => {
        this.loading = true
        try {
          const response = await dictApi.tree()
          this.tree = response.data ?? []
          syncDictTreeState(this.tree)
          this.loaded = true
          this.lastLoadedAt = Date.now()
        } finally {
          this.loading = false
          refreshDictPromise = null
        }
      })()

      return refreshDictPromise
    },

    clearDict() {
      this.tree = []
      syncDictTreeState([])
      this.loaded = false
      this.loading = false
      this.lastLoadedAt = null
    },
  },
  persist: {
    storage: localStorage,
    pick: ['tree', 'loaded', 'lastLoadedAt'],
    afterHydrate: ({ store }) => {
      store.loading = false
      syncDictTreeState(store.tree)
    },
  },
})
